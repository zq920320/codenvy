/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class FSValueDataManager {

    /** Logger. */
    private static final Logger LOGGER                              = LoggerFactory.getLogger(FSValueDataManager.class);

    /** Runtime parameter name. Contains the directory where values are stored. */
    private static final String ANALYTICS_RESULT_DIRECTORY_PROPERTY = "analytics.result.directory";

    /** The value of {@value ScriptService#ANALYTICS_RESULT_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String  RESULT_DIRECTORY                    = System.getProperty(ANALYTICS_RESULT_DIRECTORY_PROPERTY);

    /** The file name where value is stored. */
    private static final String FILE_NAME                           = "value";

    /**
     * {@inheritDoc}
     */
    public static ValueData load(MetricType metricType, Map<String, String> uuid) throws IOException {
        File file = getFile(metricType, uuid);
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

    /**
     * {@inheritDoc}
     */
    public static void store(ValueData value, MetricType metricType, Map<String, String> uuid) throws IOException {
        File file = getFile(metricType, uuid);
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

    /**
     * Returns the file to store in or load value from.
     */
    protected static File getFile(MetricType metricType, Map<String, String> uuid) throws IOException {
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

        builder.append(FILE_NAME);
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

    /** Translate date from format yyyyMMdd into format like yyyy/MM/dd and {@link File#separatorChar} is used as delimiter. */
    private static String translateDateToRelativePath(String date) {
        StringBuilder builder = new StringBuilder();

        builder.append(date.substring(0, 4));
        builder.append(File.separatorChar);

        builder.append(date.substring(4, 6));
        builder.append(File.separatorChar);

        builder.append(date.substring(6, 8));

        return builder.toString();
    }

    /**
     * Creates all needed sub tree and removes target file if exists.
     */
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

    /**
     * Checks if file exists and throws an exception otherwise.
     */
    private static void validateExistance(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist " + file.getAbsolutePath());
        }
    }
}
