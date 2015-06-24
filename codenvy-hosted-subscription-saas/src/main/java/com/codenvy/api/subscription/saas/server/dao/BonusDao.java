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
package com.codenvy.api.subscription.saas.server.dao;

import com.codenvy.api.subscription.saas.server.billing.bonus.Bonus;
import com.codenvy.api.subscription.saas.server.billing.bonus.BonusFilter;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * DAO interface offers means to perform CRUD operations with {@link Bonus} data.
 *
 * @author Sergii Leschenko
 */
public interface BonusDao {
    /**
     * Adds new bonus to persistent layer
     *
     * @param bonus
     *         POJO representation of bonus
     */
    Bonus create(Bonus bonus) throws ServerException, NotFoundException;

    /**
     * Deactivate bonus resources by given id
     *
     * @param bonusId
     *         id of bonus that will be deactivated prepaid GB*h
     * @param till
     *         after this time bonus will be inactive
     */
    void remove(Long bonusId, long till) throws ServerException;

    /**
     * Returns bonuses which match to conditions in {@link BonusFilter}
     */
    List<Bonus> getBonuses(BonusFilter bonusFilter) throws ServerException;
}
