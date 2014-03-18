package com.codenvy.migration.convertor;

import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.organization.model.Account;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionConverter implements ObjectConverter<Account, Subscription> {
    public static final String START_TIME = "tariff_start_time";
    public static final String END_TIME = "tariff_end_time";
    public static final String TRANSACTION_ID = "tariff_transactionid";
    public static final String NAME_TARIFF = "tariff_plan";

    static final String FACTORY_TARIFF_ID = "Managed Factory";
    static final String PREMIUM_TARIFF_ID = "Premium 1.0";

    static final Map<String, String> tariffPlans = new HashMap<>();

    public SubscriptionConverter() {
        tariffPlans.put("Managed Factory", FACTORY_TARIFF_ID);
        tariffPlans.put("Tracked Factory", FACTORY_TARIFF_ID);
        tariffPlans.put("Managed Factory for Tyler", FACTORY_TARIFF_ID);
        tariffPlans.put("Premium Classroom Account", PREMIUM_TARIFF_ID);
        tariffPlans.put("Personal Premium", PREMIUM_TARIFF_ID);
        tariffPlans.put("New Super Plan", PREMIUM_TARIFF_ID);
    }

    @Override
    public Subscription convert(Account account) {
        Map<String, String> properties = new HashMap<>();
        properties.put(TRANSACTION_ID, account.getAttribute(TRANSACTION_ID));

        return DtoFactory.getInstance().createDto(Subscription.class)
                         .withStartDate(account.getAttribute(START_TIME))
                         .withEndDate(account.getAttribute(END_TIME))
                         .withServiceId(tariffPlans.get(account.getAttribute(NAME_TARIFF)))
                         .withProperties(properties);
    }

    public boolean accountHasSubscription(Account account) {
        return account.getAttribute(NAME_TARIFF) != null;
    }
}