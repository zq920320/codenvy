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
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataStorage implements DataStorage {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MongoDataStorage.class);

    public static final String ANALYTICS_STORAGE_MONGO_HOST     = "analytics.storage.mongo.host";
    public static final String ANALYTICS_STORAGE_MONGO_PORT     = "analytics.storage.mongo.port";
    public static final String ANALYTICS_STORAGE_MONGO_USER     = "analytics.storage.mongo.user";
    public static final String ANALYTICS_STORAGE_MONGO_PASSWORD = "analytics.storage.mongo.password";
    public static final String ANALYTICS_STORAGE_MONGO_DB       = "analytics.storage.mongo.db";

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

        if (Configurator.exists(ANALYTICS_STORAGE_MONGO_USER)) {
            stringBuilder.append(Configurator.getString(ANALYTICS_STORAGE_MONGO_USER));
            stringBuilder.append(":");
            stringBuilder.append(Configurator.getString(ANALYTICS_STORAGE_MONGO_PASSWORD));
            stringBuilder.append("@");
        }

        stringBuilder.append(Configurator.getString(ANALYTICS_STORAGE_MONGO_HOST));
        stringBuilder.append(":");
        stringBuilder.append(Configurator.getString(ANALYTICS_STORAGE_MONGO_PORT));
        stringBuilder.append("/");
        stringBuilder.append(Configurator.getString(ANALYTICS_STORAGE_MONGO_DB));

        return stringBuilder.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void initEmbeddedStorage() {
        if (isStarted()) {
            return;
        }

        File dir = new File(Configurator.ANALYTICS_TMP_DIRECTORY, "embedded-mongoDb");
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
            new MongoClient(mongoClientURI).close();
        } catch (UnknownHostException e) {
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
