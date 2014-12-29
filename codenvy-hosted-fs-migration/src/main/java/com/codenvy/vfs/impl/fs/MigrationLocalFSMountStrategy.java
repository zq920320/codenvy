/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.vfs.impl.fs;

import com.codenvy.api.core.ServerException;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.IoUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;

/**
 * Performs copy FS of given workspace and return new mount path.
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 12/26/14.
 */
public class MigrationLocalFSMountStrategy extends WorkspaceHashLocalFSMountStrategy  {

    private final java.io.File fsMountRoot;
    private final java.io.File oldFsMountRoot;
    private final java.io.File fsMountTempRoot;



    @Inject
    public MigrationLocalFSMountStrategy(@Nullable @Named("vfs.old.fs_root_dir") String glusterMountRoot,
                                         @Named("vfs.local.tmp_workspace_fs_root_dir") java.io.File fsTempRoot,
                                         @Named("vfs.local.fs_root_dir") java.io.File fsMountRoot) {

        super(fsMountRoot, fsTempRoot);
        this.fsMountRoot = fsMountRoot;
        this.fsMountTempRoot = fsTempRoot;
        this.oldFsMountRoot = (glusterMountRoot != null ? new File(glusterMountRoot) : null);
    }

    @Override
    public java.io.File getMountPath(String workspaceId) throws ServerException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ServerException("Unable get mount path for virtual file system. Workspace id is not set.");
        }
        final boolean isTmpWs = EnvironmentContext.getCurrent().isWorkspaceTemporary();

        File newWSPath =
                calculateDirPath(isTmpWs ? fsMountTempRoot : fsMountRoot, workspaceId);
        if (oldFsMountRoot != null && !isTmpWs) {
          if (!newWSPath.exists()) {
              File oldWSPath = calculateDirPath(oldFsMountRoot, workspaceId);
              if (oldWSPath.isDirectory() && oldWSPath.exists()) {
                  try {
                      IoUtil.copy(oldWSPath, newWSPath, null);
                  } catch (IOException e) {
                      throw new ServerException(String.format("Cannot copy file system %s", workspaceId), e);
                  }
              }
          }
        }
        return  newWSPath;
    }

}
