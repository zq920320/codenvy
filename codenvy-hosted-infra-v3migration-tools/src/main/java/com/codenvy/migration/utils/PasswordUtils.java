package com.codenvy.migration.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordUtils.class);

    public static final int LENGTH_GENERATED_PASSWORD = 15;

    public static String convertToLdapMD5Format(byte[] md5HashedPassword) {
        return "{MD5}" + new String(new Base64().encode(md5HashedPassword));
    }

    public static byte[] generateAndMD5HashPassword() {
        String password = RandomStringUtils.random(LENGTH_GENERATED_PASSWORD, true, true);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            LOG.error("Error hashing password", e1);
        }

        return md.digest(password.getBytes());
    }
}
