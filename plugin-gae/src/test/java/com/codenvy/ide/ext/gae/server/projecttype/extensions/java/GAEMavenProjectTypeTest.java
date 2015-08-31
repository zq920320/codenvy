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
package com.codenvy.ide.ext.gae.server.projecttype.extensions.java;

import com.codenvy.ide.ext.gae.server.projecttype.GaeMavenProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaeProjectType;
import com.codenvy.ide.ext.gae.shared.GAEConstants;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenProjectType;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GAEMavenProjectTypeTest {

    @Mock
    private MavenProjectType mavenProjectType;

    @Mock
    private GaeProjectType gaeProjectType;

    @Test
    public void validateProjectTypeDefinition() throws Exception {
        GaeMavenProjectType gaeMavenProjectType = new GaeMavenProjectType(mavenProjectType, gaeProjectType);
        assertFalse(gaeMavenProjectType.canBeMixin());
        assertTrue(gaeMavenProjectType.canBePrimary());
        assertEquals(GAEConstants.GAE_JAVA_PROJECT, gaeMavenProjectType.getDisplayName());
        assertEquals(GAEConstants.GAE_JAVA_ID, gaeMavenProjectType.getId());
        assertFalse(gaeMavenProjectType.getParents().isEmpty());
        assertTrue(gaeMavenProjectType.getParents().contains(mavenProjectType));
        assertTrue(gaeMavenProjectType.getParents().contains(gaeProjectType));
        assertThat(gaeMavenProjectType.getDefaultBuilder(), is("maven"));
    }
}