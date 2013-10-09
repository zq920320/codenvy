/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.factory;

import com.codenvy.api.factory.AdvancedFactoryUrl;
import com.codenvy.api.factory.FactoryImage;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.store.FactoryStore;
import com.codenvy.api.factory.store.SavedFactoryData;
import com.codenvy.commons.lang.NameGenerator;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 *
 */
public class MongoDBFactoryStore implements FactoryStore {

    DBCollection factories;

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBFactoryStore.class);

    public MongoDBFactoryStore() {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost", 27017);
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage());
        }
        DB db;
        if (mongoClient != null) {
            db = mongoClient.getDB("test");
        } else {
            throw new RuntimeException("Cannot connect to mongo DB");
        }

        factories = db.getCollection("factories");
    }


    @Override
    public SavedFactoryData saveFactory(AdvancedFactoryUrl factoryUrl, Set<FactoryImage> images)
            throws FactoryUrlException {

        factoryUrl.setId(NameGenerator.generate("", 16));
        Set<FactoryImage> newImages = new HashSet<>();
        for (FactoryImage image : images) {
            image.setName(NameGenerator.generate("", 16) + image.getName());
            newImages.add(image);
        }
        SavedFactoryData factoryData = new SavedFactoryData(factoryUrl, newImages);


        BasicDBObjectBuilder attributes = new BasicDBObjectBuilder();
        for (Map.Entry<String, String> attribute : factoryUrl.getProjectattributes().entrySet()) {
            attributes.add(attribute.getKey(), attribute.getValue());
        }

        List<DBObject> imageList = new ArrayList<>();
        for (FactoryImage one : newImages) {
            imageList.add(new BasicDBObjectBuilder().add("name", one.getName()).add("type", one.getMediaType())
                                                    .add("data", one.getImageData()).get());
        }

        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("v", factoryUrl.getV())
                         .add("vcs", factoryUrl.getVcs())
                         .add("vcsurl", factoryUrl.getVcsurl())
                         .add("commitid", factoryUrl.getCommitid())
                         .add("action", factoryUrl.getAction())
                         .add("openfile", factoryUrl.getOpenfile())
                         .add("keepvcsinfo", factoryUrl.getKeepvcsinfo())
                         .add("style", factoryUrl.getStyle())
                         .add("description", factoryUrl.getDescription())
                         .add("contactmail", factoryUrl.getContactmail())
                         .add("author", factoryUrl.getAuthor())
                         .add("orgid", factoryUrl.getOrgid())
                         .add("affiliateid", factoryUrl.getAffiliateid())
                         .add("projectattributes", attributes.get());

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", factoryUrl.getId());
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        factories.save(factoryDatabuilder.get());
        return factoryData;
    }

    @Override
    public void removeFactory(String id) throws FactoryUrlException {
        DBObject query = new BasicDBObject();
        query.put("_id", id);
        factories.remove(query);
    }

    @Override
    public SavedFactoryData getFactory(String id) throws FactoryUrlException {
        AdvancedFactoryUrl factoryUrl = new AdvancedFactoryUrl();
        Set<FactoryImage> images = new HashSet<>();

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = factories.findOne(query);

        // Processing factory
        factoryUrl.setId((String)res.get("_id"));
        BasicDBObject factoryAsDbObject = (BasicDBObject)res.get("factoryurl");
        factoryUrl.setV((String)factoryAsDbObject.get("v"));
        factoryUrl.setVcs((String)factoryAsDbObject.get("vcs"));
        factoryUrl.setVcsurl((String)factoryAsDbObject.get("vcsurl"));
        factoryUrl.setCommitid((String)factoryAsDbObject.get("commitid"));
        factoryUrl.setAction((String)factoryAsDbObject.get("action"));
        factoryUrl.setKeepvcsinfo((boolean)factoryAsDbObject.get("keepvcsinfo"));
        factoryUrl.setOpenfile((String)factoryAsDbObject.get("openfile"));
        factoryUrl.setStyle((String)factoryAsDbObject.get("style"));
        factoryUrl.setDescription((String)factoryAsDbObject.get("description"));
        factoryUrl.setContactmail((String)factoryAsDbObject.get("contactmail"));
        factoryUrl.setAuthor((String)factoryAsDbObject.get("author"));
        factoryUrl.setOrgid((String)factoryAsDbObject.get("orgid"));
        factoryUrl.setAffiliateid((String)factoryAsDbObject.get("affiliateid"));

        // Attributes
//        BasicDBList attributesAsDbObject = (BasicDBList)factoryAsDbObject.get("projectattributes");
//        for (Object obj : attributesAsDbObject) {
//            factoryUrl.setProjectattributes(((BasicDBObject)obj).toMap());
//        }
        factoryUrl.setProjectattributes(((BasicDBObject)factoryAsDbObject.get("projectattributes")).toMap());

        BasicDBList linksAsDbObject = (BasicDBList)res.get("images");
        for (Object obj : linksAsDbObject) {
            BasicDBObject dbobj = (BasicDBObject)obj;
            try {
                FactoryImage image = new FactoryImage();
                image.setName((String)dbobj.get("name"));
                image.setMediaType((String)dbobj.get("type"));
                image.setImageData((byte[])dbobj.get("data"));
                images.add(image);
            } catch (IOException e) {
                LOG.error("Wrong image data found for image " + dbobj.get("name"), e);
            }
        }

        return new SavedFactoryData(factoryUrl, images);
    }
}
