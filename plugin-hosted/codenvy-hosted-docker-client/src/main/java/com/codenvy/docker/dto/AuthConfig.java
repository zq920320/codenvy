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
package com.codenvy.docker.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of docker AuthConfig object
 *
 * @author andrew00x
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L29">source</a>
 */
@DTO
public interface AuthConfig {
    String getServeraddress();

    void setServeraddress(String serveraddress);

    AuthConfig withServeraddress(String serveraddress);

    String getUsername();

    void setUsername(String username);

    AuthConfig withUsername(String username);

    String getPassword();

    void setPassword(String password);

    AuthConfig withPassword(String password);

    String getEmail();

    void setEmail(String email);

    AuthConfig withEmail(String email);

    String getAuth();

    void setAuth(String auth);

    AuthConfig withAuth(String auth);
}
