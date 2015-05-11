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

import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Invoice;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Sends emails and retrieve list of emails of account owners
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionMailSender {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionMailSender.class);

    private static final String TEMPLATE_CC_ADDED       = "/email-templates/saas-add-credit-card.html";
    private static final String TEMPLATE_CC_DELETE      = "/email-templates/saas-remove-credit-card.html";

    private final String           invoiceChargedSubject;
    private final String           invoiceNoChargesSubject;
    private final String           invoiceFailedSubject;
    private final String           billingAddress;
    private final String           freeGbh;
    private final String           freeLimit;
    private final String           apiEndpoint;
    private final AccountDao       accountDao;
    private final UserDao          userDao;
    private final MailSenderClient mailClient;

    @Inject
    public SubscriptionMailSender(@Named("subscription.saas.mail.invoice.charged.subject") String invoiceChargedSubject,
                                  @Named("subscription.saas.mail.invoice.nocharges.subject") String invoiceNoChargesSubject,
                                  @Named("subscription.saas.mail.invoice.failed.subject") String invoiceFailedSubject,
                                  @Named("subscription.saas.mail.address") String billingAddress,
                                  @Named("subscription.saas.usage.free.gbh") String freeGbh,
                                  @Named("subscription.saas.free.max_limit_mb") String freeLimit,
                                  @Named("api.endpoint") String apiEndpoint,
                                  AccountDao accountDao,
                                  UserDao userDao,
                                  MailSenderClient mailClient) {
        this.invoiceChargedSubject = invoiceChargedSubject;
        this.invoiceNoChargesSubject = invoiceNoChargesSubject;
        this.invoiceFailedSubject = invoiceFailedSubject;
        this.billingAddress = billingAddress;
        this.freeGbh = freeGbh;
        this.freeLimit = Long.toString(Math.round(Long.parseLong(freeLimit) / 1000));
        this.apiEndpoint = apiEndpoint;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.mailClient = mailClient;
    }

    public void sendInvoice(Invoice invoice, String text) throws IOException, MessagingException, ServerException {
        String subject;
        List<String> accountOwnersEmails = getAccountOwnersEmails(invoice.getAccountId());
        if (accountOwnersEmails.isEmpty()) {
            LOG.error("Can't send invoice " + invoice.getId() + " because account " + invoice.getAccountId() + " hasn't owner");
            return;
        }

        switch (PaymentState.fromState(invoice.getPaymentState())) {
            case PAID_SUCCESSFULLY: {
                subject = invoiceChargedSubject;
                break;
            }
            case NOT_REQUIRED: {
                subject = invoiceNoChargesSubject;
                break;
            }
            default: {
                subject = invoiceFailedSubject;
            }
        }
        subject = String.format(subject, invoice.getId());
        LOG.debug("Send invoice to {}", accountOwnersEmails);
        sendEmail(text, subject, accountOwnersEmails, MediaType.TEXT_HTML, null);
    }

    public void sendCCAddedNotification(String accountId, String ccNumber, String ccType) throws ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        Map<String, String> properties = new HashMap<>();
        properties.put("type", ccType);
        properties.put("number", ccNumber);
        LOG.debug("Send credit card added notifications to {}", accountOwnersEmails);
        try {
            sendEmail(readAndCloseQuietly(getResource(TEMPLATE_CC_ADDED)), "Codenvy Pay-as-you-Go Subscription",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send credit card added notifications email, account: {}", accountId);
        }
    }

    public void sendCCRemovedNotification(String accountId, String ccNumber, String ccType) throws ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        Map<String, String> properties = new HashMap<>();
        properties.put("type", ccType);
        properties.put("number", ccNumber);
        LOG.debug("Send credit card removed notifications to {}", accountOwnersEmails);
        try {
            sendEmail(readAndCloseQuietly(getResource(TEMPLATE_CC_DELETE)), "Credit Card Removed from Codenvy",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send credit card removed notifications, account: {}", accountId);
        }
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

    private void sendEmail(String text, String subject, List<String> emails, String mediaType, Map<String, String> properties)
            throws IOException, MessagingException {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
        properties.put("free.gbh", freeGbh);
        properties.put("free.limit",freeLimit);
        mailClient.sendMail(billingAddress,
                            Strings.join(", ", emails.toArray(new String[emails.size()])),
                            null,
                            subject,
                            mediaType,
                            text,
                            properties);
    }

//        public void sendTrialExpiredNotification(String accountId) throws ServerException {
//        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
//        LOG.debug("Send email about trial removing to {}", accountOwnersEmails);
//        // TODO: replace text with template && check title
//        try {
//            sendEmail("Send email about trial removing", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
//         } catch (IOException | MessagingException e) {
//         }

//    }
//
//    public void sendSubscriptionChargedNotification(String accountId) throws ServerException {
//        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
//        LOG.debug("Send email about subscription charging to {}", accountOwnersEmails);
//        // TODO: replace text with template && check title
//        try {
//            sendEmail("Send email about subscription charging", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
//         } catch (IOException | MessagingException e) {
//         }

//    }
//
//    public void sendSubscriptionChargeFailNotification(String accountId) throws ServerException {
//        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
//        LOG.debug("Send email about unsuccessful subscription charging to {}", accountOwnersEmails);
//        // TODO: replace text with template && check title
//        try {
//            sendEmail("Send email about unsuccessful subscription charging", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
//         } catch (IOException | MessagingException e) {
//         }

//    }
//
//    public void sendSubscriptionExpiredNotification(String accountId, Integer days) throws ServerException {
//        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
//        LOG.debug("Send email about trial removing in {} days to {}", days, accountOwnersEmails);
//        // TODO: replace text with template && check title
//        try {
//            sendEmail("Send email about trial removing in " + days + " days", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
//         } catch (IOException | MessagingException e) {
//         }
//    }
}
