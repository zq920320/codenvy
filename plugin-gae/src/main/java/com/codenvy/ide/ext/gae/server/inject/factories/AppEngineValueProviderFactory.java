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

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ValueProvider;
import com.codenvy.ide.ext.gae.server.projecttype.valueprovider.AppEngineValueProvider;

import javax.annotation.Nonnull;

/**
 * Factory that uses for creating value provider projects.
 *
 * @author Dmitry Shnurenko
 */
public interface AppEngineValueProviderFactory {
    /**
     * Creates an instance of {@link AppEngineValueProvider} for given project.
     *
     * @param projectFolder
     *         project from which need to get app engine value
     * @return an instance of  {@link AppEngineValueProvider}
     */
    @Nonnull
    ValueProvider createAppEngineValueProvider(@Nonnull FolderEntry projectFolder);
}