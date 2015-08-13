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
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.google.inject.Singleton;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APP_ENGINE_WEB_XML_PATH;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.ERROR_WEB_ENGINE_VALIDATE;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.ERROR_YAML_VALIDATE;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Service which check project validation. For java project,service checks existence of appengine-web.xml, for python
 * and php projects, it checks existence of app.yaml file.
 *
 * @author Dmitry Shnurenko
 */
@Path("gae-validator/{ws-id}")
@Singleton
public class GAEValidateService {

    @PathParam("ws-id")
    @Inject
    private String         wsId;
    @Inject
    private ProjectManager projectManager;

    @Path("/validate")
    @GET
    @Produces(APPLICATION_JSON)
    public void validateProject(@QueryParam("projectpath") String projectPath) throws ApiException {
        Project project = projectManager.getProject(wsId, projectPath);
        String projectId = project.getConfig().getTypeId();

        VirtualFile baseFolder = project.getBaseFolder().getVirtualFile();

        VirtualFile appengineXml = baseFolder.getChild(APP_ENGINE_WEB_XML_PATH);
        VirtualFile yaml = baseFolder.getChild("app.yaml");

        if (appengineXml != null || yaml != null) {
            return;
        }

        String message = GAE_JAVA_ID.equals(projectId) ? ERROR_WEB_ENGINE_VALIDATE : ERROR_YAML_VALIDATE;
        throw new ApiException(message);
    }
}