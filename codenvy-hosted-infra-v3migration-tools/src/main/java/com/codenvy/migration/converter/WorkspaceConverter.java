package com.codenvy.migration.converter;

import com.codenvy.api.workspace.shared.dto.Attribute;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkspaceConverter implements ObjectConverter<com.codenvy.organization.model.Workspace,
        Workspace> {
    @Override
    public Workspace convert(com.codenvy.organization.model.Workspace workspaceOld) {
        List<Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, String> entry : workspaceOld.getAttributes().entrySet()) {
            Attribute attribute = DtoFactory.getInstance().createDto(Attribute.class)
                                            .withName(entry.getKey())
                                            .withValue(entry.getValue());
            attributes.add(attribute);
        }

        return DtoFactory.getInstance().createDto(Workspace.class)
                         .withId(workspaceOld.getId())
                         .withOrganizationId(workspaceOld.getOwner().getId())
                         .withName(workspaceOld.getName())
                         .withAttributes(attributes);
    }
}
