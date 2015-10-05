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
package com.codenvy.ide.ext.gae.server.inject.factories;

import com.codenvy.ide.ext.gae.server.applications.JavaApplication;
import com.codenvy.ide.ext.gae.server.applications.yaml.PHPApplication;
import com.codenvy.ide.ext.gae.server.applications.yaml.PythonApplication;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URL;

/**
 * Factory that uses for creating different GAE application with yaml file (e.g. Python, PHP).
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public interface ApplicationFactory {

    /**
     * Create an instance of {@link PythonApplication} from given root directory.
     *
     * @param applicationDirectory
     *         directory where a project is located
     * @return an instance of  {@link PythonApplication}
     */
    @NotNull
    PythonApplication createPythonApplication(@NotNull File applicationDirectory);

    /**
     * Create an instance of {@link PHPApplication} from given root directory.
     *
     * @param applicationDirectory
     *         directory where a project is located
     * @return an instance of  {@link PHPApplication}
     */
    @NotNull
    PHPApplication createPHPApplication(@NotNull File applicationDirectory);

    /**
     * Create an instance of {@link JavaApplication} using url to binaries.
     *
     * @param url
     *         path to directory which contains binaries of project
     * @return an instance of  {@link JavaApplication}
     */
    @NotNull
    JavaApplication createJavaApplication(@NotNull URL url);

}