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
package com.codenvy.api.account.impl.shared.dto;

import com.codenvy.dto.shared.DTO;import java.lang.String;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/28/15.
 *
 */
@DTO
public interface ClientToken {
    String getToken();

    void setToken(String token);

    ClientToken withToken(String token);
}
