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
import java.util.*;

public class ServiceLinkInjector {

    List<String> snippetTypes = Collections.unmodifiableList(Arrays.asList("markdown", "url", "html"));

    public void injectLinks(AdvancedFactoryUrl factoryUrl, Set<String> imagesIds, UriInfo uriInfo) {
        Set<Link> links = new LinkedHashSet<>();

        links.add(generateFactoryUrlLink(factoryUrl.getId(), uriInfo));
        for (String imageId : imagesIds) {
            links.add(generateFactoryImageLink(imageId, uriInfo));
        }
        links.addAll(generateSnippetLinks(factoryUrl.getId(), uriInfo));

        factoryUrl.setLinks(links);
    }

    private Link generateFactoryImageLink(String imageId, UriInfo uriInfo) {
        return new Link("image/" + imageId.substring(imageId.lastIndexOf('.') + 1),
                             generatePath(imageId, "image", uriInfo), "image");
    }

    private Link generateFactoryUrlLink(String id, UriInfo uriInfo) {
        return new Link(MediaType.APPLICATION_JSON, generatePath(id, "", uriInfo), "self");
    }

    private Set<Link> generateSnippetLinks(String id, UriInfo uriInfo) {
       Set<Link> result = new LinkedHashSet<>();
      for (String snippetType : snippetTypes) {
          result.add(new Link("text/plain", generatePath(id, "", "snippet", "type=" + snippetType, uriInfo),
                              "snippet/" + snippetType));
      }
        return result;
    }

    // maybe use URLbuilder here ?
    private String generatePath(String id, String rel, UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder();
        if (uriInfo != null)
            sb.append(uriInfo.getBaseUri());
        sb.append("/");
        sb.append(rel);
        sb.append("/");
        sb.append(id);
        return sb.toString();
    }

    private String generatePath(String id, String rel, String servletPath, String query, UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder();
        if (uriInfo != null)
            sb.append(uriInfo.getBaseUri());
        sb.append("/");
        sb.append(rel);
        sb.append("/");
        sb.append(id);
        sb.append("/");
        sb.append(servletPath);
        sb.append("?");
        sb.append(query);
        return sb.toString();
    }
}
