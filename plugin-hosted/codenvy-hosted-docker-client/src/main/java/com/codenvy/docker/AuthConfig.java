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
package com.codenvy.docker;

/**
 * @author andrew00x
 */
public class AuthConfig {
    private String serveraddress;
    private String username;
    private String password;

    public AuthConfig(String serveraddress, String username, String password) {
        this.serveraddress = serveraddress;
        this.username = username;
        this.password = password;
    }

    public AuthConfig(AuthConfig other) {
        this.serveraddress = other.serveraddress;
        this.username = other.username;
        this.password = other.password;
    }

    public AuthConfig() {
    }

    public String getServeraddress() {
        return serveraddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setServeraddress(String serveraddress) {
        this.serveraddress = serveraddress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
