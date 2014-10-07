/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.account.server.dao.PlanDao;
import com.codenvy.api.account.shared.dto.Plan;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Implementation of {@link com.codenvy.api.account.server.dao.PlanDao} based on MongoDB storage.</p>
 *
 * @author Alexander Garagatyi
 */
public class PlanDaoImpl implements PlanDao {
    private static final String PLAN_COLLECTION = "organization.storage.db.plan.collection";

    private final DBCollection planCollection;

    @Inject
    public PlanDaoImpl(DB db, @Named(PLAN_COLLECTION) String planCollectionName) {
        planCollection = db.getCollection(planCollectionName);
        planCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
    }

    @Override
    public Plan getPlanById(String planId) throws NotFoundException, ServerException {
        try {
            final DBObject planObj = planCollection.findOne(new BasicDBObject("id", planId));
            if (null == planObj) {
                throw new NotFoundException("No plan " + planId + " found");
            }
            return toPlan(planObj);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public List<Plan> getPlans() throws ServerException {
        try (DBCursor plans = planCollection.find()) {
            final ArrayList<Plan> result = new ArrayList<>(plans.size());
            for (DBObject planObj : plans) {
                result.add(toPlan(planObj));
            }
            return result;
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    private Plan toPlan(DBObject dbObj) {
        final BasicDBObject planObj = (BasicDBObject)dbObj;
        @SuppressWarnings("unchecked") //properties is always Map of Strings
        final Map<String, String> properties = (Map<String, String>)planObj.get("properties");
        return DtoFactory.getInstance().createDto(Plan.class).withId(planObj.getString("id"))
                         .withPaid(planObj.getBoolean("paid"))
                         .withSalesOnly(planObj.getBoolean("salesOnly"))
                         .withServiceId(planObj.getString("serviceId"))
                         .withProperties(properties);
    }
}
