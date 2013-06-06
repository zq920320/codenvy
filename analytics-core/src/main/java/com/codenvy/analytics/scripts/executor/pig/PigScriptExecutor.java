/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.scripts.executor.pig;

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.apache.commons.lang.time.DateUtils;
import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Pig-latin script executor.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PigScriptExecutor implements ScriptExecutor {

    /** Logger. */
    private static final Logger LOGGER                               = LoggerFactory.getLogger(PigScriptExecutor.class);

    /** Runtime parameter name. Contains the directory where script are located. */
    private static final String ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY = "analytics.scripts.directory";

    /** Runtime parameter name. Contains the directory where logs are located. */
    public static final String  ANALYTICS_LOGS_DIRECTORY_PROPERTY    = "analytics.logs.directory";

    /** Runtime parameter name. Contains the directory where results are stored. */
    private static final String ANALYTICS_RESULT_DIRECTORY_PROPERTY  = "analytics.result.directory";

    /** The value of {@value #ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String  SCRIPTS_DIRECTORY                    = System.getProperty(ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY);

    /** The value of {@value #ANALYTICS_LOGS_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String  LOGS_DIRECTORY                       = System.getProperty(ANALYTICS_LOGS_DIRECTORY_PROPERTY);

    /** The value of {@value ScriptService#ANALYTICS_RESULT_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String  RESULT_DIRECTORY                     = System.getProperty(ANALYTICS_RESULT_DIRECTORY_PROPERTY);

    /**
     * Parameter name in Pig script contains the resources are needed to be inspected. Can be either the name of single resource (file or
     * directory) or the list of comma separated resources. Wildcard characters are supported.
     */
    public static final String  LOG                                  = "log";

    /** Pig relation containing execution result. */
    private final String        FINAL_RELATION                       = "result";

    /** Script execution mode. */
    private final ExecType      execType;

    public PigScriptExecutor() {
        execType = ExecType.LOCAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData executeAndReturn(ScriptType scriptType, Map<String, String> context) throws IOException {
        context = Utils.clone(context);
        validateParameters(scriptType, context);

        if (!isExecutionAllowed(context)) {
            return ValueDataFactory.createValueData(scriptType.getValueDataClass(), Collections.<Tuple> emptyList().iterator());
        }

        String path = getInspectedPaths(context);
        if (path.isEmpty()) {
            return ValueDataFactory.createValueData(scriptType.getValueDataClass(), Collections.<Tuple> emptyList().iterator());
        }

        InputStream scriptContent = readScriptContent(scriptType);
        try {
            LOGGER.info("Script execution " + scriptType + " is started with data located: " + path);

            PigServer server = new PigServer(execType);
            try {
                server.registerScript(scriptContent, context);
                return ValueDataFactory.createValueData(scriptType.getValueDataClass(), server.openIterator(FINAL_RELATION));
            } finally {
                server.shutdown();
            }
        } finally {
            LOGGER.info("Execution " + scriptType + " is finished");
            scriptContent.close();
        }
    }

    /** @return if it is allowed to execute query. */
    protected boolean isExecutionAllowed(Map<String, String> context) throws IOException {
        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }

    /**
     * Checks if all parameters that are needed to script execution have been added to context;
     */
    private void validateParameters(ScriptType scriptType, Map<String, String> context) throws IOException {
        for (MetricParameter param : scriptType.getParams()) {
            if (!context.containsKey(param.getName())) {
                throw new IOException("Key field " + param + " is absent in execution context");
            }
        }
    }


    /** @return the script file name */
    private String getScriptFileName(ScriptType scriptType) {
        return scriptType.toString().toLowerCase() + ".pig";
    }

    /**
     * Selects sub directories with data to inspect based on given date parameters.
     * 
     * @return comma separated list of directories
     * @throws IOException if any exception is occurred
     */
    private String getInspectedPaths(Map<String, String> context) throws IOException {
        String path = context.get(LOG);

        if (path == null) {
            try {
                path = LOGS_DIRECTORY;

                if (Utils.containsFromDateParam(context) && Utils.containsToDateParam(context)) {
                    path =
                           LogLocationOptimizer.generatePaths(LOGS_DIRECTORY, Utils.getFromDateParam(context),
                                                              Utils.getToDateParam(context));
                }
            } catch (IllegalStateException e) {
                throw new IOException(e);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        path = path.replace("\\", "/"); // hack for windows (stupid OS)

        context.put(LOG, path);
        return path;
    }

    /** Reads script from file. */
    private InputStream readScriptContent(ScriptType scriptType) throws IOException {
        File scriptFile = new File(SCRIPTS_DIRECTORY, getScriptFileName(scriptType));
        if (!scriptFile.exists()) {
            throw new IOException("Resource " + scriptFile.getAbsolutePath() + " not found");
        }

        InputStream scriptContent = new BufferedInputStream(new FileInputStream(scriptFile));
        try {
            return readAndFixImport(scriptContent);
        } finally {
            scriptContent.close();
        }
    }

    /** Set the absolute paths to script are used in IMPORT commands. */
    private InputStream readAndFixImport(InputStream is) throws IOException {
        int lastPos = 0;
        final String regex = "IMPORT\\s'(.+\\.pig)';";
        final StringBuilder builder = new StringBuilder();

        Pattern importPattern = Pattern.compile(regex);
        String scriptContnent = getStreamContentAsString(is);

        Matcher matcher = importPattern.matcher(scriptContnent);
        while (matcher.find()) {
            File importFile = extractRelativePath(regex, scriptContnent, matcher);

            builder.append(scriptContnent.substring(lastPos, matcher.start()));
            builder.append("IMPORT '");
            builder.append(importFile.getAbsolutePath().replace("\\", "/"));
            builder.append("';");

            lastPos = matcher.end();
        }
        builder.append(scriptContnent.substring(lastPos));

        return new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
    }

    /**
     * Extracts relative path to pig script out of IMPORT command.
     * 
     * @return absolute path to script located in {@value #SCRIPTS_DIRECTORY}.
     */
    private File extractRelativePath(final String regex, String scriptContnent, Matcher matcher) throws IOException {
        String importCommand = scriptContnent.substring(matcher.start(), matcher.end());
        String importFileName = importCommand.replaceAll(regex, "$1");

        File importFile = new File(SCRIPTS_DIRECTORY, importFileName);
        if (!importFile.exists()) {
            throw new IOException("Resource " + importFile + " not found");
        }
        return importFile;
    }

    /** Reads a stream until its end and returns its content as a byte array. */
    private byte[] getStreamContentAsBytes(InputStream is) throws IOException, IllegalArgumentException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] data = new byte[8192];

            for (int read = is.read(data); read > -1; read = is.read(data)) {
                output.write(data, 0, read);
            }

            return output.toByteArray();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("An exception occurred: " + ignore.getMessage());
                    }
                } catch (RuntimeException ignore) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("An exception occurred: " + ignore.getMessage());
                    }
                }
            }
        }
    }

    /** Returns the content of the specified stream as a string using the <code>UTF-8</code> charset. */
    private String getStreamContentAsString(InputStream is) throws IOException {
        byte[] bytes = getStreamContentAsBytes(is);
        return new String(bytes, "UTF-8");
    }
}
