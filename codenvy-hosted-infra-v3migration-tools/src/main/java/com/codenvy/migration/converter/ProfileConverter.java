package com.codenvy.migration.converter;

import com.codenvy.api.user.shared.dto.Attribute;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.api.user.shared.dto.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileConverter implements ObjectConverter<com.codenvy.organization.model.Profile,
        Profile> {
    @Override
    public Profile convert(com.codenvy.organization.model.Profile profile) {
        List<Attribute> attributes = new ArrayList<>();
        Map<String, String> preferences = new HashMap<>();
        for (Map.Entry<String, String> entry : profile.getAttributes().entrySet()) {
            if (entry.getKey().startsWith("ssh.key") || entry.getKey().startsWith("paas.credential")) {
                preferences.put(entry.getKey(), entry.getValue());
            } else {
                Attribute attribute = DtoFactory.getInstance().createDto(Attribute.class);
                attribute.setName(entry.getKey());
                attribute.setValue(entry.getValue());
                attributes.add(attribute);
            }
        }

        return DtoFactory.getInstance().createDto(Profile.class)
                         .withAttributes(attributes)
                         .withPreferences(preferences);
    }
}

