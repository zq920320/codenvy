package com.codenvy.migration;


import com.codenvy.organization.model.AbstractOrganizationUnit;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.User;
import com.codenvy.organization.model.Workspace;

import java.util.HashMap;
import java.util.Map;

/**
 * This class has containers for objects of type user, workspace, account.
 * It allows to transfer objects from one class to another using one container.
 *
 * @author Sergiy Leschenko
 */
public class MemoryStorage {
    private final Map<String, User>      users      = new HashMap<>();
    private final Map<String, Workspace> workspaces = new HashMap<>();
    private final Map<String, Account>   accounts   = new HashMap<>();

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Workspace> getWorkspaces() {
        return workspaces;
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public User getUserById(String userId) {
        return users.get(userId);
    }

    public Workspace getWorkspaceById(String workspaceId) {
        return workspaces.get(workspaceId);
    }

    public Account getAccountById(String accountId) {
        return accounts.get(accountId);
    }

    public void add(AbstractOrganizationUnit object) throws Exception {
        if (object instanceof User) {
            User user = (User)object;
            users.put(user.getId(), user);
        } else if (object instanceof Workspace) {
            Workspace workspace = (Workspace)object;
            workspaces.put(workspace.getId(), workspace);
        } else if (object instanceof Account) {
            Account account = (Account)object;
            accounts.put(account.getId(), account);
        } else {
            throw new Exception("Type of this object is not supported: " + object.getClass());
        }
    }
}
