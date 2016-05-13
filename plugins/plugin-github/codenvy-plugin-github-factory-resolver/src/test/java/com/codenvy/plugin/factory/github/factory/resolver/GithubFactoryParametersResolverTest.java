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
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.factory.github.factory.resolver.GithubFactoryParametersResolver.GITHUB_PATTERN;
import static com.codenvy.plugin.factory.github.factory.resolver.GithubFactoryParametersResolver.URL_PARAMETER_NAME;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Validate operations performed by the Github Factory service
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GithubFactoryParametersResolverTest {

    /**
     * Instance of resolver that will be tested.
     */
    @InjectMocks
    private GithubFactoryParametersResolver githubFactoryParametersResolver;

    /**
     * Check URLs are valid with regexp
     */
    @Test
    public void checkRegexp() {
        List<String> urls = new ArrayList<>();
        urls.add("https://github.com/eclipse/che");
        urls.add("https://github.com/eclipse/che/tree/4.2.x");
        urls.add("https://github.com/eclipse/che/tree/master/");
        urls.add("https://github.com/eclipse/che/tree/master/dashboard/");
        urls.add("https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git");
        urls.add("https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git/");
        urls.parallelStream().forEach((url) -> assertTrue(GITHUB_PATTERN.matcher(url).matches(), "url " + url + " is invalid"));
    }

    /**
     * Check missing parameter name can't be accepted by this resolver
     */
    @Test
    public void checkMissingParameter() throws BadRequestException {
        Map<String, String> parameters = singletonMap("foo", "this is a foo bar");
        boolean accept = githubFactoryParametersResolver.accept(parameters);
        // shouldn't be accepted
        assertFalse(accept);
    }

    /**
     * Check url which is not a github url can't be accepted by this resolver
     */
    @Test
    public void checkInvalidAcceptUrl() throws BadRequestException {
        Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, "http://www.eclipse.org/che");
        boolean accept = githubFactoryParametersResolver.accept(parameters);
        // shouldn't be accepted
        assertFalse(accept);
    }

    /**
     * Check github url will be be accepted by this resolver
     */
    @Test
    public void checkValidAcceptUrl() throws BadRequestException {
        Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, "https://github.com/codenvy/codenvy.git");
        boolean accept = githubFactoryParametersResolver.accept(parameters);
        // shouldn't be accepted
        assertTrue(accept);
    }


    /**
     * Check that with a simple valid URL github url it works
     */
    @Test
    public void shouldReturnGitHubSimpleFactory() throws Exception {

        String githubUrl = "https://github.com/eclipse/che";
        Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, githubUrl);
        Factory responseFactory = githubFactoryParametersResolver.createFactory(parameters);

        // check we have a project and with github URL
        WorkspaceConfigDto workspaceConfigDto = responseFactory.getWorkspace();
        assertNotNull(workspaceConfigDto);
        List<ProjectConfigDto> projects = workspaceConfigDto.getProjects();
        assertNotNull(projects);
        assertEquals(projects.size(), 1);

        ProjectConfigDto projectConfigDto = projects.get(0);
        assertNotNull(projects);
        SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
        assertNotNull(sourceStorageDto);
        assertEquals(sourceStorageDto.getType(), "git");
        assertEquals(sourceStorageDto.getLocation(), githubUrl);
        assertEquals(sourceStorageDto.getParameters(), Collections.emptyMap());

    }

    /**
     * Check that we've expected branch when url contains a branch name
     */
    @Test
    public void shouldReturnGitHubBranchFactory() throws Exception {

        String githubUrl = "https://github.com/eclipse/che/tree/4.2.x";
        String githubCloneUrl = "https://github.com/eclipse/che";
        String githubBranch = "4.2.x";
        Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, githubUrl);
        Factory responseFactory = githubFactoryParametersResolver.createFactory(parameters);

        // check we have a project and with github URL
        WorkspaceConfigDto workspaceConfigDto = responseFactory.getWorkspace();
        assertNotNull(workspaceConfigDto);
        List<ProjectConfigDto> projects = workspaceConfigDto.getProjects();
        assertNotNull(projects);
        assertEquals(projects.size(), 1);

        ProjectConfigDto projectConfigDto = projects.get(0);
        assertNotNull(projects);
        SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
        assertNotNull(sourceStorageDto);
        assertEquals(sourceStorageDto.getType(), "git");
        assertEquals(sourceStorageDto.getLocation(), githubCloneUrl);
        Map<String, String> sourceParameters = sourceStorageDto.getParameters();
        assertEquals(sourceParameters.size(), 1);
        assertEquals(sourceParameters.get("branch"), githubBranch);

    }

    /**
     * Check that we have a sparse checkout "keepDir" if url contains branch and subtree.
     */
    @Test
    public void shouldReturnGitHubBranchAndKeepdirFactory() throws Exception {

        String githubUrl = "https://github.com/eclipse/che/tree/4.2.x/dashboard";
        String githubCloneUrl = "https://github.com/eclipse/che";
        String githubBranch = "4.2.x";
        String githubKeepdir = "dashboard";

        Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, githubUrl);
        Factory responseFactory = githubFactoryParametersResolver.createFactory(parameters);

        // check we have a project and with github URL
        WorkspaceConfigDto workspaceConfigDto = responseFactory.getWorkspace();
        assertNotNull(workspaceConfigDto);
        List<ProjectConfigDto> projects = workspaceConfigDto.getProjects();
        assertNotNull(projects);
        assertEquals(projects.size(), 1);

        ProjectConfigDto projectConfigDto = projects.get(0);
        assertNotNull(projects);
        SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
        assertNotNull(sourceStorageDto);
        assertEquals(sourceStorageDto.getType(), "git");
        assertEquals(sourceStorageDto.getLocation(), githubCloneUrl);
        Map<String, String> sourceParameters = sourceStorageDto.getParameters();
        assertEquals(sourceParameters.size(), 2);
        assertEquals(sourceParameters.get("branch"), githubBranch);
        assertEquals(sourceParameters.get("keepDir"), githubKeepdir);

    }

}
