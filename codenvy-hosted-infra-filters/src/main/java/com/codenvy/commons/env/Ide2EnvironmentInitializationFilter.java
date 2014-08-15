/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.commons.env;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;

/** Initialize  EnvironmentContext variables; */
@Singleton
public class Ide2EnvironmentInitializationFilter implements Filter {
    //public static final Pattern TENANT_URL_PATTERN = Pattern.compile("^/ws/(?:rest/|websocket/)?([^/]+?)(?:/.*)?$");

    private final File   vfsRootDir;
    private final File   tempVfsRootDir;
    private final File   vfsIndexDir;
    private final String wsNotFoundRedirectUrl;

    @Inject
    public Ide2EnvironmentInitializationFilter(@Named("vfs.local.fs_root_dir") File vfsRoot,
                                               @Named("vfs.local.fs_index_root_dir") File vfsIndexRoot,
                                               @Named("sys.java.io.tmpdir") File tempDir,
                                               @Named("error.page.workspace_not_found_redirect_url") String wsNotFoundRedirectUrl) {
        this.vfsRootDir = vfsRoot;
        this.vfsIndexDir = vfsIndexRoot;
        this.tempVfsRootDir = new File(tempDir + "/tempWorkspacesFS");
        this.wsNotFoundRedirectUrl = wsNotFoundRedirectUrl;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String requestUrl = httpRequest.getRequestURL().toString();
        try {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            if (context.isWorkspaceTemporary()) {
                context.setVariable(EnvironmentContext.VFS_ROOT_DIR, tempVfsRootDir);
            } else {
                context.setVariable(EnvironmentContext.VFS_ROOT_DIR, vfsRootDir);
            }
            UriBuilder ub = UriBuilder.fromUri(requestUrl).replacePath(null).replaceQuery(null);

            context.setVariable(EnvironmentContext.VFS_INDEX_DIR, vfsIndexDir);
            context.setVariable(EnvironmentContext.MASTERHOST_NAME, request.getServerName());
            context.setVariable(EnvironmentContext.MASTERHOST_PORT, request.getServerPort());
            context.setVariable(EnvironmentContext.MASTERHOST_URL, ub.build().toString());
            context.setVariable(EnvironmentContext.WORKSPACE_URL,
                                ub.replacePath("/ws/").path(context.getWorkspaceName()).build().toString());
            context.setVariable(EnvironmentContext.GIT_SERVER, "git");
            chain.doFilter(request, response);
        } finally {
            EnvironmentContext.reset();
        }
    }

    @Override
    public void destroy() {
    }
}
