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
package com.codenvy.ide.ext.gae.client.wizard.java;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Class contains business logic which allows create scratch of google application project or import
 * project from other location(for example github).
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class GAEJavaPagePresenter extends AbstractWizardPage<ImportProject> implements GAEJavaPageView.ActionDelegate {
    private final GAEJavaPageView         view;
    private final GAELocalizationConstant locale;
    private final GAEServiceClient        service;
    private final GAEAsyncCallbackFactory callBackFactory;
    private final GAEUtil                 gaeUtil;

    @Inject
    public GAEJavaPagePresenter(GAEJavaPageView view,
                                GAELocalizationConstant locale,
                                GAEServiceClient service,
                                GAEAsyncCallbackFactory callBackFactory,
                                GAEUtil gaeUtil) {
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
        this.service = service;
        this.callBackFactory = callBackFactory;
        this.gaeUtil = gaeUtil;
    }

    /** {@inheritDoc} */
    @Override
    public void init(ImportProject dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));

        if (CREATE == wizardMode) {
            setAttribute(VERSION, locale.wizardDefaultAppVersion());
            setAttribute(APPLICATION_ID, locale.wizardApplicationIdDefault());
        } else if (UPDATE == wizardMode) {
            String projectPath = context.get(PROJECT_PATH_KEY);
            service.readGAEMavenParameters(projectPath,
                                           callBackFactory.build(GAEMavenInfo.class, new SuccessCallback<GAEMavenInfo>() {
                                               @Override
                                               public void onSuccess(GAEMavenInfo gaeMavenInfo) {
                                                   setAttribute(ARTIFACT_ID, gaeMavenInfo.getArtifactId());
                                                   setAttribute(GROUP_ID, gaeMavenInfo.getGroupId());
                                                   setAttribute(VERSION, gaeMavenInfo.getVersion());
                                                   setAttribute(APPLICATION_ID, gaeMavenInfo.getApplicationId());
                                               }
                                           }));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        boolean isArtifactIdCompleted = !getAttributeValue(ARTIFACT_ID).isEmpty();
        boolean isGroupIdCompleted = !getAttributeValue(GROUP_ID).isEmpty();
        boolean isVersionFieldCompleted = !getAttributeValue(VERSION).isEmpty();
        boolean isApplicationIdCompleted = gaeUtil.isCorrectAppId(getAttributeValue(APPLICATION_ID));

        return isArtifactIdCompleted && isGroupIdCompleted && isVersionFieldCompleted && isApplicationIdCompleted;
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        setAttribute(ARTIFACT_ID, view.getArtifactIdValue());
        setAttribute(GROUP_ID, view.getGroupIdValue());
        setAttribute(VERSION, view.getVersionValue());
        setAttribute(APPLICATION_ID, view.getGaeAppIdValue());

        validateView();

        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void go(@Nonnull AcceptsOneWidget container) {
        container.setWidget(view);

        view.setFocusToApplicationIdField();

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        final String projectName = dataObject.getProject().getName();

        // use project name for artifactId and groupId for new project
        if (CREATE == wizardMode && projectName != null) {
            if (getAttributeValue(ARTIFACT_ID).isEmpty()) {
                setAttribute(ARTIFACT_ID, projectName);
            }
            if (getAttributeValue(GROUP_ID).isEmpty()) {
                setAttribute(GROUP_ID, projectName);
            }
            updateDelegate.updateControls();
        }

        updateView();
        validateView();
    }

    /** Updates view from data-object. */
    private void updateView() {
        view.setGroupIdValue(getAttributeValue(GROUP_ID));
        view.setArtifactIdValue(getAttributeValue(ARTIFACT_ID));
        view.setVersion(getAttributeValue(VERSION));
        view.setGaeApplicationId(getAttributeValue(APPLICATION_ID));
    }

    private void validateView() {
        view.showGroupIdInCorrectIndicator(view.getGroupIdValue().isEmpty());
        view.showArtifactIdInCorrectIndicator(view.getArtifactIdValue().isEmpty());
        view.showVersionInCorrectIndicator(view.getVersionValue().isEmpty());
        view.showApplicationIdInCorrectIndicator(!gaeUtil.isCorrectAppId(view.getGaeAppIdValue()));
    }

    /** Reads single value of attribute from data-object. */
    @Nonnull
    private String getAttributeValue(String attributeId) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        List<String> values = attributes.get(attributeId);
        if (!(values == null || values.isEmpty())) {
            return values.get(0);
        }
        return "";
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(@Nonnull String attrId, @Nonnull String value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, Arrays.asList(value));
    }
}