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
package com.codenvy.plugin.contribution.vcs.client.hosting;

import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.CurrentUser;

import javax.validation.constraints.NotNull;

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
    @NotNull
    String getName();

    /**
     * Returns the VCS Host.
     *
     * @return the VCS Host never {@code null}.
     */
    @NotNull
    String getHost();

    /**
     * Checks if the given remote URL is hosted by this service.
     *
     * @param remoteUrl
     *         the remote url to check.
     * @return {@code true} if the given remote url is hosted by this service, {@code false} otherwise.
     */
    boolean isHostRemoteUrl(@NotNull String remoteUrl);

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
    void getPullRequest(@NotNull String owner,
                        @NotNull String repository,
                        @NotNull String username,
                        @NotNull String branchName,
                        @NotNull AsyncCallback<PullRequest> callback);

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
    void createPullRequest(@NotNull String owner,
                           @NotNull String repository,
                           @NotNull String username,
                           @NotNull String headRepository,
                           @NotNull String headBranchName,
                           @NotNull String baseBranchName,
                           @NotNull String title,
                           @NotNull String body,
                           @NotNull AsyncCallback<PullRequest> callback);

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
    void fork(@NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Returns the information of the given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     */
    void getRepository(@NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Returns the repository name from the given url.
     *
     * @param url
     *         the url.
     * @return the repository name, never {@code null}.
     */
    @NotNull
    String getRepositoryNameFromUrl(@NotNull String url);

    /**
     * Returns the repository owner from the given url.
     *
     * @param url
     *         the url.
     * @return the repository owner, never {@code null}.
     */
    @NotNull
    String getRepositoryOwnerFromUrl(@NotNull String url);

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
    void getUserFork(@NotNull String user, @NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Returns the user information on the repository host.
     *
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getUserInfo()}
     */
    @Deprecated
    void getUserInfo(@NotNull AsyncCallback<HostUser> callback);

    /**
     * Returns the user information on the repository host.
     */
    Promise<HostUser> getUserInfo();

    /**
     * Makes the remote SSH url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url, never {@code null}.
     */
    @NotNull
    String makeSSHRemoteUrl(@NotNull String username, @NotNull String repository);

    /**
     * Makes the remote HTTP url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url, never {@code null}.
     */
    @NotNull
    String makeHttpRemoteUrl(@NotNull String username, @NotNull String repository);

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
    @NotNull
    String makePullRequestUrl(@NotNull String username, @NotNull String repository, @NotNull String pullRequestNumber);

    /**
     * Use the VCS hosting comment markup language to format the review factory URL.
     *
     * @param reviewFactoryUrl
     *         the review factory URL to format.
     * @return the formatted review factory URL.
     */
    @NotNull
    String formatReviewFactoryUrl(@NotNull String reviewFactoryUrl);

    /**
     * Authenticate on the hosting service.
     *
     * @param user
     *         the user to authenticate
     * @param callback
     *         what to do once authentication is done
     * @deprecated use {@link #authenticate(CurrentUser)}
     */
    @Deprecated
    void authenticate(@NotNull CurrentUser user, @NotNull AsyncCallback<HostUser> callback);

    /**
     * Authenticates the current user on the hosting service.
     *
     * @param user
     *         the user to authenticate
     * @return the promise which resolves host user or rejects with an error
     */
    Promise<HostUser> authenticate(CurrentUser user);
}
