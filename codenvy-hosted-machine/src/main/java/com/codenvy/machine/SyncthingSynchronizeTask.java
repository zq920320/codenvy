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
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.FileLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.machine.server.SynchronizeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Alexander Garagatyi
 */
public class SyncthingSynchronizeTask implements SynchronizeTask {
    private static final Logger LOG = LoggerFactory.getLogger(SyncthingSynchronizeTask.class);

    private final String            syncTaskExecutable;
    private final String            syncTaskConfTemplate;
    private final String            syncPath;
    private final int               listenPort;
    private final int               apiPort;
    private final String            remoteClientAddress;

    private Process process;

    @Inject
    public SyncthingSynchronizeTask(String syncTaskExecutable,
                                    String syncTaskConfTemplate,
                                    String syncPath,
                                    int listenPort,
                                    int apiPort,
                                    String remoteClientAddress) throws ServerException {

        this.syncTaskExecutable = syncTaskExecutable;
        this.syncTaskConfTemplate = syncTaskConfTemplate;
        this.syncPath = syncPath;
        this.listenPort = listenPort;
        this.apiPort = apiPort;
        this.remoteClientAddress = remoteClientAddress;
    }

    @Override
    public void run() {
        LOG.info("Server sync task is starting");
        try {
            final Path tempDirectory = Files.createTempDirectory("machine-sync-");
            CommandLine cl = new CommandLine(syncTaskExecutable)
                    .add(syncTaskConfTemplate + "/*")
                    .add(tempDirectory.toString())
                    .add(syncPath)
                    .add(String.valueOf(listenPort))
                    .add(String.valueOf(apiPort))
                    .add(remoteClientAddress);

            ProcessBuilder processBuilder = new ProcessBuilder().redirectErrorStream(true).command(cl.toShellCommand());
            process = processBuilder.start();

            LineConsumer logConsumer = new FileLineConsumer(new File(tempDirectory.toString(), "logs.txt"));
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
        }
        LOG.info("Sync task is finishing");
    }

    @Override
    public void cancel() throws Exception {
        ProcessUtil.kill(process);
    }
}
