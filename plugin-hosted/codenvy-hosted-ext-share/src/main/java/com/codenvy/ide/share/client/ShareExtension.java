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
package com.codenvy.ide.share.client;

import com.codenvy.ide.share.client.share.ShareAction;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import static com.codenvy.ide.share.client.share.ShareAction.SHARE_ACTION_ID;
import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_MAIN_MENU;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;

/**
 * @author Sergii Leschenko
 */
@Singleton
@Extension(title = "Share", version = "3.0.0")
public class ShareExtension {

    @Inject
    public ShareExtension(ActionManager actionManager,
                          ShareAction shareAction,
                          ShareResources resources) {

        /*
         * Inject resources and js
         */
        ScriptInjector.fromUrl("https://apis.google.com/js/client:plusone.js?parsetags=explicit")
                      .setWindow(TOP_WINDOW)
                      .inject();

        ScriptInjector.fromUrl("https://connect.facebook.net/en_US/sdk.js")
                      .setWindow(TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void result) {
                              init();
                          }

                          @Override
                          public void onFailure(Exception reason) {
                          }

                          private native void init() /*-{
                              $wnd.FB.init({
                                  appId: "318167898391385",
                                  xfbml: true,
                                  version: "v2.1"
                              });
                          }-*/;
                      }).inject();

        resources.shareCSS().ensureInjected();

        actionManager.registerAction(SHARE_ACTION_ID, shareAction);
        final DefaultActionGroup rightMainMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RIGHT_MAIN_MENU);
        rightMainMenuGroup.add(shareAction, new Constraints(AFTER, "expandEditor"));
    }
}
