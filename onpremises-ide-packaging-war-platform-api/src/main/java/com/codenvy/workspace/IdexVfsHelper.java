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
package com.codenvy.workspace;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.vfs.impl.fs.WorkspaceHashLocalFSMountStrategy;
import com.codenvy.workspace.listener.VfsCleanupPerformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;

import static com.codenvy.commons.lang.IoUtil.removeDirectory;

/**
 * @author Alexander Garagatyi
 */
public class IdexVfsHelper implements VfsCleanupPerformer {
    private static final Logger LOG = LoggerFactory.getLogger(IdexVfsHelper.class);

    private final VirtualFileSystemRegistry vfsRegistry;

    private final File tempVfsRootDir;
    private       File persistentVfsRootDir;

    @Inject
    public IdexVfsHelper(VirtualFileSystemRegistry vfsRegistry, @Named("vfs.local.tmp_workspace_fs_root_dir") File tempFs,
                         @Named("vfs.local.fs_root_dir") File persistentFs) {
        this.vfsRegistry = vfsRegistry;
        this.tempVfsRootDir = tempFs;
        this.persistentVfsRootDir = persistentFs;
    }

    @Override
    public void unregisterProvider(String wsId) throws IOException {
        try {
            vfsRegistry.unregisterProvider(wsId);
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void removeFS(String wsId, boolean isTemporary) throws IOException {
        File fsFolder;
        if (isTemporary) {
            fsFolder = WorkspaceHashLocalFSMountStrategy.calculateDirPath(tempVfsRootDir, wsId);
        } else {
            fsFolder = WorkspaceHashLocalFSMountStrategy.calculateDirPath(persistentVfsRootDir, wsId);
        }
        if (!removeDirectory(fsFolder.getAbsolutePath())) {
            LOG.warn("Can't remove virtual filesystem with path {}", fsFolder.getAbsolutePath());
        }
    }
}
