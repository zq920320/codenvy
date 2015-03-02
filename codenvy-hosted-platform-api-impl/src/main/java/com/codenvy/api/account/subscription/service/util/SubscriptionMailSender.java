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
package com.codenvy.api.account.subscription.service.util;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.commons.lang.Strings;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sends emails and retrieve list of emails of account owners
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionMailSender {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionMailSender.class);

    @Inject
    private AccountDao accountDao;

    @Inject
    private UserDao userDao;

    @Inject
    private MailSenderClient mailClient;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    public List<String> getAccountOwnersEmails(String accountId) throws ServerException {
        List<String> emails = new ArrayList<>();
        for (Member member : accountDao.getMembers(accountId)) {
            if (member.getRoles().contains("account/owner")) {
                try {
                    User user = userDao.getById(member.getUserId());

                    emails.add(user.getEmail());
                } catch (ServerException | NotFoundException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return emails;
    }

    public void sendEmail(String text, List<String> emails) throws IOException, MessagingException {
        sendEmail(text, "Subscription notification", emails, MediaType.TEXT_PLAIN, null);
    }

    public void sendEmail(String text, String subject, List<String> emails, String mediaType, Map<String, String> properties)
            throws IOException, MessagingException {
        if (properties != null) {
            properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
        }
        mailClient.sendMail("noreply@codenvy.com",
                            Strings.join(", ", emails.toArray(new String[emails.size()])),
                            null,
                            subject,
                            mediaType,
                            text,
                            properties);
    }
}
