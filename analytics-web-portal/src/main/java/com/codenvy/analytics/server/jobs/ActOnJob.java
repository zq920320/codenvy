/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.ldap.ReadOnlyUserManager;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.SetListStringValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.UsersWorkspacesFilter;
import com.codenvy.organization.exception.OrganizationServiceException;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActOnJob implements Job {

    private static final String FILE_NAME_TEMPLATE = "${file_name}";

    private static final String       EMAIL                     = "email";

    private static final String       FTP_PASSWORD_PARAM        = "ftp_password";
    private static final String       FTP_LOGIN_PARAM           = "ftp_login";
    private static final String       FTP_SERVER_PARAM          = "ftp_server";
    private static final String       FTP_PORT_PARAM            = "ftp_port";
    private static final String       FTP_TIMEOUT_PARAM         = "ftp_timeout";
    private static final String       FTP_MAX_EFFORTS_PARAM     = "ftp_maxEfforts";
    private static final String       FTP_AUTH_PARAM            = "ftp_auth";

    private static final String       MAIL_SMTP_AUTH            = "mail.smtp.auth";
    private static final String       MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String       MAIL_SMTP_HOST            = "mail.smtp.host";
    private static final String       MAIL_SMTP_PORT            = "mail.smtp.port";
    private static final String       MAIL_USER                 = "mail.user";
    private static final String       MAIL_PASSWORD             = "mail.password";
    private static final String       MAIL_TO                   = "mail.to";
    private static final String       MAIL_SUBJECT              = "mail.subject";
    private static final String       MAIL_TEXT                 = "mail.text";

    private static final String       CRON_SCHEDULING           = "cron.scheduling";

    private static final String       USER_PROFILE_HEADERS      = "user-profile-headers";
    private static final String       METRIC_HEADERS            = "metric-headers";
    private static final String       USER_PROFILE_ATTRIBUTES   = "user-profile-attributes";
    private static final String       METRIC_NAMES              = "metric-names";

    private static final Logger       LOGGER                    = LoggerFactory.getLogger(ActOnJob.class);
    private static final String       ACTION_PROPERTIES         = System.getProperty("analytics.job.acton.properties");

    private final ReadOnlyUserManager userManager;
    private final Properties          actonProperties;

    public ActOnJob() throws OrganizationServiceException, IOException {
        this.userManager = new ReadOnlyUserManager();
        this.actonProperties = readProperties();
    }

    /**
     * For testing purpose.
     */
    public ActOnJob(ReadOnlyUserManager userManager, Properties actonProperties) throws IOException {
        this.userManager = userManager;
        this.actonProperties = actonProperties;
    }

    public Trigger getTrigger() {
        String scheduling = actonProperties.getProperty(CRON_SCHEDULING);
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduling)).build();
    }

    /**
     * @return initialized job
     */
    public JobDetail getJobDetail() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(ActOnJob.class.getName()));
        jobDetail.setJobClass(ActOnJob.class);

        return jobDetail;
    }

    /**
     * Reads FTP connections properties.
     */
    private Properties readProperties() throws FileNotFoundException, IOException {
        Properties ftpProps = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(ACTION_PROPERTIES));
        try {
            ftpProps.load(in);
        } finally {
            in.close();
        }

        return ftpProps;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("ActOnJob is started");
        long start = System.currentTimeMillis();

        try {
            File file = prepareFile();

            transfer(file);
            sendMail(file.getName());

            LOGGER.info("File " + file.getName() + " was transfered successfully");

            if (!file.delete()) {
                LOGGER.warn("File " + file.getName() + " can not be removed");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        } finally {
            LOGGER.info("ActOnJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Sends notification about transfered file.
     */
    protected void sendMail(String fileName) throws IOException {
        Properties props = new Properties();
        props.put(MAIL_SMTP_AUTH, actonProperties.get(MAIL_SMTP_AUTH));
        props.put(MAIL_SMTP_STARTTLS_ENABLE, actonProperties.get(MAIL_SMTP_STARTTLS_ENABLE));
        props.put(MAIL_SMTP_HOST, actonProperties.get(MAIL_SMTP_HOST));
        props.put(MAIL_SMTP_PORT, actonProperties.get(MAIL_SMTP_PORT));

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                                                  protected PasswordAuthentication getPasswordAuthentication() {
                                                      return new PasswordAuthentication(actonProperties.getProperty(MAIL_USER),
                                                                                        actonProperties.getProperty(MAIL_PASSWORD));
                                                  }
                                              });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(actonProperties.getProperty(MAIL_USER)));
            message.setRecipients(Message.RecipientType.TO,
                                  InternetAddress.parse(actonProperties.getProperty(MAIL_TO)));
            message.setSubject(actonProperties.getProperty(MAIL_SUBJECT).replace(FILE_NAME_TEMPLATE, fileName));
            message.setText(actonProperties.getProperty(MAIL_TEXT));

            Transport.send(message);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }


    /**
     * Sends file directly to FTP server.
     */
    private void transfer(File file) throws SocketException, IOException {
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
                continue;

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

    private void openConnection(FTPSClient ftp) throws SocketException, IOException, SSLException {
        final int timeout = Integer.valueOf(actonProperties.getProperty(FTP_TIMEOUT_PARAM));
        final int port = Integer.valueOf(actonProperties.getProperty(FTP_PORT_PARAM));
        final String server = actonProperties.getProperty(FTP_SERVER_PARAM);
        final String username = actonProperties.getProperty(FTP_LOGIN_PARAM);
        final String password = actonProperties.getProperty(FTP_PASSWORD_PARAM);

        ftp.setDataTimeout(timeout);
        ftp.setDefaultTimeout(timeout);
        ftp.connect(server, port);

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

    private void doTransfer(File file, FTPSClient ftp) throws FileNotFoundException, IOException {
        InputStream in = new FileInputStream(file);
        try {
            if (!ftp.storeFile(file.getName(), in)) {
                throw new IOException("File " + file.getName() + " was not transfered to the server");
            }
        } finally {
            in.close();
        }
    }

    protected File prepareFile() throws IOException {
        Map<String, String> context = initilalizeContext();
        File file = createFile(context);

        Set<String> activeUsers = getActiveUsers(context);

        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            writeHeaders(out);

            for (String user : activeUsers) {
                writeUserProfileAttributes(out, user);
                writeMetricsValues(context, out, user);
            }
        } finally {
            out.close();
        }

        return file;
    }

    private File createFile(Map<String, String> context) {
        return new File(System.getProperty("java.io.tmpdir"), Utils.getToDateParam(context) + ".csv");
    }

    private void writeMetricsValues(Map<String, String> context, BufferedWriter out, String user) throws IOException {
        String[] metricNames = actonProperties.getProperty(METRIC_NAMES).split(",");

        for (int i = 0; i < metricNames.length; i++) {
            context = Utils.clone(context);
            context.put(MetricFilter.FILTER_USER.name(), user);

            writeMetricValue(out, metricNames[i], context);
            if (i < metricNames.length - 1) {
                out.write(",");
            }
        }

        out.newLine();
    }

    /**
     * Initializes context.<br>
     * {@link Utils#initializeContext(TimeUnit, Date)}
     */
    protected Map<String, String> initilalizeContext() throws IOException {
        return Utils.initializeContext(TimeUnit.DAY, new Date());
    }

    private void writeMetricValue(BufferedWriter out, String metricName, Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(metricName);

        String value = metric.getValue(context).getAsString();
        out.write(value);
    }

    private void writeUserProfileAttributes(BufferedWriter out, String user) throws IOException {
        String[] attributesNames = actonProperties.getProperty(USER_PROFILE_ATTRIBUTES).split(",");

        Map<String, String> attributes;
        try {
            attributes = userManager.getUserAttributes(user);
        } catch (OrganizationServiceException e) {
            throw new IOException(e);
        }

        for (String name : attributesNames) {
            if (name.equals(EMAIL)) {
                writeString(out, user);
            } else {
                writeString(out, attributes.get(name));
            }

            out.write(",");
        }
    }

    /**
     * Write string value accordingly to CSV specification.
     */
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
        out.write(actonProperties.getProperty(USER_PROFILE_HEADERS));
        out.write(",");
        out.write(actonProperties.getProperty(METRIC_HEADERS));
        out.newLine();
    }

    /**
     * Extracts list of all active users.
     */
    protected Set<String> getActiveUsers(Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_WORKAPCES_SET);

        SetListStringValueData valueData = (SetListStringValueData)metric.getValue(context);
        ListListStringValueData listVD = new ListListStringValueData(valueData);

        Filter filter = new UsersWorkspacesFilter(listVD);

        return filter.getAvailable(MetricFilter.FILTER_USER).getAll();
    }
}
