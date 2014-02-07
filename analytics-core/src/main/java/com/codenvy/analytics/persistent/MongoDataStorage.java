/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.persistent;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Net;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Storage;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Timeout;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.io.directories.FixedPath;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Map;

/**
 * Utility class. Provides connection with underlying storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@Singleton
public class MongoDataStorage {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDataStorage.class);

    private static final String URL      = "analytics.storage.url";
    private static final String EMBEDDED = "analytics.storage.embedded";

    private final MongoClientURI uri;
    private final DB             mongoDb;

    private final Configurator configurator;

    @Inject
    public MongoDataStorage(Configurator configurator) throws IOException {
        this.configurator = configurator;
        this.uri = new MongoClientURI(configurator.getString(URL));

        if (configurator.getBoolean(EMBEDDED)) {
            try {
                initEmbeddedStorage();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new IOException(e);
            }
        }

        MongoClient client = initializeClient();
        this.mongoDb = client.getDB(uri.getDatabase());
        this.mongoDb.setWriteConcern(WriteConcern.ACKNOWLEDGED);
    }

    /**
     * Initialize Mongo client.
     *
     * @throws IOException
     */
    private MongoClient initializeClient() throws IOException {
        final MongoClient mongoClient = new MongoClient(uri);
        try {
            DB db = mongoClient.getDB(uri.getDatabase());

            if (isAuthRequired(uri)) {
                db.authenticate(uri.getUsername(), uri.getPassword());
            }
        } catch (Exception e) {
            mongoClient.close();
            throw new IOException(e);
        }

        LOG.info("Mongo client connected to server");
        return mongoClient;
    }

    /** @return database to which connection was opened */
    public DB getDb() {
        return mongoDb;
    }

    private boolean isAuthRequired(MongoClientURI clientURI) {
        return clientURI.getUsername() != null && !clientURI.getUsername().isEmpty();
    }

    public DataLoader createdDataLoader() {
        return new MongoDataLoader(getDb());
    }

    public void putStorageParameters(Map<String, String> context) {
        if (uri.getUsername() == null) {
            Parameters.STORAGE_URL.put(context, uri.toString());
            Parameters.STORAGE_USER.put(context, "''");
            Parameters.STORAGE_PASSWORD.put(context, "''");
        } else {
            String password = new String(uri.getPassword());
            String serverUrlNoPassword = uri.toString().replace(uri.getUsername() + ":" + password + "@", "");
            Parameters.STORAGE_URL.put(context, serverUrlNoPassword);
            Parameters.STORAGE_USER.put(context, uri.getUsername());
            Parameters.STORAGE_PASSWORD.put(context, password);
        }
    }

    private void initEmbeddedStorage() {
        if (isStarted()) {
            return;
        }

        File dirTemp = new File(configurator.getTmpDir(), "embedded-getDb-tmp");
        if (!dirTemp.exists() && !dirTemp.mkdirs()) {
            throw new IllegalStateException("Can't create directory tree " + dirTemp.getAbsolutePath());
        }

        File databaseDir = new File(configurator.getTmpDir(), "embedded-getDb-database");
        if (!databaseDir.exists() && !databaseDir.mkdirs()) {
            throw new IllegalStateException("Can't create directory tree " + databaseDir.getAbsolutePath());
        }

        LOG.info("Embedded MongoDB is starting up");

        RuntimeConfig config = new RuntimeConfig();
        config.setTempDirFactory(new FixedPath(dirTemp.getAbsolutePath()));

        Net net = new Net(null, 12000, false);
        Storage storage = new Storage(databaseDir.getAbsolutePath(), null, 0);

        MongodStarter starter = MongodStarter.getInstance(config);
        MongodExecutable mongoExe = starter.prepare(new MongodConfig(Version.V2_3_0, net, storage, new Timeout()));
        final MongodProcess mongodProcess;

        try {
            mongodProcess = mongoExe.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        LOG.info("Embedded MongoDB has been started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Embedded MongoDB is shutting down");
                mongodProcess.stop();
            }
        });
    }

    /**
     * Checks if embedded storage is started. If connection can be opened, then it means storage is started. All JVM
     * share the same storage.
     */
    private boolean isStarted() {
        try {
            try (Socket sock = new Socket()) {
                URI url = new URI(configurator.getString(URL));

                int timeout = 500;
                InetAddress addr = InetAddress.getByName(url.getHost());
                SocketAddress sockaddr = new InetSocketAddress(addr, url.getPort());

                sock.connect(sockaddr, timeout);
            }

        } catch (Throwable e) {
            return false;
        }

        return true;
    }
}
