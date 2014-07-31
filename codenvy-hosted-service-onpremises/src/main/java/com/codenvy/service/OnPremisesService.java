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
package com.codenvy.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service provide functionality of On-premises subscription.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OnPremisesService extends SubscriptionService {

    private static final Map<TariffEntry, Double> PRICES;

    static {
        //todo mb move it to (@DynaModule or store in json) and @Inject?
        PRICES = new HashMap<>();

        PRICES.put(TariffEntry.of("commercial", "5"), 50D);
        PRICES.put(TariffEntry.of("commercial", "10"), 2000D);
        PRICES.put(TariffEntry.of("commercial", "25"), 5000D);
        PRICES.put(TariffEntry.of("commercial", "50"), 10000D);
        PRICES.put(TariffEntry.of("commercial", "500"), 95000D);
        PRICES.put(TariffEntry.of("commercial", "2000"), 350000D);
        PRICES.put(TariffEntry.of("commercial", "10000"), 1600000D);
        PRICES.put(TariffEntry.of("commercial", "unlimited"), 1920000D);

        PRICES.put(TariffEntry.of("academic", "5"), 25D);
        PRICES.put(TariffEntry.of("academic", "10"), 1000D);
        PRICES.put(TariffEntry.of("academic", "25"), 2500D);
        PRICES.put(TariffEntry.of("academic", "50"), 5000D);
        PRICES.put(TariffEntry.of("academic", "500"), 47500D);
        PRICES.put(TariffEntry.of("academic", "2000"), 175000D);
        PRICES.put(TariffEntry.of("academic", "10000"), 8000000D);
        PRICES.put(TariffEntry.of("academic", "unlimited"), 960000D);

        PRICES.put(TariffEntry.of("startup", "5"), 10D);
        PRICES.put(TariffEntry.of("startup", "10"), 20D);
        PRICES.put(TariffEntry.of("startup", "25"), 50D);
        PRICES.put(TariffEntry.of("startup", "50"), 100D);

        PRICES.put(TariffEntry.of("openSource", "5"), 0D);
        PRICES.put(TariffEntry.of("openSource", "10"), 0D);
        PRICES.put(TariffEntry.of("openSource", "25"), 0D);
        PRICES.put(TariffEntry.of("openSource", "50"), 0D);
        PRICES.put(TariffEntry.of("openSource", "500"), 0D);
        PRICES.put(TariffEntry.of("openSource", "2000"), 0D);
        PRICES.put(TariffEntry.of("openSource", "10000"), 0D);
        PRICES.put(TariffEntry.of("openSource", "unlimited"), 0D);
    }

    private final AccountDao accountDao;

    @Inject
    public OnPremisesService(AccountDao accountDao) {
        super("onPremises", "onPremises");
        this.accountDao = accountDao;
    }

    /**
     * @param subscription
     *         new subscription
     * @throws com.codenvy.api.core.ApiException
     *         if subscription state is not valid
     */
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ApiException {
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties required");
        }


        final Double price = PRICES.get(TariffEntry.of(ensureExistsAndGet("Package", subscription).toLowerCase(),
                                                       ensureExistsAndGet("Users", subscription).toLowerCase()));

        if (price == null) {
            throw new NotFoundException("Tariff plan not found");
        }

        for (Subscription current : accountDao.getSubscriptions(subscription.getAccountId())) {
            if (getServiceId().equals(current.getServiceId())) {
                if (current.getState().equals(Subscription.State.WAIT_FOR_PAYMENT)) {
                    throw new ConflictException("Subscription with WAIT_FOR_PAYMENT state already exists");
                } else {
                    throw new ConflictException("Subscriptions limit exhausted");
                }
            }
        }

        if ("true".equals((subscription.getProperties().get("codenvy:trial")))) {
            final Calendar calendar = Calendar.getInstance();
            subscription.setStartDate(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            subscription.setEndDate(calendar.getTimeInMillis());
            subscription.setState(Subscription.State.ACTIVE);
        } else {
            final Calendar calendar = Calendar.getInstance();
            subscription.setStartDate(calendar.getTimeInMillis());
            calendar.add(Calendar.YEAR, 1);
            subscription.setEndDate(calendar.getTimeInMillis());
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {

    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {

    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
    }

    @Override
    public double tarifficate(Subscription subscription) throws ApiException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ConflictException("Subscription properties required");
        }
        final Double price = PRICES.get(TariffEntry.of(ensureExistsAndGet("Package", subscription).toLowerCase(),
                                                       ensureExistsAndGet("Users", subscription).toLowerCase()
                                                      ));
        if (price == null) {
            throw new NotFoundException("Tariff not found");
        }
        return price;
    }

    private String ensureExistsAndGet(String propertyName, Subscription src) throws ConflictException {
        final String target = src.getProperties().get(propertyName);
        if (target == null) {
            throw new ConflictException(String.format("Subscription property %s required", propertyName));
        }
        return target;
    }

    private static class TariffEntry {

        static TariffEntry of(String pack, String ram) {
            return new TariffEntry(pack, ram);
        }

        final String pack;
        final String users;

        TariffEntry(String pack, String users) {
            this.pack = Objects.requireNonNull(pack);
            this.users = Objects.requireNonNull(users);
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
                   users.equals(other.users);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = hash * 31 + pack.hashCode();
            hash = hash * 31 + users.hashCode();
            return hash;
        }
    }
}
