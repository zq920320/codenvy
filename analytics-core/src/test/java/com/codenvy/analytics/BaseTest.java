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

package com.codenvy.analytics;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.io.directories.FixedPath;

import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.PigServer;
import com.mongodb.MongoException;

import org.apache.pig.data.TupleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {
    public static final    String BASE_DIR = "target";
    protected static final Logger LOG      = LoggerFactory.getLogger(BaseTest.class);

    protected final TupleFactory     tupleFactory = TupleFactory.getInstance();
    protected final DateFormat       dateFormat   = new SimpleDateFormat("yyyyMMdd");
    protected final DateFormat       timeFormat   = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
    protected final SimpleDateFormat dirFormat    =
            new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

    private MongodProcess embeddedMongoProcess;

    protected final Configurator     configurator;
    protected final PigServer        pigServer;
    protected final MongoDataStorage mongoDataStorage;

    public BaseTest() {
        this.configurator = Injector.getInstance(Configurator.class);
        this.pigServer = Injector.getInstance(PigServer.class);
        this.mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
    }

    @BeforeTest
    public void setUp() throws Exception {
        File dirTemp = new File(configurator.getTmpDir(), "embedded-getDb-tmp");
        dirTemp.mkdirs();

        RuntimeConfig config = new RuntimeConfig();
        config.setTempDirFactory(new FixedPath(dirTemp.getAbsolutePath()));

        int port = new Random().nextInt(100) + 10000;
        MongodStarter starter = MongodStarter.getInstance(config);
        MongodExecutable mongoExe = starter.prepare(new MongodConfig(Version.V2_3_0, port, false));

        try {
            embeddedMongoProcess = mongoExe.start();
        } catch (IOException | MongoException e) {
            embeddedMongoProcess = mongoExe.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                embeddedMongoProcess.stop();
            }
        });
    }
}
