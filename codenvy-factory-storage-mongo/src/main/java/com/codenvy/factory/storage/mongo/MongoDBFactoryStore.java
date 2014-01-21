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
package com.codenvy.factory.storage.mongo;

import com.codenvy.api.factory.*;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.factory.MongoDbConfiguration;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/** Implementation of the MongoDB factory storage. */
public class MongoDBFactoryStore implements FactoryStore {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBFactoryStore.class);

    DBCollection factories;

    public MongoDBFactoryStore(String host, int port, String dbName, String collectionName, String username, String password) {
        MongoClient mongoClient;
        DB db;
        if (dbName == null || dbName.isEmpty() || collectionName == null || collectionName.isEmpty()) {
            throw new RuntimeException("Parameters 'database' and 'collection' can't be null or empty.");
        }

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(dbName);

            if (username != null && password != null) {
                if (!db.authenticate(username, password.toCharArray()))
                    throw new RuntimeException("Wrong MongoDB credentians, authentication failed.");

            }
            factories = db.getCollection(collectionName);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't connect to MongoDB.");
        }
    }

    public MongoDBFactoryStore(MongoDbConfiguration dbConf) {
        this(dbConf.getHost(), dbConf.getPort(), dbConf.getDatabase(), dbConf.getCollectionname(), dbConf.getUsername(),
             dbConf.getPassword());
    }


    @Override
    public String saveFactory(AdvancedFactoryUrl factoryUrl, Set<FactoryImage> images) throws FactoryUrlException {

        factoryUrl.setId(NameGenerator.generate("", 16));
        BasicDBObjectBuilder attributes = BasicDBObjectBuilder.start(factoryUrl.getProjectattributes());

        BasicDBObject welcomeList = new BasicDBObject();
        for (Map.Entry<String, WelcomeGreeting> welcomeEntry : factoryUrl.getWelcome().entrySet()) {
            BasicDBObject welcomeDBObject = new BasicDBObject();
            welcomeDBObject.put("title", welcomeEntry.getValue().getTitle());
            welcomeDBObject.put("iconUrl", welcomeEntry.getValue().getIconUrl());
            welcomeDBObject.put("content", welcomeEntry.getValue().getContent());

            welcomeList.put(welcomeEntry.getKey(), welcomeDBObject);
        }

        List<DBObject> imageList = new ArrayList<>();
        for (FactoryImage one : images) {
            imageList.add(new BasicDBObjectBuilder().add("name", one.getName())
                                                    .add("type", one.getMediaType())
                                                    .add("data", one.getImageData()).get());
        }

        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("v", factoryUrl.getV())
                         .add("vcs", factoryUrl.getVcs())
                         .add("vcsurl", factoryUrl.getVcsurl())
                         .add("commitid", factoryUrl.getCommitid())
                         .add("action", factoryUrl.getAction())
                         .add("openfile", factoryUrl.getOpenfile())
                         .add("vcsinfo", factoryUrl.getVcsinfo())
                         .add("style", factoryUrl.getStyle())
                         .add("description", factoryUrl.getDescription())
                         .add("contactmail", factoryUrl.getContactmail())
                         .add("author", factoryUrl.getAuthor())
                         .add("orgid", factoryUrl.getOrgid())
                         .add("affiliateid", factoryUrl.getAffiliateid())
                         .add("vcsbranch", factoryUrl.getVcsbranch())
                         .add("projectattributes", attributes.get())
                         .add("userid", factoryUrl.getUserid())
                         .add("validsince", factoryUrl.getValidsince())
                         .add("validuntil", factoryUrl.getValiduntil())
                         .add("created", factoryUrl.getCreated())
                         .add("variables", VariableHelper.toBasicDBFormat(factoryUrl.getVariables()))
                         .add("welcome", welcomeList);

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", factoryUrl.getId());
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        factories.save(factoryDatabuilder.get());
        return factoryUrl.getId();
    }

    @Override
    public void removeFactory(String id) throws FactoryUrlException {
        DBObject query = new BasicDBObject();
        query.put("_id", id);
        factories.remove(query);
    }

    @Override
    public AdvancedFactoryUrl getFactory(String id) throws FactoryUrlException {
        AdvancedFactoryUrl factoryUrl = new AdvancedFactoryUrl();
        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = factories.findOne(query);
        if (res == null) {
            return null;
        }

        // Processing factory
        factoryUrl.setId((String)res.get("_id"));
        BasicDBObject factoryAsDbObject = (BasicDBObject)res.get("factoryurl");
        factoryUrl.setV((String)factoryAsDbObject.get("v"));
        factoryUrl.setVcs((String)factoryAsDbObject.get("vcs"));
        factoryUrl.setVcsurl((String)factoryAsDbObject.get("vcsurl"));
        factoryUrl.setCommitid((String)factoryAsDbObject.get("commitid"));
        factoryUrl.setAction((String)factoryAsDbObject.get("action"));
        factoryUrl.setVcsinfo((boolean)factoryAsDbObject.get("vcsinfo"));
        factoryUrl.setOpenfile((String)factoryAsDbObject.get("openfile"));
        factoryUrl.setStyle((String)factoryAsDbObject.get("style"));
        factoryUrl.setDescription((String)factoryAsDbObject.get("description"));
        factoryUrl.setContactmail((String)factoryAsDbObject.get("contactmail"));
        factoryUrl.setAuthor((String)factoryAsDbObject.get("author"));
        factoryUrl.setOrgid((String)factoryAsDbObject.get("orgid"));
        factoryUrl.setAffiliateid((String)factoryAsDbObject.get("affiliateid"));
        factoryUrl.setVcsbranch((String)factoryAsDbObject.get("vcsbranch"));
        factoryUrl.setProjectattributes(((BasicDBObject)factoryAsDbObject.get("projectattributes")).toMap());
        factoryUrl.setUserid((String)factoryAsDbObject.get("userid"));
        factoryUrl.setValidsince((long)factoryAsDbObject.get("validsince"));
        factoryUrl.setValiduntil((long)factoryAsDbObject.get("validuntil"));
        factoryUrl.setCreated((long)factoryAsDbObject.get("created"));
        factoryUrl.setVariables(VariableHelper.fromBasicDBFormat(factoryAsDbObject));

        Map<String, WelcomeGreeting> welcome = new HashMap<>();
        BasicDBObject welcomeList = (BasicDBObject)factoryAsDbObject.get("welcome");
        for (Map.Entry<String, Object> o1 : welcomeList.entrySet()) {
            BasicDBObject welcomeDBObject = (BasicDBObject)o1.getValue();

            welcome.put(o1.getKey(), new WelcomeGreeting((String)welcomeDBObject.get("title"),
                                                         (String)welcomeDBObject.get("iconUrl"),
                                                         (String)welcomeDBObject.get("content")));
        }

        factoryUrl.setWelcome(welcome);

        return factoryUrl;
    }

    @Override
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws FactoryUrlException {
        Set<FactoryImage> images = new HashSet<>();

        DBObject query = new BasicDBObject();
        query.put("_id", factoryId);
        DBObject res = factories.findOne(query);
        if (res == null) {
            return Collections.EMPTY_SET;
        }

        BasicDBList imagesAsDbObject = (BasicDBList)res.get("images");
        for (Object obj : imagesAsDbObject) {
            BasicDBObject dbobj = (BasicDBObject)obj;
            try {
                if (imageId == null || dbobj.get("name").equals(imageId)) {
                    FactoryImage image = new FactoryImage();
                    image.setName((String)dbobj.get("name"));
                    image.setMediaType((String)dbobj.get("type"));
                    image.setImageData((byte[])dbobj.get("data"));
                    images.add(image);
                }
            } catch (IOException e) {
                LOG.error("Wrong image data found for image " + dbobj.get("name"), e);
            }
        }

        return images;
    }
}
