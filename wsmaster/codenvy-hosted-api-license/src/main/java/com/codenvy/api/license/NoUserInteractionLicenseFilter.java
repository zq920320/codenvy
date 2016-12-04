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
package com.codenvy.api.license;

import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.api.license.server.LicenseService;
import com.codenvy.api.license.shared.dto.LegalityDto;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import static com.codenvy.api.license.shared.model.Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED;

/**
 * TODO
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class NoUserInteractionLicenseFilter implements Filter {
    @Inject
    protected RequestFilter          requestFilter;
    @Inject
    protected HttpJsonRequestFactory requestFactory;
    @Inject
    @Named("che.api")
    protected String                 apiEndpoint;

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpServletResponse httpResponse = (HttpServletResponse)response;
        if (requestFilter.shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (fairSourceLicenseIsNotAccepted(requestFactory, apiEndpoint)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, CodenvyLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE);
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    static boolean fairSourceLicenseIsNotAccepted(HttpJsonRequestFactory requestFactory, String apiEndpoint) throws ForbiddenException, BadRequestException, IOException, ConflictException, NotFoundException, ServerException, UnauthorizedException {
        LegalityDto legality = requestFactory.fromUrl(UriBuilder.fromUri(apiEndpoint)
                                                                .path(LicenseService.class)
                                                                .path(LicenseService.class,
                                                                                 "isSystemUsageLegal").build().toString())
                                             .useGetMethod()
                                             .request()
                                             .asDto(LegalityDto.class);

        return legality.getIssues().stream().anyMatch(issue -> issue.getStatus().equals(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED));
    }

    @Override
    public void destroy() {
    }
}
