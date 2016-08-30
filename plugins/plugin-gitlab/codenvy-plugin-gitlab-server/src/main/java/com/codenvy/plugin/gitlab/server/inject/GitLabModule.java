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
package com.codenvy.plugin.gitlab.server.inject;

import com.codenvy.plugin.gitlab.server.GitLabKeyUploader;
import com.codenvy.plugin.gitlab.server.GitLabService;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.ssh.key.script.SshKeyUploader;

/**
 * @author Mihail Kuznyetsov
 */
@DynaModule
public class GitLabModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitLabService.class);
        Multibinder.newSetBinder(binder(), SshKeyUploader.class).addBinding().to(GitLabKeyUploader.class);
    }
}
