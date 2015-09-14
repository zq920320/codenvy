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
package com.codenvy.ide.ext.gae.client.wizard.yaml;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;

/**
 * Class contains business logic which allows create scratch of google application project or import
 * project from other location(for example github).
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class GAEYamlPagePresenter extends AbstractWizardPage<ImportProject> implements GAEYamlPageView.ActionDelegate {
    private final GAEYamlPageView         view;
    private final GAEServiceClient        service;
    private final GAEAsyncCallbackFactory callBackFactory;
    private final GAEUtil                 gaeUtil;
    private final GAELocalizationConstant locale;

    @Inject
    public GAEYamlPagePresenter(GAEYamlPageView view,
                                GAELocalizationConstant locale,
                                GAEServiceClient service,
                                GAEAsyncCallbackFactory callBackFactory,
                                GAEUtil gaeUtil) {
        this.view = view;
        this.view.setDelegate(this);
        this.service = service;
        this.locale = locale;
        this.callBackFactory = callBackFactory;
        this.gaeUtil = gaeUtil;
    }

    /** {@inheritDoc} */
    @Override
    public void init(ImportProject dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));

        if (CREATE == wizardMode) {
            setAttribute(APPLICATION_ID, locale.wizardApplicationIdDefault());
        } else if (UPDATE == wizardMode) {
            String projectPath = context.get(PROJECT_PATH_KEY);
            service.readGAEYamlParameters(projectPath,
                                          callBackFactory.build(YamlParameterInfo.class, new SuccessCallback<YamlParameterInfo>() {
                                              @Override
                                              public void onSuccess(YamlParameterInfo yamlParameterInfo) {
                                                  setAttribute(APPLICATION_ID, yamlParameterInfo.getApplicationId());
                                              }
                                          }));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        return gaeUtil.isCorrectAppId(getAttributeValue(APPLICATION_ID));
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        setAttribute(APPLICATION_ID, view.getGaeAppIdValue());

        validateView();

        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);

        view.setFocusToApplicationIdField();

        updateView();
        validateView();
    }

    /** Updates view from data-object. */
    private void updateView() {
        view.setGaeApplicationId(getAttributeValue(APPLICATION_ID));
    }

    private void validateView() {
        view.showApplicationIdInCorrectIndicator(!gaeUtil.isCorrectAppId(view.getGaeAppIdValue()));
    }

    /** Reads single value of attribute from data-object. */
    @NotNull
    private String getAttributeValue(String attributeId) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        List<String> values = attributes.get(attributeId);
        if (!(values == null || values.isEmpty())) {
            return values.get(0);
        }
        return "";
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(@NotNull String attrId, @NotNull String value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, Arrays.asList(value));
    }
}