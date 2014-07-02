/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.services.acton;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.pig.PigRunner;
import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class is aimed to calculate Act-On report based on log written into the external file. Actual pig-scripts is
 * used for calculation.
 * How-to use:
 * 1. Uncomment "@Test" annotation above the revisionByUsingActOn() method.
 * 2. Setup log date as constant LOG_DATE.
 * 3. Write log into the file "/src/test/resources/messages".
 * 4. Run class by command "mvn clean test -Dtest=DataRevision".
 * 5. Review Act-On report in the file "target/ideuserupdate.csv".
 * <p/>
 * Example of log body:
 * <p/>
 * 10.9.136.60 2014-02-25 03:00:23,222[ool-7-thread-10]  [INFO ] [o.e.ide.IDESessionService 83]
 * [dmitry.ndp@gmail.com][dmitryndp][7E346ADB04C23A813B7057F8093C6045] - EVENT#session-started#
 * SESSION-ID#96AB7A86-630A-422C-9E26-601769817228# USER#dmitry.ndp@gmail.com# WS#dmitryndp# WINDOW#ide#
 * 10.93.62.79 2014-02-25 03:00:z47,931[io-8080-exec-13]  [INFO ] [c.c.organization.UserService 197]    [][][] -
 * EVENT#user-update-profile# USER#dmitry.ndp@gmail.com# FIRSTNAME#Dmytro# LASTNAME#ndp-test-30.05# COMPANY## PHONE#0#
 * JOBTITLE##
 * <p/>
 * 10.9.136.60 2014-02-25 22:56:14,681[pool-7-thread-5]  [INFO ] [o.e.ide.IDESessionService 110]
 * [dmitry.ndp@gmail.com][dmitryndp][7E346ADB04C23A813B7057F8093C6045] - EVENT#session-finished#
 * SESSION-ID#96AB7A86-630A-422C-9E26-601769817228# USER#dmitry.ndp@gmail.com# WS#dmitryndp# WINDOW#ide#
 * <p/>
 * * @author Dmytro Nochevnov
 */
public class DataRevision extends BaseTest {

    /**
     * Set date of log for revision
     */
    private static final String LOG_DATE = "2014-02-25";

    /**
     * Log file name
     */
    private static final String TEST_STATISTICS = "messages";

    private PigRunner pigRunner;

    //    @Test
    public void revisionByUsingActOn() throws Exception {
        pigRunner = getPigRunner();

        runScript();

        ActOn job = Injector.getInstance(ActOn.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, LOG_DATE.replace("-", ""));
        builder.put(Parameters.TO_DATE, getCurrentDate().replace("-", ""));

        job.prepareFile(builder.build());
    }

    private File getResourceAsBytes(String originalDate, String newDate) throws Exception {
        File initialLog = new File(getClass().getClassLoader().getResource(TEST_STATISTICS).getFile());

        try (InputStream in = new BufferedInputStream(new FileInputStream(initialLog))) {
            String fixedFilePath = initialLog.getPath() + "_fixed";
            File fixedLog = new File(fixedFilePath);
            fixedLog.createNewFile();

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(fixedLog))) {
                String resourceAsString = new String(ByteStreams.toByteArray(in), "UTF-8");
                resourceAsString = resourceAsString.replace(originalDate, newDate);

                ByteStreams.write(resourceAsString.getBytes("UTF-8"), new OutputSupplier<OutputStream>() {
                    @Override
                    public OutputStream getOutput() throws IOException {
                        return out;
                    }
                });

                return fixedLog;
            } finally {
                in.close();
            }
        }
    }

    private void runScript() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);
        context =
                context.cloneAndPut(Parameters.LOG, getResourceAsBytes(LOG_DATE, getYerstedayDate()).getAbsolutePath());

        pigRunner.forceExecute(context);
    }

    /** Get pig runner with default configuration */
    private PigRunner getPigRunner() {
        return Injector.getInstance(PigRunner.class);
    }

    private String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        return df.format(calendar.getTime());
    }

    private String getYerstedayDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return df.format(calendar.getTime());
    }
}
