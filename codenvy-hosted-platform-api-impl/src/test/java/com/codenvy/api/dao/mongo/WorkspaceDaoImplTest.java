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

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentStateImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspaceDaoImpl}.
 *
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceDaoImplTest {

    MongoCollection<UsersWorkspaceImpl> collection;
    WorkspaceDaoImpl                    workspaceDao;

    @BeforeMethod
    public void setUpDb() {
        final Fongo fongo = new Fongo("Workspace test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        final MongoDatabase database = fongo.getDatabase("workspaces")
                                            .withCodecRegistry(fromRegistries(defaultRegistry,
                                                                              fromCodecs(new UsersWorkspaceImplCodec(defaultRegistry))));
        collection = database.getCollection("workspaces", UsersWorkspaceImpl.class);
        workspaceDao = new WorkspaceDaoImpl(database, "workspaces");
    }

    @Test
    public void testCreateWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);

        final UsersWorkspaceImpl result = collection.find(Filters.eq("_id", workspace.getId())).first();
        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Workspace must not be null")
    public void testCreateWorkspaceWhenWorkspaceIsNull() throws Exception {
        workspaceDao.create(null);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void testCreateWorkspaceWhenWorkspaceWithSuchIdAlreadyExists() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);
        workspace.setName("anotherName");// to prevent same name restriction
        workspaceDao.create(workspace);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void testCreateWorkspaceWhenWorkspaceWithSuchNameAndOwnerAlreadyExists() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);
        workspaceDao.create(workspace);
    }

    @Test(expectedExceptions = ServerException.class)
    public void testCreateWorkspaceWhenMongoExceptionWasThrew() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).insertOne(any()));

        new WorkspaceDaoImpl(db, "workspaces").create(createWorkspace());
    }

    @Test
    public void testUpdateWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);
        workspace.setName("new-workspace-name");
        workspace.setDescription("new-workspace-description");

        workspaceDao.update(workspace);

        final UsersWorkspaceImpl result = collection.find(Filters.eq("_id", workspace.getId())).first();
        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testUpdateWhenWorkspaceDoesNotExist() throws Exception {
        final MongoDatabase db = mockDatabase(col -> when(col.updateOne(any(), any())).thenReturn(null));

        new WorkspaceDaoImpl(db, "workspaces").update(createWorkspace());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace update must not be null")
    public void testUpdateWorkspaceWhenWorkspaceIsNull() throws Exception {
        workspaceDao.update(null);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void testUpdateWorkspaceWhenWorkspaceWithSuchNameAndOwnerAlreadyExists() throws Exception {
        final MongoDatabase db = mockDatabase(col -> when(col.findOneAndReplace(any(), any())).thenThrow(mock(MongoWriteException.class)));

        new WorkspaceDaoImpl(db, "workspaces").update(createWorkspace());
    }

    @Test(expectedExceptions = ServerException.class)
    public void testUpdateWorkspaceWhenMongoExceptionWasThrew() throws Exception {
        final MongoDatabase db = mockDatabase(col -> when(col.findOneAndReplace(any(), any())).thenThrow(mock(MongoException.class)));

        new WorkspaceDaoImpl(db, "workspaces").update(createWorkspace());
    }

    @Test
    public void testRemoveWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);
        assertEquals(collection.count(Filters.eq("_id", workspace.getId())), 1);

        workspaceDao.remove(workspace.getId());

        assertEquals(collection.count(Filters.eq("_id", workspace.getId())), 0);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace identifier must not be null")
    public void testRemoveWorkspaceWithNullId() throws Exception {
        workspaceDao.remove(null);
    }

    @Test
    public void testGetWorkspaceById() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);

        final UsersWorkspaceImpl result = workspaceDao.get(workspace.getId());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetWorkspaceByIdWhenWorkspaceDoesNotExist() throws Exception {
        workspaceDao.get("workspace123");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace identifier must not be null")
    public void testGetWorkspaceByIdWithNullId() throws Exception {
        workspaceDao.get(null);
    }

    @Test
    public void testGetWorkspaceByNameAndOwner() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);

        final UsersWorkspaceImpl result = workspaceDao.get(workspace.getName(), workspace.getOwner());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetWorkspaceByNameAndOwnerWhenWorkspaceDoesNotExist() throws Exception {
        workspaceDao.get("workspace-name", "workspace-owner");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace name must not be null")
    public void testGetWorkspaceByNameAndOwnerWithNullName() throws Exception {
        workspaceDao.get(null, "workspace-owner");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace owner must not be null")
    public void testGetWorkspaceByNameAndOwnerWithNullOwner() throws Exception {
        workspaceDao.get("workspace name", null);
    }

    @Test
    public void testGetWorkspacesByOwner() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        final UsersWorkspaceImpl workspace2 = createWorkspace();
        workspace2.setName(workspace.getName() + '2');
        final UsersWorkspaceImpl workspace3 = new UsersWorkspaceImpl(workspace, generate("ws", 16), workspace.getOwner() + '2');
        workspace3.setName(workspace.getName() + '3');
        collection.insertMany(asList(workspace, workspace2, workspace3));

        final List<UsersWorkspaceImpl> result = workspaceDao.getByOwner(workspace.getOwner());

        assertEquals(new HashSet<>(result), new HashSet<>(asList(workspace, workspace2)));
    }

    @Test
    public void testGetWorkspacesByOwnerWhenUserDoesNotOwnAnyWorkspace() throws Exception {
        assertTrue(workspaceDao.getByOwner("test-user").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace owner must not be null")
    public void testGetWorkspacesByOwnerWhenOwnerIsNull() throws Exception {
        workspaceDao.getByOwner(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWorkspaceEncoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final UsersWorkspaceImplCodec codec = new UsersWorkspaceImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test workspace
        final UsersWorkspaceImpl workspace = createWorkspace();

        // encode workspace
        codec.encode(null, workspace, null);

        // check encoding result
        final Document result = documentHolder[0];
        assertEquals(result.getString("_id"), workspace.getId(), "Workspace id");
        assertEquals(result.getString("name"), workspace.getName(), "Workspace name");
        assertEquals(result.getString("owner"), workspace.getOwner(), "Workspace owner");
        assertEquals(result.getString("description"), workspace.getDescription(), "Workspace description");
        assertEquals(result.getString("defaultEnvName"), workspace.getDefaultEnvName(), "Workspace defaultEnvName");

        // check attributes
        final List<Document> attributes = (List<Document>)result.get("attributes");
        assertEquals(attributes, mapAsDocumentsList(workspace.getAttributes()), "Workspace attributes");

        // check commands
        final List<Document> commands = (List<Document>)result.get("commands");
        assertEquals(commands.size(), workspace.getCommands().size(), "Workspace commands size");
        for (int i = 0; i < commands.size(); i++) {
            final CommandImpl command = workspace.getCommands().get(i);
            final Document document = commands.get(i);

            assertEquals(document.getString("name"), command.getName(), "Command name");
            assertEquals(document.getString("commandLine"), command.getCommandLine(), "Command line");
            assertEquals(document.getString("type"), command.getType(), "Command type");
        }

        // check projects
        final List<Document> projects = (List<Document>)result.get("projects");
        assertEquals(projects.size(), workspace.getProjects().size());
        for (int i = 0; i < projects.size(); i++) {
            final ProjectConfigImpl project = workspace.getProjects().get(i);
            final Document projDoc = projects.get(0);

            assertEquals(project.getName(), projDoc.getString("name"), "Project nam");
            assertEquals(project.getType(), projDoc.getString("type"), "Project type");
            assertEquals(project.getDescription(), projDoc.getString("description"));
            assertEquals(project.getPath(), projDoc.getString("path"));

            final List<Document> mixinTypes = (List<Document>)projDoc.get("mixinTypes");
            assertEquals(project.getMixinTypes(), mixinTypes, "Mixin types");

            final List<Document> attrsList = (List<Document>)projDoc.get("attributes");
            assertEquals(attrsList.size(), project.getAttributes().size());
            for (Document attrDoc : attrsList) {
                final String attrName = attrDoc.getString("name");
                final List<String> attrValue = (List<String>)attrDoc.get("value");

                assertEquals(project.getAttributes().get(attrName), attrValue, "Attribute values");
            }

            if (project.getSource() != null) {
                final Document source = (Document)projDoc.get("source");

                assertNotNull(source);
                assertEquals(source.getString("type"), project.getSource().getType(), "Source type");
                assertEquals(source.getString("location"), project.getSource().getLocation(), "Source location");

                final List<Document> parameters = (List<Document>)source.get("parameters");
                assertEquals(documentsListAsMap(parameters), project.getSource().getParameters(), "Source parameters");
            }
        }

        // check environments
        final Map<String, Document> environments = (Map<String, Document>)result.get("environments");
        assertEquals(environments.size(), workspace.getEnvironments().size());
        for (Map.Entry<String, Document> envEntry : environments.entrySet()) {
            final EnvironmentStateImpl environment = workspace.getEnvironments().get(envEntry.getKey());
            final Document envDoc = envEntry.getValue();

            assertEquals(envDoc.getString("name"), environment.getName());

            if (environment.getRecipe() != null) {
                final Document document = envDoc.get("recipe", Document.class);
                assertEquals(document.getString("type"), environment.getRecipe().getType(), "Environment recipe type");
                assertEquals(document.getString("script"), environment.getRecipe().getScript(), "Environment recipe script");
            }

            final List<Document> machineConfigs = (List<Document>)envDoc.get("machineConfigs");
            assertEquals(machineConfigs.size(), environment.getMachineConfigs().size());
            for (int i = 0; i < machineConfigs.size(); i++) {
                final Document machineDoc = machineConfigs.get(i);
                final MachineConfigImpl machine = environment.getMachineConfigs().get(i);

                assertEquals(machineDoc.getBoolean("isDev"), Boolean.valueOf(machine.isDev()));
                assertEquals(machineDoc.getString("name"), machine.getName(), "Machine name");
                assertEquals(machineDoc.getString("type"), machine.getType(), "Machine type");
                if (machine.getSource() != null) {
                    final Document sourceDoc = machineDoc.get("source", Document.class);

                    assertEquals(sourceDoc.getString("type"), machine.getSource().getType(), "Machine source type");
                    assertEquals(sourceDoc.getString("location"), machine.getSource().getLocation(), "Machine source location");
                }
                if (machine.getLimits() != null) {
                    final Document limitsDoc = machineDoc.get("limits", Document.class);

                    assertEquals(limitsDoc.getInteger("memory", 0), machine.getLimits().getMemory(), "Machine RAM limit");
                }
            }
        }
    }

    /**
     * As workspace encoding was tested with assertion on each encoded document field
     * there is no reason to do the same with decoding. To check if document is decoded - it is
     * enough to check that decoding of encoded document produces exactly equal workspace to encoded one.
     *
     * <p>Simplified test case:
     * <pre>
     *     UsersWorkspaceImpl ws = ...
     *
     *     Document encodedWs = codec.encode(ws)
     *
     *     UsersWorkspaceImpl result = codec.decode(encodedWs)
     *
     *     assert ws.equals(result)
     * </pre>
     *
     * @see #testWorkspaceEncoding()
     */
    @Test(dependsOnMethods = "testWorkspaceEncoding")
    public void testWorkspaceDecoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final UsersWorkspaceImplCodec codec = new UsersWorkspaceImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test workspace
        final UsersWorkspaceImpl workspace = createWorkspace();

        // encode workspace
        codec.encode(null, workspace, null);

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(documentHolder[0]);

        final UsersWorkspaceImpl result = codec.decode(null, null);

        assertEquals(result, workspace);
    }

    private static UsersWorkspaceImpl createWorkspace() {
        // environments
        final RecipeImpl recipe = new RecipeImpl();
        recipe.setType("dockerfile");
        recipe.setScript("FROM codenvy/jdk7\nCMD tail -f /dev/null");

        final MachineSourceImpl machineSource = new MachineSourceImpl("recipe", "recipe-url");
        final MachineConfigImpl machineCfg1 = new MachineConfigImpl(true,
                                                                    "dev-machine",
                                                                    "machine-type",
                                                                    machineSource,
                                                                    new LimitsImpl(512));
        final MachineConfigImpl machineCfg2 = new MachineConfigImpl(false,
                                                                    "non-dev-machine",
                                                                    "machine-type-2",
                                                                    machineSource,
                                                                    new LimitsImpl(2048));

        final EnvironmentImpl env1 = new EnvironmentImpl("my-environment", recipe, asList(machineCfg1, machineCfg2));
        final EnvironmentImpl env2 = new EnvironmentImpl("my-environment-2", recipe, singletonList(machineCfg1));

        final Map<String, EnvironmentImpl> environments = new HashMap<>(4);
        environments.put(env1.getName(), env1);
        environments.put(env2.getName(), env2);

        // projects
        final ProjectConfigImpl project1 = new ProjectConfigImpl();
        project1.setName("test-project-name");
        project1.setDescription("This is test project");
        project1.setPath("/path/to/project");
        project1.setType("maven");
        project1.setMixinTypes(singletonList("git"));

        final Map<String, List<String>> projectAttrs = new HashMap<>(4);
        projectAttrs.put("project.attribute1", singletonList("value1"));
        projectAttrs.put("project.attribute2", asList("value2", "value3"));
        project1.setAttributes(projectAttrs);

        final Map<String, String> sourceParameters = new HashMap<>(4);
        sourceParameters.put("source-parameter-1", "value1");
        sourceParameters.put("source-parameter-2", "value2");
        project1.setStorage(new SourceStorageImpl("sources-type", "sources-location", sourceParameters));

        final List<ProjectConfigImpl> projects = singletonList(project1);

        // commands
        final List<CommandImpl> commands = new ArrayList<>(3);
        commands.add(new CommandImpl("MCI", "mvn clean install", "maven"));
        commands.add(new CommandImpl("bower install", "bower install", "bower"));
        commands.add(new CommandImpl("build without tests", "mvn clean install -Dmaven.test.skip", "maven"));

        // attributes
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("test.attribute1", "test-value1");
        attributes.put("test.attribute2", "test-value2");
        attributes.put("test.attribute3", "test-value3");

        return UsersWorkspaceImpl.builder()
                                 .setId(generate("workspace", 16))
                                 .setName("workspace-name")
                                 .setDescription("This is test workspace")
                                 .setOwner("user123")
                                 .setAttributes(attributes)
                                 .setCommands(commands)
                                 .setProjects(projects)
                                 .setEnvironments(environments)
                                 .setDefaultEnvName(env1.getName())
                                 .build();
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<UsersWorkspaceImpl>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<UsersWorkspaceImpl> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("workspaces", UsersWorkspaceImpl.class)).thenReturn(collection);

        return database;
    }
}
