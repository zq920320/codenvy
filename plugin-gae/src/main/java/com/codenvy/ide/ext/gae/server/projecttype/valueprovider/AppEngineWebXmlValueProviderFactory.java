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
package com.codenvy.ide.ext.gae.server.projecttype.valueprovider;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import com.codenvy.ide.ext.gae.server.inject.factories.AppEngineValueProviderFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

/**
 * Factory allows get instance of special value provider using special name.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class AppEngineWebXmlValueProviderFactory implements ValueProviderFactory {

    private final AppEngineValueProviderFactory valueProviderFactory;

    @Inject
    public AppEngineWebXmlValueProviderFactory(AppEngineValueProviderFactory valueProviderFactory) {
        this.valueProviderFactory = valueProviderFactory;
    }

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(@NotNull FolderEntry projectFolder) {
        return valueProviderFactory.createAppEngineValueProvider(projectFolder);
    }

}
