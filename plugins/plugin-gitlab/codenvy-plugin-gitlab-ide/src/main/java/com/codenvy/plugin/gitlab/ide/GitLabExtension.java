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
package com.codenvy.plugin.gitlab.ide;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;

import javax.inject.Singleton;

/**
 * @author Mihail Kuznyetsov
 */
@Singleton
@Extension(title = "GitLab", version = "3.0.0")
public class GitLabExtension {

    public static final String GITLAB_HOST = "gitlab.codenvy-stg.com";

    @Inject
    public GitLabExtension(SshKeyUploaderRegistry registry, GitLabSshKeyUploader gitHubSshKeyProvider) {
        registry.registerUploader(GITLAB_HOST, gitHubSshKeyProvider);
    }
}
