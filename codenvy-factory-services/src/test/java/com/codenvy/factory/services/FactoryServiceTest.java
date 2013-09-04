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
package com.codenvy.factory.services;

import com.codenvy.factory.commons.AdvancedFactoryUrl;
import com.codenvy.factory.commons.Image;
import com.codenvy.factory.store.FactoryStore;
import com.jayway.restassured.response.Response;

import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.ext.ExceptionMapper;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.testng.Assert.assertEquals;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryServiceTest {
    private final String          SERVICE_PATH    = "/factory";
    private final ExceptionMapper exceptionMapper = new FactoryServiceExceptionMapper();

    @Mock
    private FactoryStore factoryStore;

    @InjectMocks
    private FactoryService factoryService;

    @Test(enabled = false)
    public void shouldBeAbleToSaveFactory() throws Exception {
        // given
        AdvancedFactoryUrl expectedFactoryUrl = new AdvancedFactoryUrl();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().post(SERVICE_PATH);

        // then
        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getObject("", AdvancedFactoryUrl.class), expectedFactoryUrl);
    }

    @Test(enabled = false)
    public void shouldReturnStatus400IfSaveRequestHaveNotFactoryInfo() throws Exception {
        // given
        AdvancedFactoryUrl expectedFactoryUrl = new AdvancedFactoryUrl();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().post(SERVICE_PATH);

        // then
        assertEquals(response.statusCode(), 400);
        assertEquals(response.body().asString(), "No factory URL information found in 'factoryUrl' section of multipart form-data");
    }

    @Test(enabled = false)
    public void shouldBeAbleToSaveFactoryWithOutImage() throws Exception {
        // given
        AdvancedFactoryUrl expectedFactoryUrl = new AdvancedFactoryUrl();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().post(SERVICE_PATH);

        // then
        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getObject("", AdvancedFactoryUrl.class), expectedFactoryUrl);
    }

    @Test(enabled = false)
    public void shouldBeAbleToGetFactory() throws Exception {
        // given
        AdvancedFactoryUrl expectedFactoryUrl = new AdvancedFactoryUrl();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().get(SERVICE_PATH + "/correctFactoryId");

        // then
        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getObject("", AdvancedFactoryUrl.class), expectedFactoryUrl);
    }

    @Test(enabled = false)
    public void shouldReturnStatus400OnGetFactoryWithIllegalId() throws Exception {
        // given
        AdvancedFactoryUrl expectedFactoryUrl = new AdvancedFactoryUrl();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().get(SERVICE_PATH + "/illegalFactoryId");

        // then
        assertEquals(response.statusCode(), 400);
        assertEquals(response.body().asString(), String.format("Factory URL with id %s is not found.", "illegalFactoryId"));
    }

    @Test(enabled = false)
    public void shouldBeAbleToGetFactoryImage() throws Exception {
        // given
        Image expectedImage = new Image();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().get(SERVICE_PATH + "/image/imageId");

        // then
        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getObject("", Image.class), expectedImage);
    }

    @Test
    public void shouldReturnStatus400OnGetFactoryImageWithIllegalId() throws Exception {
        // given
        Image expectedImage = new Image();

        // when
        Response response = given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().get(SERVICE_PATH + "/image/illegalImageId");

        // then
        assertEquals(response.statusCode(), 400);
        assertEquals(response.body().asString(), String.format("Image with id %s is not found.", "illegalImageId"));
    }
}
