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
package org.eclipse.che.security.oauth;

import org.eclipse.che.inject.DynaModule;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Register BitbucketOauthAuthenticator in guice container.
 *
 * @author Michail Kuznyetsov
 */
@DynaModule
public class GitLabModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<OAuthAuthenticator> oAuthAuthenticators = Multibinder.newSetBinder(binder(), OAuthAuthenticator.class);
        oAuthAuthenticators.addBinding().to(GitLabOAuthAuthenticator.class);

        bindConstant().annotatedWith(Names.named("oauth.gitlab.clientid")).to("9fe11ed20cc2583d85d0cc51775d9b23da2922e5493b8cc13767982c63c96423");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.clientsecret")).to("c4eed8cfd264ffd7b154c9a33654b41a829a0d9f672535a19abee5111e7c7da8");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.authuri")).to("http://gitlab.codenvy-stg.com/oauth/authorize");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.tokenuri")).to("http://gitlab.codenvy-stg.com/oauth/token");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.redirecturis")).to("https://aio.codenvy-dev.com/api/oauth/callback");
    }
}
