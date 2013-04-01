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
package com.codenvy.dashboard.scripts;

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
public class ScriptExecutor
{
    /**
     * Logger.
     */
    private static final Logger       LOG                                  = LoggerFactory.getLogger(ScriptExecutor.class);

    /**
     * Runtime parameter name. Contains the directory where script are located.
     */
    public static final String        DASHBOARD_SCRIPTS_DIRECTORY_PROPERTY = "dashboard.scripts.directory";


    /**
     * The value of {@value #DASHBOARD_SCRIPTS_DIRECTORY_PROPERTY} runtime parameter.
     */
    public static final String        SCRIPTS_DIRECTORY                    = System.getProperty(DASHBOARD_SCRIPTS_DIRECTORY_PROPERTY);

    /**
     * {@link ScriptType} to execute.
     */
    private final ScriptType          scriptType;

    /**
     * Script execution mode.
     */
    private ExecType                  execType;

    /**
     * Execution parameters context.
     */
    private final Map<String, String> context                              = new HashMap<String, String>();

    /**
     * {@link ScriptExecutor} constructor.
     */
    public ScriptExecutor(ScriptType scriptType) throws ExecException
    {
        this.scriptType = scriptType;
        this.execType = ExecType.LOCAL;
    }

    /**
     * Setter for {@link #execType}.
     */
    public ScriptExecutor setExecutionMode(ExecType execType)
    {
        this.execType = execType;
        return this;
    }

    /**
     * Put value into {@link #context}.
     */
    public ScriptExecutor setParam(String key, String value)
    {
        this.context.put(key, value);
        return this;
    }

    /**
     * Put values into {@link #context}.
     */
    public ScriptExecutor setParams(Map<String, String> params)
    {
        this.context.putAll(params);
        return this;
    }

    /**
     * Run script and returns iterator by {@link Tuple} or null if script returned nothing.
     * 
     * @throws IOException if something gone wrong
     */
    public FileObject executeAndReturnResult(String storeLocation) throws IOException
    {
        validateMandatoryParameters();
        addAdditionalParameters();

        File scriptFile = new File(SCRIPTS_DIRECTORY, scriptType.getScriptFileName());
        if (!scriptFile.exists())
        {
            throw new IOException("Resource " + scriptFile.getAbsolutePath() + " not found");
        }

        InputStream scriptContent = readScriptContent(scriptFile);
        try
        {
            Tuple tuple = doExecute(scriptContent);

            Object value = tuple == null ? scriptType.getResultType().getEmptyResult() : tuple.get(0);
            return scriptType.createFileObject(storeLocation, context, value);
        } finally
        {
            scriptContent.close();
        }
    }

    /**
     * Executes script and ensures result contains only one tuple.
     */
    private Tuple doExecute(InputStream scriptContent) throws IOException
    {
        PigServer server = new PigServer(execType);
        try
        {
            server.registerScript(scriptContent, context);
            Iterator<Tuple> iter = server.openIterator(Constants.FINAL_RELATION);

            Tuple result = iter.next();
            if (iter.hasNext())
            {
                throw new IOException("Returned more than one tuple");
            }

            return result;
        } finally
        {
            server.shutdown();
        }
    }

    /**
     * Checks if all parameters that are needed to script execution have been added to context;
     */
    private void validateMandatoryParameters() throws IOException
    {
        for (ScriptParameters param : scriptType.getMandatoryParams())
        {
            String paramName = param.getName();
            if (!context.containsKey(paramName))
            {
                throw new IOException("Key field " + paramName + " is absent in execution context");
            }
        }
    }

    /**
     * Adds additional parameters into context if they are not there.
     */
    private void addAdditionalParameters() throws IOException
    {
        for (ScriptParameters param : scriptType.getAdditionalParams())
        {
            String paramName = param.getName();
            if (!context.containsKey(paramName))
            {
                context.put(paramName, param.getDefaultValue());
            }
        }
    }

    /**
     * Reads script from file.
     */
    private InputStream readScriptContent(File scriptFile) throws IOException
    {
        InputStream scriptContent = new BufferedInputStream(new FileInputStream(scriptFile));
        try
        {
            return replaceImportCommands(scriptContent);
        } finally
        {
            scriptContent.close();
        }
    }

    /**
     * Set the absolute paths to script are used in IMPORT commands.
     */
    private InputStream replaceImportCommands(InputStream is) throws IOException
    {
        int lastPos = 0;
        final String regex = "IMPORT\\s'(.+\\.pig)';";
        final StringBuilder builder = new StringBuilder();

        Pattern importPattern = Pattern.compile(regex);
        String scriptContnent = getStreamContentAsString(is);

        Matcher matcher = importPattern.matcher(scriptContnent);
        while (matcher.find())
        {
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
    private File extractRelativePath(final String regex, String scriptContnent, Matcher matcher) throws IOException
    {
        String importCommand = scriptContnent.substring(matcher.start(), matcher.end());
        String importFileName = importCommand.replaceAll(regex, "$1");

        File importFile = new File(SCRIPTS_DIRECTORY, importFileName);
        if (!importFile.exists())
        {
            throw new IOException("Resource " + importFile + " not found");
        }
        return importFile;
    }

    /**
     * Reads a stream until its end and returns its content as a byte array.
     */
    private byte[] getStreamContentAsBytes(InputStream is) throws IOException, IllegalArgumentException
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] data = new byte[8192];

            for (int read = is.read(data); read > -1; read = is.read(data))
            {
                output.write(data, 0, read);
            }

            return output.toByteArray();
        } finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                } catch (IOException ignore)
                {
                    if (LOG.isTraceEnabled())
                    {
                        LOG.trace("An exception occurred: " + ignore.getMessage());
                    }
                } catch (RuntimeException ignore)
                {
                    if (LOG.isTraceEnabled())
                    {
                        LOG.trace("An exception occurred: " + ignore.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Returns the content of the specified stream as a string using the <code>UTF-8</code> charset.
     */
    private String getStreamContentAsString(InputStream is) throws IOException
    {
        byte[] bytes = getStreamContentAsBytes(is);
        return new String(bytes, "UTF-8");
    }
}
