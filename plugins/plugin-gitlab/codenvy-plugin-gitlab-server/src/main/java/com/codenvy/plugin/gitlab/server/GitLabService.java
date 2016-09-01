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
package com.codenvy.plugin.gitlab.server;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * REST service to access api of GitLab.
 *
 * @author Michail Kuznyetsov
 */
@Path("/gitlab")
public class GitLabService {
    private static final Logger LOG = LoggerFactory.getLogger(GitLabService.class);

    @Inject
    private SshServiceClient sshServiceClient;

    @Inject
    private GitLabKeyUploader gitLabKeyUploader;

    @POST
    @Path("ssh/generate")
    public void updateSSHKey() throws ApiException {
        final String host = "gitlab.codenvy-stg.com";
        SshPair sshPair = null;
        try {
            sshPair = sshServiceClient.getPair("vcs", host);
        } catch (NotFoundException ignored) {
        }

        if (sshPair != null) {
            if (sshPair.getPublicKey() == null) {
                sshServiceClient.removePair("vcs", host);
                sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("vcs")
                                                                                            .withName(host));
            }
        } else {
            sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("vcs")
                                                                                        .withName(host));
        }

        // update public key
        try {
            gitLabKeyUploader.uploadKey(sshPair.getPublicKey());
        } catch (IOException e) {
            LOG.error("Upload github ssh key fail", e);
            throw new GitException(e.getMessage(), e);
        }
    }
}
