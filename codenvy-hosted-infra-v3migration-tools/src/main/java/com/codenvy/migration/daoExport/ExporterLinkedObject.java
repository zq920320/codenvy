package com.codenvy.migration.daoExport;


import com.codenvy.api.account.shared.dto.Account;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.shared.dto.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExporterLinkedObject implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ExporterLinkedObject.class);

    private CountDownLatch  doneSignal;
    private DaoManager      daoManager;
    private User            user;
    private Profile         profile;
    private Account         account;
    private Subscription    subscription;
    private List<Workspace> workspaces;

    public ExporterLinkedObject(CountDownLatch doneSignal, DaoManager daoManager, User user, Profile profile, Account account,
                                Subscription subscription,
                                List<Workspace> workspaces) {
        this.workspaces = workspaces;
        this.account = account;
        this.user = user;
        this.profile = profile;
        this.daoManager = daoManager;
        this.subscription = subscription;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        try {
            daoManager.addUser(user);
            daoManager.addProfile(profile);

            if (account != null) {
                try {
                    daoManager.addAccount(account);
                } catch (ApiException e) {
                    LOG.error("Error exporting organization " + account.getId(), e);
                }
                if (subscription != null) {
                    try {
                        daoManager.addAccountSubscription(subscription);
                    } catch (ApiException e) {
                        LOG.error("Error exporting subscription " + account.getId(), e);
                    }
                }
            }

            for (Workspace workspace : workspaces) {
                try {
                    daoManager.addWorkspace(workspace);
                } catch (ApiException e) {
                    LOG.error("Error exporting workspace " + workspace.getId(), e);
                }
            }
        } catch (ApiException e) {
            LOG.error("Error exporting user " + user.getId(), e);
        }

        doneSignal.countDown();
    }

}
