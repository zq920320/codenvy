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
package com.codenvy.runner.docker;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.project.shared.dto.RunnerEnvironment;
import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.runner.internal.ResourceAllocators;
import com.codenvy.docker.DockerConnector;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author andrew00x
 */
@Singleton
public class DockerRunner extends BaseDockerRunner {
    @Inject
    public DockerRunner(@Named(Constants.DEPLOY_DIRECTORY) java.io.File deployDirectoryRoot,
                        @Named(Constants.APP_CLEANUP_TIME) int cleanupTime,
                        @Named(HOST_NAME) String hostName,
                        @Nullable @Named(WATCH_UPDATE_OF_PROJECT_TYPES) String[] watchUpdateProjectTypes,
                        ResourceAllocators allocators,
                        CustomPortService portService,
                        DockerConnector dockerConnector,
                        EventService eventService,
                        ApplicationLinksGenerator applicationLinksGenerator) {
        super(deployDirectoryRoot,
              cleanupTime,
              hostName,
              watchUpdateProjectTypes == null ? Collections.<String>emptySet() : new HashSet<>(Arrays.asList(watchUpdateProjectTypes)),
              allocators,
              portService,
              dockerConnector,
              eventService,
              applicationLinksGenerator);
    }

    public List<RunnerEnvironment> getEnvironments() {
        // Must no appears as 'system' runners.
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public String getDescription() {
        return "The linux container runtime";
    }

    @Override
    protected DockerEnvironment getDockerEnvironment(RunRequest request) throws IOException, RunnerException {
        return new CustomDockerEnvironment(request);
    }
}
