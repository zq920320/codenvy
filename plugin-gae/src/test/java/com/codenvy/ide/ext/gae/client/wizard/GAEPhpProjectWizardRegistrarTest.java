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
import com.google.inject.Provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PHP_ID;
import static org.eclipse.che.ide.ext.php.shared.ProjectAttributes.PHP_CATEGORY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public class GAEPhpProjectWizardRegistrarTest {
    @Mock
    private Provider<GAEYamlPagePresenter> gaeYamlPageProvider;

    @InjectMocks
    private GAEPhpProjectWizardRegistrar wizardRegistrar;

    @Test
    public void shouldReturnCorrectProjectTypeId() throws Exception {
        assertThat(wizardRegistrar.getProjectTypeId(), equalTo(GAE_PHP_ID));
    }

    @Test
    public void shouldReturnCorrectCategory() throws Exception {
        assertThat(wizardRegistrar.getCategory(), equalTo(PHP_CATEGORY));
    }

    @Test
    public void shouldReturnPages() throws Exception {
        assertThat(wizardRegistrar.getWizardPages(), hasItem(gaeYamlPageProvider));
    }
}
