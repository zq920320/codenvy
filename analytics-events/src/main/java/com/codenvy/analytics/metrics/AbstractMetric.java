/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.ValueManager;

import org.apache.pig.backend.executionengine.ExecException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractMetric implements Metric {

    /** The file name where calculated metric is stored. */
    public static final String FILE_NAME = "value";

    /**
     * Metric type associated with.
     */
    protected final MetricType metricType;

    AbstractMetric(MetricType metricType) {
        this.metricType = metricType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(Map<String, String> context) throws IOException {
        try {
            return loadValue(context);
        } catch (FileNotFoundException e) {
            Object value = evaluateValue(context);

            if (isStoreAllowed(context)) {
                storeValue(value, context);
            }

            return value;
        }
    }

    /**
     * Stores value into the file.
     */
    protected synchronized void storeValue(Object value, Map<String, String> context) throws IOException {
        File file = getFile(context);

        ensureDestination(file);

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            getValueManager().store(writer, value);
        } finally {
            writer.close();
        }
    }

    /** Loads value from the file. */
    protected synchronized Object loadValue(Map<String, String> context) throws IOException {
        File file = getFile(context);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist " + file.getAbsolutePath());
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            return getValueManager().load(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * TODO
     */
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        String toDateParam = context.get(ScriptParameters.TO_DATE.getName());

        if (toDateParam == null) {
            return false;
        }

        Calendar toDate = Calendar.getInstance();
        try {
            toDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(toDateParam));
        } catch (ParseException e) {
            throw new IOException(e);
        }

        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.MILLISECOND, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);

        return currentDate.after(toDate);
    }

    /**
     * Evaluates metric value.
     */
    abstract protected Object evaluateValue(Map<String, String> context) throws IOException;

    /**
     * To be able to store and load specific value it is need to have dedicated {@link ValueManager} to operate with them.
     */
    abstract protected ValueManager getValueManager();

    /**
     * For testing purpose.
     */
    protected ScriptExecutor getScriptExecutor(ScriptType scriptType) throws ExecException
    {
        return new ScriptExecutor(scriptType);
    }

    /**
     * Returns the file to store in or load value from.
     */
    private File getFile(Map<String, String> context) throws IOException {
        File dir = new File(ScriptExecutor.RESULT_DIRECTORY);

        StringBuilder builder = new StringBuilder();
        builder.append(metricType.toString().toLowerCase());
        builder.append(File.separatorChar);

        for (Entry<String, String> entry : makeKeys(context).entrySet()) {
            builder.append(toRelativePath(entry));
            builder.append(File.separatorChar);
        }
        builder.append(FILE_NAME);

        return new File(dir, builder.toString());
    }

    /** Translates key into relative path of the destination file. */
    private String toRelativePath(Entry<String, String> entry) {
        if (entry.getKey().equals(ScriptParameters.FROM_DATE.getName())) {
            return translateDateToRelativePath(entry.getValue());
        }

        return entry.getValue().toLowerCase();
    }

    /** Translate date from format YYYYMMDD into format like YYYY/MM/DD and {@link File#separatorChar} is used as delimiter. */
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

    /** Preparation unique sequences to identify stored value. */
    protected LinkedHashMap<String, String> makeKeys(Map<String, String> executionParams) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();

        for (ScriptParameters param : getMandatoryParams()) {
            String paramKey = param.getName();
            String paramValue = executionParams.get(paramKey);

            if (paramValue == null) {
                throw new IOException("There is no mandatory parameter " + paramKey + " in context");
            }

            keys.put(paramKey, paramValue);
        }

        for (ScriptParameters param : getAdditionalParams()) {
            String paramKey = param.getName();
            String paramValue = executionParams.get(paramKey);

            if (paramValue == null) {
                paramValue = param.getDefaultValue();
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }
}
