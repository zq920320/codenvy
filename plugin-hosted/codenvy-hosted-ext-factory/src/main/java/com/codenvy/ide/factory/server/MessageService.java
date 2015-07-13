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
package com.codenvy.ide.factory.server;

import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service for configuring messages on client side from external configuration
 *
 * @author Sergii Leschenko
 */
@Path("ui-messages")
public class MessageService {
    public static final String UNSTAGED_CHANGES_WARNING = "ui.message.unstaged.changes.warning";
    public final String unstagedChangesWarningMessage;

    @Inject
    public MessageService(@Named(UNSTAGED_CHANGES_WARNING) String unstagedChangesWarningMessage) {
        this.unstagedChangesWarningMessage = unstagedChangesWarningMessage;
    }

    @GET
    @Path("{msg-name}")
    @Produces(MediaType.TEXT_PLAIN)
    /**
     * Gives message that was inject from external configuration
     *
     * @param messageName does not must contain prefix "ui.message"
     */
    public String acceptFactory(@PathParam("msg-name") String messageName) {
        messageName = "ui.message." + messageName;
        switch (messageName) {
            case UNSTAGED_CHANGES_WARNING:
                return unstagedChangesWarningMessage;
            default: {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }
    }
}
