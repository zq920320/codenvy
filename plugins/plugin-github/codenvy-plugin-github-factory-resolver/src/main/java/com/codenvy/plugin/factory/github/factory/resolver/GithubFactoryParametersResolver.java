/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.factory.github.factory.resolver;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Provides Factory Parameters resolver for github repositories.
 *
 * @author Florent Benoit
 */
public class GithubFactoryParametersResolver implements FactoryParametersResolver {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GithubFactoryParametersResolver.class);

    /**
     * Parameter name.
     */
    protected static final String URL_PARAMETER_NAME = "url";

    /**
     * Connection timeout of 10seconds.
     */
    private static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    /**
     * Regexp to find repository details (repository name, project name and branch and subfolder)
     * Examples of valid URLs are in the test class.
     */
    protected static final Pattern GITHUB_PATTERN = Pattern.compile("^(?:http)(?:s)?(?:\\:\\/\\/)github.com/(?<repoUser>[^/]++)/(?<repoName>[^/]++)(?:/tree/(?<branchName>[^/]++)(?:/(?<subFolder>.*))?)?$");

    /**
     * Check if this resolver can be used with the given parameters.
     *
     * @param factoryParameters
     *         map of parameters dedicated to factories
     * @return true if it will be accepted by the resolver implementation or false if it is not accepted
     */
    @Override
    public boolean accept(@NotNull Map<String, String> factoryParameters) {
        // Check if url parameter is a github URL
        return factoryParameters.containsKey(URL_PARAMETER_NAME) && GITHUB_PATTERN.matcher(factoryParameters.get(URL_PARAMETER_NAME)).matches();
    }

    /**
     * Create factory object based on provided parameters
     *
     * @param factoryParameters
     *         map containing factory data parameters provided through URL
     * @throws BadRequestException when data are invalid
     */
    @Override
    public Factory createFactory(@NotNull Map<String, String> factoryParameters) throws BadRequestException {

        // no need to check null value of url parameter as accept() method has performed the check
        String githubUrl = factoryParameters.get("url");

        // Apply github url to the regexp
        Matcher matcher = GITHUB_PATTERN.matcher(githubUrl);
        if (!matcher.matches()) {
            throw new BadRequestException(String.format(
                    "The given github url %s is not a valid URL github url. It should start with https://github.com/<user>/<repo>",
                    githubUrl));
        }

        String repoUser = matcher.group("repoUser");
        String repoName = matcher.group("repoName");
        String branchName = matcher.group("branchName");
        String subfolder = matcher.group("subFolder");

        // build factory for the given github url
        Map<String, String> parameters = new HashMap<>();
        if (branchName == null) {
            // use master if not specified
            branchName = "master";
        } else {
            parameters.put("branch", branchName);
        }
        if (subfolder != null) {
            parameters.put("keepDir", subfolder);
        }

        // github location for the clone
        String githubLocation = "https://github.com/" + repoUser + "/" + repoName;

        // FIXME: use it configurable and move it to
        final String DEFAULT_DOCKER_FILE_LOCATION = "https://dockerfiles.codenvycorp.com/templates-4.0/factory/factory-dockerfile";

        // check if repository contains a codenvy Dockerfile
        String dockerFileLocation = null;
        String githubDockerFileLocation = "https://raw.githubusercontent.com/" + repoUser + "/" + repoName + "/" +branchName + "/.codenvy.dockerfile";
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection)new URL(githubDockerFileLocation).openConnection();
            httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            final int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                dockerFileLocation = githubDockerFileLocation;
            }
        } catch (IOException ioe) {
            LOG.debug("Unable to check if remote location {0} is available or not", githubDockerFileLocation);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        // apply default location if there is none
        if (dockerFileLocation == null) {
            dockerFileLocation = DEFAULT_DOCKER_FILE_LOCATION;
        }

        String envName = repoUser;

        // Create factory object
        SourceStorageDto
                sourceStorageDto = newDto(SourceStorageDto.class).withLocation(githubLocation).withType("git").withParameters(parameters);
        ProjectConfigDto projectConfig = newDto(ProjectConfigDto.class).withSource(sourceStorageDto).withName(repoName).withType("blank")
                                                                       .withPath("/".concat(repoName));

        LimitsDto limitsDto = newDto(LimitsDto.class).withRam(2000);
        MachineSourceDto machineSourceDto = newDto(MachineSourceDto.class).withType("dockerfile").withLocation(dockerFileLocation);
        MachineConfigDto
                machineConfigDto =
                newDto(MachineConfigDto.class).withLimits(limitsDto).withType("docker").withSource(machineSourceDto).withDev(true)
                                              .withName("ws-machine");

        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withName(envName).withMachineConfigs(singletonList(machineConfigDto));

        final WorkspaceConfigDto
                workspaceConfig = newDto(WorkspaceConfigDto.class).withDefaultEnv(envName).withProjects(singletonList(projectConfig))
                                                                  .withEnvironments(singletonList(environmentDto)).withName(repoUser);
        final Factory factory = newDto(Factory.class).withWorkspace(workspaceConfig).withV("4.0");

        return factory;

    }
}
