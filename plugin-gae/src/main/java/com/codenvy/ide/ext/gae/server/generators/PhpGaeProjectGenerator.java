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
package com.codenvy.ide.ext.gae.server.generators;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_PHP;
import static org.eclipse.che.ide.MimeType.TEXT_YAML;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PHP_ID;
import static java.io.File.separator;

/**
 * The generator for GAE php project. This generator has to create scratch GAE project with. New Project contains helloworld.php and GAE
 * configured file app.yaml. This file is using for deploying.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class PhpGaeProjectGenerator extends GeneralYamlProjectGenerator {

    private static final String PHP_NAME = "helloworld.php";

    private static final String PATH_TO_PHP  = separator + "template" + separator + "php" + separator + "helloworld";
    private static final String PATH_TO_YAML = separator + "template" + separator + "php" + separator + "app";

    @Inject
    public PhpGaeProjectGenerator(GAEServerUtil gaeServerUtil) {
        super(GAE_PHP_ID, gaeServerUtil);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map <String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        createFile(baseFolder, YAML_NAME, PATH_TO_YAML, TEXT_YAML);
        createFile(baseFolder, PHP_NAME, PATH_TO_PHP, APPLICATION_PHP);

        try {
            applyYamlApplicationId(baseFolder, attributes);
        } catch (ApiException e) {
            throw new ServerException("Can't update app.yaml file: " + e.getMessage(), e);
        }
    }
}