package com.codenvy.plugin.gitlab.ide.inject;

import com.codenvy.plugin.gitlab.ide.GitLabClientService;
import com.codenvy.plugin.gitlab.ide.GitLabSshKeyUploader;
import com.codenvy.plugin.gitlab.ide.authenticator.GitLabAuthenticatorImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;

/**
 * @author Michail Kuznyetsov
 */
@ExtensionGinModule
public class GitLabGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(GitLabClientService.class);
        bind(GitLabSshKeyUploader.class);

        GinMultibinder.newSetBinder(binder(), OAuth2Authenticator.class).addBinding().to(GitLabAuthenticatorImpl.class);
    }
}
