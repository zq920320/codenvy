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
package com.codenvy.ide.ext.gae.server.projecttype.extensions.python;

import com.codenvy.ide.ext.gae.server.projecttype.GaeProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaePythonProjectType;
import com.codenvy.ide.ext.gae.shared.GAEConstants;
import org.eclipse.che.ide.ext.python.server.project.type.PythonProjectType;

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
public class GAEPythonProjectTypeDescriptionExtensionTest {

    @Mock
    private PythonProjectType pythonProjectType;

    @Mock
    private GaeProjectType gaeProjectType;

    @Test
    public void validateProjectTypeDefinition() {
        GaePythonProjectType gaePythonProjectType = new GaePythonProjectType(pythonProjectType, gaeProjectType);
        assertFalse(gaePythonProjectType.canBeMixin());
        assertTrue(gaePythonProjectType.canBePrimary());
        assertEquals(GAEConstants.GAE_PYTHON_PROJECT, gaePythonProjectType.getDisplayName());
        assertEquals(GAEConstants.GAE_PYTHON_ID, gaePythonProjectType.getId());
        assertFalse(gaePythonProjectType.getParents().isEmpty());
        assertTrue(gaePythonProjectType.getParents().contains(pythonProjectType));
        assertTrue(gaePythonProjectType.getParents().contains(gaeProjectType));
    }
}