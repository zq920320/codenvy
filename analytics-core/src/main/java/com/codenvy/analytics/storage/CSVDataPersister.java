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

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ValueData;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CSVDataPersister implements DataPersister {

    private static final Logger           LOG                = LoggerFactory.getLogger(CSVDataPersister.class);
    private static final String           REPORTS_DIR        = Configurator.getString("analytics.reports.dir");
    private static final String           BACKUP_REPORTS_DIR = Configurator.getString("analytics.backup.reports.dir");
    private static final SimpleDateFormat dirFormat          =
            new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

    /** {@inheritDoc} */
    @Override
    public void retainData(String tableName,
                           List<ValueData> fields,
                           List<List<ValueData>> data,
                           Map<String, String> context) throws IOException {

        try {
            File csvFile = getFile(tableName, REPORTS_DIR, context);
            createParentDirIfNotExists(csvFile);

            File csvBackupFile = getFile(tableName, BACKUP_REPORTS_DIR, context);
            createParentDirIfNotExists(csvBackupFile);

            doStore(csvBackupFile, fields, data);

            Files.copy(csvBackupFile, csvFile);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** Utility method. Restores report directory. */
    public static void restoreBackup() throws IOException {
        File reportDir = new File(REPORTS_DIR);
        File backupReportDir = new File(BACKUP_REPORTS_DIR);

        FileUtils.deleteDirectory(reportDir);
        if (backupReportDir.exists()) {
            FileUtils.copyDirectory(backupReportDir, reportDir);
            LOG.info("CSV reports have been restored");
        }
    }

    protected void doStore(File csvFile, List<ValueData> fields, List<List<ValueData>> data) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"))) {
            writer.write(getDataAsString(fields));

            for (List<ValueData> rowData : data) {
                writer.write(getDataAsString(rowData));
            }
        }
    }

    private void createParentDirIfNotExists(File csvFile) throws IOException {
        File parentDir = csvFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Can't create directory tree" + parentDir.getPath());
            }
        }
    }

    protected File getFile(String tableName, String reportsDir, Map<String, String> context) throws ParseException {
        Calendar toDate = Utils.getToDate(context);

        StringBuilder filePath = new StringBuilder();
        filePath.append(reportsDir);
        filePath.append(File.separatorChar);
        filePath.append(dirFormat.format(toDate.getTime()));
        filePath.append(File.separatorChar);
        filePath.append(tableName.toLowerCase());
        filePath.append(".csv");

        return new File(filePath.toString());
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
