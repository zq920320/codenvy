/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server.rest;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import com.codenvy.ide.ext.gae.server.AppEngineClient;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APP_ENGINE_WEB_XML_PATH;
import static java.io.File.separator;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Class contains business logic that allows to communicate with Google App Engine SDK. Executes different commands in SDK.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Andrey Plotnikov
 */
@Path("/appengine/{ws-id}")
public class AppEngineService {
    private static final String APP_YAML = "app.yaml";

    @Inject
    private AppEngineClient           client;
    @Inject
    private VirtualFileSystemRegistry vfsRegistry;
    @Inject
    private OAuthTokenProvider        oauthTokenProvider;
    @Inject
    private GAEServerUtil             gaeServerUtil;

    @PathParam("ws-id")
    private String wsId;

    /** Gets user token for google */
    @GET
    @Path("user")
    @Produces(APPLICATION_JSON)
    public OAuthToken getUser() {
        OAuthToken token = null;
        try {
            token = oauthTokenProvider.getToken("google", getUserId());
        } catch (IOException e) {
            // Error when try to refresh access token. User may try re-authenticate.
        }

        return token == null ? DtoFactory.getInstance().createDto(OAuthToken.class) : token;
    }

    /** Deploy application to GAE and return general information about application */
    @GET
    @Path("update")
    @Produces(APPLICATION_JSON)
    public ApplicationInfo update(@QueryParam("projectpath") String projectPath, @QueryParam("bin") URL bin) throws IOException,
                                                                                                                    ApiException {
        return client.update(wsId, projectPath, bin, getUserId());
    }

    @NotNull
    private String getUserId() {
        return EnvironmentContext.getCurrent().getUser().getId();
    }

    /** Apply given application ID into configuration file of the current project */
    @GET
    @Path("change-appid")
    public Response changeApplicationId(@QueryParam("projectpath") String projectPath, @QueryParam("app_id") String appId)
            throws ApiException, IOException {

        VirtualFileSystemProvider vfsProvider = vfsRegistry.getProvider(wsId);
        MountPoint mountPoint = vfsProvider.getMountPoint(false);

        String newAppId = appId.substring(2, appId.length());

        try {
            changeAppEngXml(mountPoint, projectPath, newAppId);
        } catch (NotFoundException e) {
            try {
                changeAppEngYaml(mountPoint, projectPath, newAppId);
            } catch (Exception e1) {
                return Response.serverError()
                               .entity("Unable to modify App Engine application settings.")
                               .type(MediaType.TEXT_PLAIN)
                               .build();
            }
        }
        return Response.ok("<html><body onLoad=\"javascript:window.close();\" style=\"font-family: Verdana, Bitstream Vera Sans, " +
                           "sans-serif; font-size: 13px; font-weight: bold;\">" + "<div align=\"center\" style=\"margin: 100 auto; " +
                           "border: dashed 1px #CACACA; width: 450px;\">" + "<p>Your application has been created.<br>Close this tab " +
                           "and use the Deploy button in Codenvy.</p>" + "</div></body></html>")
                       .type(MediaType.TEXT_HTML).build();
    }

    /**
     * Change appengine-web.xml file setting application's id.
     *
     * @param mountPoint
     *         mount point of virtual filesystem
     * @param path
     *         path to project's root
     * @param appId
     *         application's id
     */
    private void changeAppEngXml(@NotNull MountPoint mountPoint,
                                 @NotNull String path,
                                 @NotNull String appId) throws ApiException, IOException {
        VirtualFile fileAppEngineWebXml = mountPoint.getVirtualFile(path + separator + APP_ENGINE_WEB_XML_PATH);
        gaeServerUtil.setApplicationIdToWebAppEngine(fileAppEngineWebXml, appId);
    }

    /**
     * Change app.yaml file setting application's id.
     *
     * @param mountPoint
     *         mount point of virtual filesystem
     * @param path
     *         path to project's root
     * @param appId
     *         application's id
     */
    private void changeAppEngYaml(@NotNull MountPoint mountPoint,
                                  @NotNull String path,
                                  @NotNull String appId) throws ApiException, IOException {
        VirtualFile fileAppEngYaml = mountPoint.getVirtualFile(path + separator + APP_YAML);
        gaeServerUtil.setApplicationIdToAppYaml(fileAppEngYaml, appId);
    }

}