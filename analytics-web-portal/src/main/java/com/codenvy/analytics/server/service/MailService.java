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


package com.codenvy.analytics.server.service;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MailService {

    private static final String MAIL_SMTP_AUTH            = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_HOST            = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT            = "mail.smtp.port";
    private static final String MAIL_USER                 = "mail.user";
    private static final String MAIL_PASSWORD             = "mail.password";
    private static final String MAIL_TO                   = "mail.to";
    private static final String MAIL_SUBJECT              = "mail.subject";
    private static final String MAIL_TEXT                 = "mail.text";
    private final Properties mailProps;

    public MailService(Properties mailProps) {
        this.mailProps = mailProps;
    }

    public void send() throws IOException {
       send(Collections.EMPTY_LIST);
    }

    public void send(File file) throws IOException {
        ArrayList files = new ArrayList<>();
        files.add(file);
        send(files);
    }

    public void send(List<File> files) throws IOException {
        try {
            Multipart multipart = new MimeMultipart();
            prepareTextPart(multipart);
            for (File file : files){
                prepareAttachmentPart(file, multipart);
            }
            Message message = getMessage(getSession());
            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    public void setSubject(String subject) {
        mailProps.setProperty(MAIL_SUBJECT, subject);
    }

    public void setText(String text) {
        mailProps.setProperty(MAIL_TEXT, text);
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
        messagePart.setText(mailProps.getProperty(MAIL_TEXT));
        multipart.addBodyPart(messagePart);
    }

    private Message getMessage(Session session) throws MessagingException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(mailProps.getProperty(MAIL_USER)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailProps.getProperty(MAIL_TO)));
        message.setSubject(mailProps.getProperty(MAIL_SUBJECT));
        message.setSentDate(new Date());
        return message;
    }

    private Session getSession() {
        Authenticator authenticator = getAuthenticator();

        Properties sessProps = new Properties();
        setProperty(sessProps, MAIL_SMTP_AUTH);
        setProperty(sessProps, MAIL_SMTP_STARTTLS_ENABLE);
        setProperty(sessProps, MAIL_SMTP_HOST);
        setProperty(sessProps, MAIL_SMTP_PORT);

        return Session.getInstance(sessProps, authenticator);
    }

    private Authenticator getAuthenticator() {
        final String user = mailProps.getProperty(MAIL_USER);
        final String password = mailProps.getProperty(MAIL_PASSWORD);

        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
    }

    private void setProperty(Properties sessProps, String key) {
        sessProps.put(key, mailProps.get(key));
    }
}

