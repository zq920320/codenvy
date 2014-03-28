package com.codenvy.migration.converter;

import com.codenvy.api.user.shared.dto.User;
import com.codenvy.dto.server.DtoFactory;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.codenvy.migration.utils.PasswordUtils.convertToLdapMD5Format;
import static com.codenvy.migration.utils.PasswordUtils.generateAndMD5HashPassword;

public class UserConverter implements ObjectConverter<com.codenvy.organization.model.User, User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserConverter.class);

    @Override
    public User convert(com.codenvy.organization.model.User user) {
        User result = DtoFactory.getInstance().createDto(User.class);

        result.setId(user.getId());

        try {
            byte[] hexFormatPassword = Hex.decodeHex(user.getPassword().toCharArray());
            result.setPassword(convertToLdapMD5Format(hexFormatPassword));
        } catch (Exception e) {
            result.setPassword(convertToLdapMD5Format(generateAndMD5HashPassword()));
            LOG.warn(String.format("User %s has invalid password %s and password was generated automatically", user.getId(),
                                   user.getPassword()));
        }

        Set<String> allAliases = user.getAliases();
        Iterator<String> iteratorAlias = allAliases.iterator();
        result.setEmail(iteratorAlias.next());
        iteratorAlias.remove();

        if (!allAliases.isEmpty()) {
            result.setAliases(new ArrayList<>(allAliases));
        }

        return result;
    }

}
