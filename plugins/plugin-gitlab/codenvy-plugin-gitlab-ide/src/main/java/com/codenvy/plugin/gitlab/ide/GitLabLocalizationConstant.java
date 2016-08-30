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
package com.codenvy.plugin.gitlab.ide;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

/**
 * @author Mihail Kuznyetsov
 */
public interface GitLabLocalizationConstant extends Messages {
    @LocalizableResource.Key("authorization.dialog.title")
    String authorizationDialogTitle();

    @LocalizableResource.Key("authorization.dialog.text")
    String authorizationDialogText(String name);

    @LocalizableResource.Key("authorization.request.rejected")
    String authorizationRequestRejected();

    @LocalizableResource.Key("authorization.failed")
    String authorizationFailed();

}
