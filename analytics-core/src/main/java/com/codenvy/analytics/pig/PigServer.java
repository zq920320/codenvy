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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.mongodb.DBObject;

import org.apache.pig.ExecType;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.text.DateFormat;
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
@Singleton
public class PigServer {

    private static final String     LOGS_DIR    = "analytics.pig.logs_dir";
    private static final String     SCRIPTS_DIR = "analytics.pig.scripts_dir";
    private static final Logger     LOG         = LoggerFactory.getLogger(PigServer.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

    private final String scriptDir;
    private final String logsDir;

    private final org.apache.pig.PigServer server;
    private final MongoDataStorage         mongoDataStorage;
    private final List<String>             outdatedScriptDirectories;

    @Inject
    public PigServer(Configurator configurator, MongoDataStorage mongoDataStorage) throws ParseException, IOException {
        this.mongoDataStorage = mongoDataStorage;
        this.logsDir = configurator.getString(LOGS_DIR);
        this.scriptDir = configurator.getString(SCRIPTS_DIR);
        this.server = initEmbeddedServer();
        this.outdatedScriptDirectories = getOutdatedScriptDirectories();
    }

    private List<String> getOutdatedScriptDirectories() {
        List<String> result = new ArrayList<>();

        File[] files = new File(scriptDir).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(file.getName());
            }
        }

        Collections.sort(result);

        return result;
    }

    private org.apache.pig.PigServer initEmbeddedServer() throws IOException {
        final org.apache.pig.PigServer server = initializeServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Embedded PigServer is shutting down");
                server.shutdown();
            }
        });

        return server;
    }

    /**
     * Run the script.
     *
     * @param scriptType
     *         specific script type to execute
     * @param context
     *         contains all necessary value parameters required by given {@link com.codenvy.analytics.pig.scripts
     *         .ScriptType}
     * @throws IOException
     *         if something gone wrong or if a required parameter is absent
     */
    public void execute(ScriptType scriptType, Map<String, String> context) throws IOException, ParseException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());
        try {
            if (Parameters.LOG.get(context).isEmpty()) {
                return;
            }

            String script = readScriptContent(scriptType, context);

            try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {
                server.setBatchOn();
                server.registerScript(scriptContent, context);
                server.executeBatch();
            } finally {
                LOG.info("Execution " + scriptType + " is finished");
            }
        } finally {
            LOG.info("Execution " + scriptType + " is finished");
        }
    }

    private org.apache.pig.PigServer initializeServer() throws IOException {
        org.apache.pig.PigServer server = new org.apache.pig.PigServer(ExecType.LOCAL);

        server.debugOff();
        server.registerJar(PigServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        server.registerJar(DBObject.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        return server;
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
    public Iterator<Tuple> executeAndReturn(ScriptType scriptType,
                                            Map<String, String> context) throws IOException, ParseException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());

        String script = readScriptContent(scriptType, context);
        script = removeRedundantCode(script);

        try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {

            if (Parameters.LOG.get(context).isEmpty()) {
                return Collections.emptyIterator();
            }

            server.setBatchOn();
            server.registerScript(scriptContent, context);
            Iterator<Tuple> iterator = server.openIterator("result");

            List<Tuple> tuples = new ArrayList<>();
            while (iterator.hasNext()) {
                tuples.add(iterator.next());
            }

            server.executeBatch();

            return tuples.iterator();
        } finally {
            LOG.info("Execution " + scriptType + " has finished");
        }
    }

    /** Checks if all parameters that are needed to script execution are added to context; */
    private Map<String, String> validateAndAdjustContext(ScriptType scriptType,
                                                         Map<String, String> context) throws IOException {
        context = Utils.clone(context);
        mongoDataStorage.putStorageParameters(context);

        if (!Parameters.LOG.exists(context)) {
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

    /**
     * @return the script file name, check if outdated script should be used based on date from context.
     */
    private File getScriptFileName(ScriptType scriptType, Map<String, String> context) throws ParseException {
        for (String dir : outdatedScriptDirectories) {
            Date date = DATE_FORMAT.parse(dir);

            if (Utils.getToDate(context).getTimeInMillis() < date.getTime()) {
                File script = new File(scriptDir, dir + File.separator + scriptType.toString().toLowerCase() + ".pig");

                if (script.exists()) {
                    LOG.info("Script " + scriptType + " will be used from " + dir + " directory");
                    return script;
                }

                break;
            }
        }

        return new File(scriptDir, scriptType.toString().toLowerCase() + ".pig");
    }

    /**
     * Selects sub directories with data to inspect based on given date parameters.
     *
     * @throws IOException
     *         if any exception is occurred
     */
    private void setOptimizedPaths(Map<String, String> context) throws IOException {
        try {
            String path = LogLocationOptimizer.generatePaths(new File(logsDir).getAbsolutePath(),
                                                             Parameters.FROM_DATE.get(context),
                                                             Parameters.TO_DATE.get(context));
            Parameters.LOG.put(context, path);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** Reads script from file. */
    private String readScriptContent(ScriptType scriptType, Map<String, String> context)
            throws IOException, ParseException {
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
    private String removeRedundantCode(String script) throws IOException {
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
    private String fixImport(String script) throws IOException {
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
    private File getMacroFile(final String regex, String scriptContent, Matcher matcher)
            throws IOException {
        String importCommand = scriptContent.substring(matcher.start(), matcher.end());
        String importFileName = importCommand.replaceAll(regex, "$1");

        File importFile = new File(scriptDir, importFileName);
        if (!importFile.exists()) {
            throw new IOException("Resource " + importFile + " not found");
        }
        return importFile;
    }

    /** Reads a stream until its end and returns its content as a byte array. */
    private String getStreamContentAsString(InputStream is) throws IOException, IllegalArgumentException {
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
