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
package com.codenvy.machine.backup;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.ChannelsImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineMetadataImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineStateImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceFsBackupSchedulerTest {
    @Mock
    private MachineManager machineManager;
    @Mock
    private Recipe         recipe;

    @Mock
    private MachineBackupManager backupManager;

    private WorkspaceFsBackupScheduler scheduler;

    final List<MachineStateImpl> machines = new ArrayList<>();

    @Mock
    private Instance machineInstance;

    @Mock
    private InstanceNode node;

    @BeforeMethod
    public void setUp() throws Exception {
        scheduler = new WorkspaceFsBackupScheduler(machineManager, backupManager, 5 * 60);

        when(machineManager.getMachinesStates()).thenReturn(machines);

        machines.add(new MachineImpl(true,
                                     "displayName1",
                                     "type1",
                                     new MachineSourceImpl("sourcetype1", "location1"),
                                     new LimitsImpl(1024),
                                     "id1",
                                     new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                             singletonMap("prop", "pvalue1"),
                                                             singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel1", "statuschannel1"),
                                     "workspaceId1",
                                     "owner1",
                                     "envName",
                                     MachineStatus.RUNNING));

        when(machineManager.getMachine("id1")).thenReturn(machineInstance);

        when(machineInstance.getNode()).thenReturn(node);

        when(node.getHost()).thenReturn("192.168.0.1");
        when(node.getProjectsFolder()).thenReturn("/workspace1");
    }

    private void verifyMachine1Backup() throws Exception {
        verify(machineManager).getMachinesStates();
        verify(machineManager).getMachine("id1");
        verify(backupManager).backupWorkspace("workspaceId1", "/workspace1", "192.168.0.1");
    }

    @AfterMethod
    public void tearDown() throws Exception {
        machines.clear();
    }

    @Test
    public void shouldBackupWorkspaceFsOfMachines() throws Exception {
        machines.add(new MachineImpl(true,
                                     "displayName2",
                                     "type2",
                                     new MachineSourceImpl("sourcetype2", "location2"),
                                     new LimitsImpl(1024),
                                     "id2",
                                     new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                             singletonMap("prop", "pvalue1"),
                                                             singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel2", "statuschannel2"),
                                     "workspaceId2",
                                     "owner2",
                                     "envName",
                                     MachineStatus.RUNNING));
        when(machineManager.getMachine("id2")).thenReturn(machineInstance);
        when(node.getHost()).thenReturn("192.168.0.1").thenReturn("192.168.0.2");
        when(node.getProjectsFolder()).thenReturn("/workspace1").thenReturn("/workspace2");

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verifyMachine1Backup();
        verify(machineManager).getMachine("id2");
        verify(backupManager).backupWorkspace("workspaceId2", "/workspace2", "192.168.0.2");
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test(enabled = false) //TODO: fixme
    public void shouldNotBackupWorkspaceOfNonDevMachines() throws Exception {
        machines.add(new MachineImpl(true,
                                     "displayName2",
                                     "type2",
                                     new MachineSourceImpl("sourcetype2", "location2"),
                                     new LimitsImpl(1024),
                                     "id2",
                                     new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                             singletonMap("prop", "pvalue1"),
                                                             singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel2", "statuschannel2"),
                                     "workspaceId2",
                                     "owner2",
                                     "envName",
                                     MachineStatus.RUNNING));

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verifyMachine1Backup();
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test
    public void shouldNotBackupMachinesInNonRunningStatus() throws Exception {
        machines.add(new MachineImpl(true,
                                     "displayName2",
                                     "type2",
                                     new MachineSourceImpl("sourcetype2", "location2"),
                                     new LimitsImpl(1024),
                                     "id2",
                                     new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                             singletonMap("prop", "pvalue1"),
                                                             singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel2", "statuschannel2"),
                                     "workspaceId2",
                                     "owner2",
                                     "envName",
                                     MachineStatus.CREATING));
        machines.add(new MachineImpl(true,
                                     "displayName3",
                                     "type3",
                                     new MachineSourceImpl("sourcetype2", "location2"),
                                     new LimitsImpl(1024),
                                     "id3",
                                     new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                             singletonMap("prop", "pvalue1"),
                                                             singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel3", "statuschannel3"),
                                     "workspaceId3",
                                     "owner3",
                                     "envName",
                                     MachineStatus.DESTROYING));

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verifyMachine1Backup();
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test
    public void shouldBeAbleToBackupWorkspacesOfOtherMachinesIfMachineRetrievalFails() throws Exception {
        machines.add(new MachineImpl(true,
                                     "displayName2",
                                     "type2",
                                     new MachineSourceImpl("sourcetype2", "location2"),
                                     new LimitsImpl(1024),
                                     "id2", new MachineMetadataImpl(singletonMap("var1", "value1"),
                                                                    singletonMap("prop", "pvalue1"),
                                                                    singletonMap("8080", new ServerImpl("ref1", "address1", "url1"))),
                                     new ChannelsImpl("outputchannel12", "statuschannel2"),
                                     "workspaceId2",
                                     "owner2",
                                     "envName",
                                     MachineStatus.RUNNING));
        when(machineManager.getMachine("id2")).thenThrow(new MachineException(""));

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verifyMachine1Backup();
        verify(machineManager).getMachine("id2");
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test
    public void shouldNotBackupAnythingIfMachinesListRetrievalFails() throws Exception {
        when(machineManager.getMachinesStates()).thenThrow(new MachineException(""));

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verify(machineManager).getMachinesStates();
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test
    public void shouldNotBackupMachineIfLastSyncTimeoutTooLow() throws Exception {
        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        // second synchronization
        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verify(machineManager, times(2)).getMachinesStates();
        verify(machineManager).getMachine("id1");
        verify(backupManager).backupWorkspace("workspaceId1", "/workspace1", "192.168.0.1");
        verifyNoMoreInteractions(machineManager, backupManager);
    }

    @Test
    public void shouldBackupMachineFsIfLastSyncTimeoutIsExpired() throws Exception {
        scheduler = new WorkspaceFsBackupScheduler(machineManager, backupManager, 0);

        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        // second synchronization
        scheduler.scheduleBackup();

        // make sure that executor started threads
        Thread.sleep(500);

        verify(machineManager, times(2)).getMachinesStates();
        verify(machineManager, times(2)).getMachine("id1");
        verify(backupManager, times(2)).backupWorkspace("workspaceId1", "/workspace1", "192.168.0.1");
        verifyNoMoreInteractions(machineManager, backupManager);
    }

}