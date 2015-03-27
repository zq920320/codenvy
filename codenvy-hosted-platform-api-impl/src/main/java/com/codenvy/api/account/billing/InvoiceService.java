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

import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.impl.shared.dto.InvoiceDescriptor;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Max Shaposhnik
 */
@Api(value = "/invoice",
        description = "Invoice manager")
@Path("/invoice/{accountId}")
public class InvoiceService extends Service {
    private final BillingService    billingService;
    private final TemplateProcessor templateProcessor;

    @Inject
    public InvoiceService(BillingService billingService, TemplateProcessor initializer) {
        this.billingService = billingService;
        this.templateProcessor = initializer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public List<InvoiceDescriptor> getAccountInvoices(@ApiParam(value = "Account ID", required = true)
                                                      @PathParam("accountId") String accountId,
                                                      @ApiParam(value = "Max items count", required = false)
                                                      @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                                      @ApiParam(value = "Skip count", required = false)
                                                      @QueryParam("skipCount") int skipCount,
                                                      @ApiParam(value = "Period start time", required = false)
                                                      @DefaultValue("-1") @QueryParam("startPeriod") long startPeriod,
                                                      @ApiParam(value = "Period end time", required = false)
                                                      @DefaultValue("-1") @QueryParam("endPeriod") long endPeriod)
            throws NotFoundException, ServerException {
        List<InvoiceDescriptor> result = new ArrayList<>();
        InvoiceFilter filter = InvoiceFilter.builder()
                                            .withAccountId(accountId)
                                            .withMaxItems(maxItems)
                                            .withSkipCount(skipCount)
                                            .withFromDate(startPeriod)
                                            .withTillDate(endPeriod)
                                            .build();

        for (Invoice invoice : billingService.getInvoices(filter)) {
            result.add(toDescriptor(invoice));
        }
        return result;
    }

    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public InvoiceDescriptor getAccountInvoiceJson(@ApiParam(value = "Account ID", required = true)
                                                   @PathParam("accountId") String accountId,
                                                   @ApiParam(value = "Invoice ID", required = true)
                                                   @PathParam("invoiceId") long invoiceId) throws NotFoundException, ServerException {
        return toDescriptor(billingService.getInvoice(invoiceId));
    }

    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public Response getAccountInvoiceHtml(@ApiParam(value = "Account ID", required = true)
                                          @PathParam("accountId") final String accountId,
                                          @ApiParam(value = "Invoice ID", required = true)
                                          @PathParam("invoiceId") final long invoiceId) throws NotFoundException, ServerException {
        final Invoice invoice = billingService.getInvoice(invoiceId);
        StreamingOutput response = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try (PrintWriter w = new PrintWriter(outputStream)) {
                    templateProcessor.processTemplate(invoice, w);
                    w.flush();
                } catch (ServerException | ForbiddenException | NotFoundException e) {
                    throw new WebApplicationException(e);
                }
            }
        };
        return Response.ok(response).build();
    }

    private InvoiceDescriptor toDescriptor(Invoice invoice) {
        return DtoFactory.getInstance().createDto(InvoiceDescriptor.class).withId(invoice.getId())
                         .withAccountId(invoice.getAccountId())
                         .withCreationDate(invoice.getCreationDate())
                         .withFromDate(invoice.getFromDate())
                         .withTillDate(invoice.getTillDate())
                         .withCharges(invoice.getCharges())
                         .withCreditCardId(invoice.getCreditCardId())
                         .withMailingDate(invoice.getMailingDate())
                         .withPaymentDate(invoice.getPaymentDate())
                         .withPaymentState(invoice.getPaymentState())
                         .withTotal(invoice.getTotal())
                         .withLinks(generateLinks(invoice));
    }

    private List<Link> generateLinks(Invoice invoice) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link jsonLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getAccountInvoiceJson")
                                                               .build(invoice.getAccountId(), invoice.getId())
                                                               .toString(),
                                                     MediaType.APPLICATION_JSON,
                                                     null,
                                                     "self");
        final Link httpLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getAccountInvoiceHtml")
                                                               .build(invoice.getAccountId(), invoice.getId())
                                                               .toString(),
                                                     MediaType.TEXT_HTML,
                                                     null,
                                                     "html view");
        return Arrays.asList(jsonLink, httpLink);
    }
}
