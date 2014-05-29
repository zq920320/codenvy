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

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Context;
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

/**
 * Utility class. Provides connection with underlying storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@Singleton
public class MongoDataStorage {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDataStorage.class);

    private static final String URL      = "analytics.mongodb.url";
    private static final String EMBEDDED = "analytics.mongodb.embedded";

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

    public void putStorageParameters(Context.Builder builder) {
        builder.put(Parameters.STORAGE_URL, uri.toString());
    }

    private void initEmbeddedStorage() throws IOException {
        if (isStarted()) {
            return;
        }

        LOG.info("Embedded MongoDB is starting up");

        MongodConfigBuilder mongodConfigBuilder = new MongodConfigBuilder();
        mongodConfigBuilder.net(new Net(12000, false));
        mongodConfigBuilder.replication(new Storage(getDir("database"), null, 0));
        mongodConfigBuilder.version(Version.V2_5_4);

        RuntimeConfigBuilder runtimeConfigBuilder = new RuntimeConfigBuilder().defaults(Command.MongoD);

        MongodStarter starter = MongodStarter.getInstance(runtimeConfigBuilder.build());
        final MongodExecutable mongoExe = starter.prepare(mongodConfigBuilder.build());

        try {
            mongoExe.start();
            LOG.info("Embedded MongoDB is started");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mongoExe.stop();
                LOG.info("Embedded MongoDB is stopped");
            }
        });
    }

    private String getDir(String dirName) {
        File dirTemp = new File(configurator.getTmpDir(), "mongodb" + File.separator + dirName);
        if (!dirTemp.exists() && !dirTemp.mkdirs()) {
            throw new IllegalStateException("Can't create directory tree " + dirTemp.getAbsolutePath());
        }

        return dirTemp.getAbsolutePath();
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
