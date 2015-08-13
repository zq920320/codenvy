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

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;
import com.codenvy.ide.ext.gae.client.wizard.yaml.GAEYamlPagePresenter;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.annotation.Nonnull;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PHP_ID;
import static org.eclipse.che.ide.ext.php.shared.ProjectAttributes.PHP_CATEGORY;

/**
 * Provides information for registering GAE PHP project type in project wizard.
 *
 * @author Artem Zatsarynnyy
 */
public class GAEPhpProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final Array<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public GAEPhpProjectWizardRegistrar(Provider<GAEYamlPagePresenter> mavenPagePresenter) {
        wizardPages = Collections.createArray();
        wizardPages.add(mavenPagePresenter);
    }

    @Nonnull
    public String getProjectTypeId() {
        return GAE_PHP_ID;
    }

    @Nonnull
    public String getCategory() {
        return PHP_CATEGORY;
    }

    @Nonnull
    public Array<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
