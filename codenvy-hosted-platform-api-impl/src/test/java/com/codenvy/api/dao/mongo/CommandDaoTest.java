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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.ManagedCommand;
import org.eclipse.che.api.machine.server.command.CommandImpl;
import org.eclipse.che.api.machine.server.dao.CommandDao;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link CommandDao}
 *
 * @author Eugene Voevodin
 */
public class CommandDaoTest extends BaseDaoTest {

    private CommandDaoImpl commandDao;

    @BeforeMethod
    public void setUp() {
        setUp("commands");
        commandDao = new CommandDaoImpl(db, "commands");
    }

    @Test
    public void shouldBeAbleToGetCommand() throws Exception {
        final ManagedCommand example = new CommandImpl().withId("command123")
                                                 .withName("MAVEN_CLEAN_INSTALL")
                                                 .withCommandLine("mvn clean install")
                                                 .withCreator("user123")
                                                 .withWorkspaceId("workspace123")
                                                 .withVisibility("public")
                                                 .withType("maven")
                                                 .withWorkingDir("/project");
        collection.save(commandDao.asDBObject(example));

        final ManagedCommand command = commandDao.getCommand(example.getId());

        assertEquals(command, example);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Command with id 'fake' was not found")
    public void shouldThrowNotFoundExceptionWhenRecipeWithGivenIdWasNotFound() throws Exception {
        commandDao.getCommand("fake");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRethrowMongoExceptionAsServerExceptionWhenGettingCommand() throws Exception {
        collection = mock(DBCollection.class);
        when(collection.findOne("id")).thenThrow(new MongoException("Mongo exception"));

        db = mock(DB.class);
        when(db.getCollection("commands")).thenReturn(collection);

        commandDao = new CommandDaoImpl(db, "commands");

        commandDao.getCommand("id");
    }

    @Test
    public void shouldBeAbleToCreateNewCommand() throws Exception {
        final ManagedCommand example = new CommandImpl().withId("command123")
                                                 .withName("MAVEN_CLEAN_INSTALL")
                                                 .withCommandLine("mvn clean install")
                                                 .withCreator("user123")
                                                 .withWorkspaceId("workspace123")
                                                 .withVisibility("public")
                                                 .withType("maven")
                                                 .withWorkingDir("/project");

        commandDao.create(example);

        final DBObject result = collection.findOne(example.getId());
        assertEquals(commandDao.asCommand(result), example);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingCommandWithIdWhichIsReserved() throws Exception {
        final CommandImpl example = new CommandImpl().withId("command123")
                                                     .withName("MAVEN_CLEAN_INSTALL")
                                                     .withCommandLine("mvn clean install")
                                                     .withCreator("user123")
                                                     .withWorkspaceId("workspace123")
                                                     .withVisibility("public")
                                                     .withType("maven")
                                                     .withWorkingDir("/project");

        commandDao.create(example);
        commandDao.create(example.withName("new name"));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingCommandWithReservedCombinationOfNameCreatorAndWorkspaceId() throws Exception {
        final CommandImpl example = new CommandImpl().withId("command123")
                                                     .withName("MAVEN_CLEAN_INSTALL")
                                                     .withCommandLine("mvn clean install")
                                                     .withCreator("user123")
                                                     .withWorkspaceId("workspace123")
                                                     .withVisibility("public")
                                                     .withType("maven")
                                                     .withWorkingDir("/project");

        commandDao.create(example);
        commandDao.create(example.withId("command234"));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRethrowMongoExceptionAsServerExceptionWhenCreatingCommand() throws Exception {
        collection = mock(DBCollection.class);
        when(collection.insert(any(DBObject.class))).thenThrow(new MongoException("Mongo exception"));

        db = mock(DB.class);
        when(db.getCollection("commands")).thenReturn(collection);

        commandDao = new CommandDaoImpl(db, "commands");

        commandDao.create(mock(ManagedCommand.class));
    }

    @Test
    public void shouldBeAbleToRemoveCommand() throws Exception {
        final CommandImpl example = new CommandImpl().withId("command123")
                                                     .withName("MAVEN_CLEAN_INSTALL")
                                                     .withCommandLine("mvn clean install")
                                                     .withCreator("user123")
                                                     .withWorkspaceId("workspace123")
                                                     .withVisibility("public")
                                                     .withType("maven")
                                                     .withWorkingDir("/project");
        collection.insert(commandDao.asDBObject(example));

        commandDao.remove(example.getId());

        assertNull(collection.findOne(example.getId()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRethrowMongoExceptionAsServerExceptionWhenRemovingCommand() throws Exception {
        collection = mock(DBCollection.class);
        when(collection.remove(any(DBObject.class))).thenThrow(new MongoException("Mongo exception"));

        db = mock(DB.class);
        when(db.getCollection("commands")).thenReturn(collection);

        commandDao = new CommandDaoImpl(db, "commands");

        commandDao.remove("id");
    }

    @Test
    public void shouldBeAbleToUpdateCommand() throws Exception {
        final CommandImpl example = new CommandImpl().withId("command123")
                                                     .withName("MAVEN_CLEAN_INSTALL")
                                                     .withCommandLine("mvn clean install")
                                                     .withCreator("user123")
                                                     .withWorkspaceId("workspace123")
                                                     .withVisibility("public")
                                                     .withType("maven")
                                                     .withWorkingDir("/project");
        collection.insert(commandDao.asDBObject(example));

        commandDao.update(new CommandImpl().withId(example.getId())
                                           .withName("MCI NO TESTS")
                                           .withCommandLine("mvn clean install -Dmaven.test.skip")
                                           .withVisibility("private")
                                           .withWorkingDir("/project2"));

        final ManagedCommand updated = commandDao.asCommand(collection.findOne(example.getId()));
        assertEquals(updated, example.withName("MCI NO TESTS")
                                     .withCommandLine("mvn clean install -Dmaven.test.skip")
                                     .withVisibility("private")
                                     .withWorkingDir("/project2"));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenUpdatingCommandWithReservedCombinationOfNameCreatorAndWorkspaceId() throws Exception {
        final CommandImpl example = new CommandImpl().withId("command123")
                                                     .withName("MAVEN_CLEAN_INSTALL")
                                                     .withCommandLine("mvn clean install")
                                                     .withCreator("user123")
                                                     .withWorkspaceId("workspace123")
                                                     .withVisibility("public")
                                                     .withType("maven")
                                                     .withWorkingDir("/project");
        collection.insert(commandDao.asDBObject(example));
        collection.insert(commandDao.asDBObject(example.withId("command234")
                                                       .withName("MCI NO TESTS")));

        commandDao.update(new CommandImpl().withId("command123")
                                           .withName("MCI NO TESTS")
                                           .withCommandLine("mvn clean install -Dmaven.test.skip")
                                           .withVisibility("private")
                                           .withWorkingDir("/project2"));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRethrowMongoExceptionAsServerExceptionWhenUpdatingCommand() throws Exception {
        collection = mock(DBCollection.class);
        when(collection.update(any(DBObject.class), any(DBObject.class))).thenThrow(new MongoException("Mongo exception"));

        db = mock(DB.class);
        when(db.getCollection("commands")).thenReturn(collection);

        commandDao = new CommandDaoImpl(db, "commands");

        commandDao.update(new CommandImpl());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenCommandWithSpecifiedIdWasNotFound() throws Exception {
        commandDao.update(new CommandImpl().withId("123"));
    }

    @Test
    public void shouldBeAbleToGetCommands() throws Exception {
        final CommandImpl example1 = new CommandImpl().withId("command123")
                                                      .withName("MAVEN_CLEAN_INSTALL")
                                                      .withCommandLine("mvn clean install")
                                                      .withCreator("user123")
                                                      .withWorkspaceId("workspace123")
                                                      .withVisibility("public")
                                                      .withType("maven")
                                                      .withWorkingDir("/project");
        final CommandImpl example2 = clone(example1).withId("command234")
                                                    .withWorkspaceId("other")
                                                    .withName("MCI NO TESTS")
                                                    .withCommandLine("mvn clean install -Dmaven.test.skip");
        final CommandImpl example3 = clone(example1).withId("command345")
                                                    .withName("gs")
                                                    .withCommandLine("git status")
                                                    .withVisibility("private");
        final CommandImpl example4 = clone(example1).withId("command456")
                                                    .withName("gb")
                                                    .withCreator("someone")
                                                    .withCommandLine("gradle build")
                                                    .withVisibility("private");
        final CommandImpl example5 = clone(example1).withId("command567")
                                                    .withName("mci")
                                                    .withCreator("someone")
                                                    .withCommandLine("mvn clean install")
                                                    .withVisibility("public");
        for (CommandImpl example : asList(example1, example2, example3, example4, example5)) {
            collection.insert(commandDao.asDBObject(example));
        }

        final List<ManagedCommand> commands = commandDao.getCommands("workspace123", "user123", 0, 30);

        assertEquals(new HashSet<>(commands), new HashSet<>(asList(example1,    //is public in workspace123
                                                                   example3,    //is private but owned by user123
                                                                   example5))); //is public in workspace workspace123
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRethrowMongoExceptionAsServerExceptionWhenGettingCommands() throws Exception {
        collection = mock(DBCollection.class);
        when(collection.find(any(DBObject.class))).thenThrow(new MongoException("Mongo exception"));

        db = mock(DB.class);
        when(db.getCollection("commands")).thenReturn(collection);

        commandDao = new CommandDaoImpl(db, "commands");

        commandDao.getCommands("", "", 0, 30);
    }

    private CommandImpl clone(ManagedCommand command) {
        return new CommandImpl().withId(command.getId())
                                .withName(command.getName())
                                .withWorkspaceId(command.getWorkspaceId())
                                .withCreator(command.getCreator())
                                .withCommandLine(command.getCommandLine())
                                .withType(command.getType())
                                .withVisibility(command.getVisibility())
                                .withType(command.getType());
    }
}
