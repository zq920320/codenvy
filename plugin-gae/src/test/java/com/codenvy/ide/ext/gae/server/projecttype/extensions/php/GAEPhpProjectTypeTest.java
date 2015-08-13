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
package com.codenvy.ide.ext.gae.server.projecttype.extensions.php;

import com.codenvy.ide.ext.gae.server.projecttype.GaePhpProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaeProjectType;
import com.codenvy.ide.ext.gae.shared.GAEConstants;
import org.eclipse.che.ide.ext.php.server.project.type.PhpProjectType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GAEPhpProjectTypeTest {

    @Mock
    private PhpProjectType phpProjectType;

    @Mock
    private GaeProjectType gaeProjectType;

    @Test
    public void validateProjectTypeDefinition() {
        GaePhpProjectType gaePhpProjectType = new GaePhpProjectType(phpProjectType, gaeProjectType);
        assertFalse(gaePhpProjectType.canBeMixin());
        assertTrue(gaePhpProjectType.canBePrimary());
        assertEquals(GAEConstants.GAE_PHP_PROJECT, gaePhpProjectType.getDisplayName());
        assertEquals(GAEConstants.GAE_PHP_ID, gaePhpProjectType.getId());
        assertFalse(gaePhpProjectType.getParents().isEmpty());
        assertTrue(gaePhpProjectType.getParents().contains(phpProjectType));
        assertTrue(gaePhpProjectType.getParents().contains(gaeProjectType));
    }
}