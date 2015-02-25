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

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.machine.server.ProjectBindingImpl;
import com.codenvy.api.machine.server.Snapshot;
import com.codenvy.api.machine.server.SnapshotStorage;
import com.codenvy.api.machine.server.spi.ImageKey;
import com.codenvy.api.machine.shared.ProjectBinding;
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
public class MongoSnapshotStorageImpl implements SnapshotStorage {
    private static final Logger LOG                = LoggerFactory.getLogger(MongoSnapshotStorageImpl.class);
    private static final String MACHINE_COLLECTION = "storage.machine.collection";

    private final DBCollection machineCollection;

    @Inject
    public MongoSnapshotStorageImpl(@Named("mongo.db.machine") DB db, @Named(MACHINE_COLLECTION) String machineCollectionName) {
        machineCollection = db.getCollection(machineCollectionName);
        machineCollection.ensureIndex(new BasicDBObject("workspaceId", 1));
        machineCollection.ensureIndex(new BasicDBObject("owner", 1));
        // TODO add index for path
    }

    @Override
    public Snapshot getSnapshot(String snapshotId) throws NotFoundException, ServerException {
        final DBObject snapshotDocument;
        try {
            snapshotDocument = machineCollection.findOne(new BasicDBObject("_id", snapshotId));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve snapshot");
        }
        if (snapshotDocument == null) {
            throw new NotFoundException(format("Snapshot with id %s not found", snapshotId));
        }
        return toSnapshot(snapshotDocument);
    }

    @Override
    public void saveSnapshot(Snapshot snapshot) throws ServerException, ForbiddenException {
        requiredNotNull(snapshot.getOwner(), "Owner");
        requiredNotNull(snapshot.getId(), "Id");
        requiredNotNull(snapshot.getImageType(), "Image type");
        requiredNotNull(snapshot.getWorkspaceId(), "Workspace id");
        requiredNotNull(snapshot.getImageKey(), "Image key");
        try {
            machineCollection.save(toDBObject(snapshot));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to save snapshot");
        }
    }

    @Override
    public List<Snapshot> findSnapshots(String owner, String workspaceId, ProjectBinding project) throws ServerException {
        BasicDBObject query = new BasicDBObject("owner", owner);
        query.append("workspaceId", workspaceId);
        query.append("projectBindings.path", project.getPath());

        try (DBCursor snapshots = machineCollection.find(query)) {
            final ArrayList<Snapshot> result = new ArrayList<>();
            for (DBObject snapshotObj : snapshots) {
                result.add(toSnapshot(snapshotObj));
            }
            return result;
        } catch (MongoException e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("It is not possible to retrieve snapshots");
        }
    }

    @Override
    public void removeSnapshot(String snapshotId) throws NotFoundException, ServerException {
        try {
            final WriteResult writeResult = machineCollection.remove(new BasicDBObject("_id", snapshotId));

            if (writeResult.getN() == 0) {
                throw new NotFoundException(format("Snapshot with id %s not found", snapshotId));
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove snapshot");
        }
    }

    private Snapshot toSnapshot(DBObject object) {
        final BasicDBObject snapshotObject = (BasicDBObject)object;
        final BasicDBList projectBindingsObject = (BasicDBList)snapshotObject.get("projectBindings");
        final List<ProjectBinding> projectBindings = new ArrayList<>(projectBindingsObject.size());
        for (Object projectBinding : projectBindingsObject) {
            projectBindings.add(new ProjectBindingImpl().withPath(((BasicDBObject)projectBinding).getString("path")));
        }

        return new Snapshot(snapshotObject.getString("_id"),
                            snapshotObject.getString("imageType"),
                            new ImageKeyImpl(MongoUtil.asMap(snapshotObject.get("imageKey"))),
                            snapshotObject.getString("owner"),
                            snapshotObject.getLong("creationDate"),
                            snapshotObject.getString("workspaceId"),
                            projectBindings,
                            snapshotObject.getString("description"));
    }

    private DBObject toDBObject(Snapshot snapshot) {
        final BasicDBList projectBindings = new BasicDBList();
        for (ProjectBinding projectBinding : snapshot.getProjects()) {
            projectBindings.add(new BasicDBObject("path", projectBinding.getPath()));
        }

        return new BasicDBObject().append("_id", snapshot.getId())
                                  .append("imageType", snapshot.getImageType())
                                  .append("imageKey", MongoUtil.asDBList(snapshot.getImageKey().getFields()))
                                  .append("owner", snapshot.getOwner())
                                  .append("workspaceId", snapshot.getWorkspaceId())
                                  .append("projectBindings", projectBindings)
                                  .append("creationDate", snapshot.getCreationDate())
                                  .append("description", snapshot.getDescription());
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws com.codenvy.api.core.ForbiddenException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws ForbiddenException {
        if (object == null) {
            throw new ForbiddenException(subject + " required");
        }
    }

    private static class ImageKeyImpl implements ImageKey {
        private final Map<String, String> fields;

        public ImageKeyImpl(Map<String, String> fields) {
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
