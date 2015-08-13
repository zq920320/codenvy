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
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import com.codenvy.ide.ext.gae.client.actions.DeployApplicationAction;

import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;

/**
 * Codenvy IDE3 extension provides functionality for Google App Engine integration. It has to provides major operation for GAE application:
 * create application in GAE, deploy/update application in GAE and etc.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@Singleton
@Extension(title = "Google App Engine", version = "1.0.0")
public class GAEExtension {

    private static final String GIT_GROUP_MAIN_MENU = "git";

    // use Maven project tree for 'Google App Engine Project (GAEJava)'
    @Inject
    public void setUpJavaProjectTree(TreeStructureProviderRegistry treeStructureProviderRegistry) {
        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(GAE_JAVA_ID, MavenProjectTreeStructureProvider.ID);
    }

    @Inject
    public void setUpActions(GAELocalizationConstant locale,
                             ActionManager actionManager,
                             GAEResources resources,
                             DeployApplicationAction deployApplicationAction,
                             DebuggerManager debuggerManager,
                             DebuggerPresenter debuggerPresenter) {

        resources.gaeCSS().ensureInjected();

        DefaultActionGroup deploy = new DefaultActionGroup(locale.deployGroupMenu(), true, actionManager);

        DefaultActionGroup mainMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
        mainMenuGroup.add(deploy, new Constraints(Anchor.AFTER, GIT_GROUP_MAIN_MENU));

        deployApplicationAction.getTemplatePresentation().setIcon(resources.gaeLogoDashboard());

        deploy.add(deployApplicationAction);

        debuggerManager.registeredDebugger(GAE_JAVA_ID, debuggerPresenter);
    }
}