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


package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FSValueDataManager {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FSValueDataManager.class);

    /** Runtime parameter name. Contains the directory where values are stored. */
    private static final String ANALYTICS_RESULT_DIRECTORY_PROPERTY = "analytics.result.directory";

    /** The value of {@value #ANALYTICS_RESULT_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String RESULT_DIRECTORY = System.getProperty(ANALYTICS_RESULT_DIRECTORY_PROPERTY);

    /** The directory where Pig scripts can store data in. */
    public static final String SCRIPT_STORE_DIRECTORY =
            RESULT_DIRECTORY + File.separator + "scripts_data" + File.separator + "store";

    /** The directory where Pig scripts can load data from. */
    public static final String SCRIPT_LOAD_DIRECTORY =
            RESULT_DIRECTORY + File.separator + "scripts_data" + File.separator + "load";

    /** The file name where value is stored. */
    private static final String FILE_NAME_VALUE = "value";

    /** The file name where value is stored. */
    private static final String FILE_NAME_NUMBER = "number";

    /** {@inheritDoc} */
    public static ValueData loadValue(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        File file = getValueFile(metricType, uuid);
        validateExistance(file);

        return doLoad(file);
    }

    /** {@inheritDoc} */
    public static ValueData loadNumber(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        File file = getNumberFile(metricType, uuid);
        validateExistance(file);

        return doLoad(file);
    }

    private static ValueData doLoad(File file) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

        try {
            return (ValueData)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
    }

    /** {@inheritDoc} */
    public static void storeValue(ValueData value, MetricType metricType, LinkedHashMap<String, String> uuid)
            throws IOException {
        File file = getValueFile(metricType, uuid);
        ensureDestination(file);

        doStore(value, file);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("File " + file.getPath() + " is created");
        }
    }

    /** {@inheritDoc} */
    public static void storeNumber(ValueData value, MetricType metricType, LinkedHashMap<String, String> uuid)
            throws IOException {
        File file = getNumberFile(metricType, uuid);
        ensureDestination(file);

        doStore(value, file);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("File " + file.getPath() + " is created");
        }
    }

    private static void doStore(ValueData value, File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            out.writeObject(value);
        } finally {
            out.close();
        }
    }

    /** Returns the file to store in or load value from. */
    protected static File getValueFile(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return getFile(metricType, FILE_NAME_VALUE, uuid);
    }

    /** Returns the file to store in or load value from. */
    protected static File getNumberFile(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return getFile(metricType, FILE_NAME_NUMBER, uuid);
    }

    /** Returns the file to store in or load value from. */
    protected static File getFile(MetricType metricType, String fileName, LinkedHashMap<String, String> uuid)
            throws IOException {
        File dir = new File(RESULT_DIRECTORY);

        StringBuilder builder = new StringBuilder();
        builder.append(metricType.toString().toLowerCase());
        builder.append(File.separatorChar);

        for (Entry<String, String> entry : uuid.entrySet()) {
            String element;

            if (Utils.isFromDateParam(entry)) {
                element = translateDateToRelativePath(entry.getValue());
            } else if (Utils.isToDateParam(entry) && !Utils.containsFromDateParam(uuid)) {
                element = translateDateToRelativePath(entry.getValue());
            } else if (Utils.isAlias(entry)) {
                element = translateAliasToRelativePath(entry.getValue());
            } else {
                element = entry.getValue().toLowerCase();
            }

            builder.append(element);
            builder.append(File.separatorChar);
        }

        builder.append(fileName);
        return new File(dir, builder.toString());
    }

    /** Translates user's alias to relative path */
    private static String translateAliasToRelativePath(String alias) {
        if (alias.length() < 3) {
            return alias;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(alias.substring(0, 1));
        builder.append(File.separatorChar);

        builder.append(alias.substring(1, 2));
        builder.append(File.separatorChar);

        builder.append(alias.substring(2, 3));
        builder.append(File.separatorChar);

        builder.append(alias.substring(3));

        return builder.toString();
    }

    /**
     * Translate date from format yyyyMMdd into format like yyyy/MM/dd and {@link File#separatorChar} is used as
     * delimiter.
     */
    private static String translateDateToRelativePath(String date) {
        StringBuilder builder = new StringBuilder();

        builder.append(date.substring(0, 4));
        builder.append(File.separatorChar);

        builder.append(date.substring(4, 6));
        builder.append(File.separatorChar);

        builder.append(date.substring(6, 8));

        return builder.toString();
    }

    /** Creates all needed sub tree and removes target file if exists. */
    private static void ensureDestination(File file) throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create directory tree " + dir.getAbsolutePath());
            }
        }

        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File " + file.getAbsolutePath() + " already exists and can not be removed");
            }
        }
    }

    /** Checks if file exists and throws an exception otherwise. */
    private static void validateExistance(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist " + file.getAbsolutePath());
        }
    }
}
