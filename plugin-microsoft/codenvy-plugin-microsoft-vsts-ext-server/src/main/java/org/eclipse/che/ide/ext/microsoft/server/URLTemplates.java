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
package org.eclipse.che.ide.ext.microsoft.server;

import com.google.api.client.repackaged.com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkArgument;
import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Defines URL templates for Mirosoft VSTS API api version 1.0.
 *
 * @author Yevhenii Voevodin
 */
@Beta
@Singleton
public class URLTemplates {

    private static final String SCHEMA = "https";
    private static final String HOST   = "visualstudio.com";

    /** Links for {@link #getTeamBaseUrl()} url. */
    private static final String REPOSITORY                = "/%s/_apis/git/repositories/%s";
    private static final String REPOSITORIES              = "/%s/_apis/git/repositories";
    private static final String PULL_REQUESTS             = "/_apis/git/repositories/%s/pullrequests";
    private static final String PULL_REQUEST              = "/_apis/git/repositories/%s/pullrequests/%s";
    private static final String PROJECT_REPO_PULL_REQUEST = "/%s/_apis/git/repositories/%s/pullrequests/%s";

    /** Links for {@link #getAppVsspsBaseUrl()} url. */
    public static final String PROFILE = "/_apis/profile/profiles/me";

    private static final String HTML_PULL_REQUEST         = "/%s/_git/%s/pullrequest/%s";

    private final String accountName;
    private final String apiVersion;
    private final String collection;

    @Inject
    public URLTemplates(@Named("microsoft.vsts.rest.client.account_name") String accountName,
                        @Named("microsoft.vsts.rest.client.api_version") String apiVersion,
                        @Named("microsoft.vsts.rest.client.collection") String collection) {
        this.accountName = accountName;
        this.apiVersion = apiVersion;
        this.collection = collection;
    }

    public String profileUrl() {
        return getAppVsspsBaseUrl() + PROFILE;
    }

    /**
     * Returns repository url.
     *
     * @param project
     *         team project id or name
     * @param repoName
     *         id or name of the repository
     * @throws IllegalArgumentException
     *         when either {@code project} or {@code repoName} is null or empty
     */
    public String repositoryUrl(String project, String repoName) {
        checkArgument(!isNullOrEmpty(project), "Project name required");
        checkArgument(!isNullOrEmpty(repoName), "Repository name required");
        return getTeamBaseUrl() + format(REPOSITORY, project, repoName) + getApiVersion();
    }

    /**
     * Returns repositories url.
     *
     * @param project
     *         team project id or name
     * @throws IllegalArgumentException
     *         when {@code project} is null or empty
     */
    public String repositoriesUrl(String project) {
        checkArgument(!isNullOrEmpty(project), "Project required");
        return getTeamBaseUrl() + format(REPOSITORIES, project) + getApiVersion();
    }

    /**
     * Returns the url for pull requests.
     *
     * @param repoId
     *         id of the repository
     * @throws IllegalArgumentException
     *         when {@code repository} is null or empty
     */
    public String pullRequestsUrl(String repoId) {
        checkArgument(!isNullOrEmpty(repoId), "Repository id required");
        return getTeamBaseUrl() + format(PULL_REQUESTS, repoId) + getApiVersion();
    }

    /**
     * Returns pull request url.
     *
     * @param repoId
     *         id of the repository
     * @param pullRequest
     *         id of the pull request
     * @throws IllegalArgumentException
     *         when either {@code repository} or {@code pullRequest} is null or empty
     */
    public String pullRequestUrl(String repoId, String pullRequest) {
        checkArgument(!isNullOrEmpty(repoId), "Repository required");
        checkArgument(!isNullOrEmpty(pullRequest), "Pull request required");
        return getTeamBaseUrl() + format(PULL_REQUEST, repoId, pullRequest) + getApiVersion();
    }

    /**
     * Returns pull request url.
     *
     * @param projectName
     *         the name of the project
     * @param repositoryName
     *         the name of the repository
     * @param prId
     *         the id of the pull request
     */
    public String pullRequestUrl(String projectName, String repositoryName, String prId) {
        checkArgument(!isNullOrEmpty(projectName), "Project name required");
        checkArgument(!isNullOrEmpty(repositoryName), "Repository name required");
        checkArgument(!isNullOrEmpty(prId), "Pull request id required");
        return getTeamBaseUrl() + format(PROJECT_REPO_PULL_REQUEST, projectName, repositoryName, prId) + getApiVersion();
    }

    /**
     * Returns pull request html url.
     *
     * @param projectName
     *         the name of the project
     * @param repositoryName
     *         the name of the repository
     * @param prId
     *         the id of the pull request
     */
    public String pullRequestHtmlUrl(String projectName, String repositoryName, String prId) {
        checkArgument(!isNullOrEmpty(projectName), "Project name required");
        checkArgument(!isNullOrEmpty(repositoryName), "Repository name required");
        checkArgument(!isNullOrEmpty(prId), "Pull request id required");
        return getTeamBaseUrl() + format(HTML_PULL_REQUEST, projectName, repositoryName, prId);
    }

    private String getTeamBaseUrl() {
        return SCHEMA + "://" + accountName + '.' + HOST + '/' + collection;
    }

    private String getAppVsspsBaseUrl() {
        return SCHEMA + "://app.vssps." + HOST;
    }

    private String getApiVersion() {
        return "?api-version=" + apiVersion;
    }
}
