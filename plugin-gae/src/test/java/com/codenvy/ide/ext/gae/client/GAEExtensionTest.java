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
package com.codenvy.ide.ext.gae.client;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import com.codenvy.ide.ext.gae.client.actions.DeployApplicationAction;

import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
* @author Valeriy Svydenko
* @author Dmitry Shnurenko
*/
@RunWith(GwtMockitoTestRunner.class)
public class GAEExtensionTest {

    @Mock
    private TreeStructureProviderRegistry treeStructureProviderRegistry;

    @Mock
    private ActionManager           actionManager;
    @Mock
    private ImageResource           logoImage;
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private DefaultActionGroup      mainActionGroup;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            resources;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private DeployApplicationAction updateAction;
    @Mock
    private DebuggerManager         debuggerManager;
    @Mock
    private DebuggerPresenter       debuggerPresenter;


    @InjectMocks
    private GAEExtension extension;

    @Before
    public void setUp() throws Exception {
        when(actionManager.getAction(IdeActions.GROUP_MAIN_MENU)).thenReturn(mainActionGroup);
    }

    @Test
    public void projectTreeShouldBeSetUp() throws Exception {
        extension.setUpJavaProjectTree(treeStructureProviderRegistry);
        verify(treeStructureProviderRegistry).associateProjectTypeToTreeProvider(GAE_JAVA_ID, MavenProjectTreeStructureProvider.ID);
    }

    @Test
    public void actionsShouldBeSet() throws Exception {
        when(resources.gaeLogoDashboard()).thenReturn(logoImage);

        extension.setUpActions(locale, actionManager, resources, updateAction, debuggerManager, debuggerPresenter);

        verify(resources.gaeCSS()).ensureInjected();
        verify(locale).deployGroupMenu();
        verify(resources).gaeLogoDashboard();
        verify(updateAction.getTemplatePresentation()).setIcon(logoImage);
        verify(debuggerManager).registeredDebugger(GAE_JAVA_ID, debuggerPresenter);
    }
}