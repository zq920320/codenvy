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
package com.codenvy.ide.ext.gae.server.applications.yaml;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.File;

/**
 * Representation of Python application that has an ability to be deployed to GAE. It is Python GAE application with all
 * parameters(configuration).
 *
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 */
public class PythonApplication extends YamlApplication {
    @Inject
    public PythonApplication(@Assisted File applicationDirectory) {
        super(applicationDirectory, "Python");
    }
}