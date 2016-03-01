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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Links {
    /**
     * Get self link.
     *
     * @return {@link Link} self
     */
    Link getSelf();

    void setSelf(final Link self);

    Links withSelf(final Link self);

    /**
     * Get workItemUpdates link.
     *
     * @return {@link Link} workItemUpdates
     */
    Link getWorkItemUpdates();

    void setWorkItemUpdates(final Link workItemUpdates);

    Links withWorkItemUpdates(final Link workItemUpdates);

    /**
     * Get workItemRevisions link.
     *
     * @return {@link Link} workItemRevisions
     */
    Link getWorkItemRevisions();

    void setWorkItemRevisions(final Link workItemRevisions);

    Links withWorkItemRevisions(final Link workItemRevisions);

    /**
     * Get workItemHistory link.
     *
     * @return {@link Link} workItemHistory
     */
    Link getWorkItemHistory();

    void setWorkItemHistory(final Link workItemHistory);

    Links withWorkItemHistory(final Link workItemHistory);

    /**
     * Get html link.
     *
     * @return {@link Link} html
     */
    Link getHtml();

    void setHtml(final Link html);

    Links withHtml(final Link html);

    /**
     * Get workItemType link.
     *
     * @return {@link Link} workItemType
     */
    Link getWorkItemType();

    void setWorkItemType(final Link workItemType);

    Links withWorkItemType(final Link workItemType);

    /**
     * Get fields link.
     *
     * @return {@link Link} fields
     */
    Link getFields();

    void setFields(final Link fields);

    Links withFields(final Link fields);
}
