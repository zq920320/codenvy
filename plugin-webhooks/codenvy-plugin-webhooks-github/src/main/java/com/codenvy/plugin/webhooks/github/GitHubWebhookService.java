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
package com.codenvy.plugin.webhooks.github;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.plugin.webhooks.AuthConnection;
import com.codenvy.plugin.webhooks.FactoryConnection;
import com.codenvy.plugin.webhooks.BaseWebhookService;
import com.codenvy.plugin.webhooks.connectors.Connector;
import com.codenvy.plugin.webhooks.github.shared.PullRequestEvent;
import com.codenvy.plugin.webhooks.github.shared.PushEvent;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(
        value = "/github-webhook",
        description = "GitHub webhooks handler"
)
@Path("/github-webhook")
public class GitHubWebhookService extends BaseWebhookService {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookService.class);

    private final FactoryConnection factoryConnection;

    private static final String GITHUB_WEBHOOKS_PROPERTIES_FILENAME = "github-webhooks.properties";
    private static final String GITHUB_REQUEST_HEADER               = "X-GitHub-Event";

    @Inject
    public GitHubWebhookService(final AuthConnection authConnection, final FactoryConnection factoryConnection) {
        super(authConnection);

        this.factoryConnection = factoryConnection;
    }

    @ApiOperation(value = "Handle GitHub webhook events",
                  response = Response.class)
    @ApiResponses({
                          @ApiResponse(code = 200, message = "OK"),
                          @ApiResponse(code = 202, message = "The request has been accepted for processing, but the processing has not been completed."),
                          @ApiResponse(code = 500, message = "Internal Server Error")
                  })
    @POST
    @Consumes(APPLICATION_JSON)
    public Response handleGithubWebhookEvent(@ApiParam(value = "New contribution", required = true)
                                             @Context HttpServletRequest request)
            throws ServerException {

        Response response = null;
        try (ServletInputStream inputStream = request.getInputStream()) {
            if (inputStream != null) {
                String githubHeader = request.getHeader(GITHUB_REQUEST_HEADER);
                if (!isNullOrEmpty(githubHeader)) {
                    switch (githubHeader) {
                        case "push":
                            final PushEvent pushEvent = DtoFactory.getInstance().createDtoFromJson(inputStream, PushEvent.class);
                            response = handlePushEvent(pushEvent);
                            break;
                        case "pull_request":
                            final PullRequestEvent PRevent = DtoFactory.getInstance().createDtoFromJson(inputStream, PullRequestEvent.class);
                            response = handlePullRequestEvent(PRevent);
                            break;
                        default:
                            response = Response.accepted(new GenericEntity<>(
                                    "GitHub message \'" + githubHeader + "\' received. It isn't intended to be processed.", String.class))
                                               .build();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }

        return response;
    }

    /**
     * Handle GitHub {@link PushEvent}
     *
     * @param contribution
     *         the push event to handle
     * @return HTTP 200 response if event was processed successfully
     * HTTP 202 response if event was processed partially
     * @throws ServerException
     */
    private Response handlePushEvent(PushEvent contribution) throws ServerException {
        LOG.debug("{}", contribution);

        // Set current Codenvy user
        EnvironmentContext.getCurrent().setUser(new TokenUser());

        // Get contribution data
        final String contribRepositoryHtmlUrl = contribution.getRepository().getHtmlUrl();
        final String[] contribRefSplit = contribution.getRef().split("/");
        final String contribBranch = contribRefSplit[contribRefSplit.length - 1];

        // Get factory that:
        // 1) Is configured in a webhook
        // 2) Contains a project for given repository and branch
        final Optional<Factory> factory = Optional.ofNullable(
                getWebhookConfiguredFactory(contribRepositoryHtmlUrl, contribRepositoryHtmlUrl, contribBranch));
        if (!factory.isPresent()) {
            return Response.accepted(
                    new GenericEntity<>("No factory found for repository " + contribRepositoryHtmlUrl + " and branch " + contribBranch,
                                        String.class)).build();
        }

        // Add factory link within third-party services
        Factory f = factory.get();
        // Get 'open factory' URL
        final Optional<Link> factoryLink = Optional.ofNullable(f.getLink(FACTORY_URL_REL));
        if (!factoryLink.isPresent()) {
            return Response.accepted(
                    new GenericEntity<>("Factory " + f.getId() + " do not contain mandatory \'" + FACTORY_URL_REL + "\' link",
                                        String.class))
                           .build();
        }
        final Link link = factoryLink.get();

        // Get connectors configured for the factory
        final List<Connector> connectors = getConnectors(f.getId());

        // Add factory link within third-party services
        connectors.forEach(connector -> connector.addFactoryLink(link.getHref()));
        return Response.ok().build();
    }

    /**
     * Handle GitHub {@link PullRequestEvent}
     *
     * @param prEvent
     *         the pull request event to handle
     * @return HTTP 200 response if event was processed successfully
     * HTTP 202 response if event was processed partially
     * @throws ServerException
     */
    private Response handlePullRequestEvent(PullRequestEvent prEvent) throws ServerException {
        LOG.debug("{}", prEvent);

        // Set current Codenvy user
        EnvironmentContext.getCurrent().setUser(new TokenUser());

        // Check that event indicates a successful merging
        final String action = prEvent.getAction();
        if (!"closed".equals(action)) {
            return Response
                    .accepted(new GenericEntity<>(
                            "PullRequest Event action is " + action + ". " + this.getClass().getSimpleName() + " do not handle this one.",
                            String.class))
                    .build();
        }
        final boolean isMerged = prEvent.getPullRequest().getMerged();
        if (!isMerged) {
            return Response.accepted(new GenericEntity<>("Pull Request was closed with unmerged commits !", String.class)).build();
        }

        // Get head repository data
        final String prHeadRepositoryHtmlUrl = prEvent.getPullRequest().getHead().getRepo().getHtmlUrl();
        final String prHeadBranch = prEvent.getPullRequest().getHead().getRef();
        final String prHeadCommitId = prEvent.getPullRequest().getHead().getSha();

        // Get base repository data
        final String prBaseRepositoryHtmlUrl = prEvent.getPullRequest().getBase().getRepo().getHtmlUrl();

        // Get factory that:
        // 1) Is configured in a webhook
        // 2) Contains a project for given repository and branch
        final Optional<Factory> factory =
                Optional.ofNullable(getWebhookConfiguredFactory(prBaseRepositoryHtmlUrl, prHeadRepositoryHtmlUrl, prHeadBranch));
        if (!factory.isPresent()) {
            return Response.accepted(new GenericEntity<>("No factory found for branch " + prHeadBranch, String.class)).build();
        }
        final Factory f = factory.get();

        // Update project into the factory with given repository and branch
        final Factory updatedfactory =
                updateProjectInFactory(f, matchingProjectPredicate(prHeadRepositoryHtmlUrl, prHeadBranch), prBaseRepositoryHtmlUrl,
                                       prHeadCommitId);

        // Update factory with new project data
        final Optional<Factory> persistedFactory = Optional.ofNullable(factoryConnection.updateFactory(updatedfactory));
        if (!persistedFactory.isPresent()) {
            return Response.accepted(
                    new GenericEntity<>(
                            "Error during update of factory with source location " + prBaseRepositoryHtmlUrl + " & commitId " +
                            prHeadCommitId,
                            String.class)).build();
        }
        LOG.debug("Factory successfully updated with source location {} & commitId {}", prBaseRepositoryHtmlUrl, prHeadCommitId);

        // TODO Remove factory id from webhook

        return Response.ok().build();
    }

    /**
     * Get factory that is configured in a webhook for given base repository
     * and contains a project for given head repository and head branch
     *
     * @param baseRepositoryHtmlUrl
     *         the URL of the repository for which a webhook is configured
     * @param headRepositoryHtmlUrl
     *         the URL of the repository that a project into the factory is configured with
     * @param headBranch
     *         the name of the branch that a project into the factory is configured with
     * @return the factory that is configured in a webhook and contains a project that matches given repo and branch
     * @throws ServerException
     */
    private Factory getWebhookConfiguredFactory(String baseRepositoryHtmlUrl, String headRepositoryHtmlUrl, String headBranch)
            throws ServerException {

        // Get webhook configured for given repository
        final Optional<GithubWebhook> webhook = Optional.ofNullable(getGitHubWebhook(baseRepositoryHtmlUrl));
        if (!webhook.isPresent()) {
            throw new ServerException("No webhook configured for repository " + baseRepositoryHtmlUrl);
        }
        final GithubWebhook w = webhook.get();

        // Get factory id's listed into the webhook
        final List<String> factoryIDs = Arrays.asList(w.getFactoryIDs());

        // Get factory that contains a project for given repository and branch
        Factory factory = null;
        for (String factoryId : factoryIDs) {
            Optional<Factory> obtainedFactory = Optional.ofNullable(factoryConnection.getFactory(factoryId));
            if (obtainedFactory.isPresent()) {
                final Factory f = obtainedFactory.get();
                final List<ProjectConfigDto> projects = f.getWorkspace().getProjects()
                                                         .stream()
                                                         .filter(matchingProjectPredicate(headRepositoryHtmlUrl, headBranch))
                                                         .collect(toList());
                if (!projects.isEmpty()) {
                    factory = f;
                    break;
                }
            }
        }
        return factory;
    }

    /**
     * Get a {@link java.util.function.Predicate} that matches project configured with given repository and branch
     *
     * @param repositoryHtmlUrl
     *         the repo that the project has to match
     * @param branch
     *         the branch that the project has to match
     * @return the {@link java.util.function.Predicate} that matches relevant project(s)
     */
    private Predicate<ProjectConfigDto> matchingProjectPredicate(String repositoryHtmlUrl, String branch) {
        return (p -> p.getSource() != null
                     && !isNullOrEmpty(p.getSource().getType())
                     && !isNullOrEmpty(p.getSource().getLocation())
                     && (repositoryHtmlUrl.equals(p.getSource().getLocation())
                         || (repositoryHtmlUrl + ".git").equals(p.getSource().getLocation()))
                     && ("master".equals(branch)
                         || (!isNullOrEmpty(p.getSource().getParameters().get("branch"))
                             && branch.equals(p.getSource().getParameters().get("branch")))));
    }

    /**
     * Update project matching given predicate in given factory
     *
     * @param factory
     *         the factory to search for projects
     * @param matchingProjectPredicate
     *         the matching predicate project must fulfill
     * @param baseRepository
     *         the repository to set as source location for matching project in factory
     * @param headCommitId
     *         the commitId to set as 'commitId' parameter for matching project in factory
     * @return the project that matches the predicate given in argument
     * @throws ServerException
     */
    private Factory updateProjectInFactory(Factory factory, Predicate<ProjectConfigDto> matchingProjectPredicate, String baseRepository,
                                             String headCommitId) throws ServerException {
        // Get matching project in factory
        WorkspaceConfigDto workspace = factory.getWorkspace();
        final List<ProjectConfigDto> matchingProjects = workspace.getProjects()
                                                                 .stream()
                                                                 .filter(matchingProjectPredicate)
                                                                 .collect(toList());

        if (matchingProjects.isEmpty()) {
            throw new ServerException(
                    "Factory " + factory.getId() + " contains no project for given repository and branch.");
        } else if (matchingProjects.size() > 1) {
            throw new ServerException(
                    "Factory " + factory.getId() + " contains several projects for given repository and branch");
        }
        ProjectConfigDto matchingProject = matchingProjects.get(0);

        // Update repository and commitId
        final SourceStorageDto source = matchingProject.getSource();
        final Map<String, String> projectParams = source.getParameters();
        source.setLocation(baseRepository);
        projectParams.put("commitId", headCommitId);

        // Clean branch parameter if exist
        projectParams.remove("branch");

        // Replace existing project with updated one
        source.setParameters(projectParams);
        matchingProject.setSource(source);

        final List<ProjectConfigDto> factoryProjects = workspace.getProjects();
        factoryProjects.removeIf(p -> matchingProject.getName().equals(p.getName()));
        factoryProjects.add(matchingProject);
        workspace.setProjects(factoryProjects);

        return factory.withWorkspace(workspace);
    }

    /**
     * Get webhook configured for a given repository
     *
     * @param repositoryUrl
     *         the URL of the repository
     * @return the webhook configured for the repository or null if no webhook is configured for this repository
     * @throws ServerException
     */
    private GithubWebhook getGitHubWebhook(String repositoryUrl) throws ServerException {
        List<GithubWebhook> webhooks = getGitHubWebhooks();
        GithubWebhook webhook = null;
        for (GithubWebhook w : webhooks) {
            String webhookRepositoryUrl = w.getRepositoryUrl();
            if (repositoryUrl.equals(webhookRepositoryUrl)) {
                webhook = w;
            }
        }
        return webhook;
    }

    /**
     * Get all configured webhooks
     *
     * GitHub webhook: [webhook-name]=[webhook-type],[repository-url],[factory-id];[factory-id];...;[factory-id]
     *
     * @return the list of all webhooks contained in GITHUB_WEBHOOKS_PROPERTIES_FILENAME properties fil
     */
    private static List<GithubWebhook> getGitHubWebhooks() throws ServerException {
        List<GithubWebhook> webhooks = new ArrayList<>();
        Properties webhooksProperties = getProperties(GITHUB_WEBHOOKS_PROPERTIES_FILENAME);
        Set<String> keySet = webhooksProperties.stringPropertyNames();
        keySet.stream().forEach(key -> {
            String value = webhooksProperties.getProperty(key);
            String[] valueSplit = value.split(",");
            switch (valueSplit[0]) {
                case "github":
                    String[] factoriesIDs = valueSplit[2].split(";");
                    GithubWebhook githubWebhook = new GithubWebhook(valueSplit[1], factoriesIDs);
                    webhooks.add(githubWebhook);
                    LOG.debug("new GithubWebhook({}, {})", valueSplit[1], Arrays.toString(factoriesIDs));
                    break;
                default:
                    break;
            }
        });
        return webhooks;
    }
}
