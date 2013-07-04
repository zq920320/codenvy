/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JRebelJob implements Job, ForceableJobRunByContext {

    private static final String MESSAGE_TEMPLATE_DATE          = "${date}";
    private static final String MESSAGE_TEMPLATE_USER_PROFILES = "${user_profiles}";

    private static final String MAIL_SMTP_AUTH                 = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE      = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_HOST                 = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT                 = "mail.smtp.port";
    private static final String MAIL_USER                      = "mail.user";
    private static final String MAIL_PASSWORD                  = "mail.password";
    private static final String MAIL_TO                        = "mail.to";
    private static final String MAIL_SUBJECT                   = "mail.subject";
    private static final String MAIL_TEXT                      = "mail.text";

    private static final Logger LOGGER                         = LoggerFactory.getLogger(JRebelJob.class);
    private static final String JREBEL_PROPERTIES              = System.getProperty("analytics.job.jrebel.properties");

    private final Properties    jrebelProperties;

    public JRebelJob() throws IOException {
        this.jrebelProperties = readProperties();
    }

    private Properties readProperties() throws IOException {
        Properties properties = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(new File(JREBEL_PROPERTIES)));
        try {
            properties.load(in);
        } finally {
            in.close();
        }

        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, String> initializeContext = Utils.initializeContext(TimeUnit.DAY, new Date());

            run(initializeContext);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void run(Map<String, String> initializeContext) throws IOException {
        LOGGER.info("JRebelJob is started");
        long start = System.currentTimeMillis();

        try {
            Metric metric = MetricFactory.createMetric(MetricType.JREBEL_USER_PROFILE_INFO_GATHERING);

            ListListStringValueData value = (ListListStringValueData)metric.getValue(initializeContext);
            StringBuilder builder = new StringBuilder();
            for (ListStringValueData item : value.getAll()) {
                builder.append(item.toString());
                builder.append('\n');
            }

            sendMail(builder.toString(), Utils.getToDateParam(initializeContext));
        } finally {
            LOGGER.info("JRebelJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void sendMail(String userProfiles, String date) throws IOException {
        Properties props = new Properties();
        props.put(MAIL_SMTP_AUTH, jrebelProperties.get(MAIL_SMTP_AUTH));
        props.put(MAIL_SMTP_STARTTLS_ENABLE, jrebelProperties.get(MAIL_SMTP_STARTTLS_ENABLE));
        props.put(MAIL_SMTP_HOST, jrebelProperties.get(MAIL_SMTP_HOST));
        props.put(MAIL_SMTP_PORT, jrebelProperties.get(MAIL_SMTP_PORT));

        Session session = Session.getInstance(props,
                                              new javax.mail.Authenticator() {
                                                  @Override
                                                  protected PasswordAuthentication getPasswordAuthentication() {
                                                      return new PasswordAuthentication(jrebelProperties.getProperty(MAIL_USER),
                                                                                        jrebelProperties.getProperty(MAIL_PASSWORD));
                                                  }
                                              });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(jrebelProperties.getProperty(MAIL_USER)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(jrebelProperties.getProperty(MAIL_TO)));
            message.setSubject(jrebelProperties.getProperty(MAIL_SUBJECT).replace(MESSAGE_TEMPLATE_DATE, date));
            message.setText(jrebelProperties.getProperty(MAIL_TEXT).replace(MESSAGE_TEMPLATE_USER_PROFILES, userProfiles));
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forceRun(Map<String, String> context) throws Exception {
        run(context);
    }
}
