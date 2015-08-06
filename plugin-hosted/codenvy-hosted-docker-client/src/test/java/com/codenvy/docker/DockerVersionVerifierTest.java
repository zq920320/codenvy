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

import org.eclipse.che.api.core.ServerException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class DockerVersionVerifierTest {

    @Mock
    private DockerConnector       dockerConnector;

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Unsupported docker version x.x.x")
    public void shouldThrowServerExceptionWhenDockerVersionIsIncompatible() throws Exception {
        //mock docker version
        Version version = new Version();
        version.setVersion("x.x.x");
        when(dockerConnector.getVersion()).thenReturn(version);
        //prepare verifies
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});

        verifier.checkCompatibility();
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Impossible to get docker version")
    public void shouldThrowIOExceptionWhenVersionJsonParsing() throws Exception {
        when(dockerConnector.getVersion()).thenThrow(new IOException());
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});
        verifier.checkCompatibility();
    }

    @Test
    public void supportedVersionTest() throws Exception {
        Version version = new Version();
        version.setVersion("1.6.0");
        when(dockerConnector.getVersion()).thenReturn(version);
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});
        verifier.checkCompatibility();
    }
}
