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

import com.codenvy.api.account.impl.shared.dto.ClientToken;
import com.codenvy.api.account.impl.shared.dto.CreditCardDescriptor;
import com.codenvy.api.account.impl.shared.dto.NewCreditCard;
import com.codenvy.api.account.shared.dto.CreditCard;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.Service;
import com.codenvy.api.core.rest.annotations.Required;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.core.util.LinksHelper;
import com.codenvy.dto.server.DtoFactory;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Credit cards operations service.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/28/15.
 */
@Path("/creditcard")
public class CreditCardService extends Service {

    private final CreditCardDao creditCardDao;

    @Inject
    public CreditCardService(CreditCardDao creditCardDao) {
        this.creditCardDao = creditCardDao;
    }

    @ApiOperation(value = "Client token",
                  notes = "Get client token. Roles: account/owner, system/admin, system/manager.",
                  position = 14)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{accountId}/token")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public ClientToken getClientToken(@ApiParam(value = "Account ID", required = true)
                                      @PathParam("accountId") String accountId)
            throws NotFoundException, ServerException, ForbiddenException {
        return DtoFactory.getInstance().createDto(ClientToken.class).withToken(creditCardDao.getClientToken(accountId));
    }


    @ApiOperation(value = "Add credit card",
                  notes = "Add credit card to account. Roles: account/owner, system/admin, system/manager.",
                  position = 14)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/{accountId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public void addCreditCardToAccount(@ApiParam(value = "Account ID", required = true)
                                       @PathParam("accountId") String accountId,
                                       @ApiParam(value = "Client nonce", required = true)
                                       @Required NewCreditCard creditCard)
            throws NotFoundException, ServerException, ForbiddenException {

        creditCardDao.registerCard(accountId, creditCard.getNonce(), creditCard.getStreetAddress(), creditCard.getCity(),
                                   creditCard.getState(), creditCard.getCountry());
    }

    @ApiOperation(value = "Get credit cards",
                  notes = "Get all credit cards registered to account. Roles: account/owner, system/admin, system/manager.",
                  position = 14)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public List<CreditCardDescriptor> getCreditCardsOfAccount(@ApiParam(value = "Account ID", required = true)
                                                              @PathParam("accountId") String accountId)
            throws NotFoundException, ServerException, ForbiddenException {
        List<CreditCardDescriptor> result = new ArrayList<>();
        List<CreditCard> storedCards = creditCardDao.getCards(accountId);
        for (CreditCard card : storedCards) {
            result.add(DtoFactory.getInstance().createDto(CreditCardDescriptor.class).withAccountId(card.getAccountId())
                                 .withType(card.getType())
                                 .withNumber(card.getNumber())
                                 .withCardholder(card.getCardholder())
                                 .withExpiration(card.getExpiration())
                                 .withLinks(generateLinks(card)));
        }
        return result;
    }


    @ApiOperation(value = "Remove credit card",
                  notes = "Remove credit card and make account free. Roles: account/owner, system/admin, system/manager.",
                  position = 15)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{accountId}/{cardNumber}")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public void removeCreditCardFromAccount(@ApiParam(value = "Account ID", required = true)
                                            @PathParam("accountId") String accountId,
                                            @PathParam("cardNumber") String cardNumber) throws NotFoundException, ServerException,
                                                                                               ForbiddenException {

        for (CreditCard storedCard : creditCardDao.getCards(accountId)) {
            if (storedCard.getNumber().equals(cardNumber)) {
                creditCardDao.deleteCard(accountId, storedCard.getToken());
                break;
            }
        }
        // TODO charge if user has consumed paid resources
        // TODO send email
    }


    private List<Link> generateLinks(CreditCard card) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link removeCard = LinksHelper.createLink(HttpMethod.DELETE,
                                                       uriBuilder.clone()
                                                                 .path(getClass(), "removeCreditCardFromAccount")
                                                                 .build(card.getAccountId(), card.getToken())
                                                                 .toString(),
                                                       null,
                                                       null,
                                                       "remove card");
        return Arrays.asList(removeCard);
    }
}
