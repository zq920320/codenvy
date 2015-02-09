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
package com.codenvy.api.account.subscription.onpremises;

import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.AccountResources;
import com.codenvy.api.account.subscription.service.util.SubscriptionCharger;
import com.codenvy.api.account.subscription.service.util.SubscriptionExpirationMailSender;
import com.codenvy.api.account.subscription.service.util.SubscriptionTrialRemover;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service provide functionality of On-premises subscription.
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
public class OnPremisesSubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesSubscriptionService.class);
    private final AccountDao                       accountDao;
    private final SubscriptionCharger              chargeUtil;
    private final SubscriptionExpirationMailSender expirationUtil;
    private final SubscriptionTrialRemover         removeUtil;

    @Inject
    public OnPremisesSubscriptionService(AccountDao accountDao,
                                         SubscriptionCharger chargeUtil,
                                         SubscriptionExpirationMailSender expirationUtil,
                                         SubscriptionTrialRemover removeUtil) {
        super("OnPremises", "OnPremises");
        this.accountDao = accountDao;
        this.chargeUtil = chargeUtil;
        this.expirationUtil = expirationUtil;
        this.removeUtil = removeUtil;
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        if (subscription.getProperties().get("Package") == null) {
            throw new ConflictException("Subscription property 'Package' required");
        }
        if (subscription.getProperties().get("Users") == null) {
            throw new ConflictException("Subscription property 'Users' required");
        }

        try {
            if (!accountDao.getActiveSubscriptions(subscription.getAccountId(), getServiceId()).isEmpty()) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {

    }

    @Override
    public void onCheckSubscriptions() throws ApiException {
        removeUtil.removeExpiredTrial(this);

        expirationUtil.sendEmailAboutExpiringTrial(getServiceId(), 2);

        expirationUtil.sendEmailAboutExpiredTrial(getServiceId(), 2);

        expirationUtil.sendEmailAboutExpiredTrial(getServiceId(), 7);

        chargeUtil.charge(this);

//        removeUtil.removeExpiredSubscriptions(this);
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
    }

    @Override
    public AccountResources getAccountResources(Subscription subscription) throws ServerException {
        return DtoFactory.getInstance().createDto(AccountResources.class);
    }
}
