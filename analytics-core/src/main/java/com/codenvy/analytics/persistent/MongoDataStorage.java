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
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Map;

/**
 * Utility class. Provides connection with underlying storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MongoDataStorage {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDataStorage.class);

    private static final String  URL      = Configurator.getString("analytics.storage.url");
    private static final boolean EMBEDDED = Configurator.getBoolean("analytics.storage.embedded");

    private static final MongoClientURI uri;
    private static final MongoClient    client;
    private static final DB             mongoDb;

    private static MongodProcess embeddedMongoProcess;

    static {
        uri = new MongoClientURI(URL);

        if (EMBEDDED) {
            initEmbeddedStorage();
        }


        try {
            client = initializeClient();
            mongoDb = client.getDB(uri.getDatabase());
            mongoDb.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Initialize Mongo client.
     *
     * @throws IOException
     */
    private static MongoClient initializeClient() throws IOException {
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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mongoClient.close();
                LOG.info("Mongo client disconnected from server");
            }
        });

        return mongoClient;
    }

    /** @return database to which connection was opened */
    public static DB getDb() {
        return mongoDb;
    }

    private static boolean isAuthRequired(MongoClientURI clientURI) {
        return clientURI.getUsername() != null && !clientURI.getUsername().isEmpty();
    }

    public static DataLoader createdDataLoader() {
        try {
            return new MongoDataLoader();
        } catch (MongoException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void putStorageParameters(Map<String, String> context) {
        if (uri.getUsername() == null) {
            Parameters.STORAGE_URL.put(context, uri.toString());
            Parameters.STORAGE_USER.put(context, "''");
            Parameters.STORAGE_PASSWORD.put(context, "''");
        } else {
            String password = new String(uri.getPassword());
            String serverUrlNoPassword = uri.toString().replace(uri.getUsername() + ":" +
                                                                password + "@", "");
            Parameters.STORAGE_URL.put(context, serverUrlNoPassword);
            Parameters.STORAGE_USER.put(context, uri.getUsername());
            Parameters.STORAGE_PASSWORD.put(context, password);
        }
    }

    private static void initEmbeddedStorage() {
        if (isStarted()) {
            return;
        }

        File dirTemp = new File(Configurator.getTmpDir(), "embedded-getDb-tmp");
        if (!dirTemp.exists() && !dirTemp.mkdirs()) {
            throw new IllegalStateException("Can't create directory tree " + dirTemp.getAbsolutePath());
        }

        File databaseDir = new File(Configurator.getTmpDir(), "embedded-getDb-database");
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
        try {
            embeddedMongoProcess = mongoExe.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        LOG.info("Embedded MongoDB has been started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Embedded MongoDB is shutting down");
                embeddedMongoProcess.stop();
            }
        });
    }

    /**
     * Checks if embedded storage is started. If connection can be opened, then it means storage is started. All JVM
     * share the same storage.
     */
    private static boolean isStarted() {
        try {
            try (Socket sock = new Socket()) {
                URI url = new URI(URL);

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
