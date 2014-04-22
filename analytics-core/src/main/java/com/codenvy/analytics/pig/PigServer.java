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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.mongodb.DBObject;

import org.apache.pig.ExecType;
import org.apache.pig.FuncSpec;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
public class PigServer {

    private static final String     LOGS_DIR    = "analytics.pig.logs_dir";
    private static final String     SCRIPTS_DIR = "analytics.pig.scripts_dir";
    private static final Logger     LOG         = LoggerFactory.getLogger(PigServer.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

    private final String scriptDir;
    private final String logsDir;

    private       org.apache.pig.PigServer server;
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
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.add(file.getName());
                }
            }
        }

        Collections.sort(result);

        return result;
    }

    private org.apache.pig.PigServer initEmbeddedServer() throws IOException {
        return initializeServer();
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
    public void execute(ScriptType scriptType, Context context) throws IOException, ParseException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());
        try {
            if (context.getAsString(Parameters.LOG).isEmpty()) {
                return;
            }

            String script = readScriptContent(scriptType, context);

            try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {
                server.setBatchOn();
                server.registerScript(scriptContent, context.getAllAsString());
                server.executeBatch();
                server.discardBatch();
            } catch (Exception e) {
                LOG.error("Error script execution " + scriptType + " with " + getSecureContext(context).toString());
                throw new IOException(e);
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


        server.registerFunction("UUID", new FuncSpec("com.codenvy.analytics.pig.udf.UUID"));
        server.registerFunction("ExtractDomain", new FuncSpec("com.codenvy.analytics.pig.udf.ExtractDomain"));
        server.registerFunction("NullToEmpty", new FuncSpec("com.codenvy.analytics.pig.udf.NullToEmpty"));
        server.registerFunction("URLDecode", new FuncSpec("com.codenvy.analytics.pig.udf.URLDecode"));
        server.registerFunction("GetQueryValue", new FuncSpec("com.codenvy.analytics.pig.udf.GetQueryValue"));
        server.registerFunction("CutQueryParam", new FuncSpec("com.codenvy.analytics.pig.udf.CutQueryParam"));
        server.registerFunction("FixJobTitle", new FuncSpec("com.codenvy.analytics.pig.udf.FixJobTitle"));
        server.registerFunction("EventValidation", new FuncSpec("com.codenvy.analytics.pig.udf.EventValidation"));
        server.registerFunction("EventDescription", new FuncSpec("com.codenvy.analytics.pig.udf.EventDescription"));
        server.registerFunction("IsEventInSet", new FuncSpec("com.codenvy.analytics.pig.udf.IsEventInSet"));

        server.registerFunction("MongoStorage",
                                new FuncSpec("com.codenvy.analytics.pig.udf.MongoStorage",
                                             new String[]{"$STORAGE_USER", "$STORAGE_PASSWORD"}));

        server.registerFunction("MongoLoaderUsersProfiles",
                                new FuncSpec("com.codenvy.analytics.pig.udf.MongoLoader",
                                             new String[]{"$STORAGE_USER",
                                                          "$STORAGE_PASSWORD",
                                                          "id: chararray,user_company: chararray"}));

        server.registerFunction("MongoLoaderTest",
                                new FuncSpec("com.codenvy.analytics.pig.udf.MongoLoader",
                                             new String[]{"$STORAGE_USER",
                                                          "$STORAGE_PASSWORD",
                                                          "value:Long"}));

        return server;
    }

    /** Cell shutdown on org.apache.pig.PigServer */
    public void shutdown() {
        server.shutdown();
        server = null;
        LOG.info("Embedded PigServer is shutting down");
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
    public Iterator<Tuple> executeAndReturn(ScriptType scriptType, Context context) throws IOException, ParseException {
        context = validateAndAdjustContext(scriptType, context);

        LOG.info("Script execution " + scriptType + " is started: " + getSecureContext(context).toString());

        String script = readScriptContent(scriptType, context);
        script = removeRedundantCode(script);

        try (InputStream scriptContent = new ByteArrayInputStream(script.getBytes())) {

            if (context.getAsString(Parameters.LOG).isEmpty()) {
                return Collections.emptyIterator();
            }

            server.setBatchOn();
            server.registerScript(scriptContent, context.getAllAsString());
            Iterator<Tuple> iterator = server.openIterator("result");

            List<Tuple> tuples = new ArrayList<>();
            while (iterator.hasNext()) {
                tuples.add(iterator.next());
            }

            server.executeBatch();
            server.discardBatch();

            return tuples.iterator();
        } finally {
            LOG.info("Execution " + scriptType + " has finished");
        }
    }

    /** Checks if all parameters that are needed to script execution are added to context; */
    private Context validateAndAdjustContext(ScriptType scriptType, Context basedContext) throws IOException {
        Context.Builder builder = new Context.Builder(basedContext);
        mongoDataStorage.putStorageParameters(builder);

        if (!builder.exists(Parameters.LOG)) {
            setOptimizedPaths(builder);
        }

        Context context = builder.build();

        for (Parameters param : scriptType.getParams()) {
            if (!context.exists(param)) {
                throw new IOException("Key field " + param + " is absent in execution context");
            }

            param.validate(context.getAsString(param), context);
        }

        return context;
    }

    /** @return the script file name, check if outdated script should be used based on date from context. */
    private File getScriptFileName(ScriptType scriptType, Context context) throws ParseException {
        for (String dir : outdatedScriptDirectories) {
            Date date = DATE_FORMAT.parse(dir);

            if (context.getAsDate(Parameters.TO_DATE).getTimeInMillis() < date.getTime()) {
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
    private void setOptimizedPaths(Context.Builder builder) throws IOException {
        try {
            String path = LogLocationOptimizer.generatePaths(new File(logsDir).getAbsolutePath(),
                                                             builder.getAsString(Parameters.FROM_DATE),
                                                             builder.getAsString(Parameters.TO_DATE));
            builder.put(Parameters.LOG, path);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** Reads script from file. */
    private String readScriptContent(ScriptType scriptType, Context context)
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

    private static Context getSecureContext(Context context) {
        Context.Builder builder = new Context.Builder(context);
        builder.remove(Parameters.STORAGE_URL);
        builder.remove(Parameters.STORAGE_PASSWORD);
        builder.remove(Parameters.STORAGE_USER);

        return builder.build();
    }
}
