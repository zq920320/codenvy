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

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.ext.git.shared.PushResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Service for VCS operations.
 */
public interface VcsService {
    /**
     * Add a remote to the project VCS metadata.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name.
     * @param remoteUrl
     *         the remote URL.
     * @param callback
     *         callback when the operation is done.
     */
    void addRemote(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull String remoteUrl,
                   @Nonnull AsyncCallback<Void> callback);

    /**
     * Checkout a branch of the given project.
     *
     * @param project
     *         the project descriptor.
     * @param branchName
     *         the name of the branch to checkout.
     * @param createNew
     *         create a new branch if {@code true}.
     * @param callback
     *         callback when the operation is done.
     */
    void checkoutBranch(@Nonnull ProjectDescriptor project, @Nonnull String branchName, boolean createNew,
                        @Nonnull AsyncCallback<String> callback);

    /**
     * Commits the current changes of the given project.
     *
     * @param project
     *         the project descriptor.
     * @param includeUntracked
     *         {@code true} to include untracked files, {@code false} otherwise.
     * @param commitMessage
     *         the commit message.
     * @param callback
     *         callback when the operation is done.
     */
    void commit(@Nonnull ProjectDescriptor project, boolean includeUntracked, @Nonnull String commitMessage,
                @Nonnull AsyncCallback<Void> callback);

    /**
     * Removes a remote to the project VCS metadata.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name.
     * @param callback
     *         callback when the operation is done.
     */
    void deleteRemote(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull AsyncCallback<Void> callback);

    /**
     * Get the current branch for the project.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         callback when the operation is done.
     */
    void getBranchName(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<String> callback);

    /**
     * Returns if the given project has uncommitted changes.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         what to do if the project has uncommitted changes.
     */
    void hasUncommittedChanges(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<Boolean> callback);

    /**
     * Returns if a local branch with the given name exists in the given project.
     *
     * @param project
     *         the project descriptor.
     * @param branchName
     *         the branch name.
     * @param callback
     *         callback called when operation is done.
     */
    void isLocalBranchWithName(@Nonnull ProjectDescriptor project, @Nonnull String branchName, @Nonnull AsyncCallback<Boolean> callback);

    /**
     * List the local branches.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         what to do with the branches list.
     */
    void listLocalBranches(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<List<Branch>> callback);

    /**
     * List remotes.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         what to do with the remotes list
     */
    void listRemotes(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<List<Remote>> callback);

    /**
     * Push a local branch to the given remote.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name
     * @param localBranchNameToPush
     *         the local branch name
     * @param callback
     *         callback when the operation is done.
     */
    void pushBranch(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull String localBranchNameToPush,
                    @Nonnull AsyncCallback<PushResponse> callback);
}
