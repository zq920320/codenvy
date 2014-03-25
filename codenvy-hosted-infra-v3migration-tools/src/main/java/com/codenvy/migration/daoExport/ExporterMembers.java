package com.codenvy.migration.daoExport;

import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.user.server.exception.MembershipException;
import com.codenvy.api.user.shared.dto.Member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExporterMembers implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ExporterLinkedObject.class);

    private CountDownLatch doneSignal;
    private DaoManager     daoManager;
    private List<com.codenvy.api.user.shared.dto.Member>         workspaceMembers    = new ArrayList<>();
    private List<com.codenvy.api.organization.shared.dto.Member> organizationMembers = new ArrayList<>();

    public ExporterMembers(CountDownLatch doneSignal, DaoManager daoManager,
                           List<Member> wsMembers,
                           List<com.codenvy.api.organization.shared.dto.Member> accMembers) {
        this.doneSignal = doneSignal;
        this.daoManager = daoManager;
        this.workspaceMembers = wsMembers;
        this.organizationMembers = accMembers;
    }

    @Override
    public void run() {
        for (com.codenvy.api.user.shared.dto.Member wsMember : workspaceMembers) {
            try {
                daoManager.addWorkspaceMember(wsMember);
            } catch (MembershipException e) {
                LOG.error(String.format("Error adding role user %s in workspace %s", wsMember.getUserId(),
                                        wsMember.getWorkspaceId()), e);
            }
        }

        for (com.codenvy.api.organization.shared.dto.Member organizationMember : organizationMembers) {
            try {
                daoManager.addOrganizationMember(organizationMember);
            } catch (OrganizationException e) {
                LOG.error(String.format("Error adding role user %s in organization %s", organizationMember.getUserId(),
                                        organizationMember.getOrganizationId()), e);
            }
        }

        doneSignal.countDown();
    }

}
