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
package com.codenvy.service.workspace;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.dao.Workspace;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service provide functionality of Premium tariff plan.
 *
 * @author Sergii Kabashniuk
 * @author Sergii Leschenko
 * @author Eugene Voevodin
 */
@Singleton
public class HostedWorkspaceService extends SubscriptionService {

    private static final Map<TariffEntry, Double> PRICES;

    static {
        //todo mb move it to (@DynaModule or store in json) and @Inject?
        PRICES = new HashMap<>();
        //yearly prices for developer
        PRICES.put(TariffEntry.of("developer", "2gb", "yearly"), 1D);
        PRICES.put(TariffEntry.of("developer", "4gb", "yearly"), 12D);
        PRICES.put(TariffEntry.of("developer", "16gb", "yearly"), 53D);
        PRICES.put(TariffEntry.of("developer", "64gb", "yearly"), 198D);
        //yearly prices for team
        PRICES.put(TariffEntry.of("team", "2gb", "yearly"), 5D);
        PRICES.put(TariffEntry.of("team", "4gb", "yearly"), 24D);
        PRICES.put(TariffEntry.of("team", "16gb", "yearly"), 106D);
        PRICES.put(TariffEntry.of("team", "64gb", "yearly"), 396D);
        PRICES.put(TariffEntry.of("team", "128gb", "yearly"), 713D);
        PRICES.put(TariffEntry.of("team", "256gb", "yearly"), 1_283D);
        PRICES.put(TariffEntry.of("team", "1024gb", "yearly"), 4_619D);
        PRICES.put(TariffEntry.of("team", "4096gb", "yearly"), 16_628D);
        //yearly prices for project
        PRICES.put(TariffEntry.of("project", "2gb", "yearly"), 20D);
        PRICES.put(TariffEntry.of("project", "4gb", "yearly"), 35D);
        PRICES.put(TariffEntry.of("project", "16gb", "yearly"), 160D);
        PRICES.put(TariffEntry.of("project", "64gb", "yearly"), 600D);
        //yearly prices for enterprise
        PRICES.put(TariffEntry.of("enterprise", "2gb", "yearly"), 5D);
        PRICES.put(TariffEntry.of("enterprise", "4gb", "yearly"), 24D);
        PRICES.put(TariffEntry.of("enterprise", "16gb", "yearly"), 106D);
        PRICES.put(TariffEntry.of("enterprise", "64gb", "yearly"), 396D);
        PRICES.put(TariffEntry.of("enterprise", "128gb", "yearly"), 713D);
        PRICES.put(TariffEntry.of("enterprise", "256gb", "yearly"), 1_283D);
        PRICES.put(TariffEntry.of("enterprise", "1024gb", "yearly"), 4_619D);
        PRICES.put(TariffEntry.of("enterprise", "4096gb", "yearly"), 16_628D);
        PRICES.put(TariffEntry.of("developer", "2gb", "yearly"), 1D);
        PRICES.put(TariffEntry.of("developer", "4gb", "yearly"), 12D);
        PRICES.put(TariffEntry.of("developer", "16gb", "yearly"), 53D);
        PRICES.put(TariffEntry.of("developer", "64gb", "yearly"), 198D);
        //monthly prices for developer
        PRICES.put(TariffEntry.of("developer", "2gb", "monthly"), 5D);
        PRICES.put(TariffEntry.of("developer", "4gb", "monthly"), 14D);
        PRICES.put(TariffEntry.of("developer", "16gb", "monthly"), 66D);
        PRICES.put(TariffEntry.of("developer", "64gb", "monthly"), 248D);
        //monthly prices for team
        PRICES.put(TariffEntry.of("team", "2gb", "monthly"), 9D);
        PRICES.put(TariffEntry.of("team", "4gb", "monthly"), 28D);
        PRICES.put(TariffEntry.of("team", "16gb", "monthly"), 132D);
        PRICES.put(TariffEntry.of("team", "64gb", "monthly"), 495D);
        PRICES.put(TariffEntry.of("team", "128gb", "monthly"), 855D);
        PRICES.put(TariffEntry.of("team", "256gb", "monthly"), 1_540D);
        PRICES.put(TariffEntry.of("team", "1024gb", "monthly"), 5_543D);
        PRICES.put(TariffEntry.of("team", "4096gb", "monthly"), 19_954D);
        //monthly prices for project
        PRICES.put(TariffEntry.of("project", "2gb", "monthly"), 24D);
        PRICES.put(TariffEntry.of("project", "4gb", "monthly"), 42D);
        PRICES.put(TariffEntry.of("project", "16gb", "monthly"), 200D);
        PRICES.put(TariffEntry.of("project", "64gb", "monthly"), 750D);
        //monthly prices for enterprise
        PRICES.put(TariffEntry.of("enterprise", "2gb", "monthly"), 42D);
        PRICES.put(TariffEntry.of("enterprise", "4gb", "monthly"), 74D);
        PRICES.put(TariffEntry.of("enterprise", "16gb", "monthly"), 350D);
        PRICES.put(TariffEntry.of("enterprise", "64gb", "monthly"), 1_313D);
        PRICES.put(TariffEntry.of("enterprise", "128gb", "monthly"), 2_268D);
        PRICES.put(TariffEntry.of("enterprise", "256gb", "monthly"), 4_048D);
        PRICES.put(TariffEntry.of("enterprise", "1024gb", "monthly"), 14_697D);
        PRICES.put(TariffEntry.of("enterprise", "4096gb", "monthly"), 52_908D);
    }

    private final WorkspaceDao workspaceDao;
    private final AccountDao   accountDao;

    @Inject
    public HostedWorkspaceService(WorkspaceDao workspaceDao, AccountDao accountDao) {
        super("HostedWorkspace", "Hosted Workspace");
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
    }

    /**
     * <p> Validates subscription state.
     * If ACTIVE subscription already exists sets new dates for given subscription.</p>
     * The valid combinations of subscription states:
     * <ul>
     * <li>ACTIVE & ACTIVE</li>
     * <li>ACTIVE & WAIT_FOR_PAYMENT</li>
     * <li>just ACTIVE</li>
     * <li>just WAIT_FOR_PAYMENT</li>
     * </ul>
     *
     * @param subscription
     *         new subscription
     * @throws ApiException
     *         if subscription state is not valid
     */
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ApiException {
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties required");
        }
        final Double price = PRICES.get(TariffEntry.of(ensureExistsAndGet("Package", subscription).toLowerCase(),
                                                       ensureExistsAndGet("RAM", subscription).toLowerCase(),
                                                       ensureExistsAndGet("TariffPlan", subscription).toLowerCase()));
        if (price == null) {
            throw new NotFoundException("Tariff plan not found");
        }
        final String wsId = ensureExistsAndGet("codenvy:workspace_id", subscription);
        final List<Subscription> allSubscriptions = accountDao.getSubscriptions(subscription.getAccountId());
        final List<Subscription> wsSubscriptions = new LinkedList<>();
        for (Subscription current : allSubscriptions) {
            final Map<String, String> properties = current.getProperties();
            if (getServiceId().equals(current.getServiceId()) && wsId.equals(properties.get("codenvy:workspace_id"))) {
                wsSubscriptions.add(current);
            }
        }
        if (wsSubscriptions.size() == 2) {
            throw new ServerException("Subscriptions limit exhausted");
        }
        switch (subscription.getState()) {
            case WAIT_FOR_PAYMENT:
                if (search(wsSubscriptions, Subscription.State.WAIT_FOR_PAYMENT) != null) {
                    throw new ConflictException("Subscription with WAIT_FOR_PAYMENT state already exists");
                }
                break;
            case ACTIVE:
                final Subscription active = search(wsSubscriptions, Subscription.State.ACTIVE);
                if (active != null) {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(active.getEndDate());
                    subscription.setStartDate(calendar.getTimeInMillis());
                    if ("yearly".equalsIgnoreCase(subscription.getProperties().get("TariffPlan"))) {
                        calendar.add(Calendar.YEAR, 1);
                    } else {
                        calendar.add(Calendar.MONTH, 1);
                    }
                    subscription.setEndDate(calendar.getTimeInMillis());
                }
                break;
            default:
                throw new ServerException("Incorrect subscription state");
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        if (subscription.getState() == Subscription.State.ACTIVE) {
            addWorkspaceAttributes(subscription);
        }
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        removeWorkspaceAttributes(subscription);
    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        final long currentTime = System.currentTimeMillis();
        if (currentTime >= subscription.getStartDate() &&
            currentTime <= subscription.getEndDate() &&
            subscription.getState() == Subscription.State.ACTIVE) {
            addWorkspaceAttributes(subscription);
        }
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
        if (newSubscription.getState() == Subscription.State.ACTIVE) {
            addWorkspaceAttributes(newSubscription);
        } else {
            removeWorkspaceAttributes(newSubscription);
        }
    }

    @Override
    public double tarifficate(Subscription subscription) throws ApiException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ConflictException("Subscription properties required");
        }
        final String tariffPlan = ensureExistsAndGet("TariffPlan", subscription);
        final Double price = PRICES.get(TariffEntry.of(ensureExistsAndGet("Package", subscription).toLowerCase(),
                                                       ensureExistsAndGet("RAM", subscription).toLowerCase(),
                                                       tariffPlan.toLowerCase()));
        if (price == null) {
            throw new NotFoundException("Tariff not found");
        }
        return "yearly".equalsIgnoreCase(tariffPlan) ? price * 12D : price;
    }

    private void addWorkspaceAttributes(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ConflictException("Subscription properties required");
        }
        final String wsId = ensureExistsAndGet("codenvy:workspace_id", subscription);
        final String tariffPackage = ensureExistsAndGet("Package", subscription);
        final Workspace workspace = workspaceDao.getById(wsId);
        final Map<String, String> wsAttributes = workspace.getAttributes();
        switch (tariffPackage.toLowerCase()) {
            case "developer":
            case "team":
                //1 hour
                wsAttributes.put("codenvy:runner_lifetime", String.valueOf(TimeUnit.HOURS.toSeconds(1)));
                break;
            case "project":
            case "enterprise":
                //unlimited
                wsAttributes.put("codenvy:runner_lifetime", "-1");
                break;
            default:
                throw new NotFoundException(String.format("Package %s not found", tariffPackage));
        }
        wsAttributes.put("codenvy:runner_ram", String.valueOf(convert(ensureExistsAndGet("RAM", subscription))));
        workspaceDao.update(workspace);
    }

    private String ensureExistsAndGet(String propertyName, Subscription src) throws ConflictException {
        final String target = src.getProperties().get(propertyName);
        if (target == null) {
            throw new ConflictException(String.format("Subscription property %s required", propertyName));
        }
        return target;
    }

    /**
     * Searches for subscription by status
     *
     * @param src
     *         where to search
     * @param target
     *         target subscription state
     * @return found subscription object or {@code null} if subscription with given status missed in source
     */
    private Subscription search(List<Subscription> src, Subscription.State target) {
        for (Subscription current : src) {
            if (current.getState() == target) {
                return current;
            }
        }
        return null;
    }

    private void removeWorkspaceAttributes(Subscription subscription) throws NotFoundException, ServerException, ConflictException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ServerException("Subscription properties required");
        }
        final String wsId = ensureExistsAndGet("codenvy:workspace_id", subscription);
        final Workspace workspace = workspaceDao.getById(wsId);
        final Map<String, String> wsAttributes = workspace.getAttributes();
        wsAttributes.remove("codenvy:runner_ram");
        wsAttributes.remove("codenvy:runner_lifetime");
        workspaceDao.update(workspace);
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

    private static class TariffEntry {

        static TariffEntry of(String pack, String ram, String plan) {
            return new TariffEntry(pack, ram, plan);
        }

        final String pack;
        final String ram;
        final String plan;

        TariffEntry(String pack, String ram, String plan) {
            this.pack = Objects.requireNonNull(pack);
            this.ram = Objects.requireNonNull(ram);
            this.plan = Objects.requireNonNull(plan);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TariffEntry)) {
                return false;
            }
            final TariffEntry other = (TariffEntry)obj;
            return pack.equals(other.pack) &&
                   plan.equals(other.plan) &&
                   ram.equals(other.ram);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = hash * 31 + pack.hashCode();
            hash = hash * 31 + ram.hashCode();
            hash = hash * 31 + plan.hashCode();
            return hash;
        }
    }
}
