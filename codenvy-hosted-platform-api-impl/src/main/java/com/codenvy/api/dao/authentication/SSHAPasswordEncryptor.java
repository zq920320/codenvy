/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.api.dao.authentication;

import org.apache.commons.codec.binary.Base64;

import javax.inject.Singleton;
import javax.naming.NamingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SSHA password encryptor. Returns prefixed and hashed string in LDAP appropriate format.
 */
@Singleton
public class SSHAPasswordEncryptor implements PasswordEncryptor {

    final   String       SSHA_PREFIX = "{SSHA}";
    private SecureRandom random      = new SecureRandom();

    public String encryptPassword(byte[] password) throws NamingException {
        String ssha;
        byte[] salt = new byte[6];
        random.nextBytes(salt);
        try {
            byte[] buff = new byte[password.length + salt.length];
            System.arraycopy(password, 0, buff, 0, password.length);
            System.arraycopy(salt, 0, buff, password.length, salt.length);

            MessageDigest md = MessageDigest.getInstance("SHA");
            md.reset();
            byte[] hash = md.digest(buff);
            byte[] res = new byte[20 + salt.length];
            System.arraycopy(hash, 0, res, 0, 20);
            System.arraycopy(salt, 0, res, 20, salt.length);
            ssha = SSHA_PREFIX + Base64.encodeBase64String(res);
        } catch (NoSuchAlgorithmException e) {
            throw new NamingException(e.getLocalizedMessage());
        }
        return ssha;
    }
}
