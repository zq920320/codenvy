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
package com.codenvy.ide.ext.gae.client.wizard;

import com.codenvy.ide.ext.gae.client.wizard.yaml.GAEYamlPagePresenter;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PYTHON_ID;
import static org.eclipse.che.ide.ext.python.shared.ProjectAttributes.PYTHON_CATEGORY;

/**
 * Provides information for registering GAE Python project type in project wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class GAEPythonProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final List<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public GAEPythonProjectWizardRegistrar(Provider<GAEYamlPagePresenter> mavenPagePresenter) {
        wizardPages = new ArrayList<>();
        wizardPages.add(mavenPagePresenter);
    }

    @NotNull
    public String getProjectTypeId() {
        return GAE_PYTHON_ID;
    }

    @NotNull
    public String getCategory() {
        return PYTHON_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
