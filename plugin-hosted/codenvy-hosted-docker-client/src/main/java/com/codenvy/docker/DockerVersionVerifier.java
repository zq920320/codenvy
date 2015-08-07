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
package com.codenvy.docker;

import com.codenvy.docker.json.Version;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.util.Set;

/**
 * Verifies compatibility with docker api version.
 *
 * @author Anton Korneta
 */
@Singleton
public class DockerVersionVerifier {

    private final DockerConnector dockerConnector;
    private final Set<String>     supportedVersions;

    private static final Logger LOG = LoggerFactory.getLogger(DockerVersionVerifier.class);

    @Inject
    public DockerVersionVerifier(DockerConnector dockerConnector, @Named("machine.supported_docker_version") String[] supportedVersions) {
        this.dockerConnector = dockerConnector;
        this.supportedVersions = Sets.newHashSet(supportedVersions);
    }

    /**
     * Check docker version compatibility.
     */
    @PostConstruct
    void checkCompatibility() throws ServerException {
        try {
            Version versionInfo = dockerConnector.getVersion();
            if (!supportedVersions.contains(versionInfo.getVersion())) {
                throw new ServerException("Unsupported docker version " + versionInfo.getVersion());
            }
        } catch (IOException e) {
            LOG.info(e.getMessage());
            throw new ServerException("Impossible to get docker version", e);
        }
    }
}
