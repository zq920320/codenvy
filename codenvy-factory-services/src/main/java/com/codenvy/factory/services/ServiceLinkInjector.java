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
import com.codenvy.factory.commons.Link;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServiceLinkInjector {
    public void injectLinks(AdvancedFactoryUrl factoryUrl, Set<String> imagesIds, UriInfo uriInfo) {
        Set<Link> links = new LinkedHashSet<>();

        links.add(generateFactoryUrlLink(factoryUrl.getId(), uriInfo));
        for (String imageId : imagesIds) {
            links.add(generateFactoryImageLink(imageId, uriInfo));
        }

        factoryUrl.setLinks(links);
    }

    private Link generateFactoryImageLink(String imageId, UriInfo uriInfo) {
        Link link = new Link("image/" + imageId.substring(imageId.lastIndexOf('.') + 1), generatePath(imageId, "images", uriInfo), "image");
        return link;
    }

    private Link generateFactoryUrlLink(String id, UriInfo uriInfo) {
        Link link = new Link(MediaType.APPLICATION_JSON, generatePath(id, "", uriInfo), "self");
        return link;
    }

    private String generatePath(String id, String rel, UriInfo uriInfo) {
        return uriInfo == null ? "/" + rel + "/" + id : uriInfo.getBaseUri() + "/" + rel + "/" + id;
    }
}
