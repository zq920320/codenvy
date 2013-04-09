/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.scripts;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Pig-latin script executor.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ScriptExecutor {
    /** Logger. */
    private static final Logger       LOGGER                               = LoggerFactory.getLogger(ScriptExecutor.class);

    /** Runtime parameter name. Contains the directory where script are located. */
    private static final String       ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY = "analytics.scripts.directory";


    /** The value of {@value #ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String        SCRIPTS_DIRECTORY                    = System.getProperty(ANALYTICS_SCRIPTS_DIRECTORY_PROPERTY);

    /** Runtime parameter name. Contains the directory where logs are located. */
    private static final String       ANALYTICS_LOGS_DIRECTORY_PROPERTY    = "analytics.logs.directory";

    /** The value of {@value #ANALYTICS_LOGS_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String        LOGS_DIRECTORY                       = System.getProperty(ANALYTICS_LOGS_DIRECTORY_PROPERTY);


    /** Runtime parameter name. Contains the directory where results are stored. */
    private static final String       ANALYTICS_RESULT_DIRECTORY_PROPERTY  = "analytics.result.directory";

    /** The value of {@value ScriptService#ANALYTICS_RESULT_DIRECTORY_PROPERTY} runtime parameter. */
    public static final String        RESULT_DIRECTORY                     = System.getProperty(ANALYTICS_RESULT_DIRECTORY_PROPERTY);

    /**
     * Parameter name in Pig script contains the resources are needed to be loaded. Can be either the name of single resource (file or
     * directory) or the list of comma separated resources. Wildcard characters are supported.
     */
    public static final String        LOG                                  = "log";

    /** Pig relation containing execution result. */
    public static final String        FINAL_RELATION                       = "result";

    /** {@link ScriptType} to execute. */
    private final ScriptType          scriptType;

    /** Script execution mode. */
    private ExecType                  execType;

    /** Execution parameters context. */
    private final Map<String, String> context                              = new HashMap<String, String>();

    /**
     * The given date format is used in script execution.
     */
    public static final DateFormat    PARAM_DATE_FORMAT                    = new SimpleDateFormat("yyyyMMdd");

    /** {@link ScriptExecutor} constructor. */
    public ScriptExecutor(ScriptType scriptType) throws ExecException {
        this.scriptType = scriptType;
        this.execType = ExecType.LOCAL;
    }

    /** Setter for {@link #execType}. */
    public ScriptExecutor setExecutionMode(ExecType execType) {
        this.execType = execType;
        return this;
    }

    /** Put value into {@link #context}. */
    public ScriptExecutor setParam(String key, String value) {
        this.context.put(key, value);
        return this;
    }

    /** Put values into {@link #context}. */
    public ScriptExecutor setParams(Map<String, String> params) {
        this.context.putAll(params);
        return this;
    }

    /**
     * Run script and returns transformed result.
     * 
     * @throws IOException if something gone wrong
     */
    public Object executeAndReturnResult() throws IOException {
        File scriptFile = new File(SCRIPTS_DIRECTORY, scriptType.getScriptFileName());
        if (!scriptFile.exists()) {
            throw new IOException("Resource " + scriptFile.getAbsolutePath() + " not found");
        }

        InputStream scriptContent = readScriptContent(scriptFile);
        try {
            Tuple tuple = doExecute(scriptContent);
            return scriptType.getTupleTransformer().transform(tuple);
        } finally {
            scriptContent.close();
        }
    }

    /** Executes script and ensures result contains only one tuple. */
    private Tuple doExecute(InputStream scriptContent) throws IOException {
        validateMandatoryParameters();
        addAdditionalParameters();

        if (!context.containsKey(LOG)) {
            setLogParameter();
        }

        LOGGER.info("Execution " + scriptType.getScriptFileName() + " is started with data located: " + context.get(LOG));

        PigServer server = new PigServer(execType);
        try {
            server.registerScript(scriptContent, context);
            Iterator<Tuple> iter = server.openIterator(ScriptExecutor.FINAL_RELATION);

            Tuple result = iter.next();
            if (iter.hasNext()) {
                throw new IOException("Returned more than one tuple");
            }

            return result;
        } finally {
            server.shutdown();
            LOGGER.info("Execution " + scriptType.getScriptFileName() + " is finished");
        }
    }

    /** Add {@link #LOG} parameter into the context with trying optimize inspected data. */
    private void setLogParameter() throws IOException {
        try {
            String path = LOGS_DIRECTORY;

            if (containsParam(ScriptParameters.LAST_MINUTES) && !containsParam(ScriptParameters.FROM_DATE)
                && !containsParam(ScriptParameters.TO_DATE)) {
                path = LogLocationOptimizer.generatePathString(LOGS_DIRECTORY, getValue(ScriptParameters.LAST_MINUTES));
            } else if (containsParam(ScriptParameters.FROM_DATE) && containsParam(ScriptParameters.TO_DATE)
                       && !containsDefaultValue(ScriptParameters.FROM_DATE) && !containsDefaultValue(ScriptParameters.TO_DATE)) {
                path =
                       LogLocationOptimizer.generatePathString(LOGS_DIRECTORY, getValue(ScriptParameters.FROM_DATE),
                                                               getValue(ScriptParameters.TO_DATE));
            }

            context.put(LOG, path);
        } catch (IllegalStateException e) {
            throw new IOException(e);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private boolean containsParam(ScriptParameters param) {
        return context.containsKey(param.getName());
    }

    private boolean containsDefaultValue(ScriptParameters param) {
        return getValue(param).equals(param.getDefaultValue());
    }

    private String getValue(ScriptParameters param) {
        return context.get(param.getName());
    }

    /** Checks if all parameters that are needed to script execution have been added to context; */
    private void validateMandatoryParameters() throws IOException {
        for (ScriptParameters param : scriptType.getMandatoryParams()) {
            String paramName = param.getName();
            if (!context.containsKey(paramName)) {
                throw new IOException("Key field " + paramName + " is absent in execution context");
            }
        }
    }

    /** Adds additional parameters into context if they are not there. */
    private void addAdditionalParameters() throws IOException {
        for (ScriptParameters param : scriptType.getAdditionalParams()) {
            String paramName = param.getName();
            if (!context.containsKey(paramName)) {
                context.put(paramName, param.getDefaultValue());
            }
        }
    }

    /** Reads script from file. */
    private InputStream readScriptContent(File scriptFile) throws IOException {
        InputStream scriptContent = new BufferedInputStream(new FileInputStream(scriptFile));
        try {
            return replaceImportCommands(scriptContent);
        } finally {
            scriptContent.close();
        }
    }

    /** Set the absolute paths to script are used in IMPORT commands. */
    private InputStream replaceImportCommands(InputStream is) throws IOException {
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
            builder.append(importFile.getAbsolutePath());
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
