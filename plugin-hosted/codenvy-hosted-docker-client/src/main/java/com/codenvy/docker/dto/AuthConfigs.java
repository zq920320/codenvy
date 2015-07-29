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

import java.util.Map;

/**
 * Implementation of docker model ConfigFile object
 *
 * @author Max Shaposhnik
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L37">source</a>
 */
@DTO
public interface AuthConfigs {

    Map<String, AuthConfig> getConfigs();

    void setConfigs(Map<String, AuthConfig> configs);

    AuthConfigs withConfigs(Map<String, AuthConfig> configs);
}
