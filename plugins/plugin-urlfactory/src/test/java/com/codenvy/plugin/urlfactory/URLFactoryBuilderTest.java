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
package com.codenvy.plugin.urlfactory;

import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.compose.BuildContextDto;
import org.eclipse.che.api.workspace.shared.dto.compose.ComposeEnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.compose.ComposeServiceDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URI;

import static com.codenvy.plugin.urlfactory.URLFactoryBuilder.DEFAULT_DOCKER_IMAGE;
import static com.codenvy.plugin.urlfactory.URLFactoryBuilder.MACHINE_NAME;
import static com.codenvy.plugin.urlfactory.URLFactoryBuilder.MEMORY_LIMIT_BYTES;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Testing {@link URLFactoryBuilder}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class URLFactoryBuilderTest {

    /**
     * Check if URL is existing or not
     */
    @Mock
    private URLChecker URLChecker;

    /**
     * Grab content of URLs
     */
    @Mock
    private URLFetcher URLFetcher;

    // spy is needed for injection
    @Spy
    private ComposeFileParser composeFileParser = new ComposeFileParser(URI.create("localhost:8080"));

    /**
     * Tested instance.
     */
    @InjectMocks
    private URLFactoryBuilder urlFactoryBuilder;

    /**
     * Check if not specifying a custom docker file we have the default value
     */
    @Test
    public void checkDefaultDockerfile() throws Exception {

        ComposeServiceDto composeService = newDto(ComposeServiceDto.class).withMemLimit(MEMORY_LIMIT_BYTES);
        composeService.setImage(DEFAULT_DOCKER_IMAGE);
        ComposeEnvironmentDto composeEnv =
                newDto(ComposeEnvironmentDto.class).withServices(singletonMap(MACHINE_NAME, composeService));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class)
                .withRecipe(newDto(EnvironmentRecipeDto.class).withContent(composeFileParser.toYaml(composeEnv))
                                                              .withContentType("application/x-yaml")
                                                              .withType("compose"))
                .withMachines(singletonMap(MACHINE_NAME,
                                           newDto(ExtendedMachineDto.class).withAgents(singletonList("ws-agent"))));
        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", null);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }


    /**
     * Check that by specifying a location of custom dockerfile it's stored in the machine source if URL is accessible
     */
    @Test
    public void checkWithCustomDockerfile() throws Exception {

        String myLocation = "http://foo-location";
        ComposeServiceDto composeService = newDto(ComposeServiceDto.class).withMemLimit(MEMORY_LIMIT_BYTES);
        composeService.setBuild(newDto(BuildContextDto.class).withContext(myLocation));
        ComposeEnvironmentDto composeEnv =
                newDto(ComposeEnvironmentDto.class).withServices(singletonMap(MACHINE_NAME, composeService));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class)
                .withRecipe(newDto(EnvironmentRecipeDto.class).withContent(composeFileParser.toYaml(composeEnv))
                                                              .withContentType("application/x-yaml")
                                                              .withType("compose"))
                .withMachines(singletonMap(MACHINE_NAME,
                                           newDto(ExtendedMachineDto.class).withAgents(singletonList("ws-agent"))));
        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        when(URLChecker.exists(myLocation)).thenReturn(true);

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }

    /**
     * Check that by specifying a location of custom dockerfile it's stored in the machine source if URL is accessible
     */
    @Test
    public void checkWithNonAccessibleCustomDockerfile() throws Exception {

        String myLocation = "http://foo-location";
        ComposeServiceDto composeService = newDto(ComposeServiceDto.class).withMemLimit(MEMORY_LIMIT_BYTES);
        composeService.setImage(DEFAULT_DOCKER_IMAGE);
        ComposeEnvironmentDto composeEnv =
                newDto(ComposeEnvironmentDto.class).withServices(singletonMap(MACHINE_NAME, composeService));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class)
                .withRecipe(newDto(EnvironmentRecipeDto.class).withContent(composeFileParser.toYaml(composeEnv))
                                                              .withContentType("application/x-yaml")
                                                              .withType("compose"))
                .withMachines(singletonMap(MACHINE_NAME,
                                           newDto(ExtendedMachineDto.class).withAgents(singletonList("ws-agent"))));
        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        when(URLChecker.exists(myLocation)).thenReturn(false);

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }

    /**
     * Check that with a custom factory.json we've this factory being built
     */
    @Test
    public void checkWithCustomFactoryJsonFile() throws Exception {

        WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
        Factory templateFactory = newDto(Factory.class).withV("4.0").withName("florent").withWorkspace(workspaceConfigDto);
        String jsonFactory = DtoFactory.getInstance().toJson(templateFactory);


        String myLocation = "http://foo-location";
        when(URLChecker.exists(myLocation)).thenReturn(FALSE);
        when(URLFetcher.fetch(myLocation)).thenReturn(jsonFactory);

        Factory factory = urlFactoryBuilder.createFactory(CreateFactoryParams.create().codenvyJsonFileLocation(myLocation));

        assertEquals(templateFactory, factory);

    }


    /**
     * Check that without specifying a custom factory.json we've default factory
     */
    @Test
    public void checkWithDefaultFactoryJsonFile() throws Exception {

        Factory factory = urlFactoryBuilder.createFactory(null);

        assertNull(factory.getWorkspace());
        assertEquals(factory.getV(), "4.0");

    }
}
