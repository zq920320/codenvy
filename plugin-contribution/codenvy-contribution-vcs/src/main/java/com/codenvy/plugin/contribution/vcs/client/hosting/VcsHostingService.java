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
package com.codenvy.plugin.contribution.vcs.client.hosting;

import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.CurrentUser;

import javax.annotation.Nonnull;

/**
 * Represents a repository host
 *
 * @author Kevin Pollet
 */
public interface VcsHostingService {
    /**
     * Returns the VCS Host name.
     *
     * @return the VCS Host name never {@code null}.
     */
    @Nonnull
    String getName();

    /**
     * Checks if the given remote URL is hosted by this service.
     *
     * @param remoteUrl
     *         the remote url to check.
     * @return {@code true} if the given remote url is hosted by this service, {@code false} otherwise.
     */
    boolean isHostRemoteUrl(@Nonnull String remoteUrl);

    /**
     * Get a pull request by qualified name.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param branchName
     *         pull request branch name.
     * @param callback
     *         callback called when operation is done.
     */
    void getPullRequest(@Nonnull String owner,
                        @Nonnull String repository,
                        @Nonnull String username,
                        @Nonnull String branchName,
                        @Nonnull AsyncCallback<PullRequest> callback);

    /**
     * Creates a pull request.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param headRepository
     *         the repository containing the head branch.
     * @param headBranchName
     *         the head branch name.
     * @param baseBranchName
     *         the base branch name.
     * @param title
     *         the pull request title.
     * @param body
     *         the pull request body.
     * @param callback
     *         callback called when operation is done.
     */
    void createPullRequest(@Nonnull String owner,
                           @Nonnull String repository,
                           @Nonnull String username,
                           @Nonnull String headRepository,
                           @Nonnull String headBranchName,
                           @Nonnull String baseBranchName,
                           @Nonnull String title,
                           @Nonnull String body,
                           @Nonnull AsyncCallback<PullRequest> callback);

    /**
     * Forks the given repository for the current user.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void fork(@Nonnull String owner, @Nonnull String repository, @Nonnull AsyncCallback<Repository> callback);

    /**
     * Returns the information of the given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     */
    void getRepository(@Nonnull String owner, @Nonnull String repository, @Nonnull AsyncCallback<Repository> callback);

    /**
     * Returns the repository name from the given url.
     *
     * @param url
     *         the url.
     * @return the repository name, never {@code null}.
     */
    @Nonnull
    String getRepositoryNameFromUrl(@Nonnull String url);

    /**
     * Returns the repository owner from the given url.
     *
     * @param url
     *         the url.
     * @return the repository owner, never {@code null}.
     */
    @Nonnull
    String getRepositoryOwnerFromUrl(@Nonnull String url);

    /**
     * Returns the repository fork of the given user.
     *
     * @param user
     *         the  user.
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getUserFork(@Nonnull String user, @Nonnull String owner, @Nonnull String repository, @Nonnull AsyncCallback<Repository> callback);

    /**
     * Returns the user information on the repository host.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void getUserInfo(@Nonnull AsyncCallback<HostUser> callback);

    /**
     * Makes the remote SSH url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url, never {@code null}.
     */
    @Nonnull
    String makeSSHRemoteUrl(@Nonnull String username, @Nonnull String repository);

    /**
     * Makes the remote HTTP url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url, never {@code null}.
     */
    @Nonnull
    String makeHttpRemoteUrl(@Nonnull String username, @Nonnull String repository);

    /**
     * Makes the pull request url for the given username, repository and pull request number.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @param pullRequestNumber
     *         the pull request number.
     * @return the remote url, never {@code null}.
     */
    @Nonnull
    String makePullRequestUrl(@Nonnull String username, @Nonnull String repository, @Nonnull String pullRequestNumber);

    /**
     * Use the VCS hosting comment markup language to format the review factory URL.
     *
     * @param reviewFactoryUrl
     *         the review factory URL to format.
     * @return the formatted review factory URL.
     */
    @Nonnull
    String formatReviewFactoryUrl(@Nonnull String reviewFactoryUrl);

    /**
     * Authenticate on the hosting service.
     *
     * @param user
     *         the user to authenticate
     * @param callback
     *         what to do once authentication is done
     */
    void authenticate(@Nonnull CurrentUser user, @Nonnull AsyncCallback<HostUser> callback);
}
