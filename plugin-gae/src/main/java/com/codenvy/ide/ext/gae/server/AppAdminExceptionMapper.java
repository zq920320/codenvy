/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server;

import com.google.appengine.tools.admin.AdminException;
import com.google.inject.Singleton;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 */
@Provider
@Singleton
public class AppAdminExceptionMapper implements ExceptionMapper<AdminException> {
    /** {@inheritDoc} */
    @Override
    public Response toResponse(AdminException exception) {
        Throwable cause = exception.getCause();
        int status = 500;
        String causeMessage = cause == null ? exception.getMessage() : cause.getMessage();

        return Response
                .status(status)
                .entity(causeMessage)
                .type(TEXT_PLAIN)
                .build();
    }
}