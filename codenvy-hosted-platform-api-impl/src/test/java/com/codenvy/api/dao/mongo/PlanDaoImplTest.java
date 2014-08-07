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
import com.codenvy.api.account.shared.dto.Plan;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        db = client.getDB(DB_NAME);
        collection = db.getCollection(COLL_NAME);

        planDao = new PlanDaoImpl(db, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
        server.shutdownNow();
    }

    @Test
    public void shouldBeAbleToGetPlans() throws ServerException {
        Plan plan1 = DtoFactory.getInstance().createDto(Plan.class).withId("id1").withPaid(true).withServiceId("serv1").withProperties(
                Collections.singletonMap("key", "value"));
        Plan plan2 = DtoFactory.getInstance().createDto(Plan.class).withId("id2").withPaid(true).withServiceId("serv2").withProperties(
                Collections.singletonMap("key", "value"));

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

    private DBObject toDbObject(Plan plan) {
        final DBObject properties = new BasicDBObject();
        properties.putAll(plan.getProperties());

        return new BasicDBObject().append("id", plan.getId()).append("serviceId", plan.getServiceId()).append("paid", plan.isPaid()).append(
                "properties", properties);
    }
}