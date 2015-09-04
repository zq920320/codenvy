/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client.update;

import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;
import org.eclipse.che.api.builder.gwt.client.BuilderServiceClient;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.eclipse.che.api.builder.BuildStatus.CANCELLED;
import static org.eclipse.che.api.builder.BuildStatus.FAILED;
import static org.eclipse.che.api.builder.BuildStatus.SUCCESSFUL;
import static org.eclipse.che.api.builder.internal.Constants.LINK_REL_DOWNLOAD_RESULT;
import static org.eclipse.che.ide.extension.builder.client.BuilderExtension.BUILD_STATUS_CHANNEL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildActionTest {

    private static final String SOME_TEXT = "someText";
    private static final long   TASK_ID   = 1L;

    @Captor
    private ArgumentCaptor<SuccessCallback<BuildTaskDescriptor>>     successCallbackCaptor;
    @Captor
    private ArgumentCaptor<FailureCallback>                          failureCallbackCaptor;
    @Captor
    private ArgumentCaptor<SubscriptionHandler<BuildTaskDescriptor>> subscriptionHandlerCaptor;

    //constructor mocks
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private GAEAsyncCallbackFactory callbackFactory;
    @Mock
    private BuilderServiceClient    builderService;
    @Mock
    private DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    @Mock
    private MessageBus              messageBus;
    @Mock
    private NotificationManager     notificationManager;
    @Mock
    private BuildContext            buildContext;

    //additional mocks
    @Mock
    private ProjectDescriptor                            activeProject;
    @Mock
    private UpdateGAECallback                            updateCallback;
    @Mock
    private GAEAsyncRequestCallback<BuildTaskDescriptor> buildTaskDescriptorCallBack;
    @Mock
    private BuildTaskDescriptor                          buildTaskDescriptor;
    @Mock
    private FailureCallback                              failureCallback;
    @Mock
    private Link                                         link;
    @Mock
    private SubscriptionHandler<BuildTaskDescriptor>     buildStatusHandler;
    @Mock
    private Throwable                                    throwable;


    @InjectMocks
    private BuildAction buildAction;

    @Before
    public void setUp() {
        when(activeProject.getName()).thenReturn(SOME_TEXT);
        when(activeProject.getPath()).thenReturn(SOME_TEXT);
        when(buildTaskDescriptor.getTaskId()).thenReturn(TASK_ID);
        when(throwable.getMessage()).thenReturn(SOME_TEXT);
    }

    @Test
    public void buildShouldNoBeStartedWhenItIsInProgress() throws Exception {
        when(locale.buildInProgress(SOME_TEXT)).thenReturn(SOME_TEXT);
        buildAction.perform(activeProject, updateCallback);

        buildAction.perform(activeProject, updateCallback);

        verify(notificationManager).showError(SOME_TEXT);
        verify(locale).buildInProgress(SOME_TEXT);
        verify(activeProject).getName();
    }

    @Test
    public void buildShouldBePerformedWithSuccessStatus() throws Exception {
        when(link.getHref()).thenReturn(SOME_TEXT);
        when(link.getRel()).thenReturn(LINK_REL_DOWNLOAD_RESULT);
        when(buildTaskDescriptor.getStatus()).thenReturn(SUCCESSFUL);
        when(buildTaskDescriptor.getLinks()).thenReturn(Arrays.asList(link, link));

        callSuccessMethod();

        verify(builderService).build(SOME_TEXT, buildTaskDescriptorCallBack);
        verify(updateCallback).onSuccess(SOME_TEXT);
        verify(buildContext).setBuilding(false);
    }

    private void callSuccessMethod() {
        when(callbackFactory.build(eq(BuildTaskDescriptor.class),
                                   Matchers.<SuccessCallback<BuildTaskDescriptor>>anyObject(),
                                   Matchers.<FailureCallback>anyObject())).thenReturn(buildTaskDescriptorCallBack);

        buildAction.perform(activeProject, updateCallback);

        verify(builderService).build(SOME_TEXT, buildTaskDescriptorCallBack);

        verify(callbackFactory).build(eq(BuildTaskDescriptor.class), successCallbackCaptor.capture(), any(FailureCallback.class));

        SuccessCallback<BuildTaskDescriptor> successCallback = successCallbackCaptor.getValue();

        successCallback.onSuccess(buildTaskDescriptor);
    }

    @Test
    public void emptyStringShouldBeReturnedWhenLinkRelIsIncorrect() throws Exception {
        when(link.getRel()).thenReturn(SOME_TEXT);
        when(buildTaskDescriptor.getStatus()).thenReturn(SUCCESSFUL);
        when(buildTaskDescriptor.getLinks()).thenReturn(Arrays.asList(link));

        callSuccessMethod();

        verify(builderService).build(SOME_TEXT, buildTaskDescriptorCallBack);
        verify(updateCallback).onSuccess("");
        verify(buildContext).setBuilding(false);
    }

    @Test
    public void failureBuildCallBackShouldBeDone() throws Exception {
        buildAction.perform(activeProject, updateCallback);

        verify(callbackFactory).build(eq(BuildTaskDescriptor.class),
                                      Matchers.<SuccessCallback<BuildTaskDescriptor>>anyObject(),
                                      failureCallbackCaptor.capture());

        FailureCallback failureCallback = failureCallbackCaptor.getValue();

        failureCallback.onFailure(throwable);

        verify(updateCallback).onFailure(SOME_TEXT);
    }

    @Test
    public void buildShouldBeDoneWithStatusFailed() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(FAILED);
        when(locale.messagesBuildFailed()).thenReturn(SOME_TEXT);

        onMessageReceivedMethodCall();

        verify(buildContext).setBuilding(false);
        verify(updateCallback).onFailure(SOME_TEXT);
        verify(locale).messagesBuildFailed();
    }

    private void onMessageReceivedMethodCall() throws Exception {
        callSuccessMethod();

        verify(messageBus).subscribe(eq(BUILD_STATUS_CHANNEL + TASK_ID), subscriptionHandlerCaptor.capture());

        SubscriptionHandler<BuildTaskDescriptor> subscriptionHandler = subscriptionHandlerCaptor.getValue();
        Method onMessageReceived = subscriptionHandler.getClass().getDeclaredMethod("onMessageReceived", Object.class);
        onMessageReceived.setAccessible(true);

        onMessageReceived.invoke(subscriptionHandler, buildTaskDescriptor);

        verify(messageBus).unsubscribe(BUILD_STATUS_CHANNEL + TASK_ID, subscriptionHandler);
        verify(dtoUnmarshallerFactory).newWSUnmarshaller(BuildTaskDescriptor.class);
    }

    @Test
    public void webSocketExceptionShouldBeCaught() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(FAILED);
        doThrow(new WebSocketException(SOME_TEXT)).when(messageBus)
                                                  .subscribe(anyString(), Matchers.<SubscriptionHandler<BuildTaskDescriptor>>anyObject());

        callSuccessMethod();

        verify(updateCallback).onFailure(SOME_TEXT);
    }

    @Test
    public void buildShouldBeDoneWithStatusCancelled() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(CANCELLED);
        when(locale.messagesBuildCanceled(SOME_TEXT)).thenReturn(SOME_TEXT);

        onMessageReceivedMethodCall();

        verify(buildContext).setBuilding(false);
        verify(messageBus).unsubscribe(eq(BUILD_STATUS_CHANNEL + TASK_ID), Matchers.<SubscriptionHandler<BuildTaskDescriptor>>anyObject());
        verify(updateCallback).onFailure(SOME_TEXT);
        verify(locale).messagesBuildCanceled(SOME_TEXT);
        verify(activeProject).getName();
    }

    @Test
    public void successfulStatusShouldBeDoneAfterStatusUpdated() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(FAILED);
        callSuccessMethod();
        when(buildTaskDescriptor.getStatus()).thenReturn(SUCCESSFUL);

        verify(messageBus).subscribe(eq(BUILD_STATUS_CHANNEL + TASK_ID), subscriptionHandlerCaptor.capture());

        SubscriptionHandler<BuildTaskDescriptor> subscriptionHandler = subscriptionHandlerCaptor.getValue();
        Method onMessageReceived = subscriptionHandler.getClass().getDeclaredMethod("onMessageReceived", Object.class);
        onMessageReceived.setAccessible(true);

        onMessageReceived.invoke(subscriptionHandler, buildTaskDescriptor);

        verify(updateCallback).onSuccess(anyString());
        verify(buildContext).setBuilding(false);
    }

    @Test
    public void onErrorReceivedMethodShouldBeDone() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(CANCELLED);
        when(locale.deployError(SOME_TEXT)).thenReturn(SOME_TEXT);
        callSuccessMethod();

        verify(messageBus).subscribe(eq(BUILD_STATUS_CHANNEL + TASK_ID), subscriptionHandlerCaptor.capture());

        SubscriptionHandler<BuildTaskDescriptor> subscriptionHandler = subscriptionHandlerCaptor.getValue();
        Method onMessageFail = subscriptionHandler.getClass().getDeclaredMethod("onErrorReceived", Throwable.class);
        onMessageFail.setAccessible(true);

        onMessageFail.invoke(subscriptionHandler, throwable);

        verify(messageBus).unsubscribe(BUILD_STATUS_CHANNEL + TASK_ID, subscriptionHandler);
        verify(buildContext).setBuilding(false);
        verify(updateCallback).onFailure(SOME_TEXT);
        verify(locale).deployError(SOME_TEXT);
    }

    @Test
    public void webSocketExceptionShouldBeCaughtWhenOnErrorReceivedMethodShouldBeCalled() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(CANCELLED);
        doThrow(new WebSocketException(SOME_TEXT)).when(messageBus)
                                                  .unsubscribe(anyString(), Matchers.<SubscriptionHandler<BuildTaskDescriptor>>anyObject());
        callSuccessMethod();

        verify(messageBus).subscribe(eq(BUILD_STATUS_CHANNEL + TASK_ID), subscriptionHandlerCaptor.capture());

        SubscriptionHandler<BuildTaskDescriptor> subscriptionHandler = subscriptionHandlerCaptor.getValue();
        Method onMessageFail = subscriptionHandler.getClass().getDeclaredMethod("onErrorReceived", Throwable.class);
        onMessageFail.setAccessible(true);

        onMessageFail.invoke(subscriptionHandler, throwable);

        verify(updateCallback).onFailure(SOME_TEXT);
    }

    @Test
    public void webSocketExceptionShouldBeCaughtWhenBuildShouldBeFinished() throws Exception {
        when(buildTaskDescriptor.getStatus()).thenReturn(CANCELLED);
        doThrow(new WebSocketException(SOME_TEXT)).when(messageBus)
                                                  .unsubscribe(anyString(), Matchers.<SubscriptionHandler<BuildTaskDescriptor>>anyObject());
        onMessageReceivedMethodCall();

        verify(updateCallback).onFailure(SOME_TEXT);
    }

}