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
package com.codenvy.analytics.storage;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.io.directories.FixedPath;

import com.codenvy.analytics.Configurator;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataStorage implements DataStorage {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MongoDataStorage.class);

    public static final String TMP_DIR  = Configurator.getString("analytics.tmp.dir");
    public static final String HOST     = Configurator.getString("analytics.storage.mongo.host");
    public static final String PORT     = Configurator.getString("analytics.storage.mongo.port");
    public static final String USER     = Configurator.getString("analytics.storage.mongo.user");
    public static final String PASSWORD = Configurator.getString("analytics.storage.mongo.password");
    public static final String DB       = Configurator.getString("analytics.storage.mongo.db");

    private       MongodProcess  mongodProcess;
    private final MongoClientURI mongoClientURI;

    public MongoDataStorage() {
        this.mongoClientURI = new MongoClientURI(createStorageUrl());
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageUrl() {
        return mongoClientURI.toString();
    }

    private String createStorageUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mongodb://");

        if (USER != null) {
            stringBuilder.append(USER);
            stringBuilder.append(":");
            stringBuilder.append(PASSWORD);
            stringBuilder.append("@");
        }

        stringBuilder.append(HOST);
        stringBuilder.append(":");
        stringBuilder.append(PORT);
        stringBuilder.append("/");
        stringBuilder.append(DB);

        return stringBuilder.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void initEmbeddedStorage() {
        if (isStarted()) {
            return;
        }

        File dir = new File(TMP_DIR, "embedded-mongoDb");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Can't create directory tree " + dir.getAbsolutePath());
        }

        LOG.info("Embedded MongoDB is starting up");

        RuntimeConfig config = new RuntimeConfig();
        config.setTempDirFactory(new FixedPath(dir.getAbsolutePath()));

        MongodStarter starter = MongodStarter.getInstance(config);
        MongodExecutable mongoExe = starter.prepare(new MongodConfig(Version.V2_3_0, 12000, false));
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

    private boolean isStarted() {
        try {
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            try {
                DB db = mongoClient.getDB(mongoClientURI.getDatabase());
                db.getCollectionNames();
            } finally {
                mongoClient.close();
            }
        } catch (Throwable e) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public DataLoader createDataLoader() throws IOException {
        return new MongoDataLoader(mongoClientURI);
    }
}
