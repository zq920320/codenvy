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
package com.codenvy.ide.onpremises.inject;


import com.codenvy.ide.onpremises.permits.ResourcesLockedActionPermitImpl;
import com.codenvy.ide.onpremises.permits.ResourcesLockedDenyAccessDialogImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.permits.ActionDenyAccessDialog;
import org.eclipse.che.ide.api.action.permits.Build;
import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;
import org.eclipse.che.ide.api.action.permits.Run;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Igor Vinokur
 */
@ExtensionGinModule
public class OnpremisesGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ResourcesLockedActionPermit.class).annotatedWith(Build.class).to(ResourcesLockedActionPermitImpl.class).in(Singleton.class);
        bind(ActionDenyAccessDialog.class).annotatedWith(Build.class).to(ResourcesLockedDenyAccessDialogImpl.class).in(Singleton.class);
        bind(ResourcesLockedActionPermit.class).annotatedWith(Run.class).to(ResourcesLockedActionPermitImpl.class).in(Singleton.class);
        bind(ActionDenyAccessDialog.class).annotatedWith(Run.class).to(ResourcesLockedDenyAccessDialogImpl.class).in(Singleton.class);
        bind(ResourcesLockedActionPermit.class).to(ResourcesLockedActionPermitImpl.class).in(Singleton.class);
    }
}
