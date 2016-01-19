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

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.Binary;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.model.impl.StackComponent;
import org.eclipse.che.api.workspace.server.model.impl.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.StackSource;
import org.eclipse.che.api.workspace.server.model.impl.StackSourceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * //
 *
 * @author Alexander Andrienko
 */
public class StackImplCodec implements Codec<StackImpl> {

    private Codec<Document> codec;

    public StackImplCodec(CodecRegistry registry) {
        this.codec = registry.get(Document.class);
    }

    @Override
    public StackImpl decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked")//tags is always String list
        List<String> tags = (List<String>)document.get("tags");

        WorkspaceConfig workspaceConfig = null;
        if (document.get("workspaceConfig") != null) {
            Document workspaceConfigDoc = (Document)document.get("workspaceConfig");
            workspaceConfig = UsersWorkspaceImplCodec.asWorkspaceConfig(workspaceConfigDoc);
        }

        StackSource source = null;
        if (document.get("source") != null) {
            source = asStackSource((Document)document.get("source"));
        }

        StackIcon stackIcon = null;
        if (document.get("icon") != null) {
            try {
                stackIcon = asStackIcon((Document)document.get("icon"));
            } catch (IOException | ConflictException e) {
                //todo log error
            }
        }

        @SuppressWarnings("unchecked")//components is always list
        List<Document> componentDocument = (List<Document>)document.get("components");
        List<StackComponent> components = componentDocument.stream()
                                                           .map(StackImplCodec::asStackComponent)
                                                           .collect(toList());

        return StackImpl.builder().setId(document.getString("_id"))
                        .setName(document.getString("name"))
                        .setDescription(document.getString("description"))
                        .setIconLink(document.getString("iconLink"))
                        .setScope(document.getString("scope"))
                        .setCreator(document.getString("creator"))
                        .setTags(tags)
                        .setWorkspaceConfig(workspaceConfig)
                        .setSource(source)
                        .setComponents(components)
                        .setIcon(stackIcon)
                        .build();
    }

    @Override
    public void encode(BsonWriter writer, StackImpl stack, EncoderContext encoderContext) {
        final Document document = new Document().append("_id", stack.getId())
                                                .append("name", stack.getName())
                                                .append("description", stack.getDescription())
                                                .append("iconLink", stack.getIconLink())
                                                .append("scope", stack.getScope())
                                                .append("creator", stack.getCreator())
                                                .append("tags", stack.getTags());

        if (stack.getWorkspaceConfig() != null) {
            Document workspaceConfigDocument = UsersWorkspaceImplCodec.asDocument(new WorkspaceConfigImpl(stack.getWorkspaceConfig()));
            document.append("workspaceConfig", workspaceConfigDocument);
        }

        if (stack.getSource() != null) {
            document.append("source", asDocument(stack.getSource()));
        }

        document.append("components", stack.getComponents().stream()
                                           .map(StackImplCodec::asDocument)
                                           .collect(toList()));
        if (stack.getIcon() != null) {
            document.append("icon", asDocument(stack.getIcon()));
        }

        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<StackImpl> getEncoderClass() {
        return StackImpl.class;
    }

    public static StackIcon asStackIcon(Document document) throws IOException, ConflictException {
        return new StackIcon(document.getString("mediaType"), ((Binary)document.get("data")).getData());
    }

    public static Document asDocument(StackIcon stackIcon) {
        return new Document().append("mediaType", stackIcon.getMediaType()).append("data", stackIcon.getData());
    }

    public static Document asDocument(StackComponent component) {
        return new Document().append("name", component.getName()).append("version", component.getVersion());
    }

    public static StackComponent asStackComponent(Document document) {
        return new StackComponentImpl(document.getString("name"), document.getString("version"));
    }

    public static Document asDocument(StackSource source) {
        return new Document().append("type", source.getType()).append("origin", source.getOrigin());
    }

    public static StackSource asStackSource(Document document) {
        return new StackSourceImpl(document.getString("type"), document.getString("origin"));
    }
}