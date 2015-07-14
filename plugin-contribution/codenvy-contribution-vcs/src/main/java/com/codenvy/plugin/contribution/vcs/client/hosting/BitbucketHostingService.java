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
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequestHead;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.bitbucket.client.BitbucketClientService;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketLink;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequests;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.gwt.regexp.shared.RegExp.compile;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestBranch;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLinks;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLocation;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestRepository;
import static org.eclipse.che.ide.rest.HTTPStatus.BAD_REQUEST;
import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

/**
 * {@link VcsHostingService} implementation for Bitbucket.
 *
 * @author Kevin Pollet
 */
public class BitbucketHostingService implements VcsHostingService {
    private static final int    MAX_FORK_CREATION_ATTEMPT             = 10;
    private static final String REPOSITORY_EXISTS_ERROR_MESSAGE       = "You already have a repository with this name.";
    private static final String NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE = "There are no changes to be pulled";

    /*
     * URL related constants.
     */

    private static final String SSH_URL_PREFIX   = "git@bitbucket\\.org:";
    private static final String HTTPS_URL_PREFIX = "https:\\/\\/[^@]+@bitbucket\\.org\\/";
    private static final String OWNER_REPO_REGEX = "([^\\/]+)\\/([^\\/]+)\\.git";
    private static final RegExp SSH_URL_REGEXP   = compile(SSH_URL_PREFIX + OWNER_REPO_REGEX);
    private static final RegExp HTTPS_URL_REGEXP = compile(HTTPS_URL_PREFIX + OWNER_REPO_REGEX);

    private final AppContext appContext;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final DtoFactory              dtoFactory;
    private final BitbucketClientService  bitbucketClientService;
    private final HostingServiceTemplates templates;
    private final String                  baseUrl;

    @Inject
    public BitbucketHostingService(@Nonnull final AppContext appContext,
                                   @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   @Nonnull final DtoFactory dtoFactory,
                                   @Nonnull final BitbucketClientService bitbucketClientService,
                                   @Nonnull final BitBucketTemplates templates,
                                   @Nonnull @RestContext final String baseUrl) {
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;
        this.bitbucketClientService = bitbucketClientService;
        this.templates = templates;
        this.baseUrl = baseUrl;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Bitbucket";
    }

    @Override
    public boolean isHostRemoteUrl(@Nonnull final String remoteUrl) {
        return SSH_URL_REGEXP.test(remoteUrl) || HTTPS_URL_REGEXP.test(remoteUrl);
    }

    @Override
    public void getPullRequest(@Nonnull final String owner,
                               @Nonnull final String repository,
                               @Nonnull final String username,
                               @Nonnull final String branchName,
                               @Nonnull final AsyncCallback<PullRequest> callback) {

        final Unmarshallable<BitbucketPullRequests> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketPullRequests.class);

        bitbucketClientService.getRepositoryPullRequests(owner, repository, new AsyncRequestCallback<BitbucketPullRequests>(unmarshaller) {
            @Override
            protected void onSuccess(final BitbucketPullRequests bitbucketPullRequests) {

                for (final BitbucketPullRequest oneBitbucketPullRequest : bitbucketPullRequests.getPullRequests()) {

                    final BitbucketUser author = oneBitbucketPullRequest.getAuthor();
                    final BitbucketPullRequestLocation source = oneBitbucketPullRequest.getSource();
                    if (author != null && source != null) {
                        final BitbucketPullRequestBranch pullRequestBranch = source.getBranch();

                        if (username.equals(author.getUsername()) && branchName.equals(pullRequestBranch.getName())) {
                            callback.onSuccess(valueOf(oneBitbucketPullRequest));
                            return;
                        }
                    }
                }

                callback.onFailure(new NoPullRequestException(branchName));
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void createPullRequest(@Nonnull final String owner,
                                  @Nonnull final String repository,
                                  @Nonnull final String username,
                                  @Nonnull final String headRepository,
                                  @Nonnull final String headBranchName,
                                  @Nonnull final String baseBranchName,
                                  @Nonnull final String title,
                                  @Nonnull final String body,
                                  @Nonnull final AsyncCallback<PullRequest> callback) {

        final Unmarshallable<BitbucketPullRequest> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketPullRequest.class);

        final BitbucketPullRequestLocation bitbucketPullRequestDestination = dtoFactory
                .createDto(BitbucketPullRequestLocation.class)
                .withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class).withName(baseBranchName));

        final BitbucketPullRequestLocation bitbucketPullRequestSource = dtoFactory
                .createDto(BitbucketPullRequestLocation.class)
                .withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class).withName(headBranchName))
                .withRepository(dtoFactory.createDto(BitbucketPullRequestRepository.class).withFullName(username + "/" + headRepository));

        final BitbucketPullRequest bitbucketPullRequest = dtoFactory
                .createDto(BitbucketPullRequest.class)
                .withTitle(title)
                .withDescription(body)
                .withDestination(bitbucketPullRequestDestination)
                .withSource(bitbucketPullRequestSource);

        bitbucketClientService
                .openPullRequest(owner, repository, bitbucketPullRequest, new AsyncRequestCallback<BitbucketPullRequest>(unmarshaller) {
                    @Override
                    protected void onSuccess(final BitbucketPullRequest bitbucketPullRequest) {
                        callback.onSuccess(valueOf(bitbucketPullRequest));
                    }

                    @Override
                    protected void onFailure(final Throwable exception) {
                        if (exception instanceof ServerException) {
                            final ServerException serverException = (ServerException)exception;
                            final String exceptionMessage = serverException.getMessage();

                            if (serverException.getHTTPStatus() == BAD_REQUEST
                                && exceptionMessage != null
                                && containsIgnoreCase(exceptionMessage, NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE)) {

                                callback.onFailure(new NoCommitsInPullRequestException(headBranchName, baseBranchName));
                            }

                        } else {
                            callback.onFailure(exception);
                        }
                    }
                });
    }

    @Override
    public void fork(@Nonnull final String owner, @Nonnull final String repository, @Nonnull final AsyncCallback<Repository> callback) {
        getRepository(owner, repository, new AsyncCallback<Repository>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final Repository repository) {
                fork(owner, repository.getName(), 0, repository.isPrivateRepo(), new AsyncCallback<BitbucketRepositoryFork>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onSuccess(final BitbucketRepositoryFork bitbucketRepositoryFork) {
                        callback.onSuccess(dtoFactory.createDto(Repository.class)
                                                     .withName(bitbucketRepositoryFork.getName())
                                                     .withFork(true)
                                                     .withParent(repository)
                                                     .withPrivateRepo(bitbucketRepositoryFork.isIsPrivate()));
                    }
                });
            }
        });
    }

    private void fork(@Nonnull final String owner,
                      @Nonnull final String repository,
                      final int number,
                      final boolean isForkPrivate,
                      final AsyncCallback<BitbucketRepositoryFork> callback) {

        final Unmarshallable<BitbucketRepositoryFork> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketRepositoryFork.class);
        final String forkName = number == 0 ? repository : (repository + "-" + number);

        bitbucketClientService
                .forkRepository(owner, repository, forkName, isForkPrivate,
                                new AsyncRequestCallback<BitbucketRepositoryFork>(unmarshaller) {
                                    @Override
                                    protected void onSuccess(final BitbucketRepositoryFork bitbucketRepository) {
                                        callback.onSuccess(bitbucketRepository);
                                    }

                                    @Override
                                    protected void onFailure(final Throwable exception) {
                                        if (number < MAX_FORK_CREATION_ATTEMPT && exception instanceof ServerException) {
                                            final ServerException serverException = (ServerException)exception;
                                            final String exceptionMessage = serverException.getMessage();

                                            if (serverException.getHTTPStatus() == BAD_REQUEST
                                                && exceptionMessage != null
                                                && containsIgnoreCase(exceptionMessage, REPOSITORY_EXISTS_ERROR_MESSAGE)) {

                                                fork(owner, repository, number + 1, isForkPrivate, callback);
                                            }

                                        } else {
                                            callback.onFailure(exception);
                                        }
                                    }
                                });
    }

    @Override
    public void getRepository(@Nonnull final String owner,
                              @Nonnull final String repository,
                              @Nonnull final AsyncCallback<Repository> callback) {

        final Unmarshallable<BitbucketRepository> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketRepository.class);

        bitbucketClientService.getRepository(owner, repository, new AsyncRequestCallback<BitbucketRepository>(unmarshaller) {
            @Override
            protected void onSuccess(final BitbucketRepository bitbucketRepository) {
                callback.onSuccess(valueOf(bitbucketRepository));
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Nonnull
    @Override
    public String getRepositoryNameFromUrl(@Nonnull final String url) {
        if (SSH_URL_REGEXP.test(url)) {
            return SSH_URL_REGEXP.exec(url).getGroup(2);
        }
        return HTTPS_URL_REGEXP.exec(url).getGroup(2);
    }

    @Nonnull
    @Override
    public String getRepositoryOwnerFromUrl(@Nonnull final String url) {
        if (SSH_URL_REGEXP.test(url)) {
            return SSH_URL_REGEXP.exec(url).getGroup(1);
        }
        return HTTPS_URL_REGEXP.exec(url).getGroup(1);
    }

    @Override
    public void getUserFork(@Nonnull final String user,
                            @Nonnull final String owner,
                            @Nonnull final String repository,
                            @Nonnull final AsyncCallback<Repository> callback) {

        final Unmarshallable<BitbucketRepositories> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketRepositories.class);

        bitbucketClientService.getRepositoryForks(owner, repository, new AsyncRequestCallback<BitbucketRepositories>(unmarshaller) {
            @Override
            protected void onSuccess(final BitbucketRepositories bitbucketRepositories) {
                for (final BitbucketRepository oneBitbucketRepository : bitbucketRepositories.getRepositories()) {
                    final BitbucketUser owner = oneBitbucketRepository.getOwner();

                    if (owner != null && user.equals(owner.getUsername())) {
                        callback.onSuccess(valueOf(oneBitbucketRepository));
                        return;
                    }
                }

                callback.onFailure(new NoUserForkException(user));
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getUserInfo(@Nonnull final AsyncCallback<HostUser> callback) {
        final Unmarshallable<BitbucketUser> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BitbucketUser.class);

        bitbucketClientService.getUser(new AsyncRequestCallback<BitbucketUser>(unmarshaller) {
            @Override
            protected void onSuccess(final BitbucketUser user) {
                final HostUser hostUser = dtoFactory.createDto(HostUser.class)
                                                    .withId(user.getUuid())
                                                    .withName(user.getDisplayName())
                                                    .withLogin(user.getUsername())
                                                    .withUrl(user.getLinks().getSelf().getHref());

                callback.onSuccess(hostUser);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Nonnull
    @Override
    public String makeSSHRemoteUrl(@Nonnull final String username, @Nonnull final String repository) {
        return templates.sshUrlTemplate(username, repository);
    }

    @Nonnull
    @Override
    public String makeHttpRemoteUrl(@Nonnull final String username, @Nonnull final String repository) {
        return templates.httpUrlTemplate(username, repository);
    }

    @Nonnull
    @Override
    public String makePullRequestUrl(@Nonnull final String username,
                                     @Nonnull final String repository,
                                     @Nonnull final String pullRequestNumber) {
        return templates.pullRequestUrlTemplate(username, repository, pullRequestNumber);
    }

    @Nonnull
    @Override
    public String formatReviewFactoryUrl(@Nonnull final String reviewFactoryUrl) {
        final String protocol = Window.Location.getProtocol();
        final String host = Window.Location.getHost();

        return templates.formattedReviewFactoryUrlTemplate(protocol, host, reviewFactoryUrl);
    }

    @Override
    public void authenticate(@Nonnull final CurrentUser user, @Nonnull final AsyncCallback<HostUser> callback) {
        final WorkspaceDescriptor workspace = this.appContext.getWorkspace();
        if (workspace == null) {
            callback.onFailure(new Exception("Error accessing current workspace"));
            return;
        }
        final String authUrl = baseUrl
                               + "/oauth/1.0/authenticate?oauth_provider=bitbucket&userId=" + user.getProfile().getId()
                               + "&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + workspace.getName();

        new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {
            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                getUserInfo(callback);
            }
        }).loginWithOAuth();
    }


    /**
     * Converts an instance of {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository} into a {@link
     * com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository}.
     *
     * @param bitbucketRepository
     *         the Bitbucket repository to convert.
     * @return the corresponding {@link com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository} instance or {@code null} if given
     * bitbucketRepository is {@code null}.
     */
    private Repository valueOf(final BitbucketRepository bitbucketRepository) {
        if (bitbucketRepository == null) {
            return null;
        }

        final BitbucketRepository bitbucketRepositoryParent = bitbucketRepository.getParent();
        final Repository parent = bitbucketRepositoryParent == null ? null :
                                  dtoFactory.createDto(Repository.class)
                                            .withFork(bitbucketRepositoryParent.getParent() != null)
                                            .withName(bitbucketRepositoryParent.getName())
                                            .withParent(null)
                                            .withPrivateRepo(bitbucketRepositoryParent.isIsPrivate())
                                            .withCloneUrl(getCloneHttpsUrl(bitbucketRepositoryParent));

        return dtoFactory.createDto(Repository.class)
                         .withFork(bitbucketRepositoryParent != null)
                         .withName(bitbucketRepository.getName())
                         .withParent(parent)
                         .withPrivateRepo(bitbucketRepository.isIsPrivate())
                         .withCloneUrl(getCloneHttpsUrl(bitbucketRepository));
    }

    /**
     * Converts an instance of {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest} into a {@link
     * com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest}.
     *
     * @param bitbucketPullRequest
     *         the bitbucket pull request to convert.
     * @return the corresponding {@link com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest} instance or {@code null} if
     * given bitbucketPullRequest is {@code null}.
     */
    private PullRequest valueOf(final BitbucketPullRequest bitbucketPullRequest) {
        if (bitbucketPullRequest == null) {
            return null;
        }

        final String pullRequestId = String.valueOf(bitbucketPullRequest.getId());
        final BitbucketPullRequestLocation pullRequestSource = bitbucketPullRequest.getSource();
        final BitbucketPullRequestBranch pullRequestBranch = pullRequestSource != null ? pullRequestSource.getBranch() : null;
        final BitbucketPullRequestLinks pullRequestLinks = bitbucketPullRequest.getLinks();
        final BitbucketLink pullRequestHtmlLink = pullRequestLinks != null ? pullRequestLinks.getHtml() : null;
        final BitbucketLink pullRequestSelfLink = pullRequestLinks != null ? pullRequestLinks.getSelf() : null;

        final PullRequestHead pullRequestHead = dtoFactory.createDto(PullRequestHead.class)
                                                          .withLabel(pullRequestBranch != null ? pullRequestBranch.getName() : null);

        return dtoFactory.createDto(PullRequest.class)
                         .withId(pullRequestId)
                         .withUrl(pullRequestSelfLink != null ? pullRequestSelfLink.getHref() : null)
                         .withHtmlUrl(pullRequestHtmlLink != null ? pullRequestHtmlLink.getHref() : null)
                         .withNumber(pullRequestId)
                         .withState(bitbucketPullRequest.getState().name())
                         .withHead(pullRequestHead);
    }

    /**
     * Return the HTTPS clone url for the given {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     *
     * @param bitbucketRepository
     *         the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     * @return the HTTPS clone url from the clone links or {@code null} if none.
     */
    private String getCloneHttpsUrl(@Nonnull final BitbucketRepository bitbucketRepository) {
        if (bitbucketRepository.getLinks() != null && bitbucketRepository.getLinks().getClone() != null) {
            for (final BitbucketLink oneCloneLink : bitbucketRepository.getLinks().getClone()) {
                if (oneCloneLink.getName() != null && "https".equals(oneCloneLink.getName())) {
                    return oneCloneLink.getHref();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "BitbucketHostingService";
    }
}
