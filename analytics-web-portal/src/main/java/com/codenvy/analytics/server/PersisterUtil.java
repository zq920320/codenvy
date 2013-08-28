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


package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.shared.TableData;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class PersisterUtil {

    public static final String CSV_EXT      = ".csv";
    public static final String BIN_EXT      = ".bin";
    public static final String CHARSET_NAME = "utf-8";

    public static void saveTablesToBinFile(Object value, String fileName) throws IOException {
        ObjectOutputStream out =
                new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(fileName))));

        try {
            out.writeObject(value);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<TableData> loadTablesFromBinFile(String fileName) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getFile(fileName))));

        try {
            return (List<TableData>)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static File getFile(String fileName) {
        return new File(FSValueDataManager.RESULT_DIRECTORY, fileName);
    }

    public static void saveTablesToCsvFile(List<TableData> tables, String fileName) throws IOException {
        writeTablesToFile(tables, System.getProperty("analytics.csv.reports.directory"), fileName);
        writeTablesToFile(tables, System.getProperty("analytics.csv.reports.backup.directory"), fileName);
    }

    public static void saveTableToCsvFile(TableData tableData, String fileName) throws IOException {
        writeTableToFile(tableData, System.getProperty("analytics.csv.reports.directory"), fileName);
        writeTableToFile(tableData, System.getProperty("analytics.csv.reports.backup.directory"), fileName);
    }

    private static void writeTableToFile(TableData tableData, String parentDirectoryName, String fileName)
            throws IOException {
        Writer writer =
                new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(getCsvFile(parentDirectoryName, fileName)),
                                               CHARSET_NAME));
        try {
            writer.write(tableData.getCsv());
            writer.append('\n');
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void writeTablesToFile(List<TableData> tables, String parentDirectoryName, String fileName)
            throws IOException {
        Writer writer =
                new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(getCsvFile(parentDirectoryName, fileName)),
                                               CHARSET_NAME));
        try {
            for (TableData tableData : tables) {
                writer.write(tableData.getCsv());
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static File getCsvFile(String directoryParent, String fileName) {
        return new File(getTodayCsvDirectory(directoryParent), fileName.toLowerCase());
    }

    private static File getTodayCsvDirectory(String parentName) {
        String csvDirectoryPath = parentName + File.separator + getTodayCsvDirectoryName();
        File todayCsvDirectory = new File(csvDirectoryPath);
        if (!todayCsvDirectory.exists()) {
            todayCsvDirectory.mkdirs();
        }
        return todayCsvDirectory;
    }

    private static String getTodayCsvDirectoryName() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        return new SimpleDateFormat("MM_dd_yyyy").format(calendar.getTime());
    }
}
