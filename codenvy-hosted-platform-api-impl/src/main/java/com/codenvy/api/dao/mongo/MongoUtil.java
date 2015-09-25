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

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Eugene Voevodin
 */
public final class MongoUtil {

    /**
     * Converts map to database list
     */
    public static BasicDBList asDBList(Map<String, String> src) {
        final BasicDBList list = new BasicDBList();
        for (Map.Entry<String, String> entry : src.entrySet()) {
            list.add(new BasicDBObject().append("name", entry.getKey())
                                        .append("value", entry.getValue()));
        }
        return list;
    }

    /**
     * Converts database list to Map
     */
    public static Map<String, String> asMap(Object src) {
        if (!(src instanceof BasicDBList)) {
            throw new IllegalArgumentException("BasicDBList was expected");
        }
        final BasicDBList list = (BasicDBList)src;
        final Map<String, String> map = new HashMap<>();
        for (Object obj : list) {
            final BasicDBObject attribute = (BasicDBObject)obj;
            map.put(attribute.getString("name"), attribute.getString("value"));
        }
        return map;
    }

    /**
     * Converts list of documents to a map.
     *
     * <pre>
     *     List of documents:
     *     [
     *          {
     *              "name" : "attribute1",
     *              "value" : "value1"
     *          },
     *          {
     *              "name" : "attribute2",
     *              "value" : "value2"
     *          }
     *     ]
     *
     *     Will be converted to map:
     *     {
     *         "attribute1" : "value1",
     *         "attribute2" : "value2"
     *     }
     * </pre>
     *
     * @param documents
     *         list of documents
     * @return map view of given list
     * @see #mapAsDocumentsList(Map)
     */
    public static Map<String, String> documentsListAsMap(List<Document> documents) {
        return documents.stream().collect(toMap(d -> d.getString("name"), d -> d.getString("value")));
    }

    /**
     * Converts map to list of documents.
     *
     * @return list representation of given map
     * @see #documentsListAsMap(List)
     */
    public static List<Document> mapAsDocumentsList(Map<String, ?> map) {
        return map.entrySet()
                  .stream()
                  .map(entry -> new Document("name", entry.getKey()).append("value", entry.getValue()))
                  .collect(toList());
    }

    public static List<String> asStringList(Object src) {
        if (!(src instanceof BasicDBList)) {
            throw new IllegalArgumentException("BasicDBList was expected");
        }
        final BasicDBList basicList = (BasicDBList)src;
        final List<String> result = new ArrayList<>(basicList.size());
        for (Object obj : basicList) {
            result.add(obj.toString());
        }
        return result;
    }

    public static BasicDBList asDBList(List<?> list) {
        final BasicDBList dbList = new BasicDBList();
        dbList.addAll(list);
        return dbList;
    }

    private MongoUtil() { }
}
