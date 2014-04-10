package com.codenvy.migration.converter;

import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.organization.model.Account;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionConverter implements ObjectConverter<Account, Subscription> {
    public static final String START_TIME     = "tariff_start_time";
    public static final String END_TIME       = "tariff_end_time";
    public static final String TRANSACTION_ID = "tariff_transactionid";
    public static final String NAME_TARIFF    = "tariff_plan";

    static final String FACTORY_TARIFF_ID = "TrackedFactory";
    static final String PREMIUM_TARIFF_ID = "PremiumWorkspace";

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
        Map<String, String> properties = new HashMap<>(1);
        properties.put("transactionid", account.getAttribute(TRANSACTION_ID));

        return DtoFactory.getInstance().createDto(Subscription.class)
                         .withId(NameGenerator.generate(Subscription.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH))
                         .withStartDate(Long.valueOf(account.getAttribute(START_TIME)))
                         .withEndDate(Long.valueOf(account.getAttribute(END_TIME)))
                         .withServiceId(tariffPlans.get(account.getAttribute(NAME_TARIFF)))
                         .withAccountId(account.getId())
                         .withProperties(properties);
    }

    public boolean accountHasSubscription(Account account) {
        return account.getAttribute(NAME_TARIFF) != null;
    }
}