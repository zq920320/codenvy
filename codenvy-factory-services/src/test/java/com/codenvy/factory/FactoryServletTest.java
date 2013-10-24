/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.factory;

import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.commons.lang.ZipUtils;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class FactoryServletTest {
    @Test
    public void shouldBeAbleToCheckAvailableRepo() throws URISyntaxException, IOException, FactoryUrlException {
        File testRepository = Files.createTempDirectory("testrepository").toFile();
        ZipUtils.unzip(new File(Thread.currentThread().getContextClassLoader().getResource("testrepository.zip").toURI()), testRepository);

        FactoryServlet.checkRepository("file://" + testRepository.getAbsolutePath() + "/testrepository");
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldTrowExceptionIfRepoIsNotAvailable() throws FactoryUrlException {
        FactoryServlet.checkRepository("file://workspace/cloudide.git");
    }
}
