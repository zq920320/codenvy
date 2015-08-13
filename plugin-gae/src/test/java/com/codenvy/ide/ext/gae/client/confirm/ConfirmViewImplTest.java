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
package com.codenvy.ide.ext.gae.client.confirm;

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.codenvy.ide.ext.gae.client.confirm.ConfirmView.ActionDelegate;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ConfirmViewImplTest {
    private final String SOME_STRING = "text";

    @Mock
    private GAELocalizationConstant locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            gaeResources;
    @Mock
    private ActionDelegate          delegate;

    @InjectMocks
    private ConfirmViewImpl view;

    @Before
    public void setUp() throws Exception {
        view.setDelegate(delegate);
    }

    @Test
    public void closeMethodShouldBeDoneWhenCancelButtonClicked() throws Exception {
        view.onClose();

        verify(delegate).onCancelButtonClicked();
    }

    @Test
    public void subtitleShouldBeSet() throws Exception {
        view.setSubtitle(SOME_STRING);

        verify(view.subTitleLabel).setText(SOME_STRING);
    }

    @Test
    public void subtitleStyleShouldBeAdded() throws Exception {
        view.addSubtitleStyleName(SOME_STRING);

        verify(view.subTitleLabel).addStyleName(SOME_STRING);
    }

    @Test
    public void userInstructionShouldBeSet() throws Exception {
        view.setUserInstructions(SOME_STRING);

        verify(view.instructionLabel).setText(SOME_STRING);
    }
}