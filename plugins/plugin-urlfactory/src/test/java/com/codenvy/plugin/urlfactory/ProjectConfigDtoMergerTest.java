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

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.assertEquals;

/**
 * Testing {@link ProjectConfigDtoMerger}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class ProjectConfigDtoMergerTest {

    /**
     * Location
     */
    private static final String DUMMY_LOCATION = "dummy-location";

    @InjectMocks
    private ProjectConfigDtoMerger projectConfigDtoMerger;


    private ProjectConfigDto computedProjectConfig;


    private Factory factory;


    @BeforeClass
    public void setup() {
        WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
        this.factory = newDto(Factory.class).withWorkspace(workspaceConfigDto);

        SourceStorageDto sourceStorageDto = newDto(SourceStorageDto.class).withLocation(DUMMY_LOCATION);
        computedProjectConfig = newDto(ProjectConfigDto.class).withSource(sourceStorageDto);
    }


    /**
     * Check project is added when we have no project
     */
    @Test
    public void mergeWithoutAnyProject() {

        // no project
        Assert.assertTrue(factory.getWorkspace().getProjects().isEmpty());

        // merge
        projectConfigDtoMerger.merge(factory, computedProjectConfig);

        // project
        assertEquals(factory.getWorkspace().getProjects().size(), 1);

        assertEquals(factory.getWorkspace().getProjects().get(0), computedProjectConfig);

    }


    /**
     * Check source are added if there is only one project without source
     */
    @Test
    public void mergeWithoutOneProjectWithoutSource() {

        // add existing project
        ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class);
        factory.getWorkspace().setProjects(Collections.singletonList(projectConfigDto));
        // no source storage
        Assert.assertNull(projectConfigDto.getSource());


        // merge
        projectConfigDtoMerger.merge(factory, computedProjectConfig);

        // project still 1
        assertEquals(factory.getWorkspace().getProjects().size(), 1);

        SourceStorageDto sourceStorageDto = factory.getWorkspace().getProjects().get(0).getSource();

        assertEquals(sourceStorageDto.getLocation(), DUMMY_LOCATION);


    }
}
