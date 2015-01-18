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
package com.codenvy.api.dao.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

/**
 * @author Eugene Voevodin
 */
public class MongoUtilTest {

    @Test
    public void shouldBeAbleToConvertMapToDBList() {
        final Map<String, String> src = new HashMap<>(4);
        src.put("test-key-1", "test-value-1");
        src.put("test-key-2", "test-value-2");

        final BasicDBList list = asDBList(src);

        assertEquals(list.size(), 2);
        final BasicDBObject testKey1 = new BasicDBObject("name", "test-key-1").append("value", "test-value-1");
        final BasicDBObject testKey2 = new BasicDBObject("name", "test-key-2").append("value", "test-value-2");
        assertEquals(new HashSet<>(list), new HashSet<>(asList(testKey1, testKey2)));
    }

    @Test
    public void shouldBeAbleToConvertDBListToMap() {
        final BasicDBObject testKey1 = new BasicDBObject("name", "test-key-1").append("value", "test-value-1");
        final BasicDBObject testKey2 = new BasicDBObject("name", "test-key-2").append("value", "test-value-2");
        final BasicDBList list = new BasicDBList();
        list.addAll(asList(testKey1, testKey2));

        final Map<String, String> map = asMap(list);
        assertEquals(map.size(), 2);
        assertEquals(map.get("test-key-1"), "test-value-1");
        assertEquals(map.get("test-key-2"), "test-value-2");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenArgumentIsNotBasicDBList() {
        asMap(new Object());
    }
}
