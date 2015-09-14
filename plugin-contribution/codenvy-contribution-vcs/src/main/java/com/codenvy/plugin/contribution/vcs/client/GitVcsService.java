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
package com.codenvy.plugin.contribution.vcs.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCheckoutRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Git backed implementation for {@link VcsService}.
 */
public class GitVcsService implements VcsService {
    private static final String BRANCH_UP_TO_DATE_ERROR_MESSAGE = "Everything up-to-date";

    private final GitServiceClient       service;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public GitVcsService(@NotNull final DtoFactory dtoFactory,
                         @NotNull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         @NotNull final GitServiceClient service) {
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
    }

    @Override
    public void addRemote(@NotNull final ProjectDescriptor project, @NotNull final String remote, @NotNull final String remoteUrl,
                          @NotNull final AsyncCallback<Void> callback) {
        service.remoteAdd(project, remote, remoteUrl, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void checkoutBranch(@NotNull final ProjectDescriptor project, @NotNull final String name,
                               final boolean createNew, @NotNull final AsyncCallback<String> callback) {
        service.branchCheckout(project,
                               dtoFactory.createDto(BranchCheckoutRequest.class)
                                         .withName(name)
                                         .withCreateNew(createNew),
                               new AsyncRequestCallback<String>() {
                                   @Override
                                   protected void onSuccess(final String branchName) {
                                       callback.onSuccess(branchName);
                                   }

                                   @Override
                                   protected void onFailure(final Throwable exception) {
                                       callback.onFailure(exception);
                                   }
                               });
    }

    @Override
    public void commit(@NotNull final ProjectDescriptor project, final boolean includeUntracked, @NotNull final String commitMessage,
                       @NotNull final AsyncCallback<Void> callback) {
        try {

            service.add(project, !includeUntracked, null, new RequestCallback<Void>() {
                @Override
                protected void onSuccess(Void aVoid) {

                    service.commit(project, commitMessage, true, false, new AsyncRequestCallback<Revision>() {
                        @Override
                        protected void onSuccess(final Revision revision) {
                            callback.onSuccess(null);
                        }

                        @Override
                        protected void onFailure(final Throwable exception) {
                            callback.onFailure(exception);
                        }
                    });
                }

                @Override
                protected void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }
            });

        } catch (final WebSocketException exception) {
            callback.onFailure(exception);
        }
    }

    @Override
    public void deleteRemote(@NotNull final ProjectDescriptor project, @NotNull final String remote,
                             @NotNull final AsyncCallback<Void> callback) {
        service.remoteDelete(project, remote, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getBranchName(@NotNull final ProjectDescriptor project, @NotNull final AsyncCallback<String> callback) {
        service.status(project, new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
            @Override
            protected void onSuccess(final Status status) {
                callback.onSuccess(status.getBranchName());
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void hasUncommittedChanges(@NotNull final ProjectDescriptor project, @NotNull final AsyncCallback<Boolean> callback) {
        service.status(project, new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
            @Override
            protected void onSuccess(final Status status) {
                callback.onSuccess(!status.isClean());
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void isLocalBranchWithName(@NotNull final ProjectDescriptor project, @NotNull final String branchName,
                                      @NotNull final AsyncCallback<Boolean> callback) {

        listLocalBranches(project, new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                for (final Branch oneBranch : branches) {
                    if (oneBranch.getDisplayName().equals(branchName)) {
                        callback.onSuccess(true);
                        return;
                    }
                }
                callback.onSuccess(false);
            }
        });
    }

    @Override
    public void listLocalBranches(@NotNull final ProjectDescriptor project, @NotNull final AsyncCallback<List<Branch>> callback) {
        listBranches(project, null, callback);
    }

    @Override
    public void listRemotes(@NotNull final ProjectDescriptor project, @NotNull final AsyncCallback<List<Remote>> callback) {
        final Unmarshallable<List<Remote>> unMarshaller
                = dtoUnmarshallerFactory.newListUnmarshaller(org.eclipse.che.api.git.shared.Remote.class);
        service.remoteList(project, null, false,
                           new AsyncRequestCallback<List<Remote>>(unMarshaller) {
                               @Override
                               protected void onSuccess(final List<Remote> remotes) {
                                   final List<Remote> result = new ArrayList<>();
                                   for (final Remote remote : remotes) {
                                       result.add(fromGitRemote(remote));
                                   }
                                   callback.onSuccess(result);
                               }

                               @Override
                               protected void onFailure(final Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    @Override
    public void pushBranch(@NotNull final ProjectDescriptor project, @NotNull final String remote,
                           @NotNull final String localBranchName, @NotNull final AsyncCallback<PushResponse> callback) {
        service.push(project, Arrays.asList(localBranchName), remote, true, new AsyncRequestCallback<PushResponse>() {
            @Override
            protected void onSuccess(final PushResponse result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(exception.getMessage())) {
                    callback.onFailure(new BranchUpToDateException(localBranchName));

                } else {
                    callback.onFailure(exception);
                }
            }
        });
    }

    /**
     * List branches of a given type.
     *
     * @param project
     *         the project descriptor.
     * @param whichBranches
     *         null -> list local branches; "r" -> list remote branches; "a" -> list all branches.
     * @param callback
     *         callback when the operation is done.
     */
    private void listBranches(final ProjectDescriptor project, final String whichBranches, final AsyncCallback<List<Branch>> callback) {
        final Unmarshallable<List<Branch>> unMarshaller =
                dtoUnmarshallerFactory.newListUnmarshaller(Branch.class);
        service.branchList(project, whichBranches,
                           new AsyncRequestCallback<List<Branch>>(unMarshaller) {
                               @Override
                               protected void onSuccess(final List<Branch> branches) {
                                   final List<Branch> result = new ArrayList<>();
                                   for (final Branch branch : branches) {
                                       result.add(fromGitBranch(branch));
                                   }
                                   callback.onSuccess(result);
                               }

                               @Override
                               protected void onFailure(final Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    /**
     * Converts a git branch DTO to an abstracted {@link org.eclipse.che.api.git.shared.Branch} object.
     *
     * @param gitBranch
     *         the object to convert.
     * @return the converted object.
     */
    private Branch fromGitBranch(final Branch gitBranch) {
        final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
        branch.withActive(gitBranch.isActive()).withRemote(gitBranch.isRemote())
              .withName(gitBranch.getName()).withDisplayName(gitBranch.getDisplayName());
        return branch;
    }

    /**
     * Converts a git remote DTO to an abstracted {@link org.eclipse.che.api.git.shared.Remote} object.
     *
     * @param gitRemote
     *         the object to convert.
     * @return the converted object.
     */
    private Remote fromGitRemote(final Remote gitRemote) {
        final Remote remote = GitVcsService.this.dtoFactory.createDto(Remote.class);
        remote.withName(gitRemote.getName()).withUrl(gitRemote.getUrl());
        return remote;
    }
}
