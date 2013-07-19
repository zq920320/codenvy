/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.service.MailService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ActOnJob implements Job, ForceableRunOnceJob {
    public static final String FILE_NAME = "ideuserupdate.csv";

    private static final String FTP_PASSWORD_PARAM    = "ftp_password";
    private static final String FTP_LOGIN_PARAM       = "ftp_login";
    private static final String FTP_SERVER_PARAM      = "ftp_server";
    private static final String FTP_PORT_PARAM        = "ftp_port";
    private static final String FTP_TIMEOUT_PARAM     = "ftp_timeout";
    private static final String FTP_MAX_EFFORTS_PARAM = "ftp_maxEfforts";
    private static final String FTP_AUTH_PARAM        = "ftp_auth";

    private static final Logger LOGGER            = LoggerFactory.getLogger(ActOnJob.class);
    private static final String ACTION_PROPERTIES = System.getProperty("analytics.job.acton.properties");

    private final Properties actonProperties;

    public ActOnJob() throws IOException {
        this.actonProperties = Utils.readProperties(ACTION_PROPERTIES);
    }

    public ActOnJob(Properties properties) throws IOException {
        this.actonProperties = properties;
    }

    /** {@inheritDoc} */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            run();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void run() throws IOException {
        LOGGER.info("ActOnJob is started");
        long start = System.currentTimeMillis();

        try {
            File file = prepareFile(initializeContext());

            transfer(file);
            sendMail();

            if (!file.delete()) {
                LOGGER.warn("File " + file.getName() + " can not be removed");
            }
        } finally {
            LOGGER.info("ActOnJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void sendMail() throws IOException {
        MailService mailService = new MailService(actonProperties);
        mailService.send();
    }

    /** Sends file directly to FTP server. */
    private void transfer(File file) throws IOException {
        final String auth = actonProperties.getProperty(FTP_AUTH_PARAM);
        final int maxEfforts = Integer.valueOf(actonProperties.getProperty(FTP_MAX_EFFORTS_PARAM));

        for (int i = 0; i < maxEfforts; i++) {
            FTPSClient ftp = new FTPSClient(auth, false);

            try {
                openConnection(ftp);
                doTransfer(file, ftp);
                closeConnection(ftp);

                break; // file transfered successfully

            } catch (SocketTimeoutException e) {
                LOGGER.error(e.getMessage());

            } catch (IOException e) {
                if (ftp.isConnected()) {
                    ftp.disconnect();
                }

                throw e;
            }
        }
    }

    private void closeConnection(FTPSClient ftp) throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    private void openConnection(FTPSClient ftp) throws IOException {
        final int timeout = Integer.valueOf(actonProperties.getProperty(FTP_TIMEOUT_PARAM));
        final int port = Integer.valueOf(actonProperties.getProperty(FTP_PORT_PARAM));
        final String server = actonProperties.getProperty(FTP_SERVER_PARAM);
        final String username = actonProperties.getProperty(FTP_LOGIN_PARAM);
        final String password = actonProperties.getProperty(FTP_PASSWORD_PARAM);

        ftp.setDefaultTimeout(timeout);
        ftp.connect(server, port);

        ftp.setSendBufferSize(65536);
        ftp.setBufferSize(65536);

        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            throw new IOException("FTP connection failed");
        }

        if (!ftp.login(username, password)) {
            ftp.logout();
            throw new IOException("FTP login failed");
        }

        ftp.enterLocalPassiveMode();
        ftp.execPBSZ(0);
        ftp.execPROT("P");
        ftp.setFileType(FTPSClient.ASCII_FILE_TYPE);
    }

    private void doTransfer(File file, FTPSClient ftp) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            if (!ftp.storeFile(file.getName(), in)) {
                throw new IOException("File " + file.getName() + " was not transfered to the server");
            }
        }
    }

    protected File prepareFile(Map<String, String> context) throws IOException {
        Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);

        File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            writeHeaders(out);

            ListListStringValueData valueData = (ListListStringValueData)runScript(context);
            for (ListStringValueData item : valueData.getAll()) {
                String user = item.getAll().get(0);

                try {
                    writeUserProfileAttributes(out, user);
                } catch (Exception e) {
                    LOGGER.warn("There is no information about " + user);
                    continue;
                }

                writeMetricsValues(out, item);
            }
        }

        return file;
    }

    private ValueData runScript(Map<String, String> context) throws IOException {
        File srcDir = new File(Utils.getResultDir(context), "ACTON");
        File destDir = new File(Utils.getResultDir(context), "PREV_ACTON");

        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }

        if (srcDir.exists()) {
            FileUtils.moveDirectory(srcDir, destDir);
        } else {
            destDir.mkdirs();
            File.createTempFile("prefix", "suffix", destDir);
        }

        return ScriptExecutor.INSTANCE.executeAndReturn(ScriptType.ACTON, context);
    }

    protected Map<String, String> initializeContext() throws IOException {
        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.TO_DATE.name(), MetricParameter.TO_DATE.getDefaultValue());
        Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);

        return context;
    }

    private void writeMetricsValues(BufferedWriter out, ListStringValueData item) throws IOException {
        List<String> all = item.getAll();
        for (int i = 1; i < all.size(); i++) { // skip email
            out.write(all.get(i));

            if (i < all.size() - 1) {
                out.write(",");
            }
        }

        out.newLine();
    }

    protected void writeUserProfileAttributes(BufferedWriter out, String user) throws IOException {
        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.ALIAS.name(), user);

        Metric metric = MetricFactory.createMetric(MetricType.USER_PROFILE_EMAIL);
        writeString(out, metric.getValue(context).getAsString());
        out.write(",");

        metric = MetricFactory.createMetric(MetricType.USER_PROFILE_FIRSTNAME);
        writeString(out, metric.getValue(context).getAsString());
        out.write(",");

        metric = MetricFactory.createMetric(MetricType.USER_PROFILE_LASTNAME);
        writeString(out, metric.getValue(context).getAsString());
        out.write(",");

        metric = MetricFactory.createMetric(MetricType.USER_PROFILE_PHONE);
        writeString(out, metric.getValue(context).getAsString());
        out.write(",");

        metric = MetricFactory.createMetric(MetricType.USER_PROFILE_COMPANY);
        writeString(out, metric.getValue(context).getAsString());
        out.write(",");
    }

    /** Write string value accordingly to CSV specification. */
    private void writeString(BufferedWriter out, String str) throws IOException {
        if (str == null) {
            out.write("");
        } else {
            out.write("\"");
            out.write(str.replace("\"", "\"\"")); // quoting
            out.write("\"");
        }
    }

    private void writeHeaders(BufferedWriter out) throws IOException {
        out.write("email,firstName,lastName,phone,company,projects,builts,deployments,spentTime");
        out.newLine();
    }

    /** {@inheritDoc} */
    @Override
    public void forceRun() throws Exception {
        run();
    }
}
