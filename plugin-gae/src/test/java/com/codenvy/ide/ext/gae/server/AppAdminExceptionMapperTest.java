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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Andrey Plotnikov */
@RunWith(MockitoJUnitRunner.class)
public class AppAdminExceptionMapperTest {

    private static final String MESSAGE = "some message";

    @Mock
    private AdminException          exception;
    @InjectMocks
    private AppAdminExceptionMapper mapper;

    @Test
    public void exceptionShouldBeAnalyzedWhenCauseIsEmpty() throws Exception {
        when(exception.getMessage()).thenReturn(MESSAGE);

        getAndVerifyResponse();
    }

    private void getAndVerifyResponse() {
        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus(), is(500));
        assertEquals(MESSAGE, response.getEntity());

        MultivaluedMap<String, Object> metadata = response.getMetadata();
        List<Object> contentTypes = metadata.get(HttpHeaders.CONTENT_TYPE);

        assertThat(contentTypes, not(nullValue()));
        assertThat(contentTypes.size(), is(1));
        assertEquals(TEXT_PLAIN, contentTypes.get(0));
    }

    @Test
    public void exceptionShouldBeAnalyzedWhenCauseIsNotEmpty() throws Exception {
        Exception causeException = mock(Exception.class);
        when(causeException.getMessage()).thenReturn(MESSAGE);

        when(exception.getCause()).thenReturn(causeException);

        getAndVerifyResponse();
    }

}