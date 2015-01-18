/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.auth.sso.server;


import com.codenvy.commons.user.User;

import java.util.Collection;
import java.util.Collections;

/**
 * This class represents http transport object.
 * Allows to send com.codenvy.commons.user.User between sso server and sso client.
 *
 * @author Sergii Kabashniuk
 */
public class SsoUser implements User {

    private String             name;
    private String             id;
    private String             token;
    private boolean            isTemporary;
    private Collection<String> roles;

    public SsoUser(String name, String id, String token, Collection<String> roles, boolean isTemporary) {

        this.name = name;
        this.id = id;
        this.token = token;
        this.roles = roles;
        this.isTemporary = isTemporary;
    }


    public SsoUser(User user, Collection<String> roles) {
        this(user.getName(), user.getId(), user.getToken(), roles, user.isTemporary());
    }


    public SsoUser() {
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isMemberOf(String role) {
        return roles.contains(role);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    public String getToken() {
        return token;
    }

    public Collection<String> getRoles() {
        return Collections.unmodifiableCollection(roles);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SsoUser ssoUser = (SsoUser)o;

        if (id != null ? !id.equals(ssoUser.id) : ssoUser.id != null) return false;
        if (name != null ? !name.equals(ssoUser.name) : ssoUser.name != null) return false;
        if (roles != null ? !roles.equals(ssoUser.roles) : ssoUser.roles != null) return false;
        if (token != null ? !token.equals(ssoUser.token) : ssoUser.token != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }
}
