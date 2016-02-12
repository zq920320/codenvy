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
package com.codenvy.plugin.contribution.vcs.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.BitbucketHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.GitHubHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

/**
 * Gin module definition for the contribution VCS.
 */
@ExtensionGinModule
public class ContributorVcsModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(VcsServiceProvider.class);
        bind(VcsHostingServiceProvider.class);

        final GinMultibinder<VcsHostingService> vcsHostingServiceBinder = GinMultibinder.newSetBinder(binder(), VcsHostingService.class);
        vcsHostingServiceBinder.addBinding().to(GitHubHostingService.class);
        vcsHostingServiceBinder.addBinding().to(BitbucketHostingService.class);
    }
}
