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

import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.SubscriptionService;
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
import java.util.Date;

import static com.codenvy.api.account.subscription.ServiceId.ONPREMISES;

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
    private final PaymentService                   paymentService;

    @Inject
    public OnPremisesSubscriptionService(AccountDao accountDao,
                                         SubscriptionCharger chargeUtil,
                                         SubscriptionExpirationMailSender expirationUtil,
                                         SubscriptionTrialRemover removeUtil,
                                         PaymentService paymentService) {
        super(ONPREMISES, ONPREMISES);
        this.accountDao = accountDao;
        this.chargeUtil = chargeUtil;
        this.expirationUtil = expirationUtil;
        this.removeUtil = removeUtil;
        this.paymentService = paymentService;
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
            if (accountDao.getActiveSubscription(subscription.getAccountId(), getServiceId()) != null) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        Date startTrial = subscription.getTrialStartDate();
        Date endTrial = subscription.getTrialEndDate();
        boolean presentTrialPeriod = startTrial != null && endTrial != null && (endTrial.getTime() - startTrial.getTime() > 0);

        if (!presentTrialPeriod) {
            try {
                paymentService.charge(subscription);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                // hide not user friendly exception if exception is not Api
                if (!ApiException.class.isAssignableFrom(e.getClass())) {
                    throw new ServerException("Internal server error. Please, contact support");
                }
                throw e;
            }
        }
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
