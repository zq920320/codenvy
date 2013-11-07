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

import com.mongodb.MongoClientURI;

import org.apache.pig.data.TupleFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {

    protected final TupleFactory tupleFactory = TupleFactory.getInstance();

    public static final String         BASE_DIR         = "target";
    public static final MongoClientURI MONGO_CLIENT_URI = new MongoClientURI("mongodb://localhost:12345/test.test");

    protected MongodProcess mongoProcess;

    @BeforeSuite
    public void setUp() throws Exception {
        File dir = new File(BASE_DIR + File.separator + "embeddedMongo");
        dir.mkdirs();

        RuntimeConfig config = new RuntimeConfig();
        config.setTempDirFactory(new FixedPath(dir.getAbsolutePath()));

        MongodStarter starter = MongodStarter.getInstance(config);
        MongodExecutable mongoExe = starter.prepare(new MongodConfig(Version.V2_3_0, 12345, false));
        mongoProcess = mongoExe.start();
    }

    @AfterSuite
    public void tearDown() throws Exception {
        mongoProcess.stop();
    }
}
