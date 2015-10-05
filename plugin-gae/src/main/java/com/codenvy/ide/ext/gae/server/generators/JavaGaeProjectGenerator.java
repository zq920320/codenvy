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
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.commons.xml.XMLTree;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.TEXT_XML;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.SOURCE_FOLDER;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.WEB_INF_FOLDER;

/**
 * The generator for GAE maven project. This generator has to create scratch GAE project with. New Project contains pom.xml and GAE
 * configured files (web.xml, appengine-web.xml and logging.properties). These files are using for deploying.
 *
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaGaeProjectGenerator implements CreateProjectHandler {
    public static final  String POM                     = "pom.xml";
    public static final  String LOGGING                 = "logging.properties";
    public static final  String APP_ENGINE_WEB          = "appengine-web.xml";
    public static final  String WEB                     = "web.xml";
    private static final String PATH_TO_TEMPLATE_FOLDER = "/template/java";
    private static final String PATH_TO_POM             = PATH_TO_TEMPLATE_FOLDER + "/pom";
    private static final String PATH_TO_LOGGING         = PATH_TO_TEMPLATE_FOLDER + "/logging";
    private static final String PATH_TO_APP_ENGINE_WEB  = PATH_TO_TEMPLATE_FOLDER + "/appengine-web";
    private static final String PATH_TO_WEB             = PATH_TO_TEMPLATE_FOLDER + "/web";

    private final GAEServerUtil gaeUtil;

    @Inject
    public JavaGaeProjectGenerator(GAEServerUtil gaeUtil) {
        this.gaeUtil = gaeUtil;
    }

    @Override
    public void onCreateProject(@NotNull FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        generateProject(baseFolder, attributes);
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return GAE_JAVA_ID;
    }

    private void generateProject(@NotNull FolderEntry baseFolder, Map<String, AttributeValue> attributes) throws ForbiddenException,
                                                                                                                 ConflictException,
                                                                                                                 ServerException {
        baseFolder.createFolder(SOURCE_FOLDER);
        baseFolder.createFolder(TEST_SOURCE_FOLDER);
        FolderEntry webInfFolder = baseFolder.createFolder(WEB_INF_FOLDER);

        try {
            baseFolder.createFile(POM, IOUtils.toByteArray(getClass().getResourceAsStream(PATH_TO_POM)), TEXT_XML);

            webInfFolder.createFile(LOGGING, IOUtils.toByteArray(getClass().getResourceAsStream(PATH_TO_LOGGING)), TEXT_XML);
            webInfFolder.createFile(WEB, IOUtils.toByteArray(getClass().getResourceAsStream(PATH_TO_WEB)), TEXT_XML);
            webInfFolder.createFile(APP_ENGINE_WEB, IOUtils.toByteArray(getClass().getResourceAsStream(PATH_TO_APP_ENGINE_WEB)), TEXT_XML);

            applyPomConfiguration(baseFolder, attributes);
            applyApplicationId(webInfFolder, attributes);
        } catch (ApiException | IOException e) {
            throw new ServerException("Can't write a file: " + e.getMessage(), e);
        }
    }

    private void applyPomConfiguration(@NotNull FolderEntry baseFolder, @NotNull Map<String, AttributeValue> attributes)
            throws ApiException, IOException {
        VirtualFile pomFile = baseFolder.getChild(POM).getVirtualFile();
        XMLTree pomTree = XMLTree.from(pomFile.getContent().getStream());

        AttributeValue artifactId = attributes.get(MavenAttributes.ARTIFACT_ID);
        if (artifactId != null) {
            pomTree.updateText("project/artifactId", artifactId.getList().get(0));
        }

        AttributeValue groupId = attributes.get(MavenAttributes.GROUP_ID);
        if (groupId != null) {
            pomTree.updateText("project/groupId", groupId.getList().get(0));
        }

        AttributeValue version = attributes.get(MavenAttributes.VERSION);
        if (version != null) {
            pomTree.updateText("project/version", version.getList().get(0));
        }

        pomFile.updateContent(new ByteArrayInputStream(pomTree.getBytes()), null);
    }

    private void applyApplicationId(@NotNull FolderEntry webFolder, @NotNull Map<String, AttributeValue> attributes)
            throws ApiException, IOException {
        AttributeValue applicationId = attributes.get(APPLICATION_ID);
        if (applicationId == null) {
            return;
        }

        VirtualFile webXmlFile = webFolder.getChild(APP_ENGINE_WEB).getVirtualFile();
        gaeUtil.setApplicationIdToWebAppEngine(webXmlFile, applicationId.getList().get(0));
    }

}