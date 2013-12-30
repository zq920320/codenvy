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
import com.codenvy.api.factory.SimpleFactoryUrl;
import com.codenvy.api.factory.Variable;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

import static org.testng.Assert.assertEquals;

public class SimpleFactoryUrlFormatTest {
    private SimpleFactoryUrlFormat factoryUrlFormat;
    private static String VALID_REPOSITORY_URL = "http://github.com/codenvy/cloudide";

    @BeforeMethod
    public void setUp() throws Exception {
        this.factoryUrlFormat = new SimpleFactoryUrlFormat();
    }

    @Test
    public void shouldBeAbleToParseValidFactoryUrl() throws Exception {
        //given
        SimpleFactoryUrl expectedFactoryUrl =
                new SimpleFactoryUrl("1.0", "git", VALID_REPOSITORY_URL, "1234567", null, null, false, null, null, "newBranch",
                                     Collections.singletonMap("pname", "eee"), Collections.<Variable>emptyList());

        //when
        SimpleFactoryUrl factoryUrl = factoryUrlFormat.parse(
                new URL("http://codenvy.com/factory?v=1.0&vcs=git&idcommit=1234567&vcsbranch=newBranch&pname=eee&wname=ttt&vcsurl=" +
                        enc(VALID_REPOSITORY_URL)));

        //then
        assertEquals(factoryUrl, expectedFactoryUrl);
    }

    @Test(dataProvider = "badUrlProvider-InvalidFormat", expectedExceptions = FactoryUrlException.class)
    public void shouldThrowFactoryUrlIllegalFormatExceptionIfUrlParametersIsMissing(String url) throws Exception {
        factoryUrlFormat.parse(new URL(url));
    }

    @DataProvider(name = "badUrlProvider-InvalidFormat")
    public Object[][] invalidFormatBadUrlProvider() throws UnsupportedEncodingException {
        return new Object[][]{
                {"http://codenvy.com/factory?v=2.0&vcs=git&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" + enc(VALID_REPOSITORY_URL)},
                // unsupported version
                {"http://codenvy.com/factory?vcs=git&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" + enc(VALID_REPOSITORY_URL)},
                // v par is missing
        };
    }

    @Test(dataProvider = "badUrlProvider-InvalidArgument", expectedExceptions = FactoryUrlException.class)
    public void shouldThrowFactoryUrlInvalidArgumentExceptionIfUrlHasInvalidParameters(String url) throws Exception {
        factoryUrlFormat.parse(new URL(url));
    }

    @DataProvider(name = "badUrlProvider-InvalidArgument")
    public Object[][] invalidArgumentBadUrlProvider() throws UnsupportedEncodingException {
        return new Object[][]{{"http://codenvy.com/factory?v=1.0&v=2.0&vcs=git&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" +
                               enc(VALID_REPOSITORY_URL)}, // v par has is duplicated
                              {"http://codenvy.com/factory?v=1.0&vcs=git&vcs=notagit&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" +
                               enc(VALID_REPOSITORY_URL)}, // vcs par is duplicated
                              {"http://codenvy.com/factory?v=1.0&vcs=&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" +
                               enc(VALID_REPOSITORY_URL)}, // vcs par has empty value
                              {"http://codenvy.com/factory?v=1.0&vcs=mercurial&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" + enc(
                                      VALID_REPOSITORY_URL)}, // vcs par has illegal value
                              {"http://codenvy.com/factory?v=1.0&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" +
                               enc(VALID_REPOSITORY_URL)}, // vcs par is missing
                              {"http://codenvy.com/factory?v=1.0&vcs=git&idcommit=1234567&pname=eee&wname=ttt"}, // vcsurl par is missing
                              {"http://codenvy.com/factory?v=1.0&vcs=git&idcommit=1234567&pname=eee&wname=ttt&vcsurl=" +
                               enc(VALID_REPOSITORY_URL) + "&vcsurl=" + enc(VALID_REPOSITORY_URL)} // vcsurl par is duplicated
        };
    }

    static String enc(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}
