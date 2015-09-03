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
package com.codenvy.api.subscription.saas.server.service.util;

import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.bonus.Bonus;
import com.codenvy.api.subscription.saas.server.billing.PaymentState;
import com.codenvy.api.metrics.server.period.Period;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.google.common.base.Joiner;

import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Sends saas and billing related emails.
 *
 * @author Max Shaposhnik
 */
public class SubscriptionMailSender {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionMailSender.class);

    private static final String TEMPLATE_CC_ADDED  = "/email-templates/saas-add-credit-card.html";
    private static final String TEMPLATE_CC_DELETE = "/email-templates/saas-remove-credit-card.html";

    private static final String TEMPLATE_TEMPORARY_BONUS = "/email-templates/temporary_bonus_notification.html";
    private static final String TEMPLATE_PERMANENT_BONUS = "/email-templates/permanent_bonus_notification.html";

    private static final String TEMPLATE_REFERRING_BONUS = "/email-templates/referring_user_notification.html";
    private static final String TEMPLATE_REFERRED_BONUS  = "/email-templates/referred_user_notification.html";

    private final String           invoiceChargedSubject;
    private final String           invoiceNoChargesSubject;
    private final String           invoiceFailedSubject;
    private final String           billingAddress;
    private final String           freeLimit;
    private final String           apiEndpoint;
    private final AccountDao       accountDao;
    private final UserDao          userDao;
    private final MailSenderClient mailClient;
    private final BillingService   billingService;
    private final Period           billingPeriod;

    @Inject
    public SubscriptionMailSender(@Named("subscription.saas.mail.invoice.charged.subject") String invoiceChargedSubject,
                                  @Named("subscription.saas.mail.invoice.nocharges.subject") String invoiceNoChargesSubject,
                                  @Named("subscription.saas.mail.invoice.failed.subject") String invoiceFailedSubject,
                                  @Named("subscription.saas.mail.address") String billingAddress,
                                  @Named("subscription.saas.free.max_limit_mb") Long freeLimit,
                                  @Named("api.endpoint") String apiEndpoint,
                                  AccountDao accountDao,
                                  UserDao userDao,
                                  MailSenderClient mailClient,
                                  BillingService billingService,
                                  MetricPeriod metricPeriod) {
        this.invoiceChargedSubject = invoiceChargedSubject;
        this.invoiceNoChargesSubject = invoiceNoChargesSubject;
        this.invoiceFailedSubject = invoiceFailedSubject;
        this.billingAddress = billingAddress;
        this.freeLimit = Long.toString(Math.round((float)freeLimit / 1000f));
        this.apiEndpoint = apiEndpoint;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.mailClient = mailClient;
        this.billingService = billingService;
        this.billingPeriod = metricPeriod.getCurrent();
    }

    public void sendInvoice(Invoice invoice, String text) throws IOException, MessagingException, ServerException {
        String subject;
        List<String> accountOwnersEmails = getAccountOwnersEmails(invoice.getAccountId());
        if (accountOwnersEmails.isEmpty()) {
            LOG.error("Can't send invoice {} because account {} hasn't owner", invoice.getId(), invoice.getAccountId());
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
        properties.put("free.gbh", format(billingService
                                                  .getProvidedFreeResources(accountId,
                                                                            billingPeriod.getStartDate().getTime(),
                                                                            System.currentTimeMillis())));
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
        properties.put("free.gbh", format(billingService
                                                  .getProvidedFreeResources(accountId,
                                                                            billingPeriod.getStartDate().getTime(),
                                                                            System.currentTimeMillis())));
        LOG.debug("Send credit card removed notifications to {}", accountOwnersEmails);
        try {
            sendEmail(readAndCloseQuietly(getResource(TEMPLATE_CC_DELETE)), "Credit Card Removed from Codenvy",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send credit card removed notifications, account: {}", accountId);
        }
    }

    public void sendBonusNotification(Bonus bonus) throws ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(bonus.getAccountId());
        Map<String, String> properties = new HashMap<>();
        properties.put("bonus.amount", format(bonus.getResources()));
        properties.put("free.gbh", format(billingService
                                                  .getProvidedFreeResources(bonus.getAccountId(),
                                                                            bonus.getFromDate(),
                                                                            bonus.getTillDate())));
        Calendar calendar = Calendar.getInstance();
        calendar.set(2100, Calendar.JANUARY, 1);
        LOG.debug("Send bonus notifications to {}", accountOwnersEmails);
        try {
            if (bonus.getTillDate() > calendar.getTimeInMillis()) {
                sendEmail(readAndCloseQuietly(getResource(TEMPLATE_PERMANENT_BONUS)), "Free Monthly Codenvy Gigabyte Hours for You",
                          accountOwnersEmails, MediaType.TEXT_HTML, properties);
            } else {
                sendEmail(readAndCloseQuietly(getResource(TEMPLATE_TEMPORARY_BONUS)), "Free Codenvy Gigabyte Hours for You",
                          accountOwnersEmails, MediaType.TEXT_HTML, properties);
            }
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send bonus notifications to, account: {}", bonus.getAccountId());
        }
    }

    public void sendReferringBonusNotification(Bonus bonus) throws ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(bonus.getAccountId());
        Map<String, String> properties = new HashMap<>();
        properties.put("bonus.amount", format(bonus.getResources()));
        properties.put("free.gbh", format(billingService
                                                  .getProvidedFreeResources(bonus.getAccountId(),
                                                                            billingPeriod.getStartDate().getTime(),
                                                                            System.currentTimeMillis())));
        LOG.debug("Send referring bonus notifications to {}", accountOwnersEmails);
        try {
            sendEmail(readAndCloseQuietly(getResource(TEMPLATE_REFERRING_BONUS)), "Codenvy Bonus for Referring",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send referring bonus notifications to, account: {}", bonus.getAccountId());
        }
    }


    public void sendReferredBonusNotification(Bonus bonus) throws ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(bonus.getAccountId());
        Map<String, String> properties = new HashMap<>();
        properties.put("bonus.amount", format(bonus.getResources()));
        properties.put("free.gbh", format(billingService
                                                  .getProvidedFreeResources(bonus.getAccountId(), billingPeriod.getStartDate().getTime(),
                                                                            System.currentTimeMillis())));
        LOG.debug("Send referred bonus notifications to {}", accountOwnersEmails);
        try {
            sendEmail(readAndCloseQuietly(getResource(TEMPLATE_REFERRED_BONUS)), "Codenvy Bonus for You",
                      accountOwnersEmails, MediaType.TEXT_HTML, properties);
        } catch (IOException | MessagingException e) {
            LOG.warn("Unable to send referred bonus notifications to, account: {}", bonus.getAccountId());
        }
    }

    private void sendEmail(String text, String subject, List<String> emails, String mediaType, Map<String, String> properties)
            throws IOException, MessagingException {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
        properties.put("free.limit", freeLimit);
        mailClient.sendMail(billingAddress,
                            Joiner.on(", ").join(emails),
                            null,
                            subject,
                            mediaType,
                            text,
                            properties);
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


    private String format(double d)
    {
        if((d % 1) == 0)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }
}
