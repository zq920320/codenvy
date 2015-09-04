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
package com.codenvy.ide.ext.gae.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import com.codenvy.ide.ext.gae.client.wizard.GAEJavaProjectWizardRegistrar;
import com.codenvy.ide.ext.gae.client.wizard.GAEPhpProjectWizardRegistrar;
import com.codenvy.ide.ext.gae.client.wizard.GAEPythonProjectWizardRegistrar;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

/**
 * The module that contains configuration of the client side part of the plugin.
 *
 * @author Evgen Vidolob
 * @author Andrey Plotnikov
 */
@ExtensionGinModule
public class GAEModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMultibinder<ProjectWizardRegistrar> projectWizardBinder = GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class);
        projectWizardBinder.addBinding().to(GAEJavaProjectWizardRegistrar.class);
        projectWizardBinder.addBinding().to(GAEPythonProjectWizardRegistrar.class);
        projectWizardBinder.addBinding().to(GAEPhpProjectWizardRegistrar.class);
    }
}