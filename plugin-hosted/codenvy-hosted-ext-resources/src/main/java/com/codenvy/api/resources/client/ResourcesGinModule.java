/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.resources.client;

import com.codenvy.api.resources.client.preferences.RunnerPreferencesPresenter;
import com.codenvy.api.resources.client.service.ResourcesServiceClient;
import com.codenvy.api.resources.client.service.ResourcesServiceClientImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;

/**
 * @author Sergii Leschenko
 */
@ExtensionGinModule
public class ResourcesGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ResourcesServiceClient.class).to(ResourcesServiceClientImpl.class);
        GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(RunnerPreferencesPresenter.class);
    }
}