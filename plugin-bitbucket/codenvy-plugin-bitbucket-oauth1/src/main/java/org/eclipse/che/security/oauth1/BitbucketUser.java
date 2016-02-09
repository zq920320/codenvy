/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package org.eclipse.che.security.oauth1;


import org.eclipse.che.security.oauth1.shared.User;

/**
 * Represents Bitbucket user.
 *
 * @author Kevin Pollet
 */
public class BitbucketUser implements User {
    private String username;
    private String email;

    @Override
    public final String getId() {
        return email;
    }

    @Override
    public final void setId(String id) {
        //nothing to do there is no id field in Bitbucket response
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public void setName(String name) {
        //nothing to do there is no name field in Bitbucket response
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "BitbucketUser{" +
               "id='" + getId() + '\'' +
               ", name='" + username + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
