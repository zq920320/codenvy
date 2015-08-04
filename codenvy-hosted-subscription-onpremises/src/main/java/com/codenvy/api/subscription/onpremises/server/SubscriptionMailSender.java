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
package com.codenvy.api.subscription.onpremises.server;

import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;

import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.commons.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Util class for sending OnPremises subscription notifications.
 *
 * @author Igor Vinokur
 */
public class SubscriptionMailSender {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionMailSender.class);

    private static final String FREE_ONPREM_SUBSCRIPTION_ADDED = "/email-templates/onprem_free_subscription_added_notification.html";
    private static final String PAID_ONPREM_SUBSCRIPTION_ADDED = "/email-templates/onprem_paid_subscription_added_notification.html";
    private static final String ONPREM_SUBSCRIPTION_EXPIRES    = "/email-templates/onprem_expires_notification.html";
    private static final String ONPREM_SUBSCRIPTION_EXPIRED    = "/email-templates/onprem_expired_notification.html";

    private final String           apiEndpoint;
    private final AccountDao       accountDao;
    private final UserDao          userDao;
    private final SubscriptionDao  subscriptionDao;
    private final MailSenderClient mailClient;

    @Inject
    public SubscriptionMailSender(@Named("api.endpoint") String apiEndpoint,
                                  SubscriptionDao subscriptionDao,
                                  MailSenderClient mailClient,
                                  AccountDao accountDao,
                                  UserDao userDao) {
        this.apiEndpoint = apiEndpoint;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.mailClient = mailClient;
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Send notification about adding free OnPremises subscription.
     *
     * @param accountId
     */
    public void sendFreeOnPremisesSubscriptionNotification(String accountId) {
        try {
            List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
            LOG.debug("Send free OnPremises subscription added notifications to {}", accountOwnersEmails);
            sendEmail(readAndCloseQuietly(getResource(FREE_ONPREM_SUBSCRIPTION_ADDED)), "Welcome to Codenvy On-Prem",
                      accountOwnersEmails, MediaType.TEXT_HTML, Collections.<String, String>emptyMap());
        } catch (ServerException | IOException | MessagingException e) {
            LOG.warn("Unable to send free OnPremises subscription added notifications email, account: {}", accountId);
        }
    }

    /**
     * Send notification about adding paid OnPremises subscription.
     *
     * @param subscription
     */
    public void sendPaidOnPremisesSubscriptionNotification(Subscription subscription) {
        try {
            List<String> accountOwnersEmails = getAccountOwnersEmails(subscription.getAccountId());
            LOG.debug("Send paid OnPremises subscription added notifications to {}", accountOwnersEmails);
            Map<String, String> properties = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            properties.put("subscription.startDate", dateFormat.format(subscription.getStartDate()));
            properties.put("subscription.endDate", dateFormat.format(subscription.getEndDate()));
            sendEmail(readAndCloseQuietly(getResource(PAID_ONPREM_SUBSCRIPTION_ADDED)), "Your Codenvy On-Prem Subscription",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (ServerException | IOException | MessagingException e) {
            LOG.warn("Unable to send paid OnPremises subscription added notifications email, account: {}", subscription.getAccountId());
        }
    }

    /**
     * Send notification about expiration of OnPremises subscription.
     *
     * @param serviceId
     * @param days count of days until subscription expires
     */
    public void sendEmailAboutExpiring(String serviceId, int days) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, days);

            List<Subscription> subscriptions = subscriptionDao.getSubscriptionQueryBuilder().getExpiringQuery(serviceId, days).execute();

            for (Subscription subscription : subscriptions) {
                try {
                    List<String> accountOwnersEmails = getAccountOwnersEmails(subscription.getAccountId());
                    LOG.info("Send emails about subscription ends in {} days to {}", days, accountOwnersEmails);
                    sendEmail(readAndCloseQuietly(getResource(ONPREM_SUBSCRIPTION_EXPIRES)), "Your Subscription Is Expiring Soon",
                              accountOwnersEmails, MediaType.TEXT_HTML, Collections.<String, String>emptyMap());

                    subscription.getProperties().put(String.format("codenvy:subscription-email-expiring-%s", days), "true");
                    subscriptionDao.update(subscription);
                } catch (ServerException | IOException | MessagingException | NotFoundException e) {
                    LOG.warn("Unable to send emails about subscription ends in {} days, account: {}", days, subscription.getAccountId());
                }
            }
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Send notification about OnPremises subscription expired.
     *
     * @param accountId
     */
    public void sendEmailAboutExpired(String accountId) {
        try {
            List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
            LOG.info("Send emails about subscription expired", accountOwnersEmails);
            sendEmail(readAndCloseQuietly(getResource(ONPREM_SUBSCRIPTION_EXPIRED)), "Your Subscription Has Expired",
                      accountOwnersEmails, MediaType.TEXT_HTML, Collections.<String, String>emptyMap());
        } catch (ServerException | IOException | MessagingException e) {
            LOG.warn("Unable to send emails about subscription expired, account: {}", accountId);
        }
    }

    private void sendEmail(String text, String subject, List<String> emails, String mediaType, Map<String, String> properties)
            throws IOException, MessagingException {
        Map<String, String> mailProperties = new HashMap<>(properties);
        mailProperties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));

        mailClient.sendMail("Codenvy <noreply@codenvy.com>",
                            Strings.join(", ", emails.toArray(new String[emails.size()])),
                            null,
                            subject,
                            mediaType,
                            text,
                            mailProperties);
    }

    private List<String> getAccountOwnersEmails(String accountId) throws ServerException {
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
}
