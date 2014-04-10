package com.codenvy.migration.converter;

import com.codenvy.api.account.shared.dto.Attribute;
import com.codenvy.organization.model.Account;
import com.codenvy.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountConverter implements ObjectConverter<Account, com.codenvy.api.account.shared.dto.Account> {
    @Override
    public com.codenvy.api.account.shared.dto.Account convert(Account accountOld) {
        List<Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, String> entry : accountOld.getAttributes().entrySet()) {
            attributes.add(DtoFactory.getInstance().createDto(Attribute.class)
                                     .withName(entry.getKey())
                                     .withValue(entry.getValue()));
        }

        return DtoFactory.getInstance().createDto(com.codenvy.api.account.shared.dto.Account.class)
                         .withId(accountOld.getId())
                         .withName(accountOld.getName())
                         .withOwner(accountOld.getOwner().getId())
                         .withAttributes(attributes);
    }
}
