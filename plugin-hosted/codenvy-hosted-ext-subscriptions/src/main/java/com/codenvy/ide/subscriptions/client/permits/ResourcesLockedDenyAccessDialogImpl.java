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
package com.codenvy.ide.subscriptions.client.permits;

import com.codenvy.ide.subscriptions.client.ActionPermissionLocalizationConstant;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

import org.eclipse.che.api.account.gwt.client.AccountServiceClient;
import org.eclipse.che.api.user.gwt.client.UserProfileServiceClientImpl;
import org.eclipse.che.api.user.shared.dto.ProfileDescriptor;
import org.eclipse.che.ide.api.action.permits.ActionDenyAccessDialog;
import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Implementation of resources locked deny access dialog component for build and run actions.
 *
 * @author Oleksii Orel
 */
public class ResourcesLockedDenyAccessDialogImpl implements ActionDenyAccessDialog {
    private final DialogFactory                        dialogFactory;
    private final ActionPermissionLocalizationConstant localizationConstants;
    private final UserProfileServiceClientImpl         userProfileServiceClientImpl;
    private final DtoUnmarshallerFactory               dtoUnmarshallerFactory;
    private final ResourcesLockedActionPermit          resourcesLockedActionPermit;

    private String ownerEmail;

    @Inject
    public ResourcesLockedDenyAccessDialogImpl(DialogFactory dialogFactory,
                                               ResourcesLockedActionPermit resourcesLockedActionPermit,
                                               AccountServiceClient accountServiceclient,
                                               UserProfileServiceClientImpl userProfileServiceClientImpl,
                                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                               ActionPermissionLocalizationConstant localizationConstants,
                                               AppContext appContext) {
        this.dialogFactory = dialogFactory;
        this.resourcesLockedActionPermit = resourcesLockedActionPermit;
        this.userProfileServiceClientImpl = userProfileServiceClientImpl;
        this.localizationConstants = localizationConstants;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

//        TODO Get account owner email address for user
/*
        accountServiceclient.getMembers(appContext.getWorkspace().getAccountId(), new AsyncRequestCallback<Array<MemberDescriptor>>(
                                                    dtoUnmarshallerFactory.newArrayUnmarshaller(MemberDescriptor.class)) {
                                                @Override
                                                protected void onSuccess(Array<MemberDescriptor> members) {
                                                    for (MemberDescriptor member : members.asIterable()) {
                                                        if (member.getRoles().contains("account/owner")) {
                                                            getProfileDescriptor(member.getUserId());
                                                            return;
                                                        }
                                                    }
                                                }

                                                @Override
                                                protected void onFailure(Throwable error) {
                                                    Log.error(this.getClass(), "Unable to get member descriptors", error);
                                                }
                                            }
                                           );
*/

    }

    private void getProfileDescriptor(String userId) {
        userProfileServiceClientImpl.getProfileById(userId, new AsyncRequestCallback<ProfileDescriptor>(
                                                            dtoUnmarshallerFactory.newUnmarshaller(ProfileDescriptor.class)) {
                                                        @Override
                                                        protected void onSuccess(final ProfileDescriptor profile) {
                                                            ownerEmail = profile.getAttributes().get("email");
                                                        }

                                                        @Override
                                                        protected void onFailure(Throwable error) {
                                                            Log.error(this.getClass(), "Unable to get Profile", error);
                                                        }
                                                    }
                                                   );

    }

    private String getDialogMessage() {
        final String message;
        if (resourcesLockedActionPermit.isAccountLocked()) {
            message = localizationConstants.lockedAccountDialogMessage(ownerEmail != null ? ownerEmail : "");
        } else if (resourcesLockedActionPermit.isWorkspaceLocked()) {
            message = localizationConstants.lockedWorkspaceDialogMessage(ownerEmail != null ? ownerEmail : "");
        } else {
            message = localizationConstants.unlockedDialogMessage();
        }
        return message;
    }

    private String getDialogTitle() {
        final String title;
        if (resourcesLockedActionPermit.isAccountLocked()) {
            title = localizationConstants.lockedAccountDialogTitle();
        } else if (resourcesLockedActionPermit.isWorkspaceLocked()) {
            title = localizationConstants.lockedWorkspaceDialogTitle();
        } else {
            title = localizationConstants.unlockedDialogTitle();
        }
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        dialogFactory.createMessageDialog(getDialogTitle(),
                                          new HTML(getDialogMessage()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                              }
                                          })
                     .show();
    }
}
