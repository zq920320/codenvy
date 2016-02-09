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
package com.codenvy.plugin.contribution.client.utils;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.Factory;

import javax.validation.constraints.NotNull;

/**
 * Helper providing methods to work with factory.
 *
 * @author Kevin Pollet
 */
public final class FactoryHelper {
    private static final String CREATE_PROJECT_LINK_RELATION_NAME = "create-project";

    /**
     * Disable instantiation.
     */
    private FactoryHelper() {
    }

    /**
     * Returns the create project relation link for the given factory.
     *
     * @param factory
     *         the factory.
     * @return the create project url or {@code null} if none.
     */
    public static String getCreateProjectRelUrl(@NotNull Factory factory) {
        for (final Link oneLink : factory.getLinks()) {
            if (CREATE_PROJECT_LINK_RELATION_NAME.equals(oneLink.getRel())) {
                return oneLink.getHref();
            }
        }
        return null;
    }
}
