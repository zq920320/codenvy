/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.server.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import com.codenvy.api.subscription.server.dao.PlanDao;
import com.codenvy.api.subscription.shared.dto.BillingCycleType;
import com.codenvy.api.subscription.shared.dto.Plan;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link PlanDaoImpl}
 *
 * @author Alexander Garagatyi
 */
public class PlanDaoImplTest extends BaseDaoTest {
    private PlanDao planDao;

    private static final String DB_NAME   = "test1";
    private static final String COLL_NAME = "collName";

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        db = spy(client.getDB(DB_NAME));

        collection = spy(db.getCollection(COLL_NAME));

        when(db.getCollection(COLL_NAME)).thenReturn(collection);

        planDao = new PlanDaoImpl(db, COLL_NAME);
    }

    @Test
    public void shouldBeAbleToGetPlans() throws ServerException {
        Plan plan1 = createPlan().withId("id1");
        Plan plan2 = createPlan().withId("id2");

        collection.save(toDbObject(plan1));
        collection.save(toDbObject(plan2));

        List<Plan> actual = planDao.getPlans();

        assertEquals(actual, Arrays.asList(plan1, plan2));
    }

    @Test
    public void shouldBeAbleToGetEmptyListOfPlans() throws ServerException {
        List<Plan> actual = planDao.getPlans();

        assertEquals(actual, Collections.emptyList());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionIfMongoExceptionOccurs() throws ServerException {
        doThrow(new MongoException("")).when(collection).find();

        planDao.getPlans();
    }

    @Test
    public void shouldBeAbleToGetPlansById() throws Exception {
        Plan plan1 = createPlan().withId("id1");
        Plan plan2 = createPlan().withId("id2");

        collection.save(toDbObject(plan1));
        collection.save(toDbObject(plan2));

        Plan actual = planDao.getPlanById("id2");

        assertEquals(actual, plan2);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "No plan id2 found")
    public void shouldThrowNotFoundExceptionIfPlanIsNotFoundOnGetPLanById() throws Exception {
        planDao.getPlanById("id2");
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "message")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnGetPlanById() throws Exception {
        doThrow(new MongoException("message")).when(collection).findOne(any(DBObject.class));

        planDao.getPlanById("id2");
    }

    private Plan createPlan() {
        return DtoFactory.getInstance().createDto(Plan.class)
                         .withId("plan_id")
                         .withServiceId("service_id")
                         .withProperties(Collections.singletonMap("key", "value"))
                         .withPaid(true)
                         .withSalesOnly(false)
                         .withBillingCycleType(BillingCycleType.AutoRenew)
                         .withBillingCycle(1)
                         .withBillingContractTerm(12)
                         .withDescription("plan description");
    }

    private DBObject toDbObject(Plan plan) {
        final DBObject properties = new BasicDBObject();
        properties.putAll(plan.getProperties());

        return new BasicDBObject()
                .append("id", plan.getId())
                .append("serviceId", plan.getServiceId())
                .append("properties", properties)
                .append("paid", plan.isPaid())
                .append("salesOnly", plan.getSalesOnly())
                .append("billingCycleType", plan.getBillingCycleType().toString())
                .append("billingCycle", plan.getBillingCycle())
                .append("billingContractTerm", plan.getBillingContractTerm())
                .append("description", plan.getDescription());
    }
}