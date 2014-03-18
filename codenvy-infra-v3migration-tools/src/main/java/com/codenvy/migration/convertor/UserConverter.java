package com.codenvy.migration.convertor;

import com.codenvy.api.user.shared.dto.User;
import com.codenvy.dto.server.DtoFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserConverter implements ObjectConverter<com.codenvy.organization.model.User,
        User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserConverter.class);

    private Base64 coder = new Base64();

    @Override
    public User convert(com.codenvy.organization.model.User user) {
        User result = DtoFactory.getInstance().createDto(User.class);

        result.setId(user.getId());

        try {
            result.setPassword("{MD5}" + new String(coder.encode(hexStringToByteArray(user.getPassword()))));
        } catch (Exception e) {
            String password = RandomStringUtils.random(12);

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e1) {
                LOG.error("Error hashing password", e1);
            }

            byte[] thedigest = md.digest(password.getBytes());

            result.setPassword(new String(coder.encode(thedigest)));
            LOG.warn(String.format("User %s has invalid password %s and password was generated automatically", user.getId(),
                                   user.getPassword()));
        }

        Iterator<String> iteratorAlias = user.getAliases().iterator();

        result.setEmail(iteratorAlias.next());

        List<String> aliases = new ArrayList<>();

        while (iteratorAlias.hasNext()) {
            aliases.add(iteratorAlias.next());
        }

        result.setAliases(aliases);

        return result;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
