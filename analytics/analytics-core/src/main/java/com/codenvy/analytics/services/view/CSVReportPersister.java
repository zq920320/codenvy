/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class CSVReportPersister {

    private static final Logger           LOG        = LoggerFactory.getLogger(CSVReportPersister.class);
    private static final SimpleDateFormat DIR_FORMAT =
            new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

    private static final String REPORTS_DIR        = "analytics.reports.dir";
    private static final String BACKUP_REPORTS_DIR = "analytics.reports.backup_dir";

    private final String reportsDir;
    private final String backupReportsDir;

    @Inject
    public CSVReportPersister(Configurator configurator) {
        reportsDir = configurator.getString(REPORTS_DIR);
        backupReportsDir = configurator.getString(BACKUP_REPORTS_DIR);
    }

    public File storeData(String viewId,
                          ViewData viewData,
                          Context context) throws IOException {
        try {
            File csvBackupFile = getFile(backupReportsDir, viewId, context);
            createParentDirIfNotExists(csvBackupFile);

            File csvFile = getFile(reportsDir, viewId, context);
            createParentDirIfNotExists(csvFile);

            doStore(csvBackupFile, viewData);
            Files.copy(csvBackupFile, csvFile);

            LOG.info("Report " + csvBackupFile + " was stored");

            return csvFile;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    protected File getFile(String reportsDir, String viewId, Context context) throws ParseException {
        Calendar reportDate = context.getAsDate(Parameters.REPORT_DATE);

        StringBuilder filePath = new StringBuilder();
        filePath.append(reportsDir);
        filePath.append(File.separatorChar);
        filePath.append(DIR_FORMAT.format(reportDate.getTime()));
        filePath.append(File.separatorChar);
        filePath.append(viewId.toLowerCase());
        filePath.append(".csv");

        return new File(filePath.toString());
    }

    protected void createParentDirIfNotExists(File csvFile) throws IOException {
        File parentDir = csvFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                if (!parentDir.exists()) {
                    throw new IOException("Can't create directory tree" + parentDir.getPath());
                }
            }
        }
    }

    /** Utility method. Restores report directory. */
    public void restoreBackup() throws IOException {
        File reportDir = new File(reportsDir);
        File backupReportDir = new File(backupReportsDir);

        FileUtils.deleteDirectory(reportDir);
        if (backupReportDir.exists()) {
            FileUtils.copyDirectory(backupReportDir, reportDir);
            LOG.info("CSV reports have been restored");
        }
    }

    protected synchronized void doStore(File csvFile, ViewData viewData) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"))) {
            for (SectionData sectionData : viewData.values()) {
                for (List<ValueData> rowData : sectionData) {
                    writer.write(getDataAsString(rowData));
                }
            }
        }

        LOG.info(csvFile.getPath() + " report is created");
    }

    protected String getDataAsString(List<ValueData> data) {
        StringBuilder builder = new StringBuilder();

        for (ValueData valueData : data) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append('\"');
            builder.append(valueData.getAsString().replace("\"", "\"\""));
            builder.append('\"');
        }
        builder.append('\n');

        return builder.toString();
    }

}
