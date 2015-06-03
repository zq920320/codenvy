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

import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.api.dao.sql.SQLModule;
import com.codenvy.api.dao.util.ProfileMigrator;
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
import com.codenvy.vfs.impl.fs.MigrationLocalFSMountStrategy;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.mongodb.DB;
import com.palominolabs.metrics.guice.InstrumentationModule;

import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.account.server.ResourcesManager;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.oauth.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.api.builder.BuilderAdminService;
import org.eclipse.che.api.builder.BuilderSelectionStrategy;
import org.eclipse.che.api.builder.BuilderService;
import org.eclipse.che.api.builder.LastInUseBuilderSelectionStrategy;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.factory.FactoryAcceptValidator;
import org.eclipse.che.api.factory.FactoryAcceptValidatorImpl;
import org.eclipse.che.api.factory.FactoryCreateValidator;
import org.eclipse.che.api.factory.FactoryCreateValidatorImpl;
import org.eclipse.che.api.factory.FactoryService;
import org.eclipse.che.api.runner.RandomRunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerAdminService;
import org.eclipse.che.api.runner.RunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerService;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.ide.ext.java.jdi.server.DebuggerService;
import org.eclipse.che.ide.ext.java.server.format.FormatService;
import org.eclipse.che.ide.ext.ssh.server.KeyService;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStore;
import org.eclipse.che.ide.ext.ssh.server.UserProfileSshKeyStore;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.eclipse.che.security.oauth1.OAuthAuthenticatorAuthorizationHeaderProvider;
import org.eclipse.che.vfs.impl.fs.AutoMountVirtualFileSystemRegistry;
import org.eclipse.che.vfs.impl.fs.CleanableSearcherProvider;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.MountPointCacheCleaner;
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

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.analytics.AnalyticsModule());
        install(new org.eclipse.che.api.project.server.BaseProjectModule());
        install(new org.eclipse.che.api.builder.internal.BuilderModule());
        install(new org.eclipse.che.api.runner.internal.RunnerModule());
        install(new org.eclipse.che.api.vfs.server.VirtualFileSystemModule());
        //install(new VirtualFileSystemFSModule());
        install(new org.eclipse.che.api.factory.FactoryModule());
        install(new org.eclipse.che.docs.DocsModule());

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

        //oauth 1
        bind(org.eclipse.che.security.oauth1.OAuthAuthenticatorProvider.class);
        bind(OAuthAuthorizationHeaderProvider.class).to(OAuthAuthenticatorAuthorizationHeaderProvider.class);

        //oauth 2
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);

        //factory
        bind(org.eclipse.che.api.factory.FactoryStore.class).to(com.codenvy.factory.storage.mongo.MongoDBFactoryStore.class);
        bind(FactoryAcceptValidator.class).to(FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(FactoryCreateValidatorImpl.class);
        bind(FactoryService.class);
        bind(com.codenvy.ide.factory.server.MessageService.class);

        //user-workspace-account
        bind(PasswordEncryptor.class).toInstance(new SSHAPasswordEncryptor());
        bind(DB.class).toProvider(com.codenvy.api.dao.mongo.MongoDatabaseProvider.class);
        bind(UserDao.class).to(com.codenvy.api.dao.ldap.UserDaoImpl.class);
        bind(WorkspaceDao.class).to(com.codenvy.api.dao.mongo.WorkspaceDaoImpl.class);
        bind(UserProfileDao.class).to(com.codenvy.api.dao.ldap.UserProfileDaoImpl.class);
        bind(MemberDao.class).to(com.codenvy.api.dao.mongo.MemberDaoImpl.class);
        bind(AccountDao.class).to(com.codenvy.api.dao.mongo.AccountDaoImpl.class);
        bind(PreferenceDao.class).to(com.codenvy.api.dao.mongo.PreferenceDaoImpl.class);
        bind(ResourcesManager.class).to(ResourcesManagerImpl.class);

        bind(AccountService.class);

        bind(com.codenvy.service.http.WorkspaceInfoCache.class);
        bind(com.codenvy.workspace.listener.WsCacheCleanupSubscriber.class);

        bind(com.codenvy.service.password.PasswordService.class);

        bind(VirtualFileSystemRegistry.class).to(AutoMountVirtualFileSystemRegistry.class);

        //authentication
        bind(org.eclipse.che.api.auth.AuthenticationDao.class).to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
        bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
        bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);
        bind(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class);


        //SSO
        Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.ldap.LdapAuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceAuthenticationHandler.class);


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
        bind(com.codenvy.workspace.activity.RunActivityChecker.class).asEagerSingleton();
        bind(com.codenvy.workspace.activity.WsActivityListener.class).asEagerSingleton();
        bind(com.codenvy.workspace.listener.VfsStopSubscriber.class).asEagerSingleton();

        bind(com.codenvy.workspace.CreateWsRootDirSubscriber.class).asEagerSingleton();

        bind(org.eclipse.che.api.account.server.dao.PlanDao.class).to(com.codenvy.api.dao.mongo.PlanDaoImpl.class);

        bind(ProfileMigrator.class).asEagerSingleton();

        // used own InterceptorModule
        install(new InterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerInterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

        install(new InstrumentationModule());
        install(new SQLModule());

        //turned off Modules to not to count resources
        //install(new BillingModule());
        //install(new MetricModule());
        //install(new SubscriptionModule());
        // used Multibinder to get alive SubscriptionService
        Multibinder.newSetBinder(binder(), org.eclipse.che.api.account.server.SubscriptionService.class);
        //install(new AnalyticsModule());
        //install(new ScheduleModule());


        bind(com.codenvy.api.dao.mongo.SubscriptionQueryBuilder.class).to(com.codenvy.api.dao.mongo.MongoSubscriptionQueryBuilder.class);
    }
}
