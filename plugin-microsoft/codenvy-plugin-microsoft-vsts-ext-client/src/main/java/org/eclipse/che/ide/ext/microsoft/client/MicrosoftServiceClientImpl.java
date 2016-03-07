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
package org.eclipse.che.ide.ext.microsoft.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftRepository;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftUserProfile;
import org.eclipse.che.ide.ext.microsoft.shared.dto.NewMicrosoftPullRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;


import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * @author Anton Korneta
 */
public class MicrosoftServiceClientImpl implements MicrosoftServiceClient {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderFactory          loaderFactory;
    private final String                 baseHttpUrl;

    @Inject
    public MicrosoftServiceClientImpl(@Named("cheExtensionPath") String extPath,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      AsyncRequestFactory asyncRequestFactory,
                                      AppContext appContext,
                                      LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = extPath + "/microsoft/" + appContext.getWorkspaceId();
    }

    @Override
    public Promise<MicrosoftRepository> getRepository(String account,
                                                      String collection,
                                                      String project,
                                                      String repository) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/repository/" + account + '/' + collection + '/' + project + '/' + repository)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting VSTS repository"))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftRepository.class));

    }

    @Override
    public Promise<List<MicrosoftPullRequest>> getPullRequests(String account,
                                                               String collection,
                                                               String project,
                                                               String repository) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/pullrequests/" + account + '/' + collection + '/' + project + '/' + repository)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting VSTS pull request list"))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MicrosoftPullRequest.class));

    }

    @Override
    public Promise<MicrosoftPullRequest> createPullRequest(String account,
                                                           String collection,
                                                           String project,
                                                           String repository,
                                                           NewMicrosoftPullRequest input) {
        return asyncRequestFactory.createPostRequest(baseHttpUrl + "/pullrequests/" + account + '/' + collection + '/' + project + '/' + repository, input)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Creating new pul request"))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftPullRequest.class));
    }

    @Override
    public Promise<MicrosoftPullRequest> updatePullRequest(String account,
                                                           String collection,
                                                           String project,
                                                           String repository,
                                                           String pullRequestId,
                                                           MicrosoftPullRequest pullRequest) {
        final String url = baseHttpUrl + "/pullrequests/" + account + '/' + collection + '/' + project + '/' + repository + '/' + pullRequest;
        return asyncRequestFactory.createRequest(PUT, url, pullRequest, false)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("updatePullRequest"))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftPullRequest.class));
    }

    @Override
    public Promise<MicrosoftUserProfile> getUserProfile() {
        String url = baseHttpUrl + "/profile";
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Getting user profile"))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftUserProfile.class));
    }

    @Override
    public Promise<String> makeHttpRemoteUrl(String account,
                                             String collection,
                                             String project,
                                             String repository) {
        String url = baseHttpUrl + "/url/remote/" + account + '/' + collection + '/' + project + '/' + repository;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Getting pull request url"))
                                  .send(new StringUnmarshaller());
    }

    @Override
    public Promise<String> makePullRequestUrl(String account,
                                              String collection,
                                              String project,
                                              String repository,
                                              String number) {
        String url = baseHttpUrl + "/url/pullrequest/" + account + '/' + collection + '/' +project + '/' + repository + '/' + number;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Getting user profile"))
                                  .send(new StringUnmarshaller());
    }
}
