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
package com.codenvy.api.account.billing;

import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.CreditCard;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.subscription.ServiceId;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Template engine initializer and processor.
 * @author Max Shaposhnik
 *
 */
@Singleton
public class TemplateProcessor {

    private static TemplateEngine templateEngine;

    private final WorkspaceDao workspaceDao;

    private final CreditCardDao cardDao;

    @Inject
    public TemplateProcessor(WorkspaceDao workspaceDao, CreditCardDao cardDao) {
        this.workspaceDao = workspaceDao;
        this.cardDao  = cardDao;
    }


    private final String NOT_REQUIRED_TEMPLATE_NAME      = "";
    private final String PAYMENT_FAIL_TEMPLATE_NAME      = "";
    private final String CC_MISSING_FAIL_TEMPLATE_NAME   = "";
    private final String PAID_SUCCESSFULLY_TEMPLATE_NAME = "mb_invoice_with_charges";


    @PostConstruct
    private void initialize() {
        ClassLoaderTemplateResolver templateResolver =
                new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setPrefix("/email-templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public String getTemplateName(PaymentState state) throws ServerException {
        switch (state) {
//            case NOT_REQUIRED:
//                return  NOT_REQUIRED_TEMPLATE_NAME;
//            case PAYMENT_FAIL:
//                return  PAYMENT_FAIL_TEMPLATE_NAME;
//            case CREDIT_CARD_MISSING:
//                return  CC_MISSING_FAIL_TEMPLATE_NAME;
//            case PAID_SUCCESSFULLY:
//                return  PAID_SUCCESSFULLY_TEMPLATE_NAME;
            default:
                return PAID_SUCCESSFULLY_TEMPLATE_NAME;
//                throw new ServerException(String.format("Unsupported payment state found: {}", state));
        }
    }


    public void processTemplate(Invoice invoice, Writer w) throws ServerException, NotFoundException, ForbiddenException {
        TemplateContext context = new TemplateContext();
        context.setVariable("invoice", invoice);
        context.setVariable("creationDate", new Date(invoice.getCreationDate()));
        context.setVariable("fromDate", new Date(invoice.getFromDate()));
        context.setVariable("untilDate", new Date(invoice.getUntilDate()));
        context.setVariable("sendDate", invoice.getMailingDate() == 0 ? new Date() : new Date(invoice.getMailingDate()));
        for (Charge charge : invoice.getCharges()) {
            if (charge.getServiceId().equals(ServiceId.FACTORY)) {
                context.setVariable("factoryCharge", charge);
            } else if (charge.getServiceId().equals(ServiceId.SAAS)) {
                Map<String, String> newDetails = new HashMap<>();
                for (Map.Entry<String, String> entry : charge.getDetails().entrySet()) {
                    newDetails.put(workspaceDao.getById(entry.getKey()).getName(), entry.getValue());
                }
                charge.setDetails(newDetails);
                context.setVariable("saasCharge", charge);
            }
        }
        PaymentState state = PaymentState.fromState(invoice.getPaymentState());
        if (state.equals(PaymentState.PAID_SUCCESSFULLY) || state.equals(PaymentState.PAYMENT_FAIL)) {
            for (CreditCard card : cardDao.getCards(invoice.getAccountId())) {
                if (card.getToken().equals(invoice.getCreditCardId())) {
                    context.setVariable("creditCard", card);
                    break;
                }
            }
        }
        String templateName = getTemplateName(state);
        templateEngine.process(templateName, context, w);
    }
}
