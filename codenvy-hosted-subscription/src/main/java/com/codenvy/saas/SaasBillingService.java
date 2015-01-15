/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String            billingFailedSubject;
    private final String            successfulChargeMailTemplate;
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
                              @Named("subscription.saas.mail.billing.failed.subject") String billingFailedSubject,
                              @Named("subscription.saas.mail.template.success") String successfulChargeMailTemplate,
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
        this.billingFailedSubject = billingFailedSubject;
        this.successfulChargeMailTemplate = successfulChargeMailTemplate;
        this.unsuccessfulChargeMailTemplate = unsuccessfulChargeMailTemplate;

        dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
        dateFormat.setLenient(false);
    }

    public void chargeAccounts() throws ApiException {
        chargeAccounts(getDefaultBillingStartDate(), getBillingPeriodEndDate());
    }

    public void chargeAccount(String accountId) throws ApiException {
        final Account account = accountDao.getById(accountId);

        chargeAccount(account, getDefaultBillingStartDate(), getBillingPeriodEndDate());
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
//        final Map<String, Long> memoryUsedReport = new HashMap<>();
//        memoryUsedReport.put("ws1", 30000l);
//        memoryUsedReport.put("ws2", 100000l);
//        memoryUsedReport.put("ws3", 600000l);
        final Map<String, Long> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId, startDate.getTime(), endDate.getTime());

        final List<String> accountOwnersEmails = getAccountOwnersEmails(account.getId());

        Long totalRamUsage = 0l;
        for (Map.Entry<String, Long> wsMemoryUsage : memoryUsedReport.entrySet()) {
            totalRamUsage += wsMemoryUsage.getValue();
        }
        // TODO
        totalRamUsage+=614400l;

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
                final double chargeAmount = Math.ceil(((double)totalRamUsage - freeUsage) / GIGABYTE_HOUR_IN_MEGABYTE_MINUTE) * price;
                paymentService.charge(ccToken, chargeAmount, accountId, paymentDescription);
            } catch (ForbiddenException e) {
                sendBillingFailedEmail(accountOwnersEmails);
                throw e;
            }
        }

        account.getAttributes().put("codenvy:billing.date", String.valueOf(endDate.getTime()));
        accountDao.update(account);

        if (totalRamUsage > freeUsage) {
            sendInvoiceEmail(accountOwnersEmails, memoryUsedReport);
        } else {
            // TODO send email that says user consumed less than free amount
        }
    }

    private Date getDefaultBillingStartDate() {
        // TODO use period from config
        final Calendar calendar = Calendar.getInstance();

        //calendar.add(Calendar.MONTH, -1);
        // TODO used to calculate for current month
        // TODO change to -1 to get prev month
//        calendar.add(Calendar.MONTH, 1);

        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date getBillingPeriodEndDate() {
        final Calendar calendar = Calendar.getInstance();

        // TODO used to calculate for current month
        // TODO remove to get prev month
        calendar.add(Calendar.MONTH, 1);

        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private void sendBillingFailedEmail(List<String> accountOwnersEmails) {
        try {
            mailSenderClient.sendMail(billingAddress,
                                      Strings.join(", ", accountOwnersEmails.toArray(new String[0])),
                                      null,
                                      billingFailedSubject,
                                      MediaType.TEXT_HTML,
                                      IoUtil.readAndCloseQuietly(IoUtil.getResource(unsuccessfulChargeMailTemplate)),
                                      new HashMap<String, String>());
        } catch (IOException | MessagingException innerException) {
            LOG.error(innerException.getLocalizedMessage(), innerException);
        }
    }

    private void sendInvoiceEmail(List<String> accountOwnersEmails, Map<String, Long> consumption) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Long> resourceConsumption : consumption.entrySet()) {
            stringBuilder.append("<tr><td>")
                         .append(resourceConsumption.getKey())
                         .append("</td><td>")
                         .append((double)resourceConsumption.getValue() / GIGABYTE_HOUR_IN_MEGABYTE_MINUTE)
                         .append("</td></tr>");
        }
        try {
            mailSenderClient.sendMail(billingAddress,
                                      Strings.join(", ", accountOwnersEmails.toArray(new String[0])),
                                      null,
                                      invoiceSubject,
                                      MediaType.TEXT_HTML,
                                      IoUtil.readAndCloseQuietly(IoUtil.getResource(successfulChargeMailTemplate)),
                                      Collections.singletonMap("resource.consumption", stringBuilder.toString()));
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
