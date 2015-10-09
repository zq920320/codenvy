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
package com.codenvy.ide.onpremises.permits;


import com.google.inject.Inject;

import org.eclipse.che.api.account.gwt.client.AccountServiceClient;
import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.account.shared.dto.WorkspaceLockDetails;
//import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;


/**
 * Implementation of resources locked permit for build and run actions.
 *
 * @author Oleksii Orel
 */
public class ResourcesLockedActionPermitImpl implements ResourcesLockedActionPermit {
    private final MessageBus             messageBus;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    //private final String                 workspaceLockChangeChanel;

    private boolean isWorkspaceLocked;

    @Inject
    public ResourcesLockedActionPermitImpl(MessageBus messageBus,
                                           AppContext appContext,
                                           AccountServiceClient accountServiceClient,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.messageBus = messageBus;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        this.isWorkspaceLocked = false;

//        final WorkspaceDescriptor workspace = appContext.getWorkspace();
//
//        accountServiceClient.getAccountById(workspace.getAccountId(), new AsyncRequestCallback<AccountDescriptor>(
//                dtoUnmarshallerFactory.newUnmarshaller(AccountDescriptor.class)) {
//            @Override
//            protected void onSuccess(AccountDescriptor account) {
//                if (workspace.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY)) {
//                    isWorkspaceLocked = true;
//                }
//            }
//
//            @Override
//            protected void onFailure(Throwable error) {
//                Log.error(this.getClass(), "Unable to get account descriptor", error);
//            }
//        });
//
//        workspaceLockChangeChanel = "workspace:" + workspace.getId() + ":lock";
//
//        try {
//            messageBus.subscribe(workspaceLockChangeChanel, new WorkspaceLockedStateUpdater());
//        } catch (WebSocketException e) {
//            Log.error(getClass(), "Can't open websocket connection");
//        }
    }

    @Override
    public boolean isAllowed() {
        return !isWorkspaceLocked;
    }

    @Override
    public boolean isAccountLocked() {
        return false;
    }

    @Override
    public boolean isWorkspaceLocked() {
        return isWorkspaceLocked;
    }


    private class WorkspaceLockedStateUpdater extends SubscriptionHandler<WorkspaceLockDetails> {

        WorkspaceLockedStateUpdater() {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(WorkspaceLockDetails.class));
        }

        @Override
        protected void onMessageReceived(WorkspaceLockDetails workspaceLockDetails) {
            isWorkspaceLocked = workspaceLockDetails.isLocked();
        }

        @Override
        protected void onErrorReceived(Throwable throwable) {
//            try {
//                messageBus.unsubscribe(workspaceLockChangeChanel, this);
//                Log.error(WorkspaceLockDetails.class, throwable);
//            } catch (WebSocketException e) {
//                Log.error(WorkspaceLockDetails.class, e);
//            }
        }
    }
}
