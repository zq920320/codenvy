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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.ManagedCommand;
import org.eclipse.che.api.machine.server.command.CommandImpl;
import org.eclipse.che.api.machine.server.dao.CommandDao;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.commons.lang.Strings.isNullOrEmpty;

/**
 * Mongo based implementation of {@link CommandDao}
 * <pre>
 * Database schema:
 * {
 *     "_id" : "command123...",
 *     "name: "MAVEN_CLEAN_INSTALL",
 *     "commandLine" : "mvn clean install",
 *     "creator" : "user123...",
 *     "workspaceId" : "workspace123...",
 *     "visibility" : "public",
 *     "type" : "maven",
 *     "workingDir" : "/path"
 * }
 * </pre>
 *
 * @author Eugene Voevodin
 */
public class CommandDaoImpl implements CommandDao {

    private final DBCollection commands;

    @Inject
    public CommandDaoImpl(@Named("mongo.db.organization") DB db,
                          @Named("organization.storage.db.commands.collection") String collectionName) {
        commands = db.getCollection(collectionName);
        commands.createIndex(new BasicDBObject().append("name", 1)
                                                .append("workspaceId", 1)
                                                .append("creator", 1), new BasicDBObject("unique", true));
    }

    @Override
    public void create(ManagedCommand command) throws ConflictException, ServerException {
        requireNonNull(command, "Command required");
        try {
            commands.insert(asDBObject(command));
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Command with id '%s' or name '%s' in workspace '%s' for user '%s' already exists",
                                               command.getId(),
                                               command.getName(),
                                               command.getWorkspaceId(),
                                               command.getCreator()));
        } catch (MongoException ex) {
            throw new ServerException("Impossible to create command", ex);
        }
    }

    @Override
    public void update(ManagedCommand update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Command update required");
        final BasicDBObject dbUpdate = new BasicDBObject();
        if (!isNullOrEmpty(update.getName())) {
            dbUpdate.append("name", update.getName());
        }
        if (!isNullOrEmpty(update.getCommandLine())) {
            dbUpdate.append("commandLine", update.getCommandLine());
        }
        if (!isNullOrEmpty(update.getVisibility())) {
            dbUpdate.append("visibility", update.getVisibility());
        }
        if (update.getWorkingDir() != null) {
            dbUpdate.append("workingDir", update.getWorkingDir());
        }
        try {
            WriteResult wr = commands.update(new BasicDBObject("_id", update.getId()), new BasicDBObject("$set", dbUpdate));
            if (wr.getN() == 0) {
                throw new NotFoundException("Command with id " + update.getId() + " doesn't exist");
            }
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Command with name '%s' in workspace '%s' for user '%s' already exists",
                                               update.getName(),
                                               update.getWorkspaceId(),
                                               update.getCreator()));
        } catch (MongoException ex) {
            throw new ServerException("Impossible to update command", ex);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        try {
            commands.remove(new BasicDBObject("_id", id));
        } catch (MongoException ex) {
            throw new ServerException("Impossible to remove command", ex);
        }
    }

    @Override
    public ManagedCommand getCommand(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Command id required");
        final DBObject commandObj;
        try {
            commandObj = commands.findOne(id);
        } catch (MongoException ex) {
            throw new ServerException("Impossible to retrieve command", ex);
        }
        if (commandObj == null) {
            throw new NotFoundException("Command with id '" + id + "' was not found");
        }
        return asCommand(commandObj);
    }

    /**
     * Search query:
     * <pre>
     *     $and [
     *         { workspaceId : "workspace123" },
     *         {
     *              $or : [
     *                  { visibility : "public" },
     *                  { creator : "user123" }
     *              ]
     *         }
     *     ]
     * </pre>
     */
    @Override
    public List<ManagedCommand> getCommands(String workspaceId, String creator, int skipCount, int maxItems) throws ServerException {
        List<BasicDBObject> or = asList(new BasicDBObject("visibility", "public"), new BasicDBObject("creator", creator));
        List<BasicDBObject> and = asList(new BasicDBObject("workspaceId", workspaceId), new BasicDBObject("$or", or));
        try (DBCursor cursor = commands.find(new BasicDBObject("$and", and))
                                       .skip(skipCount)
                                       .limit(maxItems)) {
            return FluentIterable.from(cursor)
                                 .transform(new Function<DBObject, ManagedCommand>() {
                                     @Nullable
                                     @Override
                                     public ManagedCommand apply(DBObject dbObject) {
                                         return asCommand(dbObject);
                                     }
                                 })
                                 .toList();
        } catch (MongoException ex) {
            throw new ServerException("Impossible to get commands", ex);
        }
    }

    BasicDBObject asDBObject(ManagedCommand command) {
        return new BasicDBObject().append("_id", command.getId())
                                  .append("name", command.getName())
                                  .append("commandLine", command.getCommandLine())
                                  .append("creator", command.getCreator())
                                  .append("workspaceId", command.getWorkspaceId())
                                  .append("visibility", command.getVisibility())
                                  .append("type", command.getType())
                                  .append("workingDir", command.getWorkingDir());
    }

    ManagedCommand asCommand(DBObject dbObj) {
        final BasicDBObject basicObj = (BasicDBObject)dbObj;
        return new CommandImpl().withId(basicObj.getString("_id"))
                                .withName(basicObj.getString("name"))
                                .withCommandLine(basicObj.getString("commandLine"))
                                .withCreator(basicObj.getString("creator"))
                                .withWorkspaceId(basicObj.getString("workspaceId"))
                                .withVisibility(basicObj.getString("visibility"))
                                .withType(basicObj.getString("type"))
                                .withWorkingDir(basicObj.getString("workingDir"));
    }
}
