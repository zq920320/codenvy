package com.codenvy.migration.daoExport;

import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.migration.MemoryStorage;
import com.codenvy.migration.converter.AccountConverter;
import com.codenvy.migration.converter.ProfileConverter;
import com.codenvy.migration.converter.SubscriptionConverter;
import com.codenvy.migration.converter.UserConverter;
import com.codenvy.migration.converter.WorkspaceConverter;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.ItemReference;
import com.codenvy.organization.model.Member;
import com.codenvy.organization.model.Role;
import com.codenvy.organization.model.User;
import com.codenvy.organization.model.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DaoExporter divides objects from {@link MemoryStorage} into independent objects, converts them using
 * {@link com.codenvy.migration.converter.ObjectConverter} and transmits to the {@link DaoManager} for exporting
 *
 * @author Sergiy Leschenko
 */
public class DaoExporter {
    private static final Logger LOG = LoggerFactory.getLogger(DaoExporter.class);

    // Constants for progress bar
    private static final int PERCENTS_RANGE = 5;
    private static final int NUMBER_PERCENT_EQUALS_ONE_DOT = 5;
    private static final int DELAY_CHECK_PROGRESS = 1000;

    private MemoryStorage memoryStorage;
    private DaoManager    daoManager;

    private UserConverter         userConvertor;
    private ProfileConverter      profileConvertor;
    private WorkspaceConverter    workspaceConvertor;
    private AccountConverter      accountConvertor;
    private SubscriptionConverter subscriptionConvertor;
    private ExecutorService       executor;
    private CountDownLatch        doneExportObjectSignal;
    private CountDownLatch        doneExportLinkSignal;

    public DaoExporter(MemoryStorage memoryStorage, DaoManager daoManager) {
        this.memoryStorage = memoryStorage;
        this.daoManager = daoManager;
        userConvertor = new UserConverter();
        profileConvertor = new ProfileConverter();
        workspaceConvertor = new WorkspaceConverter();
        accountConvertor = new AccountConverter();
        subscriptionConvertor = new SubscriptionConverter();
    }

    private List<com.codenvy.api.workspace.shared.dto.Workspace> convertListWorkspace(List<Workspace> workspaces) {
        List<com.codenvy.api.workspace.shared.dto.Workspace> resWorkspaces = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            resWorkspaces.add(workspaceConvertor.convert(workspace));
        }
        return resWorkspaces;
    }

    /** Selects sets of dependent objects and transfer them to {@link ExporterLinkedObject} for export */
    private void exportObject() {
        doneExportObjectSignal = new CountDownLatch(memoryStorage.getUsers().size());

        for (User user : memoryStorage.getUsers().values()) {
            Subscription subscription = null;
            Account account = null;
            List<Workspace> workspaces = new ArrayList<>();
            if (!user.getAccounts().isEmpty()) {
                String accountId = user.getAccounts().iterator().next().getId();
                account = memoryStorage.getAccountById(accountId);

                for (ItemReference workspaceLink : account.getWorkspaces()) {
                    workspaces.add(memoryStorage.getWorkspaceById(workspaceLink.getId()));
                }

                if (subscriptionConvertor.accountHasSubscription(account)) {
                    subscription = subscriptionConvertor.convert(account);
                    account.removeAttribute(SubscriptionConverter.START_TIME);
                    account.removeAttribute(SubscriptionConverter.END_TIME);
                    account.removeAttribute(SubscriptionConverter.TRANSACTION_ID);
                    account.removeAttribute(SubscriptionConverter.NAME_TARIFF);
                }

            }

            Profile profile = profileConvertor.convert(user.getProfile()).withUserId(user.getId()).withId(user.getId());

            executor.execute(new ExporterLinkedObject(doneExportObjectSignal, daoManager, userConvertor.convert(user), profile,
                                                      account == null ? null : accountConvertor.convert(account),
                                                      subscription,
                                                      convertListWorkspace(workspaces)));
        }
    }

    /** Method selects members which linked with user and transfer them to {@link ExporterMembers} for export */
    private void exportLink() {
        doneExportLinkSignal = new CountDownLatch(memoryStorage.getUsers().size());

        for (User user : memoryStorage.getUsers().values()) {
            List<com.codenvy.api.user.shared.dto.Member> workspaceMembers = new ArrayList<>();
            List<com.codenvy.api.organization.shared.dto.Member> organizationMembers = new ArrayList<>();
            for (ItemReference linkAccount : user.getAccounts()) {
                Account account = memoryStorage.getAccountById(linkAccount.getId());
                for (ItemReference workspaceLink : account.getWorkspaces()) {
                    Workspace workspace = memoryStorage.getWorkspaceById(workspaceLink.getId());
                    for (Member member : workspace.getMembers()) {

                        List<String> roles = new ArrayList<>();
                        for (Role role : member.getRoles()) {
                            roles.add("workspace/" + role.getName());
                        }

                        com.codenvy.api.user.shared.dto.Member workspacesMember = DtoFactory.getInstance().createDto
                                (com.codenvy.api.user.shared.dto.Member.class)
                                                        .withUserId(member.getUser().getId())
                                                        .withWorkspaceId(workspace.getId())
                                                        .withRoles(roles);
                        workspaceMembers.add(workspacesMember);

                        if (!user.getId().equals(member.getUser().getId())) {
                            com.codenvy.api.organization.shared.dto.Member organizationMember = DtoFactory.getInstance().createDto
                                    (com.codenvy.api.organization.shared.dto.Member.class)
                                         .withUserId(member.getUser().getId())
                                         .withOrganizationId(workspace.getOwner().getId())
                                         .withRoles(Arrays.asList("organization/member"));
                            organizationMembers.add(organizationMember);
                        }
                    }
                }
            }
            executor.execute(new ExporterMembers(doneExportLinkSignal, daoManager, workspaceMembers, organizationMembers));
        }
    }

    public void export() {
        executor = Executors.newFixedThreadPool(10);
        try {
            exportObject();
            LOG.info("Exporting objects...");
            try {
                printProgress(PERCENTS_RANGE, memoryStorage.getUsers().size(), doneExportObjectSignal);
            } catch (InterruptedException e) {
                LOG.error("Thread was interrupted", e);
            }
            LOG.info("Exporting of objects is done");
            exportLink();
            LOG.info("Exporting links...");
            try {
                printProgress(PERCENTS_RANGE, memoryStorage.getUsers().size(), doneExportLinkSignal);
            } catch (InterruptedException e) {
                LOG.error("Thread was interrupted", e);
            }
            LOG.info("Exporting of links is done");
        } finally {
            executor.shutdown();
        }
    }

    private static void printProgress(int percentsRange, int countOfExportObjects, CountDownLatch threadCounter) throws InterruptedException {
        LOG.info(getStatus(0));
        double percentWeight = 100d / countOfExportObjects;
        int percentsBarrier = percentsRange;
        long countOfFinishedExports;
        while ((countOfFinishedExports = threadCounter.getCount()) > 0 || percentsBarrier < 100) {
            if (percentWeight * (countOfExportObjects - countOfFinishedExports) > percentsBarrier) {
                LOG.info(getStatus(percentsBarrier));
                percentsBarrier += percentsRange;
            }
            Thread.sleep(DELAY_CHECK_PROGRESS);
        }
        LOG.info(getStatus(100));
    }

    private static String getStatus(int percents) {
        StringBuilder status = new StringBuilder();
        for (int i = 0; i < percents / NUMBER_PERCENT_EQUALS_ONE_DOT; ++i) {
            status.append(".");
        }
        status.append(percents).append("%");
        return status.toString();
    }
}
