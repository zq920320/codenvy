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
package com.codenvy.plugin.webhooks.vsts;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.plugin.webhooks.AuthConnection;
import com.codenvy.plugin.webhooks.FactoryConnection;
import com.codenvy.plugin.webhooks.UserConnection;
import com.codenvy.plugin.webhooks.BaseWebhookService;
import com.codenvy.plugin.webhooks.vsts.shared.GenericEvent;
import com.codenvy.plugin.webhooks.vsts.shared.PullRequestUpdatedEvent;
import com.codenvy.plugin.webhooks.vsts.shared.VSTSDocument;
import com.codenvy.plugin.webhooks.vsts.shared.WorkItemCreationEvent;
import com.codenvy.plugin.webhooks.vsts.shared.WorkItemCreationResource;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
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
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(
        value = "/vsts-webhook",
        description = "VSTS webhooks handler"
)
@Path("/vsts-webhook")
public class VSTSWebhookService extends BaseWebhookService {

    private static final Logger LOG                               = LoggerFactory.getLogger(VSTSWebhookService.class);
    private static final String VSTS_WEBHOOKS_PROPERTIES_FILENAME = "vsts-webhooks.properties";

    private final FactoryConnection      factoryConnection;
    private final UserConnection         userConnection;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public VSTSWebhookService(final AuthConnection authConnection, final FactoryConnection factoryConnection,
                              final UserConnection userConnection, final HttpJsonRequestFactory httpJsonRequestFactory) {
        super(authConnection);

        this.factoryConnection = factoryConnection;
        this.userConnection = userConnection;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    @ApiOperation(value = "Handle VSTS webhook events",
                  response = Response.class)
    @ApiResponses({
                          @ApiResponse(code = 200, message = "OK"),
                          @ApiResponse(code = 202, message = "The request has been accepted for processing, but the processing has not been completed."),
                          @ApiResponse(code = 500, message = "Internal Server Error")
                  })
    @POST
    @Consumes(APPLICATION_JSON)
    public Response handleVSTSWebhookEvent(@ApiParam(value = "New VSTS event", required = true)
                                           @Context HttpServletRequest request)
            throws ServerException {
        Response response = Response.accepted().build();
        try (ServletInputStream inputStream = request.getInputStream()) {
            final String requestInputString = IoUtil.readAndCloseQuietly(inputStream);

            // Create {@link GenericEvent} from JSON
            final GenericEvent genericEvent = DtoFactory.getInstance().createDtoFromJson(requestInputString, GenericEvent.class);
            final String eventType = genericEvent.getEventType();
            if (!isNullOrEmpty(eventType)) {
                switch (eventType) {
                    case "workitem.created":
                        // Create {@link WorkItemCreationEvent} from JSON
                        final WorkItemCreationEvent wicEvent =
                                DtoFactory.getInstance().createDtoFromJson(requestInputString, WorkItemCreationEvent.class);
                        response = handleWorkItemCreationEvent(wicEvent);
                        break;
                    case "git.pullrequest.updated":
                        // Create {@link PullRequestUpdatedEvent} from JSON
                        final PullRequestUpdatedEvent pruEvent =
                                DtoFactory.getInstance().createDtoFromJson(requestInputString, PullRequestUpdatedEvent.class);
                        // TODO Implement handling of Pull Request merged events
                    default:
                        response = Response.accepted(
                                new GenericEntity<>("VSTS message \'" + eventType + "\' received. It isn't intended to be processed.",
                                                    String.class)).build();
                        break;
                }
            }

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }
        return response;
    }

    /**
     * Handle VSTS {@link WorkItemCreationEvent}
     *
     * @param workItemCreationEvent
     *         the work item creation event to handle
     * @return HTTP 200 response if event was processed successfully
     * HTTP 202 response if event was processed partially
     * @throws ServerException
     */
    private Response handleWorkItemCreationEvent(WorkItemCreationEvent workItemCreationEvent) throws ServerException {
        LOG.debug("{}", workItemCreationEvent);

        // Set current Codenvy user
        EnvironmentContext.getCurrent().setUser(new TokenUser());
        // Get current user id
        final String userId = userConnection.getCurrentUser().getId();

        // Get work item data
        final WorkItemCreationResource resource = workItemCreationEvent.getResource();
        final String projectName = resource.getFields().getTeamProject().toLowerCase(Locale.getDefault());
        final String workItemId = resource.getId();
        final String workItemUrl = resource.getLinks().getSelf().getHref();

        // Get parent factory for the project
        final List<Factory> parentFactories = factoryConnection.findFactory(projectName, userId);

        if (parentFactories.isEmpty()) {
            return Response.accepted(new GenericEntity<>("No parent factory with name " + projectName + " found", String.class)).build();
        }

        Factory parentFactory = parentFactories.get(0);

        // Create Develop & Review factories from parent factory
        parentFactory.setCreator(null);
        parentFactory.setId(null);
        Map<String, String> projectSourceParameters = parentFactory.getWorkspace().getProjects().get(0).getSource().getParameters();
        projectSourceParameters.put("branch", projectName + "-" + workItemId);
        parentFactory.getWorkspace().getProjects().get(0).getSource().withParameters(projectSourceParameters);

        final Factory storedDevelopFactory = buildAndSaveWorkItemFactory(parentFactory, "develop", projectName, workItemId);
        LOG.debug("storedDevelopFactory: {}", storedDevelopFactory);
        final Factory storedReviewFactory = buildAndSaveWorkItemFactory(parentFactory, "review", projectName, workItemId);
        LOG.debug("storedReviewFactory: {}", storedReviewFactory);

        // Add factory IDs to webhook configuration
        // TODO Add factory IDs to existing webhook configuration or create new configuration otherwise

        // Push factory URLs to VSTS project settings
        String projectApiUrl = workItemUrl.split("/wit/workItems")[0];
        LOG.debug("projectApiUrl: {}", projectApiUrl);
        Optional<VSTSWebhook> webhook  = Optional.ofNullable(getVSTSWebhook(projectApiUrl));

        if (!webhook.isPresent()) {
            LOG.error("No webhook configured for project API URL {}", projectApiUrl);
            throw new ServerException("No webhook configured for project API URL " + projectApiUrl);
        }

        VSTSWebhook w = webhook.get();
        final Pair<String, String> vstsCredentials = w.getCredentials();
        final String apiVersion = w.getApiVersion();

        storeFactoryLinkOnVSTS(projectApiUrl, apiVersion, vstsCredentials, storedDevelopFactory, "WI" + workItemId + "-develop-factory");
        storeFactoryLinkOnVSTS(projectApiUrl, apiVersion, vstsCredentials, storedReviewFactory, "WI" + workItemId + "-review-factory");

        return Response.ok().build();
    }

    /**
     * Build a new factory for a VSTS work item based on the Team Project parent factory and save it
     *
     * @param parentFactory
     *         the parent factory to base the new factory on
     * @param factoryType
     *         'develop' or 'review'
     * @param projectName
     *         the name of the VSTS Team Project
     * @param workItemId
     *         the id of the VSTS work item
     * @return
     *  the new created factory
     * @throws ServerException
     */
    private Factory buildAndSaveWorkItemFactory(final Factory parentFactory, final String factoryType, final String projectName,
                                                final String workItemId)
            throws ServerException {
        final String createPolicy = ("develop".equals(factoryType) ? "perUser" : "perClick");
        Policies policies;
        if (parentFactory.getPolicies() == null) {
            policies = DtoFactory.newDto(Policies.class).withCreate(createPolicy);
        } else {
            policies = parentFactory.getPolicies().withCreate(createPolicy);
        }
        final Factory newFactory =
                parentFactory.withPolicies(policies).withName(projectName + "-" + workItemId + "-" + factoryType + "-factory");

        // Generate factory
        return factoryConnection.saveFactory(newFactory);
    }

    /**
     * Send a factory link as a project setting to VSTS extension storage
     *
     * @param projectApiUrl
     *         the API URL of the VSTS Team Project
     * @param apiVersion
     *         the version of the VSTS API to use
     * @param vstsCredentials
     *         the VSTS credentials to use against the VSTS API
     * @param factory
     *         the factory from which link will be sent
     * @param storageKey
     *         the name of the project setting key to use
     * @throws ServerException
     */
    private void storeFactoryLinkOnVSTS(final String projectApiUrl, final String apiVersion, final Pair<String, String> vstsCredentials,
                                        final Factory factory, final String storageKey) throws ServerException {
        String extensionStorageUrl = buildVSTSExtensionStorageUrl(projectApiUrl);

        // Get 'open factory' URL
        final Optional<Link> factoryLink = Optional.ofNullable(factory.getLink(FACTORY_URL_REL));
        if (!factoryLink.isPresent()) {
            throw new ServerException("Factory " + factory.getName() + " do not contain mandatory \'" + FACTORY_URL_REL + "\' link");
        }
        final Link link = factoryLink.get();

        // Create document to store
        final VSTSDocument document = DtoFactory.newDto(VSTSDocument.class).withId(storageKey).withValue(link.getHref()).withEtag("-1");

        final String userCredentials = vstsCredentials.first + ":" + vstsCredentials.second;
        final String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        HttpJsonRequest httpJsonRequest =
                httpJsonRequestFactory.fromUrl(extensionStorageUrl).usePutMethod().setBody(document).setAuthorizationHeader(basicAuth)
                                      .addQueryParam("api-version", apiVersion);
        VSTSDocument newDocument;
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            newDocument = response.asDto(VSTSDocument.class);
            LOG.debug("Factory URL stored on VSTS: {}", newDocument);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Build VSTS extension storage URL
     *
     * @param projectApiUrl
     *         the API URL of a VSTS Team Project
     * @return the VSTS extension storage URL
     */
    private String buildVSTSExtensionStorageUrl(final String projectApiUrl) {
        //https://vsts-test.visualstudio.com/DefaultCollection/_apis
        //https://vsts-test.extmgmt.visualstudio.com/DefaultCollection/_apis
        final String[] s = projectApiUrl.split("visualstudio");
        return s[0] + "extmgmt.visualstudio" + s[1] +
               "/ExtensionManagement/InstalledExtensions/stournie/codenvy-extension/Data/Scopes/Default/Current/Collections/%24settings/Documents";
    }

    /**
     * Get webhook configured for a given VSTS Team Project
     *
     * @param projectApiURL
     *         the API URL of the Team Project
     * @return the webhook configured for the Team Project or null if no webhook is configured for this Team Project
     * @throws ServerException
     */
    private VSTSWebhook getVSTSWebhook(String projectApiURL) throws ServerException {
        List<VSTSWebhook> webhooks = getVSTSWebhooks();
        VSTSWebhook webhook = null;
        for (VSTSWebhook w : webhooks) {
            String webhookProjectApiURL = w.getProjectApiURL();
            if (projectApiURL.equals(webhookProjectApiURL)) {
                webhook = w;
            }
        }
        return webhook;
    }

    /**
     * Get all configured webhooks
     *
     * VSTS webhook: [webhook-name]=[webhook-type],[api-version],[project-api-url],[username],[password]
     *
     * @return the list of all webhooks contained in VSTS_WEBHOOKS_PROPERTIES_FILENAME properties fil
     */
    private static List<VSTSWebhook> getVSTSWebhooks() throws ServerException {
        List<VSTSWebhook> webhooks = new ArrayList<>();
        Properties webhooksProperties = getProperties(VSTS_WEBHOOKS_PROPERTIES_FILENAME);
        Set<String> keySet = webhooksProperties.stringPropertyNames();
        keySet.stream().forEach(key -> {
            String value = webhooksProperties.getProperty(key);
            String[] valueSplit = value.split(",");
            switch (valueSplit[0]) {
                case "vsts":
                    VSTSWebhook webhook = new VSTSWebhook(valueSplit[1], valueSplit[2], Pair.of(valueSplit[3], valueSplit[4]));
                    webhooks.add(webhook);
                    LOG.debug("new VSTSWebhook({}, {}, {}, *******)", valueSplit[1], valueSplit[2], valueSplit[3]);
                    break;
                default:
                    break;
            }
        });
        return webhooks;
    }
}
