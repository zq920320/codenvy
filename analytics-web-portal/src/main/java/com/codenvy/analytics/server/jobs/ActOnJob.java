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


package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.server.service.MailService;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Properties;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ActOnJob implements Job {
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
        File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);

        SetStringValueData users =
                (SetStringValueData)MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET).getValue(context);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            writeHeaders(out);

            for (String user : users.getAll()) {
                try {
                    writeUserProfileAttributes(out, user);
                } catch (Exception e) {
                    LOGGER.warn(user + " is absent. May be it was removed.");
                    continue;
                }

                writeMetricsValues(out, user);
            }
        }

        return file;
    }

    /** Initialize context for whole period. */
    protected Map<String, String> initializeContext() throws IOException {
        Map<String, String> context = Utils.newContext();
        MetricParameter.FROM_DATE.putDefaultValue(context);
        MetricParameter.TO_DATE.putDefaultValue(context);

        return context;
    }

    private void writeMetricsValues(BufferedWriter out, String user) throws IOException {
        Map<String, String> context = initializeContext();
        MetricFilter.FILTER_USER.put(context, user);

        ValueData value = MetricFactory.createMetric(MetricType.PROJECT_CREATED).getValue(context);
        out.write(value.toString());
        out.write(",");

        value = MetricFactory.createMetric(MetricType.PROJECT_BUILT).getValue(context);
        out.write(value.toString());
        out.write(",");

        value = MetricFactory.createMetric(MetricType.PROJECT_DEPLOYED).getValue(context);
        out.write(value.toString());
        out.write(",");

        value = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_TOTAL).getValue(context);
        out.write(value.toString());

        out.newLine();
    }

    protected void writeUserProfileAttributes(BufferedWriter out, String user) throws IOException {
        Map<String, String> context = initializeContext();
        MetricParameter.ALIAS.put(context, user);

        UsersProfileMetric metric = (UsersProfileMetric)MetricFactory.createMetric(MetricType.USER_PROFILE);
        ListListStringValueData profile = (ListListStringValueData)metric.getValue(context);

        writeString(out, metric.getEmail(profile));
        out.write(",");

        writeString(out, metric.getFirstName(profile));
        out.write(",");

        writeString(out, metric.getLastName(profile));
        out.write(",");

        writeString(out, metric.getPhone(profile));
        out.write(",");

        writeString(out, metric.getCompany(profile));
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
}
