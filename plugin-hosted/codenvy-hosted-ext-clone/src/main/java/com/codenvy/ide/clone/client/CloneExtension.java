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
package com.codenvy.ide.clone.client;

import com.codenvy.ide.clone.client.persist.PersistProjectHandler;
import com.codenvy.ide.clone.client.persist.PersistProjectPresenter;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;

import javax.inject.Inject;

/**
 * @author Sergii Leschenko
 */
@Singleton
@Extension(title = "Clone", version = "3.0.0")
public class CloneExtension {
    @Inject
    public CloneExtension(CloneProjectPresenter cloneProjectPresenter,
                          PersistProjectPresenter persistProjectPresenter,
                          PersistProjectHandler copyToNamedWorkspaceHandler,
                          CloneResources resources) {

        cloneProjectPresenter.process();
        copyToNamedWorkspaceHandler.process();
        persistProjectPresenter.process();

        resources.cloneCSS().ensureInjected();
    }
}
