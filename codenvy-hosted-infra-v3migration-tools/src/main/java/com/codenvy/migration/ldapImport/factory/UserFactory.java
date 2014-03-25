package com.codenvy.migration.ldapImport.factory;

import com.codenvy.migration.ldapImport.LdapAttribute;
import com.codenvy.organization.model.ItemReference;
import com.codenvy.organization.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserFactory extends ObjectFactory<User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserFactory.class);

    private static final String SUITABLE_CLASS = "cloudIdeUser";

    private static final Set<String> SUITABLE_PARAMS = new HashSet<>(Arrays.asList(
            "cloudIdeAccounts",
            "cloudIdeAliases",
            "cloudIdeUserPassword",
            "cloudIdeMemberships",
            "cloudIdeAttributes",
            "ou")
    );

    @Override
    protected boolean isSuitableAttribute(String name) {
        return SUITABLE_PARAMS.contains(name);
    }

    @Override
    public User create(List<String> item) throws Exception {
        List<LdapAttribute> attributes = getAttributes(item);
        User user = new User();

        for (LdapAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case "cloudIdeAccounts":
                    for (String str : attribute.getValues()) {
                        user.addAccount(new ItemReference(str));
                    }
                    break;
                case "cloudIdeAliases":
                    for (String str : attribute.getValues()) {
                        user.addAlias(str);
                    }
                    break;
                case "cloudIdeUserPassword":
                    user.setPassword(attribute.getValue());
                    break;
                case "cloudIdeMemberships":
                    String[] pairValue = attribute.getKeyValue();
                    if (user.getMembership(pairValue[1]) == null) {
                        user.addMembership(pairValue[1]);
                    }
                    user.addMembershipRole(pairValue[0], pairValue[1]);
                    break;
                case "cloudIdeAttributes":
                    pairValue = attribute.getKeyValue();
                    if (pairValue[1].getBytes().length * 2 / 1024 > 16) {
                        LOG.warn(String.format("User %s has ssh key which size is out of 16kb, so key was deleted",
                                               user.getId()));
                    } else {
                        user.getProfile().setAttribute(pairValue[0], pairValue[1]);
                    }
                    break;
                case "ou":
                    user.setId(attribute.getValue());
                    break;
                default:
                    LOG.warn(String.format("Attribute setter for %s parameter is not found", attribute));
            }
        }

        return user;
    }

    @Override
    public boolean isSuitableClass(String objectClass) {
        return SUITABLE_CLASS.equals(objectClass);
    }

}
