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
package com.codenvy.ide.ext.gae.server.projecttype;


import org.eclipse.che.api.project.server.type.ProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.valueprovider.AppEngineWebXmlValueProviderFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class GaeProjectType extends ProjectType {

    @Inject
    public GaeProjectType(AppEngineWebXmlValueProviderFactory appEngineValueProvider) {
        super("gae", "gae", false, true);
        addVariableDefinition(APPLICATION_ID, "appid", false, appEngineValueProvider);
    }

}
