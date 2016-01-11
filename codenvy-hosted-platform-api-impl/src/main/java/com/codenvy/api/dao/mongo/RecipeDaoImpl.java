/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.api.dao.mongo;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.recipe.GroupImpl;
import org.eclipse.che.api.machine.server.recipe.PermissionsImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asStringList;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link RecipeDao} based on MongoDB storage
 * <pre>
 * Database schema:
 * {
 *     "_id" : "recipe12345...",
 *     "name" : "recipe-name",
 *     "creator: "user12345...",
 *     "type" : "recipe-type",
 *     "script" : "script-content",
 *     "tags" : [ "tag1", "tag2 ],
 *     "permissions" : {
 *         "users" : {
 *              "user1" : [ "read", "write" ],
 *              "user2" : [ "read" ]
 *         },
 *         "groups: [
 *              {
 *                  name: "workspace/admin",
 *                  unit: "workspace123",
 *                  acl: [ "read", "write" ]
 *              }
 *         ]
 *     }
 * }
 * </pre>
 *
 * @author Eugene Voevodin
 */
public class RecipeDaoImpl implements RecipeDao {

    private static final FromDBObjectToRecipeFunction FROM_OBJECT_TO_RECIPE_FUNCTION = new FromDBObjectToRecipeFunction();

    private final DBCollection recipes;

    @Inject
    public RecipeDaoImpl(@Named("mongo.db.organization") DB db, @Named("organization.storage.db.recipes.collection") String collectionName) {
        recipes = db.getCollection(collectionName);
        recipes.createIndex(new BasicDBObject("creator", 1));
        //TODO consider adding index on 'type' field
        //why NOT - for now we have the only type of recipe
        //why YES - if we got different recipe types it will improve search performance
    }

    @Override
    public void create(ManagedRecipe recipe) throws ServerException, ConflictException {
        requireNonNull(recipe, "Recipe required");
        try {
            if (recipes.findOne(recipe.getId()) != null) {
                throw new ConflictException(format("Recipe with id '%s' already exists", recipe.getId()));
            }
            recipes.save(asDBObject(recipe));
        } catch (MongoException ex) {
            throw new ServerException("Impossible to store recipe " + recipe.getId(), ex);
        }
    }

    @Override
    public void update(ManagedRecipe recipe) throws ServerException, NotFoundException {
        requireNonNull(recipe, "Recipe required");
        final BasicDBObject dbUpdate = new BasicDBObject();
        if (!isNullOrEmpty(recipe.getType())) {
            dbUpdate.append("type", recipe.getType());
        }
        if (!isNullOrEmpty(recipe.getScript())) {
            dbUpdate.append("script", recipe.getScript());
        }
        if (!isNullOrEmpty(recipe.getName())) {
            dbUpdate.append("name", recipe.getName());
        }
        if (!recipe.getTags().isEmpty()) {
            dbUpdate.append("tags", asDBList(recipe.getTags()));
        }
        if (recipe.getPermissions() != null) {
            dbUpdate.append("permissions", asDBObject(recipe.getPermissions()));
        }
        try {
            WriteResult wr = recipes.update(new BasicDBObject("_id", recipe.getId()), new BasicDBObject("$set", dbUpdate));
            if (wr.getN() == 0) {
                throw new NotFoundException("Recipe with id '" + recipe.getId() + "' was not found");
            }
        } catch (MongoException ex) {
            throw new ServerException("Impossible to update recipe", ex);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Recipe id required");
        try {
            recipes.remove(new BasicDBObject("_id", id));
        } catch (MongoException ex) {
            throw new ServerException("Impossible to remove recipe " + id, ex);
        }
    }

    @Override
    public ManagedRecipe getById(String id) throws ServerException, NotFoundException {
        requireNonNull(id, "Recipe id required");
        final DBObject recipeObj;
        try {
            recipeObj = recipes.findOne(id);
        } catch (MongoException ex) {
            throw new ServerException("Impossible to retrieve recipe " + id, ex);
        }
        if (recipeObj == null) {
            throw new NotFoundException(format("Recipe with id '%s' was not found", id));
        }
        return FROM_OBJECT_TO_RECIPE_FUNCTION.apply(recipeObj);
    }


    /**
     * <pre>
     * Example of search query:
     * {
     *      "permissions.groups" : {
     *          $elemMatch : {
     *              "acl" : {
     *                  $in: ["search"]
     *              },
     *              "name" : "public"
     *          },
     *      },
     *      tags : {
     *          $all: ["java", "mongo"]
     *      },
     *      type : "docker"
     * }
     * </pre>
     */
    @Override
    public List<ManagedRecipe> search(List<String> tags, String type, int skipCount, int maxItems) throws ServerException {

        final BasicDBObject query =
                new BasicDBObject("permissions.groups",
                                  new BasicDBObject("$elemMatch",
                                                    new BasicDBObject().append("acl",
                                                                               new BasicDBObject("$in", asDBList(asList("search"))))
                                                                       .append("name", "public")));
        if (type != null) {
            query.append("type", type);
        }
        if (tags != null && !tags.isEmpty()) {
            final BasicDBList tagsObj = new BasicDBList();
            tagsObj.addAll(tags);
            query.append("tags", new BasicDBObject("$all", tagsObj));
        }
        try (DBCursor cursor = recipes.find(query)
                                      .skip(skipCount)
                                      .limit(maxItems)) {
            return FluentIterable.from(cursor)
                                 .transform(FROM_OBJECT_TO_RECIPE_FUNCTION)
                                 .toList();
        } catch (MongoException ex) {
            throw new ServerException("Impossible to retrieve recipes", ex);
        }
    }

    @Override
    public List<ManagedRecipe> getByCreator(String creator, int skipCount, int maxItems) throws ServerException {
        requireNonNull(creator, "Recipe creator required");
        try (DBCursor cursor = recipes.find(new BasicDBObject("creator", creator))
                                      .skip(skipCount)
                                      .limit(maxItems)) {
            return FluentIterable.from(cursor)
                                 .transform(FROM_OBJECT_TO_RECIPE_FUNCTION)
                                 .toList();
        } catch (MongoException ex) {
            throw new ServerException("Impossible to retrieve recipes", ex);
        }
    }


    /**
     * Transforms database object to recipe.
     * It is stateless so thread safe.
     */
    /*used in test*/static class FromDBObjectToRecipeFunction implements Function<Object, ManagedRecipe> {

        @Nullable
        @Override
        public ManagedRecipe apply(Object input) {
            final BasicDBObject basicObj = (BasicDBObject)input;

            final RecipeImpl recipe = new RecipeImpl().withId(basicObj.getString("_id"))
                                                      .withName(basicObj.getString("name"))
                                                      .withCreator(basicObj.getString("creator"))
                                                      .withType(basicObj.getString("type"))
                                                      .withScript(basicObj.getString("script"))
                                                      .withTags(asStringList(basicObj.get("tags")));
            final BasicDBObject permObj = (BasicDBObject)basicObj.get("permissions");
            if (permObj != null) {
                final BasicDBObject usersObj = (BasicDBObject)permObj.get("users");
                final Map<String, List<String>> users = Maps.newHashMapWithExpectedSize(usersObj.size());
                for (Map.Entry<String, Object> entry : usersObj.entrySet()) {
                    users.put(entry.getKey(), asStringList(entry.getValue()));
                }
                final BasicDBList groupsList = (BasicDBList)permObj.get("groups");
                final List<Group> groups = new ArrayList<>(groupsList.size());
                for (Object groupObj : groupsList) {
                    final BasicDBObject basicGroup = (BasicDBObject)groupObj;
                    groups.add(new GroupImpl(basicGroup.getString("name"),
                                             basicGroup.getString("unit"),
                                             asStringList(basicGroup.get("acl"))));
                }
                recipe.setPermissions(new PermissionsImpl(users, groups));
            }
            return recipe;
        }
    }

    /*used in test*/ BasicDBObject asDBObject(ManagedRecipe recipe) {
        final BasicDBObject recipeObj = new BasicDBObject().append("_id", recipe.getId())
                                                           .append("name", recipe.getName())
                                                           .append("creator", recipe.getCreator())
                                                           .append("script", recipe.getScript())
                                                           .append("type", recipe.getType())
                                                           .append("tags", asDBList(recipe.getTags()));
        if (recipe.getPermissions() != null) {
            recipeObj.append("permissions", asDBObject(recipe.getPermissions()));
        }
        return recipeObj;
    }

    private BasicDBObject asDBObject(Permissions permissions) {
        final BasicDBObject users = new BasicDBObject();
        for (Map.Entry<String, List<String>> entry : permissions.getUsers().entrySet()) {
            final BasicDBList acl = new BasicDBList();
            acl.addAll(entry.getValue());
            users.put(entry.getKey(), acl);
        }
        final BasicDBList groups = new BasicDBList();
        for (Group group : permissions.getGroups()) {
            groups.add(new BasicDBObject().append("name", group.getName())
                                          .append("unit", group.getUnit())
                                          .append("acl", asDBList(group.getAcl())));
        }
        return new BasicDBObject("users", users).append("groups", groups);
    }
}
