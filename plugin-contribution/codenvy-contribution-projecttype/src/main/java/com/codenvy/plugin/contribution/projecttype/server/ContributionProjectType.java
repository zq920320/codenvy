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
package com.codenvy.plugin.contribution.projecttype.server;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

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
public class ContributionProjectType extends ProjectTypeDef {
    @Inject
    public ContributionProjectType() {
        super(CONTRIBUTION_PROJECT_TYPE_ID, CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME, false, true);

        addVariableDefinition(CONTRIBUTE_VARIABLE_NAME, "Contribution flag", false);
        addVariableDefinition(CONTRIBUTE_MODE_VARIABLE_NAME, "Contribution mode", true);
        addVariableDefinition(CONTRIBUTE_BRANCH_VARIABLE_NAME, "Branch where contribution commits has to be pulled", true);
        addVariableDefinition(PULL_REQUEST_ID_VARIABLE_NAME, "ID of the pull request being reviewed", false);
    }
}
