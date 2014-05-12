/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class CSVFileCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(com.codenvy.analytics.services.view.CSVFileCleaner.class);

    private static final String CSV_FILE_FOLDER_NAME = "csv";
    private static final String REPORT_PREFIX        = "report";
    private static final String REPORT_SUFFIX        = ".csv";

    private static final long CSV_REPORT_CLEANER_CHECK_TIME = 60 * 60 * 1000; // Check time is one hour.
    private static final long CSV_REPORT_LIVE_TIME          = 60 * 60 * 1000; // Live time of csv file is one hour.

    private final File csvReportFolder;

    @Inject
    public CSVFileCleaner(Configurator configurator) {
        this.csvReportFolder = new File(configurator.getTmpDir() + File.separator + CSV_FILE_FOLDER_NAME);

        if (!csvReportFolder.exists()) {
            if (!csvReportFolder.mkdirs()) {
                throw new IllegalStateException(
                        "Can not create directory for temporary CSV reports " + csvReportFolder.getPath());
            }
        }

        cleanAll();

        Thread cleaner = new Cleaner();
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public File createNewReportFile() throws IOException {
        return File.createTempFile(REPORT_PREFIX, REPORT_SUFFIX, csvReportFolder);
    }

    private void cleanAll() {
        for (File file : csvReportFolder.listFiles()) {
            file.delete();
        }
    }

    private void cleanByTime() throws IOException {
        long currentTime = System.currentTimeMillis();

        for (File file : csvReportFolder.listFiles()) {
            FileTime fileTime = Files.getLastModifiedTime(file.toPath());

            if (currentTime - fileTime.toMillis() >= CSV_REPORT_LIVE_TIME) {
                file.delete();
            }
        }
    }

    private class Cleaner extends Thread {
        private Cleaner() {
            super("Report cleaner");
            LOG.info(getName() + " thread is started, report directory " + csvReportFolder.getAbsolutePath());
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Thread.sleep(CSV_REPORT_CLEANER_CHECK_TIME);
                    cleanByTime();
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    LOG.error("Can not clean temporary csv files.", e);
                }
            }

            LOG.info("Report cleaner stopped working.");
        }
    }
}
