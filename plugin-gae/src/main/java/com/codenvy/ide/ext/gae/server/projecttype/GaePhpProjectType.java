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
package com.codenvy.ide.ext.gae.server.projecttype;

import org.eclipse.che.api.project.server.type.ProjectType;
import com.codenvy.ide.ext.gae.shared.GAEConstants;
import org.eclipse.che.ide.ext.php.server.project.type.PhpProjectType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static org.eclipse.che.ide.api.project.type.RunnerCategory.PHP;

/**
 * @author Vitaly Parfonov
 * @author Dmitry Shnurenko
 */
@Singleton
public class GaePhpProjectType extends ProjectType {

    @Inject
    public GaePhpProjectType(PhpProjectType phpProjectType, GaeProjectType gaeProjectType) {
        super(GAEConstants.GAE_PHP_ID, GAEConstants.GAE_PHP_PROJECT, true, false);
        addParent(phpProjectType);
        addParent(gaeProjectType);
        addRunnerCategories(Arrays.asList(PHP.toString()));
    }
}
