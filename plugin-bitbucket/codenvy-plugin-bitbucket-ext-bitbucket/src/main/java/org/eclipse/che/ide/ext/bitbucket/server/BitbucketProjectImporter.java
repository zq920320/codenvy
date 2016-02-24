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
package org.eclipse.che.ide.ext.bitbucket.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitProjectImporter;
import org.eclipse.che.vfs.impl.fs.LocalPathResolver;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

/**
 * {@link BitbucketProjectImporter} implementation for Bitbucket.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketProjectImporter extends GitProjectImporter {
    @Inject
    public BitbucketProjectImporter(@NotNull final GitConnectionFactory gitConnectionFactory,
                                    @NotNull final LocalPathResolver localPathResolver,
                                    EventService eventService) {
        super(gitConnectionFactory, localPathResolver, eventService);
    }

    @Override
    public String getId() {
        return "bitbucket";
    }

    @Override
    public String getDescription() {
        return "Import project from bitbucket.";
    }
}
