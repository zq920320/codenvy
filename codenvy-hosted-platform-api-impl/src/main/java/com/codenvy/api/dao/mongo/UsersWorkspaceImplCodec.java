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

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
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

import java.util.List;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Encodes & decodes {@link UsersWorkspaceImpl}.
 *
 * @author Eugene Voevodin
 */
public class UsersWorkspaceImplCodec implements Codec<UsersWorkspaceImpl> {

    private Codec<Document> codec;

    public UsersWorkspaceImplCodec(CodecRegistry registry) {
        codec = registry.get(Document.class);
    }

    @Override
    public UsersWorkspaceImpl decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked") // 'attributes' field is always list
        final List<Document> attributes = (List<Document>)document.get("attributes");

        @SuppressWarnings("unchecked") // 'commands' field is always list
        final List<Document> commandDocuments = (List<Document>)document.get("commands");
        final List<CommandImpl> commands = commandDocuments.stream()
                                                           .map(d -> new CommandImpl(d.getString("name"),
                                                                                     d.getString("commandLine"),
                                                                                     d.getString("type")))
                                                           .collect(toList());

        @SuppressWarnings("unchecked") // 'projects' field is always list
        final List<Document> projectDocuments = (List<Document>)document.get("projects");
        final List<ProjectConfigImpl> projects = projectDocuments.stream()
                                                                 .map(UsersWorkspaceImplCodec::asProjectConfig)
                                                                 .collect(toList());

        @SuppressWarnings("unchecked") // 'environments' fields is aways map
        final Map<String, Document> envDocuments = (Map<String, Document>)document.get("environments");
        final Map<String, EnvironmentImpl> environments = envDocuments.values()
                                                                      .stream()
                                                                      .map(UsersWorkspaceImplCodec::asEnvironment)
                                                                      .collect(toMap(EnvironmentImpl::getName, identity()));

        return UsersWorkspaceImpl.builder()
                                 .setId(document.getString("_id"))
                                 .setName(document.getString("name"))
                                 .setOwner(document.getString("owner"))
                                 .setDescription(document.getString("description"))
                                 .setDefaultEnvName(document.getString("defaultEnvName"))
                                 .setCommands(commands)
                                 .setAttributes(documentsListAsMap(attributes))
                                 .setProjects(projects)
                                 .setEnvironments(environments)
                                 .build();
    }

    @Override
    public void encode(BsonWriter writer, UsersWorkspaceImpl workspace, EncoderContext encoderContext) {
        final Document document = new Document().append("_id", workspace.getId())
                                                .append("name", workspace.getName())
                                                .append("owner", workspace.getOwner())
                                                .append("description", workspace.getDescription())
                                                .append("defaultEnvName", workspace.getDefaultEnvName())
                                                .append("attributes", mapAsDocumentsList(workspace.getAttributes()));
        document.append("commands", workspace.getCommands()
                                             .stream()
                                             .map(command -> new Document().append("name", command.getName())
                                                                           .append("commandLine", command.getCommandLine())
                                                                           .append("type", command.getType()))
                                             .collect(toList()));
        document.append("projects", workspace.getProjects()
                                             .stream()
                                             .map(UsersWorkspaceImplCodec::asDocument)
                                             .collect(toList()));
        final Document envDoc = workspace.getEnvironments()
                                         .values()
                                         .stream()
                                         .map(UsersWorkspaceImplCodec::asDocument)
                                         .reduce(new Document(), (r, d) -> r.append(d.getString("name"), d));
        document.append("environments", envDoc);
        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<UsersWorkspaceImpl> getEncoderClass() {
        return UsersWorkspaceImpl.class;
    }

    @SuppressWarnings("unchecked") // contains safe casts - see #decode
    private static ProjectConfigImpl asProjectConfig(Document document) {
        final ProjectConfigImpl projectConfig = new ProjectConfigImpl();
        projectConfig.setName(document.getString("name"));
        projectConfig.setPath(document.getString("path"));
        projectConfig.setType(document.getString("type"));
        projectConfig.setDescription(document.getString("description"));

        final List<String> mixins = (List<String>)document.get("mixins");
        projectConfig.setMixins(mixins);

        final List<Document> attributes = (List<Document>)document.get("attributes");
        projectConfig.setAttributes(attributes.stream()
                                              .collect(toMap(d -> d.getString("name"), d -> (List<String>)d.get("value"))));

        final List<Document> modules = (List<Document>)document.get("modules");
        if (modules != null) {
            projectConfig.setModules(modules.stream()
                                            .map(UsersWorkspaceImplCodec::asModuleConfig)
                                            .collect(toList()));
        }

        final Document sourceDocument = document.get("source", Document.class);
        if (sourceDocument != null) {
            final SourceStorageImpl storage = new SourceStorageImpl(sourceDocument.getString("type"),
                                                                    sourceDocument.getString("location"),
                                                                    documentsListAsMap((List<Document>)sourceDocument.get("parameters")));
            projectConfig.setSource(storage);
        }
        return projectConfig;
    }

    private static Document asDocument(ProjectConfigImpl project) {
        final Document document = new Document().append("name", project.getName())
                                                .append("path", project.getPath())
                                                .append("type", project.getType())
                                                .append("description", project.getDescription())
                                                .append("mixins", project.getMixins())
                                                .append("attributes", mapAsDocumentsList(project.getAttributes()))
                                                .append("modules", project.getModules()
                                                                          .stream()
                                                                          .map(UsersWorkspaceImplCodec::asDocument)
                                                                          .collect(toList()));

        final SourceStorage sourceStorage = project.getSource();
        if (sourceStorage != null) {
            document.append("source", new Document().append("type", sourceStorage.getType())
                                                    .append("location", sourceStorage.getLocation())
                                                    .append("parameters", mapAsDocumentsList(sourceStorage.getParameters())));
        }


        return document;
    }

    @SuppressWarnings("unchecked") // contains safe casts - see #decode
    private static ProjectConfigImpl asModuleConfig(Document document) {
        final ProjectConfigImpl moduleConfig = new ProjectConfigImpl();
        moduleConfig.setName(document.getString("name"));
        moduleConfig.setPath(document.getString("path"));
        moduleConfig.setType(document.getString("type"));
        moduleConfig.setDescription(document.getString("description"));

        final List<String> mixins = (List<String>)document.get("mixins");
        moduleConfig.setMixins(mixins);

        final List<Document> attributes = (List<Document>)document.get("attributes");
        moduleConfig.setAttributes(attributes.stream()
                                             .collect(toMap(d -> d.getString("name"), d -> (List<String>)d.get("value"))));

        final List<Document> modules = (List<Document>)document.get("modules");
        if (modules != null) {
            moduleConfig.setModules(modules.stream()
                                           .map(UsersWorkspaceImplCodec::asModuleConfig)
                                           .collect(toList()));
        }

        return moduleConfig;
    }

    private static Document asDocument(ProjectConfig module) {
        return new Document().append("name", module.getName())
                             .append("path", module.getPath())
                             .append("type", module.getType())
                             .append("description", module.getDescription())
                             .append("mixins", module.getMixins())
                             .append("attributes", mapAsDocumentsList(module.getAttributes()))
                             .append("modules", module.getModules()
                                                      .stream()
                                                      .map(UsersWorkspaceImplCodec::asDocument)
                                                      .collect(toList()));
    }

    private static EnvironmentImpl asEnvironment(Document document) {
        @SuppressWarnings("unchecked") // 'machineConfigs' field is always list of documents
        final List<Document> machineDocuments = (List<Document>)document.get("machineConfigs");
        final List<MachineConfigImpl> machineConfigs = machineDocuments.stream()
                                                                       .map(UsersWorkspaceImplCodec::asMachineConfig)
                                                                       .collect(toList());
        RecipeImpl recipe = null;
        final Document recipeDocument = document.get("recipe", Document.class);
        if (recipeDocument != null) {
            recipe = new RecipeImpl();
            recipe.setType(recipeDocument.getString("type"));
            recipe.setScript(recipeDocument.getString("script"));
        }
        return new EnvironmentImpl(document.getString("name"), recipe, machineConfigs);
    }

    private static Document asDocument(EnvironmentStateImpl environment) {
        final Document document = new Document();

        final Recipe recipe = environment.getRecipe();
        if (recipe != null) {
            document.append("recipe", new Document("type", recipe.getType()).append("script", recipe.getScript()));
        }

        document.append("name", environment.getName())
                .append("machineConfigs", environment.getMachineConfigs()
                                                     .stream()
                                                     .map(UsersWorkspaceImplCodec::asDocument)
                                                     .collect(toList()));
        return document;
    }

    private static MachineConfigImpl asMachineConfig(Document document) {
        MachineSourceImpl source = null;
        final Document sourceDocument = document.get("source", Document.class);
        if (sourceDocument != null) {
            source = new MachineSourceImpl(sourceDocument.getString("type"), sourceDocument.getString("location"));
        }
        LimitsImpl limits = null;
        final Document limitsDocument = document.get("limits", Document.class);
        if (limitsDocument != null) {
            limits = new LimitsImpl(limitsDocument.getInteger("memory", 0));
        }
        return new MachineConfigImpl(document.getBoolean("isDev"),
                                     document.getString("name"),
                                     document.getString("type"),
                                     source,
                                     limits);
    }

    private static Document asDocument(MachineConfigImpl config) {
        final Document document = new Document().append("isDev", config.isDev())
                                                .append("name", config.getName())
                                                .append("type", config.getType());
        final MachineSource source = config.getSource();
        if (source != null) {
            document.append("source", new Document("type", source.getType()).append("location", source.getLocation()));
        }
        final Limits limits = config.getLimits();
        if (limits != null) {
            document.append("limits", new Document("memory", limits.getMemory()));
        }
        return document;
    }
}
