/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.deploy;

import com.codenvy.api.account.ResourcesManagerImpl;
import com.codenvy.api.account.server.AccountService;
import com.codenvy.api.account.server.ResourcesManager;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.auth.AuthenticationService;
import com.codenvy.api.builder.BuilderAdminService;
import com.codenvy.api.builder.BuilderSelectionStrategy;
import com.codenvy.api.builder.BuilderService;
import com.codenvy.api.builder.LastInUseBuilderSelectionStrategy;
import com.codenvy.api.core.notification.WSocketEventBusServer;
import com.codenvy.api.core.rest.ApiInfoService;
import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.api.factory.FactoryAcceptValidator;
import com.codenvy.api.factory.FactoryAcceptValidatorImpl;
import com.codenvy.api.factory.FactoryCreateValidator;
import com.codenvy.api.factory.FactoryCreateValidatorImpl;
import com.codenvy.api.factory.FactoryService;
import com.codenvy.api.runner.RandomRunnerSelectionStrategy;
import com.codenvy.api.runner.RunnerAdminService;
import com.codenvy.api.runner.RunnerSelectionStrategy;
import com.codenvy.api.runner.RunnerService;
import com.codenvy.api.user.server.TokenValidator;
import com.codenvy.api.user.server.UserProfileService;
import com.codenvy.api.user.server.UserService;
import com.codenvy.api.user.server.dao.PreferenceDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.api.vfs.server.VirtualFileFilter;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.search.SearcherProvider;
import com.codenvy.api.workspace.server.WorkspaceService;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.auth.sso.client.EnvironmentContextResolver;
import com.codenvy.auth.sso.client.SSOContextResolver;
import com.codenvy.auth.sso.client.filter.ConjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.DisjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.NegationRequestFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentNumberFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentValueFilter;
import com.codenvy.auth.sso.client.filter.RegexpRequestFilter;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.RequestMethodFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromAndMethodRequestFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.codenvy.auth.sso.server.RolesExtractor;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.auth.sso.server.organization.WorkspaceCreationValidator;
import com.codenvy.everrest.CodenvyAsynchronousJobPool;
import com.codenvy.everrest.ETagResponseFilter;
import com.codenvy.ide.ext.java.jdi.server.DebuggerService;
import com.codenvy.ide.ext.java.server.format.FormatService;
import com.codenvy.ide.ext.ssh.server.KeyService;
import com.codenvy.ide.ext.ssh.server.SshKeyStore;
import com.codenvy.ide.ext.ssh.server.UserProfileSshKeyStore;
import com.codenvy.inject.DynaModule;
import com.codenvy.security.oauth.OAuthAuthenticatorProvider;
import com.codenvy.security.oauth.OAuthAuthenticatorProviderImpl;
import com.codenvy.subscription.service.saas.SaasResourcesCleaner;
import com.codenvy.subscription.service.saas.SaasWorkspaceResourcesProvider;
import com.codenvy.vfs.impl.fs.AutoMountVirtualFileSystemRegistry;
import com.codenvy.vfs.impl.fs.CleanableSearcherProvider;
import com.codenvy.vfs.impl.fs.LocalFSMountStrategy;
import com.codenvy.vfs.impl.fs.MigrationLocalFSMountStrategy;
import com.codenvy.vfs.impl.fs.MountPointCacheCleaner;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.mongodb.DB;
import com.palominolabs.metrics.guice.InstrumentationModule;

import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context listeners.
 *
 * @author Max Shaposhnik
 */
@DynaModule
public class CloudIdeApiModule extends AbstractModule {

    @Override
    protected void configure() {
        // < Copied from IDE3 api war
        bind(ApiInfoService.class);

        bind(AuthenticationService.class);
        bind(WorkspaceService.class);
        bind(UserService.class);
        bind(UserProfileService.class);

        bind(BuilderSelectionStrategy.class).to(LastInUseBuilderSelectionStrategy.class);
        bind(BuilderService.class);
        bind(BuilderAdminService.class);
        //bind(SlaveBuilderService.class);

        bind(RunnerSelectionStrategy.class).to(RandomRunnerSelectionStrategy.class);
        bind(RunnerService.class);
        bind(RunnerAdminService.class);
        //bind(SlaveRunnerService.class);

        bind(DebuggerService.class);
        bind(FormatService.class);

        bind(KeyService.class);
        bind(SshKeyStore.class).to(UserProfileSshKeyStore.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(ETagResponseFilter.class);
        bind(WSocketEventBusServer.class);

        install(new com.codenvy.api.core.rest.CoreRestModule());
        install(new com.codenvy.api.analytics.AnalyticsModule());
        install(new com.codenvy.api.project.server.BaseProjectModule());
        install(new com.codenvy.api.builder.internal.BuilderModule());
        install(new com.codenvy.api.runner.internal.RunnerModule());
        install(new com.codenvy.api.vfs.server.VirtualFileSystemModule());
        //install(new com.codenvy.vfs.impl.fs.VirtualFileSystemFSModule());
        install(new com.codenvy.api.factory.FactoryModule());
        install(new com.codenvy.docs.DocsModule());

        // Copied from IDE3 api war >

        //Temporary FS change
        final Multibinder<VirtualFileFilter> multibinder =
                Multibinder.newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));
        multibinder.addBinding().toInstance(new VirtualFileFilter() {
            @Override
            public boolean accept(VirtualFile virtualFile) {
                return !virtualFile.getPath().endsWith("/.codenvy/misc.xml");
            }
        });
        bind(LocalFSMountStrategy.class).to(MigrationLocalFSMountStrategy.class);
        bind(SearcherProvider.class).to(CleanableSearcherProvider.class);
        bind(MountPointCacheCleaner.Finalizer.class).asEagerSingleton();


        //oauth
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
        bind(com.codenvy.api.auth.oauth.OAuthTokenProvider.class).to(com.codenvy.security.oauth.OAuthAuthenticatorTokenProvider.class);
        //factory
        bind(com.codenvy.api.factory.FactoryStore.class).to(com.codenvy.factory.storage.mongo.MongoDBFactoryStore.class);
        bind(FactoryAcceptValidator.class).to(FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(FactoryCreateValidatorImpl.class);
        bind(FactoryService.class);
        bind(com.codenvy.ide.factory.server.MessageService.class);

        //user-workspace-account
        bind(PasswordEncryptor.class).toInstance(new SSHAPasswordEncryptor());
        bind(DB.class).toProvider(com.codenvy.api.dao.mongo.MongoDatabaseProvider.class);
        bind(UserDao.class).to(com.codenvy.api.dao.ldap.UserDaoImpl.class);
        bind(WorkspaceDao.class).to(com.codenvy.api.dao.mongo.WorkspaceDaoImpl.class);
        bind(UserProfileDao.class).to(com.codenvy.api.dao.mongo.UserProfileDaoImpl.class);
        bind(MemberDao.class).to(com.codenvy.api.dao.mongo.MemberDaoImpl.class);
        bind(AccountDao.class).to(com.codenvy.api.dao.mongo.AccountDaoImpl.class);
        bind(PreferenceDao.class).to(com.codenvy.api.dao.mongo.PreferenceDaoImpl.class);
        bind(ResourcesManager.class).to(ResourcesManagerImpl.class);

        bind(AccountService.class);

        bind(com.codenvy.service.http.WorkspaceInfoCache.class);
        bind(com.codenvy.workspace.listener.WsCacheCleanupSubscriber.class);

        bind(com.codenvy.service.password.PasswordService.class);

        bind(VirtualFileSystemRegistry.class).to(AutoMountVirtualFileSystemRegistry.class);

        Multibinder<com.codenvy.api.account.server.SubscriptionService> subscriptionServiceBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.account.server.SubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(com.codenvy.subscription.service.SaasSubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(com.codenvy.subscription.service.FactorySubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(com.codenvy.subscription.service.OnPremisesSubscriptionService.class);


        //authentication
        bind(com.codenvy.api.auth.AuthenticationDao.class).to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
        bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
        bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);


        //SSO
        Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.ldap.LdapAuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.RestrictedAccessAuthenticationHandler.class);


        Multibinder<RolesExtractor> rolesExtractorBinder = Multibinder.newSetBinder(binder(), RolesExtractor.class);
        rolesExtractorBinder.addBinding().to(com.codenvy.auth.sso.server.ldap.LdapRolesExtractor.class);
        rolesExtractorBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceRolesExtractor.class);

        bind(UserCreator.class).to(com.codenvy.auth.sso.server.OrgServiceUserCreator.class);
        bind(WorkspaceCreationValidator.class).to(com.codenvy.auth.sso.server.OrgServiceWorkspaceValidator.class);

        bind(SSOContextResolver.class).to(EnvironmentContextResolver.class);

        bind(com.codenvy.auth.sso.client.TokenHandler.class)
                .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.jaas.realm")).to("default_realm");
        bindConstant().annotatedWith(Names.named("auth.handler.default")).to("org");
        bindConstant().annotatedWith(Names.named("auth.sso.access_cookie_path")).to("/api/internal/sso/server");
        bindConstant().annotatedWith(Names.named("auth.sso.access_ticket_lifetime_seconds")).to(259200);
        bindConstant().annotatedWith(Names.named("auth.sso.bearer_ticket_lifetime_seconds")).to(3600);
        bindConstant().annotatedWith(Names.named("auth.sso.create_workspace_page_url")).to("/site/auth/create");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.oauth.access_denied_error_page")).to("/site/login");
        bindConstant().annotatedWith(Names.named("error.page.workspace_not_found_redirect_url")).to("/site/error/error-tenant-name");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");

        bind(RequestFilter.class).toInstance(
                new DisjunctionRequestFilter(
                        new ConjunctionRequestFilter(
                                new UriStartFromRequestFilter("/api/factory"),
                                new RequestMethodFilter("GET"),
                                new DisjunctionRequestFilter(
                                        new UriStartFromRequestFilter("/api/factory/nonencoded"),
                                        new PathSegmentValueFilter(4, "image"),
                                        new PathSegmentValueFilter(4, "snippet"),
                                        new ConjunctionRequestFilter(
                                                //api/factory/{}
                                                new PathSegmentNumberFilter(3),
                                                new NegationRequestFilter(new UriStartFromRequestFilter("/api/factory/find"))
                                        ))
                        ),
                        new UriStartFromRequestFilter("/api/analytics/public-metric"),
                        new UriStartFromRequestFilter("/api/docs"),
                        new UriStartFromAndMethodRequestFilter("POST", "/api/user/create"),
                        new RegexpRequestFilter("^/api/builder/(\\w+)/download/(.+)$")
                )
                                            );


        bindConstant().annotatedWith(Names.named("notification.server.propagate_events")).to("vfs,workspace");

        bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                .to(com.codenvy.service.http.WorkspaceInfoCache.DaoWorkspaceCacheLoader.class);

        bind(com.codenvy.workspace.listener.VfsCleanupPerformer.class).to(com.codenvy.workspace.IdexVfsHelper.class);
        bind(com.codenvy.workspace.activity.websocket.WebsocketListenerInitializer.class);
        bind(com.codenvy.workspace.activity.WsActivityListener.class).asEagerSingleton();
        bind(com.codenvy.workspace.listener.VfsStopSubscriber.class).asEagerSingleton();

        bind(com.codenvy.workspace.CreateWsRootDirSubscriber.class).asEagerSingleton();


        bind(com.braintreegateway.BraintreeGateway.class).to(com.codenvy.braintree.GuiceBraintreeGateway.class);
        bind(com.codenvy.api.account.server.PaymentService.class).to(com.codenvy.api.payment.BraintreePaymentService.class);

        bind(com.codenvy.api.account.server.dao.PlanDao.class).to(com.codenvy.api.dao.mongo.PlanDaoImpl.class);
        bind(com.codenvy.factory.workspace.FactoryWorkspaceResourceProvider.class).asEagerSingleton();
        bind(SaasWorkspaceResourcesProvider.class).asEagerSingleton();
        bind(SaasResourcesCleaner.class).asEagerSingleton();
        bind(com.codenvy.plan.PlanService.class);

        bind(com.codenvy.braintree.BraintreeWebhookService.class);
        bind(com.codenvy.api.account.server.SubscriptionAttributesValidator.class)
                .to(com.codenvy.subscription.SubscriptionAttributesValidatorImpl.class);


        install(new com.codenvy.workspace.interceptor.InterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerInterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

        install(new InstrumentationModule());


    }
}
