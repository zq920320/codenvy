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
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.impl.ProjectBindingImpl;
import org.eclipse.che.api.machine.server.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.shared.ProjectBinding;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Alexander Garagatyi
 */
public class MongoSnapshotDaoImpl implements SnapshotDao {
    private static final Logger LOG                = LoggerFactory.getLogger(MongoSnapshotDaoImpl.class);
    private static final String MACHINE_COLLECTION = "storage.machine.db.collection";

    private final DBCollection machineCollection;

    @Inject
    public MongoSnapshotDaoImpl(@Named("mongo.db.machine") DB db, @Named(MACHINE_COLLECTION) String machineCollectionName) {
        machineCollection = db.getCollection(machineCollectionName);
        machineCollection.createIndex(new BasicDBObject("workspaceId", 1));
        machineCollection.createIndex(new BasicDBObject("owner", 1));
        // TODO add index for path
    }

    @Override
    public SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        final DBObject snapshotDocument;
        try {
            snapshotDocument = machineCollection.findOne(new BasicDBObject("_id", snapshotId));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new SnapshotException("It is not possible to retrieve snapshot");
        }
        if (snapshotDocument == null) {
            throw new NotFoundException(format("Snapshot with id %s not found", snapshotId));
        }
        return toSnapshot(snapshotDocument);
    }

    @Override
    public void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException {
        try {
            machineCollection.save(toDBObject(snapshot));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new SnapshotException("It is not possible to save snapshot");
        }
    }

    @Override
    public List<SnapshotImpl> findSnapshots(String owner, String workspaceId, ProjectBinding project) throws SnapshotException {
        BasicDBObject query = new BasicDBObject("owner", owner);
        query.append("workspaceId", workspaceId);
        if (project != null) {
            query.append("projectBindings.path", project.getPath());
        }

        try (DBCursor snapshots = machineCollection.find(query)) {
            final ArrayList<SnapshotImpl> result = new ArrayList<>();
            for (DBObject snapshotObj : snapshots) {
                result.add(toSnapshot(snapshotObj));
            }
            return result;
        } catch (MongoException e) {
            LOG.error(e.getMessage(), e);
            throw new SnapshotException("It is not possible to retrieve snapshots");
        }
    }

    @Override
    public void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        try {
            final WriteResult writeResult = machineCollection.remove(new BasicDBObject("_id", snapshotId));

            if (writeResult.getN() == 0) {
                throw new NotFoundException(format("Snapshot with id %s not found", snapshotId));
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new SnapshotException ("It is not possible to remove snapshot");
        }
    }

    private SnapshotImpl toSnapshot(DBObject object) {
        final BasicDBObject snapshotObject = (BasicDBObject)object;
        final BasicDBList projectBindingsObject = (BasicDBList)snapshotObject.get("projectBindings");
        final List<ProjectBinding> projectBindings = new ArrayList<>(projectBindingsObject.size());
        for (Object projectBinding : projectBindingsObject) {
            projectBindings.add(new ProjectBindingImpl().withPath(((BasicDBObject)projectBinding).getString("path")));
        }

        return new SnapshotImpl(snapshotObject.getString("_id"),
                            snapshotObject.getString("instanceType"),
                            new InstanceKeyImpl(MongoUtil.asMap(snapshotObject.get("instanceKey"))),
                            snapshotObject.getString("owner"),
                            snapshotObject.getLong("creationDate"),
                            snapshotObject.getString("workspaceId"),
                            projectBindings,
                            snapshotObject.getString("description"),
                            snapshotObject.getBoolean("isWorkspaceBound"));
    }

    private DBObject toDBObject(SnapshotImpl snapshot) {
        final BasicDBList projectBindings = new BasicDBList();
        for (ProjectBinding projectBinding : snapshot.getProjects()) {
            projectBindings.add(new BasicDBObject("path", projectBinding.getPath()));
        }

        return new BasicDBObject().append("_id", snapshot.getId())
                                  .append("instanceType", snapshot.getType())
                                  .append("instanceKey", MongoUtil.asDBList(snapshot.getInstanceKey().getFields()))
                                  .append("owner", snapshot.getOwner())
                                  .append("workspaceId", snapshot.getWorkspaceId())
                                  .append("projectBindings", projectBindings)
                                  .append("creationDate", snapshot.getCreationDate())
                                  .append("description", snapshot.getDescription())
                                  .append("isWorkspaceBound", snapshot.isWorkspaceBound());
    }

    private static class InstanceKeyImpl implements InstanceKey {
        private final Map<String, String> fields;

        public InstanceKeyImpl(Map<String, String> fields) {
            this.fields = new LinkedHashMap<>(fields);
        }

        @Override
        public Map<String, String> getFields() {
            return Collections.unmodifiableMap(fields);
        }

        @Override
        public String toJson() {
            throw new UnsupportedOperationException();
        }
    }
}
