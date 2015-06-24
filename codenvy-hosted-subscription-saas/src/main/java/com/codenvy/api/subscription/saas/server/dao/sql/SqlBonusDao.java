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
package com.codenvy.api.subscription.saas.server.dao.sql;

import com.codenvy.api.subscription.saas.server.billing.bonus.Bonus;
import com.codenvy.api.subscription.saas.server.billing.bonus.BonusFilter;
import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.sql.ConnectionFactory;
import com.codenvy.sql.postgresql.Int8RangeType;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.postgresql.util.PGobject;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.sql.SqlQueryAppender.appendContainsRange;
import static com.codenvy.sql.SqlQueryAppender.appendEqual;
import static com.codenvy.sql.SqlQueryAppender.appendLimit;
import static com.codenvy.sql.SqlQueryAppender.appendOffset;

/**
 * @author Sergii Leschenko
 */
public class SqlBonusDao implements BonusDao {
    private static final String BONUS_INSERT = "INSERT INTO BONUSES " +
                                               "  (" +
                                               "      FACCOUNT_ID," +
                                               "      FAMOUNT," +
                                               "      FPERIOD," +
                                               "      FCAUSE," +
                                               "      FADDED" +
                                               "  )" +
                                               "    VALUES (?, ?, ?, ?, now());";

    private static final String BONUS_CLOSE_PERIOD = "UPDATE BONUSES " +
                                                     "SET fperiod=int8range(selected.period, ?) " +
                                                     "FROM " +
                                                     "  (SELECT BONUSES.fid AS id, " +
                                                     "          lower(BONUSES.fperiod) AS period " +
                                                     "   FROM BONUSES " +
                                                     "   WHERE BONUSES.fid=? " +
                                                     "   LIMIT 1) selected " +
                                                     "WHERE BONUSES.fid = selected.id ";

    private final ConnectionFactory connectionFactory;
    private final AccountDao        accountDao;

    @Inject
    public SqlBonusDao(ConnectionFactory connectionFactory, AccountDao accountDao) {
        this.connectionFactory = connectionFactory;
        this.accountDao = accountDao;
    }

    @Override
    public Bonus create(Bonus bonus) throws ServerException, NotFoundException {
        ensureAccountExistence(bonus.getAccountId());
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                long bonusId;
                connection.setAutoCommit(false);
                Int8RangeType range = new Int8RangeType(bonus.getFromDate(), bonus.getTillDate(), true, true);
                try (PreparedStatement statement = connection.prepareStatement(BONUS_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, bonus.getAccountId());
                    statement.setDouble(2, bonus.getResources());
                    statement.setObject(3, range);
                    statement.setString(4, bonus.getCause());
                    statement.execute();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        generatedKeys.next();
                        bonusId = generatedKeys.getLong(1);
                    }
                }
                connection.commit();
                return bonus.withId(bonusId);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(Long bonusId, long till) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(BONUS_CLOSE_PERIOD)) {
                    statement.setLong(1, till);
                    statement.setLong(2, bonusId);
                    statement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<Bonus> getBonuses(BonusFilter filter) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            StringBuilder invoiceSelect = new StringBuilder();
            invoiceSelect.append("SELECT FID, FACCOUNT_ID, FPERIOD, FAMOUNT, FCAUSE, FADDED").append(" FROM BONUSES ");

            appendEqual(invoiceSelect, "FACCOUNT_ID", filter.getAccountId());
            appendContainsRange(invoiceSelect, "FPERIOD", filter.getFromDate(), filter.getTillDate());

            if (filter.getCause() != null) {
                appendEqual(invoiceSelect, "FCAUSE", filter.getCause());
            }

            invoiceSelect.append(" ORDER BY FADDED DESC ");
            appendLimit(invoiceSelect, filter.getMaxItems());
            appendOffset(invoiceSelect, filter.getSkipCount());

            try (PreparedStatement statement = connection.prepareStatement(invoiceSelect.toString())) {
                statement.setFetchSize(filter.getMaxItems() != null ? filter.getMaxItems() : 0);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Bonus> result = filter.getMaxItems() != null ? new ArrayList<Bonus>(filter.getMaxItems())
                                                                      : new ArrayList<Bonus>();
                    while (invoicesResultSet.next()) {
                        result.add(toBonus(invoicesResultSet));
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private void ensureAccountExistence(String accountId) throws NotFoundException, ServerException {
        accountDao.getById(accountId);
    }

    private Bonus toBonus(ResultSet invoicesResultSet) throws SQLException {
        Int8RangeType range = new Int8RangeType((PGobject)invoicesResultSet.getObject("FPERIOD"));
        return new Bonus().withId(invoicesResultSet.getLong("FID"))
                          .withAccountId(invoicesResultSet.getString("FACCOUNT_ID"))
                          .withFromDate(range.getFrom())
                          .withTillDate(range.getUntil())
                          .withResources(invoicesResultSet.getDouble("FAMOUNT"))
                          .withCause(invoicesResultSet.getString("FCAUSE"));
    }
}
