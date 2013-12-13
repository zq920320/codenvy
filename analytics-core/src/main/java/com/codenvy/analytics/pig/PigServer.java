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


package com.codenvy.analytics.pig;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.DBObject;

import org.apache.pig.ExecType;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Pig-latin script executor.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PigServer {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PigServer.class);

    private static final String              LOGS_DIR    = Configurator.getString("analytics.logs.dir");
    private static final String              SCRIPTS_DIR = Configurator.getString("pig.scripts.dir");
    private static final String              BIN_DIR     = Configurator.getString("pig.bin.dir");
    private static final boolean             EMBEDDED    = Configurator.getBoolean("pig.embedded");
    private static final Map<String, String> PROPERTIES  = Configurator.getAll("pig.property");

    private static final Calendar OLD_SCRIPT_DATE = Calendar.getInstance();

    static {
        try {
            OLD_SCRIPT_DATE.setTime(new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT).parse("20130822"));
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        if (EMBEDDED) {
            for (Map.Entry<String, String> entry : Configurator.getAll("pig.embedded.property").entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Run the script. Mostly for testing purpose.
     *
     * @param scriptType
     *         specific script type to execute
     * @param context
     *         contains all necessary value parameters required by given {@link com.codenvy.analytics.pig.scripts
     *         .ScriptType}
     * @throws IOException
     *         if something gone wrong or if a required parameter is absent
     */
    public static void execute(ScriptType scriptType, Map<String, String> context) throws IOException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());
        try {
            if (scriptType.isLogRequired() && Parameters.LOG.get(context).isEmpty()) {
                return;
            }

            if (EMBEDDED) {
                executeOnEmbeddedServer(scriptType, context);
            } else {
                executeOnDedicatedServer(scriptType, context);
            }
        } finally {
            LOG.info("Execution " + scriptType + " has finished");
        }
    }

    private static void executeOnEmbeddedServer(ScriptType scriptType, Map<String, String> context) throws IOException {
        org.apache.pig.PigServer server = initializeServer();

        String script = readScriptContent(scriptType, context);

        try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {
            server.registerJar(PigServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            server.registerJar(DBObject.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            server.registerScript(scriptContent, context);
        } finally {
            server.shutdown();
            LOG.info("Execution " + scriptType + " has finished");
        }
    }

    private static org.apache.pig.PigServer initializeServer() throws ExecException {
        for (Map.Entry<String, String> entry : PROPERTIES.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }

        return new org.apache.pig.PigServer(ExecType.LOCAL);
    }

    private static synchronized void executeOnDedicatedServer(ScriptType scriptType, Map<String, String> context)
            throws IOException {
        String command = prepareRunCommand(scriptType, context);
        Process process = Runtime.getRuntime().exec(command);

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logProcessOutput(process);
                throw new IOException("The process has finished with wrong code " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static void logProcessOutput(Process process) throws IOException {
        try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            for (; ; ) {
                String inLine = in.readLine();
                String errLine = err.readLine();

                if (inLine != null) {
                    LOG.info(inLine);
                } else if (errLine != null) {
                    LOG.info(errLine);
                } else {
                    break;
                }
            }
        }
    }

    private static String prepareRunCommand(ScriptType scriptType, Map<String, String> context) {
        StringBuilder builder = new StringBuilder();

        builder.append(new File(BIN_DIR, "run_script.sh").getAbsolutePath());

        for (Map.Entry<String, String> entry : context.entrySet()) {
            builder.append(' ');
            builder.append("-param ");
            builder.append(entry.getKey());
            builder.append("=");
            builder.append("'").append(entry.getValue()).append("'");
        }

        builder.append(' ');
        builder.append(getScriptFileName(scriptType, context).getAbsolutePath());

        return builder.toString();
    }

    /**
     * Run the script and returns the result. Mostly for testing purpose.
     *
     * @param scriptType
     *         specific script type to execute
     * @param context
     *         contains all necessary value parameters required by given {@link com.codenvy.analytics.pig.scripts
     *         .ScriptType}
     * @throws IOException
     *         if something gone wrong or if a required parameter is absent
     */
    public static Iterator<Tuple> executeAndReturn(ScriptType scriptType,
                                                   Map<String, String> context) throws IOException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());

        org.apache.pig.PigServer server = new org.apache.pig.PigServer(ExecType.LOCAL);

        String script = readScriptContent(scriptType, context);
        script = removeRedundantCode(script);

        try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {

            if (scriptType.isLogRequired() && Parameters.LOG.get(context).isEmpty()) {
                return Collections.emptyIterator();
            }

            server.registerJar(PigServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());

            server.registerScript(scriptContent, context);
            Iterator<Tuple> iterator = server.openIterator("result");

            List<Tuple> tuples = new ArrayList<>();
            while (iterator.hasNext()) {
                tuples.add(iterator.next());
            }

            return tuples.iterator();
        } finally {
            server.shutdown();
            LOG.info("Execution " + scriptType + " has finished");
        }
    }

    /** Checks if all parameters that are needed to script execution are added to context; */
    private static Map<String, String> validateAndAdjustContext(ScriptType scriptType,
                                                                Map<String, String> context) throws IOException {
        context = Utils.clone(context);

        if (!Parameters.STORAGE_TABLE_USERS_STATISTICS.exists(context)) {
            ReadBasedMetric usersStatistic = (ReadBasedMetric)MetricFactory.getMetric(MetricType.USERS_STATISTICS);
            Parameters.STORAGE_TABLE_USERS_STATISTICS.put(context, usersStatistic.getStorageTable());
        }

//        if (!Parameters.STORAGE_TABLE_WORKSPACES_STATISTICS.exists(context)) {
//            ReadBasedMetric usersStatistic = (ReadBasedMetric)MetricFactory.getMetric(MetricType
// .WORKSPACES_STATISTICS);
//            Parameters.STORAGE_TABLE_WORKSPACES_STATISTICS.put(context, usersStatistic.getStorageTable());
//        }

        MongoDataStorage.putStorageParameters(context);

        if (!Parameters.STORAGE_TABLE_FACTORY_SESSIONS.exists(context)) {
            Parameters.STORAGE_TABLE_FACTORY_SESSIONS
                      .put(context, MetricType.FACTORY_SESSIONS_LIST.name().toLowerCase());
        }

        if (!Parameters.LOG.exists(context) && scriptType.isLogRequired()) {
            setOptimizedPaths(context);
        }

        for (Parameters param : scriptType.getParams()) {
            if (!param.exists(context)) {
                throw new IOException("Key field " + param + " is absent in execution context");
            }

            param.validate(param.get(context), context);
        }

        return context;
    }


    /** @return the script file name */
    private static File getScriptFileName(ScriptType scriptType, Map<String, String> context) {
        try {
            if (scriptType == ScriptType.PRODUCT_USAGE_SESSIONS && Utils.getToDate(context).before(OLD_SCRIPT_DATE)) {
                scriptType = ScriptType.PRODUCT_USAGE_SESSIONS_OLD;

                LOG.info(ScriptType.PRODUCT_USAGE_SESSIONS_OLD.name() + " will be used instead of " +
                         ScriptType.PRODUCT_USAGE_SESSIONS.name());
            }
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        return new File(SCRIPTS_DIR, scriptType.toString().toLowerCase() + ".pig");
    }

    /**
     * Selects sub directories with data to inspect based on given date parameters.
     *
     * @throws IOException
     *         if any exception is occurred
     */
    private static void setOptimizedPaths(Map<String, String> context) throws IOException {
        try {
            String path = LogLocationOptimizer.generatePaths(new File(LOGS_DIR).getAbsolutePath(),
                                                             Parameters.FROM_DATE.get(context),
                                                             Parameters.TO_DATE.get(context));
            Parameters.LOG.put(context, path);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** Reads script from file. */
    private static String readScriptContent(ScriptType scriptType, Map<String, String> context) throws IOException {
        File scriptFile = getScriptFileName(scriptType, context);
        if (!scriptFile.exists()) {
            throw new IOException("Resource " + scriptFile.getAbsolutePath() + " not found");
        }

        try (InputStream scriptContent = new BufferedInputStream(new FileInputStream(scriptFile))) {
            String script = getStreamContentAsString(scriptContent);
            script = fixImport(script);

            return script;
        }
    }

    /** All commands after 'result' is considering as redundant. */
    private static String removeRedundantCode(String script) throws IOException {
        int pos = script.indexOf("result");
        if (pos < 0) {
            return script;
        }

        int endLine = script.indexOf(";", pos);
        if (endLine < 0) {
            throw new IOException("");
        }

        return script.substring(0, endLine + 1);
    }

    /** Set the absolute paths to script in imports. */
    private static String fixImport(String script) throws IOException {
        int lastPos = 0;
        final String regex = "IMPORT\\s'(.+\\.pig)';";
        final StringBuilder builder = new StringBuilder();

        Pattern importPattern = Pattern.compile(regex);

        Matcher matcher = importPattern.matcher(script);
        while (matcher.find()) {
            builder.append(script.substring(lastPos, matcher.start()));

            File importFile = getMacroFile(regex, script, matcher);
            builder.append("IMPORT '");
            builder.append(importFile.getAbsolutePath().replace("\\", "/"));
            builder.append("';");

            lastPos = matcher.end();
        }
        builder.append(script.substring(lastPos));

        return builder.toString();
    }

    /** Extracts relative path to pig script out of IMPORT command. */
    private static File getMacroFile(final String regex, String scriptContent, Matcher matcher)
            throws IOException {
        String importCommand = scriptContent.substring(matcher.start(), matcher.end());
        String importFileName = importCommand.replaceAll(regex, "$1");

        File importFile = new File(SCRIPTS_DIR, importFileName);
        if (!importFile.exists()) {
            throw new IOException("Resource " + importFile + " not found");
        }
        return importFile;
    }

    /** Reads a stream until its end and returns its content as a byte array. */
    private static String getStreamContentAsString(InputStream is) throws IOException, IllegalArgumentException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];

            for (int read = is.read(data); read > -1; read = is.read(data)) {
                output.write(data, 0, read);
            }

            return new String(output.toByteArray(), "UTF-8");
        }
    }

    private static Map<String, String> getSecureContext(Map<String, String> context) {
        Map<String, String> secureContext = Utils.clone(context);
        Parameters.STORAGE_URL.remove(secureContext);
        Parameters.STORAGE_PASSWORD.remove(secureContext);
        Parameters.STORAGE_USER.remove(secureContext);

        return secureContext;
    }
}
