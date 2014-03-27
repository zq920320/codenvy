package com.codenvy.migration.daoExport;

import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.api.user.server.exception.UserException;
import com.codenvy.api.user.server.exception.UserProfileException;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
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
    private Organization    organization;
    private Subscription    subscription;
    private List<Workspace> workspaces;

    public ExporterLinkedObject(CountDownLatch doneSignal, DaoManager daoManager, User user, Profile profile, Organization organization,
                                Subscription subscription,
                                List<Workspace> workspaces) {
        this.workspaces = workspaces;
        this.organization = organization;
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

            if (organization != null) {
                try {
                    daoManager.addOrganization(organization);
                } catch (OrganizationException e) {
                    LOG.error("Error exporting organization " + organization.getId(), e);
                }
                if (subscription != null) {
                    try {
                        daoManager.addOrganizationSubcription(subscription);
                    } catch (OrganizationException e) {
                        LOG.error("Error exporting subscription " + organization.getId(), e);
                    }
                }
            }

            for (Workspace workspace : workspaces) {
                try {
                    daoManager.addWorkspace(workspace);
                } catch (WorkspaceException e) {
                    LOG.error("Error exporting workspace " + workspace.getId(), e);
                }
            }
        } catch (UserException e) {
            LOG.error("Error exporting user " + user.getId(), e);
        } catch (UserProfileException e) {
            LOG.error("Error exporting user's profile " + user.getId(), e);
        }

        doneSignal.countDown();
    }

}
