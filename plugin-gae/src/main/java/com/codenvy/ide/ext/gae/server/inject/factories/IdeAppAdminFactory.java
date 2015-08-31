/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server.inject.factories;

import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.IdeAppAdmin;

import javax.annotation.Nonnull;

import static com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;

/**
 * Factory that uses for creating app admin.
 *
 * @author Dmitry Shnurenko
 */
public interface IdeAppAdminFactory {

    /**
     * Creates an instance of {@link IdeAppAdmin} for given project.
     *
     * @param options
     *         options which need to save token
     * @param genericApplication
     *         application which is used for creating ide application admin
     * @return an instance of  {@link IdeAppAdmin}
     */
    @Nonnull
    IdeAppAdmin createIdeAppAdmin(@Nonnull ConnectOptions options, @Nonnull GenericApplication genericApplication);
}
