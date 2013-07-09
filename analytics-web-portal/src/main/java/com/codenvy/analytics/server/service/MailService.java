/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MailService {

    private static final String MAIL_SMTP_AUTH                 = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE      = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_HOST                 = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT                 = "mail.smtp.port";
    private static final String MAIL_USER                      = "mail.user";
    private static final String MAIL_PASSWORD                  = "mail.password";
    private static final String MAIL_TO                        = "mail.to";
    private static final String MAIL_SUBJECT                   = "mail.subject";
    private static final String MAIL_TEXT                      = "mail.text";

    private final Properties mailProps;

    public MailService(Properties mailProps) {
        this.mailProps = mailProps;
    }

    public void send() throws IOException {
        try {
            Message message = getMessage();
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

    private Message getMessage() throws MessagingException {
        Session session = getSession();
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(mailProps.getProperty(MAIL_USER)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailProps.getProperty(MAIL_TO)));
        message.setSubject(mailProps.getProperty(MAIL_SUBJECT));
        message.setText(mailProps.getProperty(MAIL_TEXT));

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

