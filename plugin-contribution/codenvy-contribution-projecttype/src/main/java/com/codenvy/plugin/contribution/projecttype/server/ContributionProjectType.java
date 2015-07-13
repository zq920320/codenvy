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
package com.codenvy.plugin.contribution.projecttype.server;

import org.eclipse.che.api.project.server.type.ProjectType;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_MODE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.PULL_REQUEST_ID_VARIABLE_NAME;

/**
 * The contribution project type definition.
 *
 * @author Kevin Pollet
 */
@Singleton
public class ContributionProjectType extends ProjectType {
    @Inject
    public ContributionProjectType() {
        super(CONTRIBUTION_PROJECT_TYPE_ID, CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME, false, true);

        addVariableDefinition(CONTRIBUTE_VARIABLE_NAME, "Contribution flag", false);
        addVariableDefinition(CONTRIBUTE_MODE_VARIABLE_NAME, "Contribution mode", true);
        addVariableDefinition(CONTRIBUTE_BRANCH_VARIABLE_NAME, "Branch where contribution commits has to be pulled", true);
        addVariableDefinition(PULL_REQUEST_ID_VARIABLE_NAME, "ID of the pull request being reviewed", false);
    }
}
