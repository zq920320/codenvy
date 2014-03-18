package com.codenvy.migration;

import com.codenvy.organization.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks the integrity of the data and corrects violations
 *
 * @author Sergiy Leschenko
 */
public class IntegrityChecker {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrityChecker.class);

    private MemoryStorage memoryStorage;

    public IntegrityChecker(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
    }

    public void check() {
        for (User user : memoryStorage.getUsers().values()) {
            checkUser(user);
        }

        Set<Account> accountsForDeletion = new HashSet<>();
        for (Account account : memoryStorage.getAccounts().values()) {
            try {
                checkAccount(account);
            } catch (Exception e) {
                accountsForDeletion.add(account);
                LOG.error(e.getMessage(), e);
            }
        }
        memoryStorage.getAccounts().values().removeAll(accountsForDeletion);

        Set<Workspace> workspacesForDeletion = new HashSet<>();
        for (Workspace workspace : memoryStorage.getWorkspaces().values()) {
            try {
                checkWorkspace(workspace);
            } catch (Exception e) {
                workspacesForDeletion.add(workspace);
                LOG.error(e.getMessage(), e);
            }
        }
        memoryStorage.getWorkspaces().values().removeAll(workspacesForDeletion);

        conversionAccounts();
    }

    private void checkUser(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            LOG.warn(String.format("User has null or empty id"), user);
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            LOG.warn(String.format("User %s has null or empty password", user.getId()));
        }

        if (user.getAliases().isEmpty()) {
            LOG.warn(String.format("User %s doesn't have an alias", user.getId()));
        }

        Set<ItemReference> accounts = user.getAccounts();
        for (ItemReference account : accounts) {
            if (!memoryStorage.getAccounts().containsKey(account.getId())) {
                user.removeAccount(account);
                LOG.warn(String.format("User %s has link to non-existent account %s and was deleted from there", user.getId(),
                                       account.getId()));
            }
        }

        Set<Membership> memberships = user.getMemberships();
        for (Membership membership : memberships) {
            if (!memoryStorage.getWorkspaces().containsKey(membership.getWorkspace().getId())) {
                user.removeMembership(membership);
                LOG.warn(String.format("User %s has link to non-existent workspace %s and was deleted from there", user.getId(),
                                       membership.getWorkspace().getId()));
            }
        }
    }

    private void checkAccount(Account account) throws Exception {
        if (account.getId() == null || account.getId().isEmpty()) {
            LOG.warn("Account has null or empty id", account);
        }

        if (account.getName() == null || account.getName().isEmpty()) {
            LOG.warn(String.format("Account %s has null or empty name", account.getId()));
        }

        if (account.getOwner() == null || !memoryStorage.getUsers().containsKey(account.getOwner().getId())) {
            throw new Exception(
                    String.format("Account %s doesn't have owner or has non-existent owner and it was deleted", account.getId()));
        }

        for (ItemReference workspace : account.getWorkspaces()) {
            if (!memoryStorage.getWorkspaces().containsKey(workspace.getId())) {
                account.removeWorkspace(workspace);
                LOG.warn(String.format("Account %s has link on non-existent workspace %s was deleted from there", account.getId(),
                                       workspace.getId()));
            }
        }
    }

    private void checkWorkspace(Workspace workspace) throws Exception {
        if (workspace.getId() == null || workspace.getId().isEmpty()) {
            LOG.warn("Workspace has null or empty id", workspace);
        }

        if (workspace.getName() == null || workspace.getName().isEmpty()) {
            LOG.warn(String.format("Workspace %s has null or empty name", workspace.getId()));
        }

        for (Member member : workspace.getMembers()) {
            if (!memoryStorage.getUsers().containsKey(member.getUser().getId())) {
                workspace.removeMember(member);
                LOG.warn(String.format("Workspace %s has link to non-existent member %s and was deleted from there", workspace.getId(),
                                       member.getUser().getId()));
            }
        }

        if (workspace.getOwner() == null || !memoryStorage.getAccounts().containsKey(workspace.getOwner().getId())) {
            if (workspace.getMembers().isEmpty()) {
                throw new Exception((String.format("Workspace %s doesn't have owner or has non-existent owner and it was deleted because " +
                                                   "has empty member list", workspace.getId())));
            } else {
                LOG.warn(String.format("Workspace %s doesn't have owner or has non-existent owner and it doesn't deleted because " +
                                       "has not empty member list", workspace.getId()));
            }

        }

        if (workspace.getMembers().isEmpty()) {
            Set<Member> members = new HashSet<>();
            User userOwner = memoryStorage.getUserById(memoryStorage.getAccountById(workspace.getOwner().getId()).getOwner().getId());
            Member member = new Member();
            member.setRoles(new HashSet<>(Arrays.asList(
                    new Role("admin"),
                    new Role("developer")
                                                       )
            ));
            member.setUser(new ItemReference(userOwner.getId()));
            members.add(member);

            workspace.setMembers(members);
            LOG.warn(String.format("Workspace %s with empty members list and roles were added using account-owner", workspace.getId()));
        }
    }

    /** Convert multiple accounts associated with a user to one account */
    private void conversionAccounts() {
        for (User user : memoryStorage.getUsers().values()) {
            List<Account> accounts = new ArrayList<>();
            List<Workspace> workspaces = new ArrayList<>();

            for (ItemReference accountLink : user.getAccounts()) {
                Account account = memoryStorage.getAccountById(accountLink.getId());
                accounts.add(account);
                for (ItemReference workspaceLink : account.getWorkspaces()) {
                    workspaces.add(memoryStorage.getWorkspaceById(workspaceLink.getId()));
                }
            }
            Account accountForExport = null;
            if (!accounts.isEmpty()) {
                Map<String, String> tariffs = new HashMap<>();

                for (Account account : accounts) {
                    String tariff;
                    if ((tariff = account.getAttribute("tariff_plan")) != null) {
                        tariffs.put(account.getId(), tariff);
                    }
                }

                if (tariffs.isEmpty()) {
                    accountForExport = accounts.get(0);
                } else {
                    for (Map.Entry<String, String> entry : tariffs.entrySet()) {
                        if (entry.getValue().equals("Managed Factory")) {
                            accountForExport = memoryStorage.getAccountById(entry.getKey());
                            break;
                        } else if (entry.getValue().contains("Premium")) {
                            accountForExport = memoryStorage.getAccountById(entry.getKey());
                            break;
                        }
                    }
                }

                Set<ItemReference> accWorkspaces = new HashSet<>();
                for (Workspace workspace : workspaces) {
                    workspace.setOwner(new ItemReference(accountForExport.getId()));
                    accWorkspaces.add(new ItemReference(workspace.getId()));
                }
                accountForExport.setWorkspaces(accWorkspaces);
                user.setAccounts(new HashSet<>(Arrays.asList(new ItemReference(accountForExport.getId()))));

                for (Account account : accounts) {
                    if (!account.getId().equals(accountForExport.getId())) {
                        memoryStorage.getAccounts().remove(account.getId());
                    }
                }
            }
        }
    }
}
