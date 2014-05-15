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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS})
public class UsersProfilesListExt extends AbstractUsersProfile {

    public UsersProfilesListExt() {
        super(MetricType.USERS_PROFILES_LIST_EXT);
    }


    @Override
    public ValueData postComputation(ValueData userData, Context clauses) throws IOException {
        Set<String> users = getInvolvedUsers((ListValueData)userData);
        Map<String, Map<String, ValueData>> extendedData = getExtendedData(users, clauses);
        return merge((ListValueData)userData, extendedData);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_PROFILES_LIST);
    }

    @Override
    public String getDescription() {
        return "Users' profiles with extended data";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ID,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB,
                            USER_PHONE,
                            CREATION_DATE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    private Map<String, Map<String, ValueData>> getExtendedData(Set<String> users, Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder(clauses);
        builder.put(MetricFilter.USER, Utils.getFilterAsString(users));
        builder.remove(Parameters.PAGE);
        builder.remove(Parameters.PER_PAGE);
        builder.remove(Parameters.SORT);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData valueData = ValueDataUtil.getAsList(metric, builder.build());

        Map<String, Map<String, ValueData>> result = new HashMap<>(valueData.size());
        for (ValueData row : valueData.getAll()) {
            Map<String, ValueData> data = ((MapValueData)row).getAll();
            result.put(data.get(USER).getAsString(), data);
        }

        return result;
    }

    private Set<String> getInvolvedUsers(ListValueData valueData) {
        Set<String> result = new HashSet<>(valueData.size());
        for (ValueData data : valueData.getAll()) {
            ValueData id = ((MapValueData)data).getAll().get(ID);
            if (id != null) {
                result.add(id.getAsString());
            }
        }

        return result;
    }

    private ValueData merge(ListValueData valueData, Map<String, Map<String, ValueData>> extendedData) {
        List<ValueData> list2Return = new ArrayList<>(valueData.size());

        for (ValueData profile : valueData.getAll()) {
            Map<String, ValueData> data2Return = new HashMap<>(((MapValueData)profile).getAll());

            Map<String, ValueData> m = extendedData.get(data2Return.get(ID).getAsString());
            if (m != null) {
                data2Return.putAll(m);
            }

            list2Return.add(MapValueData.valueOf(data2Return));
        }

        return ListValueData.valueOf(list2Return);
    }
}
