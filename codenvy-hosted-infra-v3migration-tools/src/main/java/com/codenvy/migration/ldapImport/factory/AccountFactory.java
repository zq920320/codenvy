package com.codenvy.migration.ldapImport.factory;

import com.codenvy.migration.ldapImport.LdapAttribute;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.ItemReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountFactory extends ObjectFactory<Account> {
    private static final Logger LOG = LoggerFactory.getLogger(AccountFactory.class);

    private static final String SUITABLE_CLASS = "cloudIdeAccount";

    private static final Set<String> SUITABLE_PARAMS = new HashSet<>(Arrays.asList(
            "cloudIdeWorkspaces",
            "cloudIdeEntityOwner",
            "cloudIdeName",
            "ou",
            "cloudIdeAttributes")
    );

    @Override
    protected boolean isSuitableAttribute(String name) {
        return SUITABLE_PARAMS.contains(name);
    }

    @Override
    public Account create(List<String> item) throws Exception {
        List<LdapAttribute> attributes = getAttributes(item);
        Account account = new Account();

        for (LdapAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case "cloudIdeWorkspaces":
                    for (String str : attribute.getValues()) {
                        account.addWorkspace(str);
                    }
                    break;
                case "cloudIdeEntityOwner":
                    account.setOwner(new ItemReference(attribute.getValue()));
                    break;
                case "cloudIdeName":
                    account.setName(attribute.getValue());
                    break;
                case "ou":
                    account.setId(attribute.getValue());
                    break;
                case "cloudIdeAttributes":
                    String[] pairStr = attribute.getKeyValue();
                    account.setAttribute(pairStr[0], pairStr[1]);
                    break;
                default:
                    LOG.warn(String.format("Attribute setter for %s parameter is not found", attribute));
            }
        }

        return account;
    }

    @Override
    public boolean isSuitableClass(String objectClass) {
        return SUITABLE_CLASS.equals(objectClass);
    }
}
