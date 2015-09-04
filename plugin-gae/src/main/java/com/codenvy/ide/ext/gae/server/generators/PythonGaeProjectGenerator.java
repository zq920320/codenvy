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

import static org.eclipse.che.ide.MimeType.TEXT_X_PYTHON;
import static org.eclipse.che.ide.MimeType.TEXT_YAML;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PYTHON_ID;
import static java.io.File.separator;

/**
 * The generator for GAE Python project. This generator has to create scratch GAE project. New Project contains app.yaml.
 *
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class PythonGaeProjectGenerator extends GeneralYamlProjectGenerator {

    private static final String CSS_FOLDER       = "css";
    private static final String JS_FOLDER        = "js";
    private static final String TEMPLATES_FOLDER = "templates";
    private static final String SCRIPT           = "main.py";
    private static final String PATH_TO_YAML     = separator + "template" + separator + "python" + separator + "app";
    private static final String PATH_TO_SCRIPT   = separator + "template" + separator + "python" + separator + "main";

    @Inject
    public PythonGaeProjectGenerator(GAEServerUtil gaeServerUtil) {
        super(GAE_PYTHON_ID, gaeServerUtil);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        baseFolder.createFolder(CSS_FOLDER);
        baseFolder.createFolder(JS_FOLDER);
        baseFolder.createFolder(TEMPLATES_FOLDER);

        createFile(baseFolder, YAML_NAME, PATH_TO_YAML, TEXT_YAML);
        createFile(baseFolder, SCRIPT, PATH_TO_SCRIPT, TEXT_X_PYTHON);

        try {
            applyYamlApplicationId(baseFolder, attributes);
        } catch (ApiException e) {
            throw new ServerException("Can't update app.yaml file: " + e.getMessage(), e);
        }
    }
}