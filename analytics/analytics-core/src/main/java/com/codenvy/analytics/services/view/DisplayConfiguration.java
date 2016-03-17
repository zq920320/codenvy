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
package com.codenvy.analytics.services.view;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "display")
public class DisplayConfiguration {

    private List<ViewConfiguration> views;

    public List<ViewConfiguration> getViews() {
        return views;
    }

    public ViewConfiguration getView(String name) throws IllegalArgumentException {
        for (ViewConfiguration view : views) {
            if (view.getName().equals(name)) {
                return view;
            }
        }

        throw new IllegalArgumentException("There is no view with name " + name);
    }

    @XmlElement(name = "view")
    public void setViews(List<ViewConfiguration> views) {
        this.views = views;
    }
}
