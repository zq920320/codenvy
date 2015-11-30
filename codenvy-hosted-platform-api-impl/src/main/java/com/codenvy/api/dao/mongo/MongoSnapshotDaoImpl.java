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
package com.codenvy.api.dao.mongo;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.impl.SnapshotImpl;

import java.util.Collections;
import java.util.List;


/**
 * @author Alexander Garagatyi
 */
public class MongoSnapshotDaoImpl implements SnapshotDao {

    @Override
    public SnapshotImpl getSnapshot(String workspaceId, String envName, String machineName) throws NotFoundException, SnapshotException {
        throw new SnapshotException("Not implemented");
    }

    @Override
    public SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        throw new SnapshotException("Not implemented");
    }

    @Override
    public void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException {
        throw new SnapshotException("Not implemented");
    }

    @Override
    public List<SnapshotImpl> findSnapshots(String owner, String workspaceId) throws SnapshotException {
        return Collections.emptyList();
    }

    @Override
    public void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        throw new SnapshotException("Not implemented");
    }
}
