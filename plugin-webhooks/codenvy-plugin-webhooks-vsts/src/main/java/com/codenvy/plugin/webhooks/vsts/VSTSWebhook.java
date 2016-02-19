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
package com.codenvy.plugin.webhooks.vsts;

import org.eclipse.che.commons.lang.Pair;

/**
 * VSTS webhook configuration that provides data to connect to a given VSTS Team Project.
 *
 * @author Stephane Tournie
 */
public class VSTSWebhook {

    private final String               vstsApiVersion;
    private final String               vstsProjectApiURL;
    private final Pair<String, String> vstsCredentials;

    public VSTSWebhook(String vstsApiVersion, String vstsProjectApiURL, Pair<String, String> vstsCredentials) {
        this.vstsApiVersion = vstsApiVersion;
        this.vstsProjectApiURL = vstsProjectApiURL;
        this.vstsCredentials = vstsCredentials;
    }

    public String getApiVersion() {
        return vstsApiVersion;
    }

    public String getProjectApiURL() {
        return vstsProjectApiURL;
    }

    public Pair<String, String> getCredentials() {
        return vstsCredentials;
    }
}
