/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.jdbc;

import com.codenvy.analytics.metrics.value.ValueData;

import java.sql.Connection;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface JdbcDataManager {

    Connection openConnection();

    void createTable(Connection connection, String name, List<ValueData> fields);

    void retainData(Connection connection, List<List<ValueData>> data);

}
