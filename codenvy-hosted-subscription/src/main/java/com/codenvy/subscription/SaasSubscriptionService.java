/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.subscription;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service provide functionality of Saas subscription.
 *
 * @author Sergii Kabashniuk
 * @author Sergii Leschenko
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class SaasSubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(SaasSubscriptionService.class);

    private final WorkspaceDao workspaceDao;
    private final AccountDao   accountDao;

    @Inject
    public SaasSubscriptionService(WorkspaceDao workspaceDao, AccountDao accountDao) {
        super("Saas", "Saas");
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
    }

    /**
     * @param subscription
     *         new subscription
     * @throws com.codenvy.api.core.ConflictException
     *         if subscription state is not valid
     * @throws com.codenvy.api.core.ServerException
     *         if internal error occurs
     */
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties required");
        }
        if (subscription.getProperties().get("Package") == null) {
            throw new ConflictException("Subscription property 'Package' required");
        }
        if (subscription.getProperties().get("RAM") == null) {
            throw new ConflictException("Subscription property 'RAM' required");
        }

        final List<Subscription> allSubscriptions;
        try {
            allSubscriptions = accountDao.getSubscriptions(subscription.getAccountId());
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        for (Subscription current : allSubscriptions) {
            if (getServiceId().equals(current.getServiceId())) {
                throw new ConflictException("Subscriptions limit exhausted");
            }
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        addWorkspaceAttributes(subscription);
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        removeWorkspaceAttributes(subscription);
    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        addWorkspaceAttributes(subscription);
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
        addWorkspaceAttributes(newSubscription);
    }

    private void addWorkspaceAttributes(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ConflictException("Subscription properties required");
        }
        List<Workspace> workspaces = workspaceDao.getByAccount(subscription.getAccountId());
        if (!workspaces.isEmpty()) {
            final String tariffPackage = ensureExistsAndGet("Package", subscription);
            final Workspace workspace = workspaces.iterator().next();
            final Map<String, String> wsAttributes = workspace.getAttributes();
            switch (tariffPackage.toLowerCase()) {
                case "developer":
                case "team":
                    //1 hour
                    wsAttributes.put("codenvy:runner_lifetime", String.valueOf(TimeUnit.HOURS.toSeconds(1)));
                    wsAttributes.put("codenvy:builder_execution_time", String.valueOf(TimeUnit.MINUTES.toSeconds(10)));
                    break;
                case "project":
                case "enterprise":
                    //unlimited
                    wsAttributes.put("codenvy:runner_lifetime", "-1");
                    wsAttributes.put("codenvy:builder_execution_time", String.valueOf(TimeUnit.MINUTES.toSeconds(10)));
                    break;
                default:
                    throw new NotFoundException(String.format("Package %s not found", tariffPackage));
            }
            wsAttributes.put("codenvy:runner_ram", String.valueOf(convert(ensureExistsAndGet("RAM", subscription))));
            workspaceDao.update(workspace);
        } else {
            throw new ConflictException("Given account don't have any workspaces.");
        }
    }

    private String ensureExistsAndGet(String propertyName, Subscription src) throws ConflictException {
        final String target = src.getProperties().get(propertyName);
        if (target == null) {
            throw new ConflictException(String.format("Subscription property %s required", propertyName));
        }
        return target;
    }

    private void removeWorkspaceAttributes(Subscription subscription) throws NotFoundException, ServerException, ConflictException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ServerException("Subscription properties required");
        }
        List<Workspace> workspaces = workspaceDao.getByAccount(subscription.getAccountId());
        if (!workspaces.isEmpty()) {
            for (Workspace workspace : workspaces) {
                final Map<String, String> wsAttributes = workspace.getAttributes();
                wsAttributes.remove("codenvy:runner_ram");
                wsAttributes.remove("codenvy:runner_lifetime");
                wsAttributes.remove("codenvy:builder_execution_time");
                workspaceDao.update(workspace);
            }
        } else {
            throw new ConflictException("Given account don't have any workspaces.");
        }
    }

    /**
     * Converts String RAM with suffix GB to int RAM in MB
     * e.g.
     * "1GB" -> 1024
     *
     * @param RAM
     *         string RAM in GB
     * @return int RAM in MB
     */
    private int convert(String RAM) throws ConflictException {
        try {
            int ramGb = Integer.parseInt(RAM.substring(0, RAM.length() - 2));
            return 1024 * ramGb;
        } catch (NumberFormatException nfEx) {
            throw new ConflictException("Bad RAM value");
        }
    }
}
