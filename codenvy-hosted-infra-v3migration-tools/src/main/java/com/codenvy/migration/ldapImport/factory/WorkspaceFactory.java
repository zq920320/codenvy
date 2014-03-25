package com.codenvy.migration.ldapImport.factory;

import com.codenvy.migration.ldapImport.LdapAttribute;
import com.codenvy.organization.model.ItemReference;
import com.codenvy.organization.model.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkspaceFactory extends ObjectFactory<Workspace> {
    private static final Logger LOG = LoggerFactory.getLogger(UserFactory.class);

    private static final String SUITABLE_CLASS = "cloudIdeWorkspace";

    private static final Set<String> SUITABLE_PARAMS = new HashSet<>(Arrays.asList(
            "cloudIdeEntityOwner",
            "cloudIdeName",
            "cloudIdeAttributes",
            "ou",
            "cloudIdeMembers")
    );

    @Override
    protected boolean isSuitableAttribute(String name) {
        return SUITABLE_PARAMS.contains(name);
    }

    @Override
    public Workspace create(List<String> item) throws Exception {
        List<LdapAttribute> attributes = getAttributes(item);

        Workspace workspace = new Workspace();

        for (LdapAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case "cloudIdeEntityOwner":
                    workspace.setOwner(new ItemReference(attribute.getValue()));
                    break;
                case "cloudIdeName":
                    workspace.setName(attribute.getValue());
                    break;
                case "cloudIdeMembers":
                    String[] pairStr = attribute.getKeyValue();
                    if (!workspace.containMember(pairStr[1])) {
                        workspace.addMember(pairStr[1]);
                    }
                    workspace.addMemberRole(pairStr[0], pairStr[1]);
                    break;
                case "ou":
                    workspace.setId(attribute.getValue());
                    break;
                case "cloudIdeAttributes":
                    pairStr = attribute.getKeyValue();
                    workspace.setAttribute(pairStr[0], pairStr[1]);
                    break;
                default:
                    LOG.warn(String.format("Attribute setter for %s parameter is not found", attribute));
            }
        }

        return workspace;
    }

    @Override
    public boolean isSuitableClass(String objectClass) {
        return SUITABLE_CLASS.equals(objectClass);
    }
}
