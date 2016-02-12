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
package com.codenvy.plugin.contribution.client.utils;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Helper class to work with notifications and standardize contribute workflow notifications.
 *
 * @author Kevin Pollet
 */
// FIXME
public final class NotificationHelper {
    /** The notification manger used to handle errors. */
    private final NotificationManager notificationManager;

    /** The contribute messages. */
    private final ContributeMessages messages;

    @Inject
    protected NotificationHelper(@NotNull final NotificationManager notificationManager, @NotNull final ContributeMessages messages) {
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    /**
     * Shows a warning notification.
     *
     * @param message
     *         the info message.
     */
    public void showInfo(@NotNull final String message) {
        showNotification(new Notification(message));
    }

    /**
     * Shows a warning notification.
     *
     * @param message
     *         the warning message.
     */
    public void showWarning(@NotNull final String message) {
        showNotification(new Notification(message));
    }

    /**
     * Shows an error notification.
     *
     * @param cls
     *         {@link java.lang.Class} where the exception is caught.
     * @param message
     *         the error message.
     */
    public void showError(@NotNull final Class<?> cls, @NotNull final String message) {
        showError(cls, new Exception(message));
    }

    /**
     * Handles an exception and display the error message in a notification.
     *
     * @param cls
     *         {@link java.lang.Class} where the exception is caught.
     * @param exception
     *         exception to handle.
     */
    public void showError(@NotNull final Class<?> cls, @NotNull final Throwable exception) {
        showError(cls, exception.getMessage(), exception);
    }

    /**
     * Log the exception, display the error message in the notification.
     */
    public void showError(@NotNull final Class<?> cls, @NotNull final String errorMessage, @NotNull final Throwable exception) {
        // workaround IDEX-2381
        final String jsonMessage = ensureJson(errorMessage);
        showNotification(new Notification(jsonMessage));
        Log.error(cls, exception);
    }

    private String ensureJson(final String input) {
        if (input == null || input.isEmpty()) {
            return "\"\"";
        }
        try {
            JSONParser.parseStrict(input);
            return input;
        } catch (final JSONException e) {
            return "{ \"message\": \"" + input + "\"}";
        }
    }

    /**
     * Shows a notification.
     *
     * @param notification
     *         notification to display.
     */
    public void showNotification(@NotNull final Notification notification) {
        notification.setContent(messages.notificationMessagePrefix(notification.getContent()));
        notificationManager.notify(notification);
    }

    /**
     * Finish a notification in progress with an error.
     *
     * @param cls
     *         the class where the exception is caught.
     * @param message
     *         the error message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithError(@NotNull final Class<?> cls,
                                            @NotNull final String message,
                                            @NotNull final Notification notification) {
        finishNotificationWithError(cls, new Exception(message), notification);
    }

    /**
     * Finish a notification in progress with an error.
     *
     * @param cls
     *         the class where the exception is caught.
     * @param exception
     *         the exception.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithError(@NotNull final Class<?> cls,
                                            @NotNull final Throwable exception,
                                            @NotNull final Notification notification) {
        finishNotification(exception.getMessage(), notification);
        Log.error(cls, exception);
    }

    /**
     * Finish a notification in progress with a warning.
     *
     * @param message
     *         the warning message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithWarning(@NotNull final String message, @NotNull final Notification notification) {
        finishNotification(message, notification);
    }

    /**
     * Finish a notification in progress.
     *
     * @param message
     *         the finish message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotification(@NotNull final String message, @NotNull final Notification notification) {
        notification.setContent(messages.notificationMessagePrefix(message));
        ((StatusNotification)notification).setStatus(SUCCESS);
    }
}
