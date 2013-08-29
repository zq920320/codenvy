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
package com.codenvy.factory.commons;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/** Service for codenvy factory features */
@Path("factory")
public class FactoryService {

    /**
     * Validate factory URL and return parsed {@link com.codenvy.factory.commons.FactoryUrl} object
     *
     * @param factoryUrl
     *         - factory URL to parse
     * @return - parsed {@link com.codenvy.factory.commons.FactoryUrl} object
     * @throws FactoryUrlException
     */
    @GET
    @Path("parse")
    @Produces({MediaType.APPLICATION_JSON})
    public FactoryUrl parseFactoryUrl(@QueryParam("factoryUrl") String factoryUrl) throws FactoryUrlException {
        try {
            return FactoryUrlParser.parse(URLDecoder.decode(factoryUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }

    }
}
