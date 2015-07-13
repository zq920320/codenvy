/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.utils;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.dto.Factory;

import javax.annotation.Nonnull;

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
    public static String getCreateProjectRelUrl(@Nonnull Factory factory) {
        for (final Link oneLink : factory.getLinks()) {
            if (CREATE_PROJECT_LINK_RELATION_NAME.equals(oneLink.getRel())) {
                return oneLink.getHref();
            }
        }
        return null;
    }
}
