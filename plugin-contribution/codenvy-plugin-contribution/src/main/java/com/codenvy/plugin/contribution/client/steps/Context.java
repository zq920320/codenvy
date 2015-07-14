/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Objects;

import static com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent.ContextProperty.CLONED_BRANCH_NAME;
import static com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent.ContextProperty.PROJECT;
import static com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent.ContextProperty.WORK_BRANCH_NAME;

/**
 * Context used to share information between the steps in the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class Context {
    /** The event bus. */
    private final EventBus eventBus;

    /** The project. */
    private ProjectDescriptor project;

    /** The name of the cloned branch. */
    private String clonedBranchName;

    /** The name of the working branch. */
    private String workBranchName;

    /** The name of the user on host VCS. */
    private String hostUserLogin;

    /** The name of the owner of the repository forked on VCS. */
    private String upstreamRepositoryOwner;

    /** The name of the repository forked on VCS. */
    private String upstreamRepositoryName;

    /** The name of the owner of the repository cloned on VCS. */
    private String originRepositoryOwner;

    /** The name of the repository cloned on VCS. */
    private String originRepositoryName;

    /** The identifier of the pull request on the hosting service. */
    private String pullRequestId;

    /** The issue number of the pull request issued for the contribution. */
    private String pullRequestIssueNumber;

    /** The generated review factory URL. */
    private String reviewFactoryUrl;

    /** The name of the forked remote. */
    private String forkedRemoteName;

    /** The name of the forked repository. */
    private String forkedRepositoryName;

    @Inject
    public Context(@Nonnull final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public ProjectDescriptor getProject() {
        return project;
    }

    public void setProject(final ProjectDescriptor project) {
        final ProjectDescriptor oldValue = this.project;
        this.project = project;

        fireContextPropertyChange(PROJECT, oldValue, project);
    }

    public String getClonedBranchName() {
        return clonedBranchName;
    }

    public void setClonedBranchName(final String clonedBranchName) {
        final String oldValue = this.clonedBranchName;
        this.clonedBranchName = clonedBranchName;

        fireContextPropertyChange(CLONED_BRANCH_NAME, oldValue, clonedBranchName);
    }

    public String getWorkBranchName() {
        return workBranchName;
    }

    public void setWorkBranchName(final String workBranchName) {
        final String oldValue = this.workBranchName;
        this.workBranchName = workBranchName;

        fireContextPropertyChange(WORK_BRANCH_NAME, oldValue, workBranchName);
    }

    public String getHostUserLogin() {
        return hostUserLogin;
    }

    public void setHostUserLogin(final String hostUserLogin) {
        this.hostUserLogin = hostUserLogin;
    }

    public String getUpstreamRepositoryOwner() {
        return upstreamRepositoryOwner;
    }

    public void setUpstreamRepositoryOwner(String upstreamRepositoryOwner) {
        this.upstreamRepositoryOwner = upstreamRepositoryOwner;
    }

    public String getUpstreamRepositoryName() {
        return upstreamRepositoryName;
    }

    public void setUpstreamRepositoryName(String upstreamRepositoryName) {
        this.upstreamRepositoryName = upstreamRepositoryName;
    }

    public String getOriginRepositoryOwner() {
        return originRepositoryOwner;
    }

    public void setOriginRepositoryOwner(final String originRepositoryOwner) {
        final String oldValue = this.originRepositoryOwner;
        this.originRepositoryOwner = originRepositoryOwner;

        fireContextPropertyChange(ContextPropertyChangeEvent.ContextProperty.ORIGIN_REPOSITORY_OWNER, oldValue, originRepositoryOwner);
    }

    public String getOriginRepositoryName() {
        return originRepositoryName;
    }

    public void setOriginRepositoryName(final String originRepositoryName) {
        final String oldValue = this.originRepositoryName;
        this.originRepositoryName = originRepositoryName;

        fireContextPropertyChange(ContextPropertyChangeEvent.ContextProperty.ORIGIN_REPOSITORY_NAME, oldValue, originRepositoryName);
    }

    /**
     * Return the pull request id for this contribution.
     *
     * @return the pull request id
     */
    public String getPullRequestId() {
        return pullRequestId;
    }

    /**
     * Sets the pull request id for this contribution.
     *
     * @param pullRequestId
     *         the new value
     */
    public void setPullRequestId(final String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    /**
     * Return the issue number of the pull request issued for this contribution.
     *
     * @return the pull request issue id
     */
    public String getPullRequestIssueNumber() {
        return pullRequestIssueNumber;
    }

    /**
     * Sets the issue number of the pull request issued for this contribution.
     *
     * @param pullRequestIssueNumber
     *         the new value
     */
    public void setPullRequestIssueNumber(final String pullRequestIssueNumber) {
        this.pullRequestIssueNumber = pullRequestIssueNumber;
    }

    /**
     * Returns the generated review factory URL (if available).
     *
     * @return factory URL
     */
    public String getReviewFactoryUrl() {
        return this.reviewFactoryUrl;
    }

    /**
     * Sets the generated review factory URL (if available).
     *
     * @param reviewFactoryUrl
     *         new value
     */
    public void setReviewFactoryUrl(final String reviewFactoryUrl) {
        this.reviewFactoryUrl = reviewFactoryUrl;
    }

    public String getForkedRemoteName() {
        return forkedRemoteName;
    }

    public void setForkedRemoteName(String forkedRemoteName) {
        this.forkedRemoteName = forkedRemoteName;
    }

    public String getForkedRepositoryName() {
        return forkedRepositoryName;
    }

    public void setForkedRepositoryName(String forkedRepositoryName) {
        this.forkedRepositoryName = forkedRepositoryName;
    }

    private void fireContextPropertyChange(final ContextPropertyChangeEvent.ContextProperty contextProperty, final Object oldValue,
                                           final Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            eventBus.fireEvent(new ContextPropertyChangeEvent(this, contextProperty));
        }
    }
}
