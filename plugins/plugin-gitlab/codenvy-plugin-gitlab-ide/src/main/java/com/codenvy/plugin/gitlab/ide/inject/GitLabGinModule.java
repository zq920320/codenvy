package com.codenvy.plugin.gitlab.ide.inject;

import com.codenvy.plugin.gitlab.ide.GitLabClientService;
import com.codenvy.plugin.gitlab.ide.GitLabSshKeyUploader;
import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class GitLabGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(GitLabClientService.class);
        bind(GitLabSshKeyUploader.class);

    }
}
