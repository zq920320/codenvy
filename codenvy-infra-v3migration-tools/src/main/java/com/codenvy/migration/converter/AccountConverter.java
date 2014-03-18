package com.codenvy.migration.converter;

import com.codenvy.api.organization.shared.dto.Attribute;
import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.organization.model.Account;
import com.codenvy.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountConverter implements ObjectConverter<Account, Organization> {
    @Override
    public Organization convert(com.codenvy.organization.model.Account accountOld) {
        List<Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, String> entry : accountOld.getAttributes().entrySet()) {
            attributes.add(DtoFactory.getInstance().createDto(Attribute.class)
                                     .withName(entry.getKey())
                                     .withValue(entry.getValue()));
        }

        return DtoFactory.getInstance().createDto(Organization.class)
                         .withId(accountOld.getId())
                         .withName(accountOld.getName())
                         .withOwner(accountOld.getOwner().getId())
                         .withAttributes(attributes);
    }
}
