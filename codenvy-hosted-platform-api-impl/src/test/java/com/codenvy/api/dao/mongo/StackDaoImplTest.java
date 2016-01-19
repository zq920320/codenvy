/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentStateImpl;
import org.eclipse.che.api.workspace.server.model.impl.ManagedStack;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.StackComponent;
import org.eclipse.che.api.workspace.server.model.impl.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.StackSourceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * //
 *
 * @author Alexander Andrienko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class StackDaoImplTest extends BaseDaoTest {

    //private static final StackDaoImpl.FromDbToStackFunction FROM_DB_TO_STACK_FUNCTION = new StackDaoImpl.FromDbToStackFunction();

    private static final String BD_COLLECTION_NAME = "stacks";
    private MongoCollection<ManagedStack> collection;
    private StackDaoImpl                  stackDao;

    //variables for init test stack
    private static final String ID_TEST          = "randomId123";
    private static final String NAME             = "Java";
    private static final String DESCRIPTION      = "Simple java stack for generation java projects";
    private static final String ICON_URL          = "http://localhost/api/files/someicon.svg";
    private static final String USER_ID           = "user123";
    private static final String CREATOR           = USER_ID;
    private static final String SCOPE             = "advanced";
    private static final String SOURCE_TYPE       = "image";
    private static final String SOURCE_ORIGIN     = "codenvy/ubuntu_jdk8";
    private static final String COMPONENT_NAME    = "Java";
    private static final String COMPONENT_VERSION = "1.8_45";

    private List<String> tags = Arrays.asList("java", "maven");

    private WorkspaceConfigImpl workspace = createWorkspace();

    @Captor
    ArgumentCaptor<Document> documentHolderCaptor;

    @BeforeMethod
    public void setUpDb() throws ServerException {
        final Fongo fongo = new Fongo("Stack test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();

        StackImplCodec codec = new StackImplCodec(defaultRegistry);
        database = fongo.getDatabase("stacks").withCodecRegistry(fromRegistries(defaultRegistry, fromCodecs(codec)));
        collection = database.getCollection(BD_COLLECTION_NAME, ManagedStack.class);


        stackDao = new StackDaoImpl(database, "stacks");
    }

    @Test
    public void encodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        StackImplCodec stackImplCodec = new StackImplCodec(CodecRegistries.fromCodecs(documentCodec));

        //prepare test stack
        final StackImpl stack = createStack();

        //launch test action
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document stackDocument = documentHolderCaptor.getValue();

        // check encoding result
        assertEquals(stackDocument.getString("_id"), stack.getId(), "Stack id");
        assertEquals(stackDocument.getString("name"), stack.getName(), "Stack name");
        assertEquals(stackDocument.getString("description"), stack.getDescription(), "Stack description");
        assertEquals(stackDocument.getString("iconLink"), stack.getIconLink(), "Stack iconLink");
        assertEquals(stackDocument.getString("scope"), stack.getScope(), "Stack scope");
        assertEquals(stackDocument.getString("creator"), stack.getCreator(), "Stack creator");

        assertEquals((List<String>)stackDocument.get("tags"), tags, "Stack tags");

        Document sourceDocument = (Document)stackDocument.get("source");
        assertEquals(sourceDocument.getString("type"), SOURCE_TYPE, "Stack source type");
        assertEquals(sourceDocument.getString("origin"), SOURCE_ORIGIN, "Stack source origin");

        List<Document> components = (List<Document>)stackDocument.get("components");
        Document componentDocument = components.get(0);
        assertEquals(componentDocument.getString("name"), COMPONENT_NAME, "Stack component type");
        assertEquals(componentDocument.getString("version"), COMPONENT_VERSION, "Stack component origin");

        //verify workspaceConfig
        Document worspaceDocument = (Document)stackDocument.get("workspaceConfig");

        assertEquals(worspaceDocument.getString("name"), workspace.getName(), "Workspace name");
        assertEquals(worspaceDocument.getString("description"), workspace.getDescription(), "Workspace description");
        assertEquals(worspaceDocument.getString("defaultEnvName"), workspace.getDefaultEnvName(), "Workspace defaultEnvName");

        // check attributes
        final List<Document> attributes = (List<Document>)worspaceDocument.get("attributes");
        assertEquals(attributes, mapAsDocumentsList(workspace.getAttributes()), "Workspace attributes");

        // check commands
        final List<Document> commands = (List<Document>)worspaceDocument.get("commands");
        assertEquals(commands.size(), workspace.getCommands().size(), "Workspace commands size");
        for (int i = 0; i < commands.size(); i++) {
            final Command command = workspace.getCommands().get(i);
            final Document document = commands.get(i);

            assertEquals(document.getString("name"), command.getName(), "Command name");
            assertEquals(document.getString("commandLine"), command.getCommandLine(), "Command line");
            assertEquals(document.getString("type"), command.getType(), "Command type");
        }

        // check projects
        final List<Document> projects = (List<Document>)worspaceDocument.get("projects");
        assertEquals(projects.size(), workspace.getProjects().size());
        for (int i = 0; i < projects.size(); i++) {
            final ProjectConfig project = workspace.getProjects().get(i);
            final Document projDoc = projects.get(0);

            assertEquals(project.getName(), projDoc.getString("name"), "Project nam");
            assertEquals(project.getType(), projDoc.getString("type"), "Project type");
            assertEquals(project.getDescription(), projDoc.getString("description"));
            assertEquals(project.getPath(), projDoc.getString("path"));

            final List<Document> mixins = (List<Document>)projDoc.get("mixins");
            assertEquals(project.getMixins(), mixins, "Mixin types");

            final List<Document> attrsList = (List<Document>)projDoc.get("attributes");
            assertEquals(attrsList.size(), project.getAttributes().size());
            for (Document attrDoc : attrsList) {
                final String attrName = attrDoc.getString("name");
                final List<String> attrValue = (List<String>)attrDoc.get("value");

                assertEquals(project.getAttributes().get(attrName), attrValue, "Attribute values");
            }

            final List<Document> modulesList = (List<Document>)projDoc.get("modules");
            assertEquals(modulesList.size(), project.getModules().size());
            for (Document module : modulesList) {
                final String moduleName = module.getString("name");
                assertEquals(moduleName, project.getModules().get(0).getName(), "Module name");
                final List<Document> submodules = (List<Document>)module.get("modules");
                assertEquals(submodules.size(), project.getModules().get(0).getModules().size(), "Modules size");
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
        final Map<String, Document> environments = (Map<String, Document>)worspaceDocument.get("environments");
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

    @Test
    public void decodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final StackImplCodec stackImplCodec = new StackImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // prepare test workspace
        final StackImpl stack = createStack();

        // encode workspace
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document document = documentHolderCaptor.getValue();

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(document);

        final ManagedStack result = stackImplCodec.decode(null, null);

        assertEquals(result, stack);
    }

    private StackImpl createStack() {
        List<StackComponent> componentsImpl = Collections.singletonList(new StackComponentImpl(COMPONENT_NAME, COMPONENT_VERSION));
        StackSourceImpl source = new StackSourceImpl(SOURCE_TYPE, SOURCE_ORIGIN);
        return StackImpl.builder()
                        .setId(ID_TEST)
                        .setName(NAME)
                        .setDescription(DESCRIPTION)
                        .setScope(SCOPE)
                        .setCreator(CREATOR)
                        .setIconLink(ICON_URL)
                        .setTags(tags)
                        .setSource(source)
                        .setWorkspaceConfig(workspace)
                        .setComponents(componentsImpl)
                        .build();
    }

    private static WorkspaceConfigImpl createWorkspace() {
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
        project1.setMixins(singletonList("git"));

        final Map<String, List<String>> projectAttrs = new HashMap<>(4);
        projectAttrs.put("project.attribute1", singletonList("value1"));
        projectAttrs.put("project.attribute2", asList("value2", "value3"));
        project1.setAttributes(projectAttrs);

        final Map<String, String> sourceParameters = new HashMap<>(4);
        sourceParameters.put("source-parameter-1", "value1");
        sourceParameters.put("source-parameter-2", "value2");
        project1.setSource(new SourceStorageImpl("sources-type", "sources-location", sourceParameters));

        final ProjectConfigImpl innerModule = new ProjectConfigImpl();
        innerModule.setName("module");
        innerModule.setPath("/module");
        innerModule.setType("sometype");
        innerModule.setDescription("description");
        innerModule.setMixins(singletonList("git"));

        final ProjectConfigImpl topModule = new ProjectConfigImpl();
        topModule.setName("module2");
        topModule.setPath("/module2");
        topModule.setType("sometype2");
        topModule.setDescription("description2");
        topModule.setMixins(singletonList("git"));
        topModule.setModules(singletonList(innerModule));

        project1.setModules(singletonList(topModule));

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

        return WorkspaceConfigImpl.builder()
                                  .setName("workspace-name")
                                  .setDescription("This is test workspace")
                                  .setAttributes(attributes)
                                  .setCommands(commands)
                                  .setProjects(projects)
                                  .setEnvironments(environments)
                                  .setDefaultEnvName(env1.getName())
                                  .build();
    }
}
