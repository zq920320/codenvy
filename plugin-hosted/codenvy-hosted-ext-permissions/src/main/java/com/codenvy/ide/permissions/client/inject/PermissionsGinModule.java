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
package com.codenvy.ide.permissions.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorView;
import com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorViewImpl;
import com.codenvy.ide.permissions.client.part.PermissionsPartView;
import com.codenvy.ide.permissions.client.part.PermissionsPartViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;

import javax.inject.Singleton;

/**
 * Bindings for the permissions extension.
 *
 * @author Kevin Pollet
 */
@ExtensionGinModule
public class PermissionsGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(PermissionsIndicatorView.class).to(PermissionsIndicatorViewImpl.class);
        bind(PermissionsPartView.class).to(PermissionsPartViewImpl.class).in(Singleton.class);
    }
}
