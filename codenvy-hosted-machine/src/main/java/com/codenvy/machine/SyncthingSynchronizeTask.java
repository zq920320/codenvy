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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.Cancellable;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.FileLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Alexander Garagatyi
 */
public class SyncthingSynchronizeTask implements Runnable, Cancellable {
    private static final Logger LOG = LoggerFactory.getLogger(SyncthingSynchronizeTask.class);

    private final String syncTaskExecutable;
    private final String syncTaskConfTemplate;
    private final String workingDir;
    private final String syncPath;
    private final int    listenPort;
    private final int    apiPort;
    private final String remoteClientAddress;
    private final String guiApiToken;

    private Process process;

    @Inject
    public SyncthingSynchronizeTask(String syncTaskExecutable,
                                    String syncTaskConfTemplate,
                                    String workingDir,
                                    String syncPath,
                                    int listenPort,
                                    int apiPort,
                                    String remoteClientAddress,
                                    String guiApiToken) throws ServerException {

        this.syncTaskExecutable = syncTaskExecutable;
        this.syncTaskConfTemplate = syncTaskConfTemplate;
        this.workingDir = workingDir;
        this.syncPath = syncPath;
        this.listenPort = listenPort;
        this.apiPort = apiPort;
        this.remoteClientAddress = remoteClientAddress;
        this.guiApiToken = guiApiToken;
    }

    @Override
    public void run() {
        LOG.info("Server sync task is starting");
        Path syncWorkingDir = null;
        try {
            syncWorkingDir = Files.createDirectories(Paths.get(workingDir, NameGenerator.generate("machine-sync-", 16)));
            CommandLine cl = new CommandLine(syncTaskExecutable)
                    .add(syncTaskConfTemplate)
                    .add(syncWorkingDir.toString())
                    .add(syncPath)
                    .add(String.valueOf(listenPort))
                    .add(String.valueOf(apiPort))
                    .add(remoteClientAddress)
                    .add(guiApiToken);

            ProcessBuilder processBuilder = new ProcessBuilder().redirectErrorStream(true).command(cl.toShellCommand());
            process = processBuilder.start();

            LineConsumer logConsumer = new FileLineConsumer(new File(syncWorkingDir.toFile(), "logs.txt"));
            try (final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = inputReader.readLine()) != null) {
                    logConsumer.writeLine(line);
                }
            } finally {
                logConsumer.close();
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (syncWorkingDir != null) {
                FileCleaner.addFile(syncWorkingDir.toFile());
            }
        }
        LOG.info("Sync task is finishing");
    }

    @Override
    public void cancel() throws Exception {
        ProcessUtil.kill(process);
    }

    public int[] getPorts() {
        return new int[] {listenPort, apiPort};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncthingSynchronizeTask that = (SyncthingSynchronizeTask)o;

        if (apiPort != that.apiPort) return false;
        if (listenPort != that.listenPort) return false;
        if (guiApiToken != null ? !guiApiToken.equals(that.guiApiToken) : that.guiApiToken != null) return false;
        if (process != null ? !process.equals(that.process) : that.process != null) return false;
        if (remoteClientAddress != null ? !remoteClientAddress.equals(that.remoteClientAddress) : that.remoteClientAddress != null)
            return false;
        if (syncPath != null ? !syncPath.equals(that.syncPath) : that.syncPath != null) return false;
        if (syncTaskConfTemplate != null ? !syncTaskConfTemplate.equals(that.syncTaskConfTemplate) : that.syncTaskConfTemplate != null)
            return false;
        if (syncTaskExecutable != null ? !syncTaskExecutable.equals(that.syncTaskExecutable) : that.syncTaskExecutable != null)
            return false;
        if (workingDir != null ? !workingDir.equals(that.workingDir) : that.workingDir != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = syncTaskExecutable != null ? syncTaskExecutable.hashCode() : 0;
        result = 31 * result + (syncTaskConfTemplate != null ? syncTaskConfTemplate.hashCode() : 0);
        result = 31 * result + (workingDir != null ? workingDir.hashCode() : 0);
        result = 31 * result + (syncPath != null ? syncPath.hashCode() : 0);
        result = 31 * result + listenPort;
        result = 31 * result + apiPort;
        result = 31 * result + (remoteClientAddress != null ? remoteClientAddress.hashCode() : 0);
        result = 31 * result + (guiApiToken != null ? guiApiToken.hashCode() : 0);
        result = 31 * result + (process != null ? process.hashCode() : 0);
        return result;
    }
}
