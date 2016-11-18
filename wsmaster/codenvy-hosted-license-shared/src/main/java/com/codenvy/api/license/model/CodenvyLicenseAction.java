/*
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
package com.codenvy.api.license.model;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.Map;

/**
 * Identifiers any action with licensed happened in the system.
 *
 * @author Anatolii Bazko
 */
public interface CodenvyLicenseAction {
    /**
     * Returns Codenvy license type.
     */
    Constants.TYPE getLicenseType();

    /**
     * Returns Codenvy action type. It explains what happened with a license.
     */
    Constants.Action getActionType();

    /**
     * Returns time when action is happened.
     */
    long getActionTimestamp();

    /**
     * Any license identifier.
     */
    @Nullable
    String getLicenseId();

    /**
     * Returns any action attributes.
     */
    Map<String, String> getAttributes();
}
