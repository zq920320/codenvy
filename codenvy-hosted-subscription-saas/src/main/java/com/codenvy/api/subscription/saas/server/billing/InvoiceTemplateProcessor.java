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
package com.codenvy.api.subscription.saas.server.billing;

import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.saas.shared.dto.Charge;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;

/**
 * Template engine initializer and processor.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class InvoiceTemplateProcessor {

    private static TemplateEngine templateEngine;

    private final WorkspaceDao workspaceDao;

    private final AccountDao accountDao;

    private final CreditCardDao cardDao;

    private final UserDao userDao;

    private final String paymentNotRequiredTemplateName;
    private final String paymentFailTemplateName;
    private final String paidSuccessfullyTemplateName;

    @Inject
    public InvoiceTemplateProcessor(@Named("subscription.saas.mail.template.success") String successTemplate,
                                    @Named("subscription.saas.mail.template.fail") String failTemplate,
                                    @Named("subscription.saas.mail.template.success.no_payment") String noPaymentRequiredTemplate,
                                    WorkspaceDao workspaceDao, AccountDao accountDao, UserDao userDao, CreditCardDao cardDao) {
        this.paidSuccessfullyTemplateName = successTemplate;
        this.paymentFailTemplateName = failTemplate;
        this.paymentNotRequiredTemplateName = noPaymentRequiredTemplate;
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
        this.cardDao = cardDao;
        this.userDao = userDao;
    }


    @PostConstruct
    private void initialize() {
        ClassLoaderTemplateResolver templateResolver =
                new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setCacheTTLMs(3600000L);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public String getTemplateName(PaymentState state) throws ServerException {
        switch (state) {
            case NOT_REQUIRED:
                return paymentNotRequiredTemplateName;
            case PAYMENT_FAIL:
            case CREDIT_CARD_MISSING:
                return paymentFailTemplateName;
            case PAID_SUCCESSFULLY:
                return paidSuccessfullyTemplateName;
            default:
                throw new ServerException(String.format("Unsupported payment state found: %s", state));
        }
    }


    public void processTemplate(Invoice invoice, Writer w) throws ServerException, NotFoundException, ForbiddenException {
        TemplateContext context = new TemplateContext();
        context.setVariable("invoice", invoice);
        context.setVariable("creationDate", new Date(invoice.getCreationDate()));
        context.setVariable("fromDate", new Date(invoice.getFromDate()));
        context.setVariable("tillDate", new Date(invoice.getTillDate()));

        context.setVariable("sendDate", invoice.getMailingDate() == 0 ? new Date() : new Date(invoice.getMailingDate()));
        for (Member member : accountDao.getMembers(invoice.getAccountId())) {
            if (member.getRoles().contains("account/owner")) {
                context.setVariable("email", userDao.getById(member.getUserId()).getEmail());
            }
        }
        for (Charge charge : invoice.getCharges()) {
            if (SAAS_SUBSCRIPTION_ID.equals(charge.getServiceId())) {
                Map<String, String> newDetails = new HashMap<>();
                int i = 0;
                for (Map.Entry<String, String> entry : charge.getDetails().entrySet()) {
                    try {
                        newDetails.put(workspaceDao.getById(entry.getKey()).getName(), entry.getValue());
                    } catch (NotFoundException e) {
                        newDetails.put(String.format("Temp Workspace %d", ++i), entry.getValue());
                    }
                }
                charge.setDetails(newDetails);
                context.setVariable("saasCharge", charge);
            }
        }
        PaymentState state = PaymentState.fromState(invoice.getPaymentState());
        if (state.equals(PaymentState.PAID_SUCCESSFULLY) || state.equals(PaymentState.PAYMENT_FAIL) ||
            state.equals(PaymentState.NOT_REQUIRED)) {
            for (CreditCard card : cardDao.getCards(invoice.getAccountId())) {
                if (card.getToken().equals(invoice.getCreditCardId())) {
                    context.setVariable("creditCard", card);
                    break;
                }
            }
        } else if (state.equals(PaymentState.CREDIT_CARD_MISSING)) {
            context.setVariable("creditCard", DtoFactory.getInstance().createDto(CreditCard.class).withType("MISSING")
                                                        .withNumber("xxxxxxxxxxxxxxxx")
                                                        .withCardholder("UNKNOWN")
                                                        .withStreetAddress("")
                                                        .withCity("")
                                                        .withState("")
                                                        .withCountry(""));
        }
        String templateName = getTemplateName(state);
        templateEngine.process(templateName, context, w);
    }
}
