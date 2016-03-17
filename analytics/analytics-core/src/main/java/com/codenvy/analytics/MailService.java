/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String SMTP_AUTH = "analytics.mail.auth";
    private static final String SMTP_HOST = "analytics.mail.host";
    private static final String SMTP_PORT = "analytics.mail.port";
    private static final String SMTP_FROM = "analytics.mail.from";

    private final String     subject;
    private final String     text;
    private final String     to;
    private final List<File> attaches;

    private final Configurator configurator;

    private MailService(String subject, String text, String to, List<File> attaches) {
        this.configurator = Injector.getInstance(Configurator.class);

        this.subject = subject;
        this.text = text;
        this.attaches = attaches;
        this.to = to;
    }

    public void send() throws IOException {
        try {
            Multipart multipart = new MimeMultipart();
            prepareTextPart(multipart);
            for (File file : attaches) {
                prepareAttachmentPart(file, multipart);
            }

            Message message = getMessage(getSession());
            message.setContent(multipart);

            Transport.send(message);
            LOG.info("Mail sent from {} to {}", new Object[]{configurator.getString(SMTP_FROM), to});
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    public static class Builder {
        private String     subject;
        private String     text;
        private String     to;
        private List<File> attaches;

        public Builder() {
            this.attaches = new ArrayList<>();
        }

        public MailService build() {
            return new MailService(subject, text, to, attaches);
        }

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder attach(File file) {
            attaches.add(file);
            return this;
        }

        public Builder attach(List<File> file) {
            attaches.addAll(file);
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }
    }

    private void prepareAttachmentPart(final File file, Multipart multipart) throws MessagingException {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        FileDataSource fileDataSource = new FileDataSource(file) {
            @Override
            public String getContentType() {
                return "text/csv";
            }
        };
        attachmentPart.setDataHandler(new DataHandler(fileDataSource));
        attachmentPart.setFileName(fileDataSource.getName());
        multipart.addBodyPart(attachmentPart);
    }

    private void prepareTextPart(Multipart multipart) throws MessagingException {
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setText(text);
        multipart.addBodyPart(messagePart);
    }

    private Message getMessage(Session session) throws MessagingException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(configurator.getString(SMTP_FROM)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setSentDate(new Date());

        return message;
    }

    private Session getSession() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", configurator.getString(SMTP_AUTH));
        properties.setProperty("mail.smtp.host", configurator.getString(SMTP_HOST));
        properties.setProperty("mail.smtp.port", configurator.getString(SMTP_PORT));

        return Session.getInstance(properties);
    }
}

