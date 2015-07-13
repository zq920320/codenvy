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
package com.codenvy.ide.share.client;

import com.codenvy.ide.share.client.share.CommitView;
import com.codenvy.ide.share.client.share.CommitViewImpl;
import com.codenvy.ide.share.client.share.ShareActionView;
import com.codenvy.ide.share.client.share.ShareActionViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@ExtensionGinModule
public class ShareGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(CommitView.class).to(CommitViewImpl.class);
        bind(ShareActionView.class).to(ShareActionViewImpl.class).in(Singleton.class);
    }
}
