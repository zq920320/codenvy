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
package com.google.appengine.tools.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintWriter;

import static com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class IdeAppAdminTest {
    @Mock
    private GenericApplication application;
    @Mock
    private PrintWriter        errorWriter;

    private IdeAppAdmin ideAppAdmin;

    @Before
    public void setUp() throws Exception {
        ideAppAdmin = new IdeAppAdmin(new ConnectOptions(),  application);
    }

    @Test
    public void applicationShouldBeReturned() throws Exception {
        assertThat(ideAppAdmin.getApplication(), equalTo(application));
    }
}