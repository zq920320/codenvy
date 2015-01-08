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

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import com.codenvy.api.account.server.dao.PlanDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.BillingCycleType;
import com.codenvy.api.account.shared.dto.Plan;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PlanDaoImpl}
 *
 * @author Alexander Garagatyi
 */
public class PlanDaoImplTest {
    private PlanDao planDao;

    private static final String DB_NAME   = "test1";
    private static final String COLL_NAME = "collName";

    protected DBCollection collection;
    protected MongoClient  client;
    protected MongoServer  server;
    protected DB           db;

    @BeforeMethod
    public void setUp() throws Exception {
        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        client = new MongoClient(new ServerAddress(serverAddress));
        db = spy(client.getDB(DB_NAME));
        collection = spy(db.getCollection(COLL_NAME));

        when(db.getCollection(COLL_NAME)).thenReturn(collection);

        planDao = new PlanDaoImpl(db, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
        server.shutdownNow();
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

    @Test
    public void shouldThrowServerExceptionIfMongoExceptionOccurs() throws ServerException {
        assertTrue(planDao.getPlans().isEmpty());
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
                         .withTrialDuration(7)
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
                .append("trialDuration", plan.getTrialDuration())
                .append("paid", plan.isPaid())
                .append("salesOnly", plan.getSalesOnly())
                .append("billingCycleType", plan.getBillingCycleType().toString())
                .append("billingCycle", plan.getBillingCycle())
                .append("billingContractTerm", plan.getBillingContractTerm())
                .append("description", plan.getDescription());
    }
}