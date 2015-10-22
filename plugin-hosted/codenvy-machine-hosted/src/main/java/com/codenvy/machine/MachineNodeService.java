/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.machine;

import com.codenvy.machine.dto.MachineCopyProjectRequest;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;

/**
 * Provides additional services for docker machines
 *
 * @author Alexander Garagatyi
 */
@Path("/internal/machine")
@Singleton
public class MachineNodeService {
    private static final Logger LOG = LoggerFactory.getLogger(MachineNodeService.class);

    private final String          apiEndpoint;

    @Inject
    public MachineNodeService(@Named("api.endpoint") String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }


    /**
     * Docker creates bound folder as root user. <b>
     * So we use this method to prevent folder creation by docker daemon.
     */
    @Path("/folder/{path:.*}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createProjectsFolderForMachine(@PathParam("path") String path) throws ServerException {
        final File projectsFolder = new File("/" + path);
        if (!projectsFolder.mkdir()) {
            if (!projectsFolder.exists() || (!projectsFolder.canWrite() || !projectsFolder.canRead())) {
                LOG.error("Can't create projects folder " + path + " for machine.");
                throw new ServerException("Can't create projects folder " + path + " for machine.");
            }
        }
    }

    @Path("/binding")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void copySourcesToMachine(MachineCopyProjectRequest binding) throws ServerException {
        File fullPath;
        if (binding.getProject() != null) {
            fullPath = new File(binding.getHostFolder(), binding.getProject());
        } else {
            fullPath = new File(binding.getHostFolder());
        }

        try {
            copyProjectSource(fullPath,
                              binding.getWorkspaceId(),
                              binding.getProject(),
                              binding.getToken());
        } catch (IOException e) {
            IoUtil.deleteRecursive(fullPath);
            LOG.warn(e.getLocalizedMessage(), e);
            throw new ServerException("Project binding failed. " + e.getLocalizedMessage());
        }
    }

    @Path("/binding")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeSourcesOnMachine(MachineCopyProjectRequest binding) throws ServerException {
        File fullPath;
        if (binding.getProject() != null) {
            fullPath = new File(binding.getHostFolder(), binding.getProject());
        } else {
            fullPath = new File(binding.getHostFolder());
        }

        if (!IoUtil.deleteRecursive(fullPath)) {
            throw new ServerException("Error occurred on removing of binding.");
        }
    }

    private void copyProjectSource(File destinationDir, String workspaceId, String project, String token) throws IOException {
        final UriBuilder zipBallUriBuilder = UriBuilder.fromUri(apiEndpoint)
                                                       .path("project")
                                                       .path(workspaceId)
                                                       .path("export")
                                                       .queryParam("token", token);
        if (project != null) {
            zipBallUriBuilder.path(project);
        } else {
            zipBallUriBuilder.path("/");
        }

        final File zipBall = IoUtil.downloadFile(null, "sourcesZip", null, zipBallUriBuilder.build().toURL());
        ZipUtils.unzip(zipBall, destinationDir);
    }
}
