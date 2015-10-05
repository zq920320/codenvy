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
package com.codenvy.ide.ext.gae.server.rest;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.commons.xml.XMLTree;
import org.eclipse.che.dto.server.DtoFactory;
import com.codenvy.ide.ext.gae.server.applications.yaml.YamlAppInfo;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Parent;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APP_ENGINE_WEB_XML_PATH;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Class contains business logic that allows to get all needed parameters for app engine application from project which is updated.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Path("gae-parameters/{ws-id}/read")
@Singleton
public class GAEParametersService {
    @PathParam("ws-id")
    @Inject
    private String         wsId;
    @Inject
    private ProjectManager projectManager;

    /** Gets DTO object which contains information about maven app engine project from pom.xml and appengine-web.xml */
    @Path("/maven")
    @GET
    @Produces(APPLICATION_JSON)
    public GAEMavenInfo readParameters(@QueryParam("projectpath") String projectPath) throws ApiException, IOException {
        VirtualFile projectFolder = getProjectFolder(projectPath);// use folder because here project can be not configure

        VirtualFile pomFile = projectFolder.getChild("pom.xml");

        if (pomFile == null) {
            throw new IllegalArgumentException("There is no pom.xml file in project: " + projectPath);
        }

        DtoFactory dtoFactory = DtoFactory.getInstance();
        GAEMavenInfo gaeMavenInfo = dtoFactory.createDto(GAEMavenInfo.class);

        Model model = Model.readFrom(pomFile);

        Parent parent = model.getParent();

        boolean isParentNull = parent == null;

        String artifactId = model.getArtifactId();
        String groupId = model.getGroupId();
        String version = model.getVersion();

        if (artifactId != null) {
            gaeMavenInfo.setArtifactId(artifactId);
        }

        if (groupId != null) {
            gaeMavenInfo.setGroupId(isParentNull ? groupId : parent.getGroupId());
        }

        if (version != null) {
            gaeMavenInfo.setVersion(isParentNull ? version : parent.getVersion());
        }

        gaeMavenInfo.setApplicationId(getWebAppEngineParameters(projectFolder));

        return gaeMavenInfo;
    }



    /** Gets DTO object which contains information about yaml parameters */
    @Path("/yaml")
    @GET
    @Produces(APPLICATION_JSON)
    public YamlParameterInfo readYamlParameters(@QueryParam("projectpath") String projectPath) throws ApiException, IOException {
        VirtualFile projectFolder = getProjectFolder(projectPath);// use folder because here project can be not configure

        VirtualFile yamlFile = projectFolder.getChild("app.yaml");

        if (yamlFile == null) {
            throw new IllegalArgumentException("There is no app.yaml file in project: " + projectPath);
        }

        YamlParameterInfo gaeYamlInfo = DtoFactory.getInstance().createDto(YamlParameterInfo.class);
        gaeYamlInfo.setApplicationId(getApplicationIdFromYaml(yamlFile));

        return gaeYamlInfo;
    }

    private VirtualFile getProjectFolder(@NotNull String projectPath) throws ForbiddenException, NotFoundException, ServerException {
        VirtualFileEntry fileEntry = projectManager.getProjectsRoot(wsId).getChild(projectPath);
        if (fileEntry != null)
            return fileEntry.getVirtualFile();
        else throw new NotFoundException(String.format("Project %s not found in %s workspace ", projectPath, wsId));
    }

    @NotNull
    private String getApplicationIdFromYaml(@NotNull VirtualFile yamlFile) throws ApiException, IOException {
        try (InputStream inputStream = yamlFile.getContent().getStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return YamlAppInfo.parse(reader).application;
        }
    }

    @NotNull
    private String getWebAppEngineParameters(@NotNull VirtualFile projectFolder) throws ApiException, IOException {

        VirtualFile appEngineWebXml = projectFolder.getChild(APP_ENGINE_WEB_XML_PATH);

        if (appEngineWebXml == null) {
            throw new IllegalArgumentException("There is no appengine-web.xml file in project: " + projectFolder.getPath());
        }

        return XMLTree.from(appEngineWebXml.getContent().getStream()).getSingleText("appengine-web-app/application");
    }

}