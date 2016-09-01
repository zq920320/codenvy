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

        bindConstant().annotatedWith(Names.named("oauth.gitlab.clientid")).to("51dfac8632d4b887023d801cb0962384e21757edd903214fbe8e876a4440fa3b");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.clientsecret")).to("b9ccdd585e31ddfbbf288229992f44bb89bd5ff08797b1a1201f9a1db2e4a778");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.authuri")).to("http://gitlab.codenvy-stg.com/oauth/authorize");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.tokenuri")).to("http://gitlab.codenvy-stg.com/oauth/token");
        bindConstant().annotatedWith(Names.named("oauth.gitlab.redirecturis")).to("https://aio.codenvy-dev.com/api/oauth/callback");
    }
}
