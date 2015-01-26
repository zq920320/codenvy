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
package com.codenvy.saas;

import com.codenvy.api.account.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.account.server.subscription.PaymentService;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.Strings;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.util.BillingDates.*;

/**
 * Charges paid saas accounts
 *
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Singleton
public class SaasBillingService {
    private static final Logger LOG                              = LoggerFactory.getLogger(SaasBillingService.class);
    public static final  Long   GIGABYTE_HOUR_IN_MEGABYTE_MINUTE = 61440l;

    private final AccountDao        accountDao;
    private final UserDao           userDao;
    private final MailSenderClient  mailSenderClient;
    private final PaymentService    paymentService;
    private final MeterBasedStorage meterBasedStorage;
    private final long              freeUsage;
    private final String            billingAddress;
    private final String            invoiceSubject;
    private final double            price;
    private final String            invoiceNoPaymentSubject;
    private final String            billingFailedSubject;
    private final String            successfulChargeMailTemplate;
    private final String            invoiceNoPaymentTemplate;
    private final String            unsuccessfulChargeMailTemplate;
    private final SimpleDateFormat  dateFormat;

    @Inject
    public SaasBillingService(AccountDao accountDao,
                              UserDao userDao,
                              MailSenderClient mailSenderClient,
                              PaymentService paymentService,
                              MeterBasedStorage meterBasedStorage,
                              @Named("subscription.saas.usage.free.mb_minutes") long freeUsage,
                              @Named("subscription.saas.price") double price,
                              @Named("subscription.saas.mail.address") String billingAddress,
                              @Named("subscription.saas.mail.invoice.subject") String invoiceSubject,
                              @Named("subscription.saas.mail.invoice.no_payment.subject") String invoiceNoPaymentSubject,
                              @Named("subscription.saas.mail.billing.failed.subject") String billingFailedSubject,
                              @Named("subscription.saas.mail.template.success") String successfulChargeMailTemplate,
                              @Named("subscription.saas.mail.template.success.no_payment") String invoiceNoPaymentTemplate,
                              @Named("subscription.saas.mail.template.fail") String unsuccessfulChargeMailTemplate) {
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.mailSenderClient = mailSenderClient;
        this.paymentService = paymentService;
        this.meterBasedStorage = meterBasedStorage;
        this.freeUsage = freeUsage;
        this.billingAddress = billingAddress;
        this.invoiceSubject = invoiceSubject;
        this.price = price;
        this.invoiceNoPaymentSubject = invoiceNoPaymentSubject;
        this.billingFailedSubject = billingFailedSubject;
        this.successfulChargeMailTemplate = successfulChargeMailTemplate;
        this.invoiceNoPaymentTemplate = invoiceNoPaymentTemplate;
        this.unsuccessfulChargeMailTemplate = unsuccessfulChargeMailTemplate;

        dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
        dateFormat.setLenient(false);
    }

    public void chargeAccounts() throws ApiException {
        chargeAccounts(getPreviousPeriodStartDate(), getPreviousPeriodEndDate());
    }

    public void chargeAccount(String accountId) throws ApiException {
        final Account account = accountDao.getById(accountId);

        chargeAccount(account, getPreviousPeriodStartDate(), getPreviousPeriodEndDate());
    }

    public void chargeAccounts(Date billingStartDate, Date billingEndDate) throws ApiException {
        final List<Account> paidSaasAccounts = accountDao.getPaidSaasAccountsWithOldBillingDate(billingEndDate);

        for (Account paidSaasAccount : paidSaasAccounts) {
            try {
                chargeAccount(paidSaasAccount, billingStartDate, billingEndDate);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    public void chargeAccount(Account account, Date billingStartDate, Date billingEndDate) throws ApiException {
        final String startBillingDateInMilliseconds = account.getAttributes().get("codenvy:billing.date");
        if (startBillingDateInMilliseconds != null) {
            final Date accountLastBillingDate = new Date(Long.parseLong(startBillingDateInMilliseconds));
            // TODO uncomment
//            if (!accountLastBillingDate.before(billingEndDate)) {
//                return;
//            }
//            billingStartDate = accountLastBillingDate;
        }
        doChargeAccount(account, billingStartDate, billingEndDate);
    }

    private void doChargeAccount(Account account, Date startDate, Date endDate) throws ApiException {
        final String accountId = account.getId();
        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", account.getId());
        final Map<String, Long> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId, startDate.getTime(), endDate.getTime());

        final List<String> accountOwnersEmails = getAccountOwnersEmails(account.getId());

        Long totalRamUsage = 0l;
        for (Map.Entry<String, Long> wsMemoryUsage : memoryUsedReport.entrySet()) {
            totalRamUsage += wsMemoryUsage.getValue();
        }
        double chargeAmount = 0;

        if (totalRamUsage > freeUsage) {
            final String ccToken = account.getAttributes().get("codenvy:creditCardToken");
            if (ccToken == null) {
                throw new ServerException("Paid account " + accountId + " doesn't have credit card token");
            }

            final String paymentDescription = "Saas; Period:" +
                                              dateFormat.format(startDate) +
                                              "-" +
                                              dateFormat.format(endDate);

            try {
                chargeAmount = Math.ceil(((double)totalRamUsage - freeUsage) / GIGABYTE_HOUR_IN_MEGABYTE_MINUTE) * price;
                paymentService.charge(ccToken, chargeAmount, accountId, paymentDescription);
            } catch (ForbiddenException e) {
                sendMailWithConsumption(accountOwnersEmails, memoryUsedReport, billingFailedSubject, unsuccessfulChargeMailTemplate,
                                        chargeAmount);
                throw e;
            }
        }

        account.getAttributes().put("codenvy:billing.date", String.valueOf(endDate.getTime()));
        // TODO uncomment to prevent multiple charges for the same period
//        accountDao.update(account);

        if (totalRamUsage > freeUsage) {
            sendMailWithConsumption(accountOwnersEmails, memoryUsedReport, invoiceSubject, successfulChargeMailTemplate, chargeAmount);
        } else {
            sendMailWithConsumption(accountOwnersEmails, memoryUsedReport, invoiceNoPaymentSubject, invoiceNoPaymentTemplate, 0);
        }
    }

    private void sendMailWithConsumption(List<String> accountOwnersEmails, Map<String, Long> consumption, String subject,
                                         String mailTemplate, double amount) {
        long totalConsumption = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Long> resourceConsumption : consumption.entrySet()) {
            totalConsumption += resourceConsumption.getValue();
            stringBuilder.append("<tr><td>")
                         .append(resourceConsumption.getKey())
                         .append("</td><td>")
                         .append((double)resourceConsumption.getValue() / GIGABYTE_HOUR_IN_MEGABYTE_MINUTE)
                         .append("</td></tr>");
        }
        final HashMap<String, String> mailTemplateProperties = new HashMap<>();
        mailTemplateProperties.put("resource.consumption", stringBuilder.toString());
        mailTemplateProperties.put("resource.consumption.total", String.valueOf(totalConsumption));
        mailTemplateProperties.put("resource.free", String.valueOf((double)freeUsage / GIGABYTE_HOUR_IN_MEGABYTE_MINUTE));
        mailTemplateProperties.put("resource.price", String.valueOf(price));
        mailTemplateProperties.put("resource.amount", String.valueOf(amount));

        try {
            mailSenderClient.sendMail(billingAddress,
                                      Strings.join(", ", accountOwnersEmails.toArray(new String[0])),
                                      null,
                                      subject,
                                      MediaType.TEXT_HTML,
                                      IoUtil.readAndCloseQuietly(IoUtil.getResource(mailTemplate)),
                                      mailTemplateProperties);
        } catch (IOException | MessagingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private List<String> getAccountOwnersEmails(String accountId) throws ServerException {
        final List<String> emails = new ArrayList<>();
        for (Member member : accountDao.getMembers(accountId)) {
            if (member.getRoles().contains("account/owner")) {
                try {
                    final User user = userDao.getById(member.getUserId());

                    emails.add(user.getEmail());
                } catch (ServerException | NotFoundException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return emails;
    }
}
