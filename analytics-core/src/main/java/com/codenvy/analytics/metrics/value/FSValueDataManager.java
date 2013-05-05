/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class FSValueDataManager implements ValueDataManager {

    /** Runtime parameter name. Contains the directory where values are stored. */
    private static final String ANALYTICS_RESULT_DIRECTORY_PROPERTY = "analytics.result.directory";

    /** The value of {@value ScriptService#ANALYTICS_RESULT_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String  RESULT_DIRECTORY                    = System.getProperty(ANALYTICS_RESULT_DIRECTORY_PROPERTY);

    /** The file name where value is stored. */
    private final String        FILE_NAME                           = "value";

    /** The {@link MetricType} instance. */
    private final MetricType    metricType;

    public FSValueDataManager(MetricType metricType) {
        this.metricType = metricType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData load(Map<String, String> uuid) throws IOException {
        File file = getFile(uuid);
        validateExistance(file);

        return doLoad(file);
    }

    private ValueData doLoad(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));

        try {
            Class< ? > clazz = Class.forName(in.readLine());
            return ValueDataFactory.createValueData(clazz, in.readLine());
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        } catch (NoSuchMethodException e) {
            throw new IOException(e);
        } catch (SecurityException e) {
            throw new IOException(e);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (InstantiationException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(ValueData value, Map<String, String> uuid) throws IOException {
        File file = getFile(uuid);
        ensureDestination(file);

        doStore(value, file);
    }

    private void doStore(ValueData value, File file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));

        try {
            out.write(value.getClass().getName());
            out.newLine();
            out.write(value.getAsString());
            out.newLine();
        } finally {
            out.close();
        }
    }

    /**
     * Returns the file to store in or load value from.
     */
    protected File getFile(Map<String, String> uuid) throws IOException {
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
            } else {
                element = entry.getValue().toLowerCase();
            }

            builder.append(element);
            builder.append(File.separatorChar);
        }

        builder.append(FILE_NAME);
        return new File(dir, builder.toString());
    }

    /** Translate date from format yyyyMMdd into format like yyyy/MM/dd and {@link File#separatorChar} is used as delimiter. */
    private String translateDateToRelativePath(String date) {
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
    private void ensureDestination(File file) throws IOException {
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
    private void validateExistance(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist " + file.getAbsolutePath());
        }
    }
}
