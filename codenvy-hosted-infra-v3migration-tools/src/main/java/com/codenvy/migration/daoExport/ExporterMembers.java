package com.codenvy.migration.daoExport;

import com.codenvy.api.core.ApiException;
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
    private List<com.codenvy.api.user.shared.dto.Member>    workspaceMembers = new ArrayList<>();
    private List<com.codenvy.api.account.shared.dto.Member> accountMembers   = new ArrayList<>();

    public ExporterMembers(CountDownLatch doneSignal, DaoManager daoManager,
                           List<Member> wsMembers,
                           List<com.codenvy.api.account.shared.dto.Member> accMembers) {
        this.doneSignal = doneSignal;
        this.daoManager = daoManager;
        this.workspaceMembers = wsMembers;
        this.accountMembers = accMembers;
    }

    @Override
    public void run() {
        for (com.codenvy.api.user.shared.dto.Member wsMember : workspaceMembers) {
            try {
                daoManager.addWorkspaceMember(wsMember);
            } catch (ApiException e) {
                LOG.error(String.format("Error adding role user %s in workspace %s", wsMember.getUserId(),
                                        wsMember.getWorkspaceId()), e);
            }
        }

        for (com.codenvy.api.account.shared.dto.Member accountMember : accountMembers) {
            try {
                daoManager.addAccountMember(accountMember);
            } catch (ApiException e) {
                LOG.error(String.format("Error adding role user %s in organization %s", accountMember.getUserId(),
                                        accountMember.getAccountId()), e);
            }
        }

        doneSignal.countDown();
    }

}
