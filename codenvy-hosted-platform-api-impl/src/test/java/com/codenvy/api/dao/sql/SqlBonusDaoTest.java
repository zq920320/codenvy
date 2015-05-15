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
package com.codenvy.api.dao.sql;

import com.codenvy.api.account.billing.Bonus;
import com.codenvy.api.account.billing.BonusDao;
import com.codenvy.api.account.billing.BonusFilter;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link SqlBonusDao}
 *
 * @author Sergii Leschenko
 */
public class SqlBonusDaoTest extends AbstractSQLTest {
    private BonusDao   bonusDao;
    private AccountDao accountDao;

    @BeforeTest
    public void initT() throws SQLException {
        DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(source);
        accountDao = Mockito.mock(AccountDao.class);
        bonusDao = new SqlBonusDao(connectionFactory, accountDao);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Account not found")
    public void shouldThrowNotFoundExceptionWhenAccountDoNotExist() throws Exception {
        when(accountDao.getById(anyString())).thenThrow(new NotFoundException("Account not found"));

        bonusDao.create(new Bonus());
    }

    @Test
    public void shouldBeAbleToAddBonus() throws Exception {
        //when
        final Bonus bonus = bonusDao.create(new Bonus().withAccountId("ac-1")
                                                       .withResources(34.34)
                                                       .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                                       .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                       .withCause("Bonus"));

        assertEquals(bonus.getId(), 1L);
        assertEquals(bonus.getAccountId(), "ac-1");
        assertEquals(bonus.getResources(), 34.34);
        assertEquals(bonus.getFromDate(), sdf.parse("01-01-2015 00:00:00").getTime());
        assertEquals(bonus.getTillDate(), sdf.parse("01-02-2015 00:00:00").getTime());
        assertEquals(bonus.getCause(), "Bonus");
    }

    @Test
    public void shouldBeAbleToAddBonusTimeForIntersectionPeriod() throws Exception {
        //when
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withCause("Bonus"));

        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(60.00)
                                   .withFromDate(sdf.parse("01-01-2015 00:10:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:10:00").getTime())
                                   .withCause("Bonus"));
    }

    @Test
    public void shouldBeAbleToRemoveBonus() throws Exception {
        //when
        final Bonus bonus = bonusDao.create(new Bonus().withAccountId("ac-1")
                                                       .withResources(34.34)
                                                       .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                                       .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                       .withCause("Bonus"));

        bonusDao.remove(bonus.getId(), sdf.parse("01-01-2015 00:10:00").getTime());
    }

    @Test
    public void shouldBeAbleToGetBonuses() throws Exception {
        //given
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withCause("Bonus"));
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withCause("Bonus"));

        //when
        final List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder().build());
        //then
        assertEquals(bonuses.size(), 2);
    }

    @Test
    public void shouldBeAbleToSelectBonusesByAccountId() throws Exception {
        //given
        final Bonus bonus = bonusDao.create(new Bonus().withAccountId("ac-1")
                                                       .withResources(34.34)
                                                       .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                                       .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                       .withCause("Bonus"));
        bonusDao.create(new Bonus().withAccountId("ac-2")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withCause("Bonus"));

        //when
        final List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder()
                                                                   .withAccountId("ac-1")
                                                                   .build());
        //then
        assertEquals(bonuses.size(), 1);
        assertEquals(bonuses.get(0), bonus);
    }

    @Test
    public void shouldBeAbleToSelectBonusesByCause() throws Exception {
        //given
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-12-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withCause("Promotion"));

        final Bonus bonus = bonusDao.create(new Bonus().withAccountId("ac-1")
                                                       .withResources(60.30)
                                                       .withFromDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                       .withTillDate(sdf.parse("01-05-2015 00:00:00").getTime())
                                                       .withCause("Bonus"));


        //when
        List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder()
                                                             .withCause("Bonus")
                                                             .build());
        //then
        assertEquals(bonuses.size(), 1);
        assertEquals(bonuses.get(0), bonus);
    }

    @Test
    public void shouldBeAbleToSelectBonusesPeriod() throws Exception {
        //given
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withResources(34.34)
                                   .withFromDate(sdf.parse("01-12-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withCause("Bonus"));

        final Bonus bonus = bonusDao.create(new Bonus().withAccountId("ac-1")
                                                       .withResources(60.30)
                                                       .withFromDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                       .withTillDate(sdf.parse("01-05-2015 00:00:00").getTime())
                                                       .withCause("Bonus"));


        //when
        List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder()
                                                             .withFromDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                                             .withTillDate(sdf.parse("01-02-2016 00:00:00").getTime())
                                                             .build());
        //then
        assertEquals(bonuses.size(), 1);
        assertEquals(bonuses.get(0), bonus);
    }
}
