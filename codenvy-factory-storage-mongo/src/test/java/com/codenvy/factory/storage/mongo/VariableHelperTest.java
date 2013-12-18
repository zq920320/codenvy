package com.codenvy.factory.storage.mongo;

import com.codenvy.api.factory.Variable;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

import org.everrest.core.impl.provider.json.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

/** Tests for {@link com.codenvy.factory.storage.mongo.VariableHelper} */
public class VariableHelperTest {
    @Test
    public void testFromBasicDBFormat() throws Exception {
        List<Variable> variables = new ArrayList<>();
        Variable.Replacement replacement = new Variable.Replacement("findText", "replaceText");
        variables.add(new Variable(Collections.singletonList("glob_pattern"), Collections.singletonList(replacement)));

        BasicDBList basicDBVariables = VariableHelper.toBasicDBFormat(variables);
        BasicDBObject factoryURLbuilder = new BasicDBObject();
        factoryURLbuilder.put("variables", basicDBVariables);

        List<Variable> result = VariableHelper.fromBasicDBFormat(factoryURLbuilder);

        Assert.assertEquals(variables, result);
    }

    @Test
    public void testToBasicDBFormat() throws Exception {
        List<Variable> variables = new ArrayList<>();
        Variable.Replacement replacement = new Variable.Replacement("findText", "replaceText");
        variables.add(new Variable(Collections.singletonList("glob_pattern"), Collections.singletonList(replacement)));



        BasicDBList basicDBVariables = VariableHelper.toBasicDBFormat(variables);
        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("variables", basicDBVariables);




        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(factoryURLbuilder.get().toString().getBytes("UTF-8")));
        JsonValue jsonValue = jsonParser.getJsonObject().getElement("variables");
        Assert.assertTrue(jsonValue.isArray());
        ArrayValue variablesArr = (ArrayValue)jsonValue;

        List<Variable> result = new ArrayList<>();
        List<String> resultFiles = new ArrayList<>();
        List<Variable.Replacement> resultReplacements = new ArrayList<>();

        Iterator<JsonValue> iterator = variablesArr.getElements();
        while (iterator.hasNext()) {
            resultFiles.clear();
            resultReplacements.clear();
            ObjectValue o = (ObjectValue)iterator.next();

            ArrayValue files = (ArrayValue)o.getElement("files");
            Iterator<JsonValue> filesIterator = files.getElements();
            while (filesIterator.hasNext()) {
                JsonValue fileValue = filesIterator.next();
                if (fileValue.isString()) {
                    resultFiles.add(fileValue.getStringValue());
                }
            }

            ArrayValue entries = (ArrayValue)o.getElement("entries");
            Iterator<JsonValue> entriesIterator = entries.getElements();
            while (entriesIterator.hasNext()) {
                JsonValue entryValue = entriesIterator.next();
                if (entryValue.isObject()) {
                    resultReplacements.add(new Variable.Replacement(entryValue.getElement("find").getStringValue(),
                                                                    entryValue.getElement("replace").getStringValue()));
                }
            }

            result.add(new Variable(resultFiles, resultReplacements));
        }



        Assert.assertEquals(variables, result);
    }
}
