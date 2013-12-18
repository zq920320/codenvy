package com.codenvy.factory.storage.mongo;

import com.codenvy.api.factory.Variable;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Variable util class to operate with parametrized variables. */
public class VariableHelper {
    public static List<Variable> fromBasicDBFormat(BasicDBObject object) {
        List<Variable> variables = new ArrayList<>();
        List<String> files = new ArrayList<>();
        List<Variable.Replacement> replacements = new ArrayList<>();
        BasicDBList basicDBVariables = (BasicDBList)object.get("variables");
        if (basicDBVariables != null) {
            for (Object o : basicDBVariables) {
                if (o instanceof BasicDBObject) {
                    BasicDBObject basicDBVariable = (BasicDBObject)o;

                    BasicDBList basicDBFiles = (BasicDBList)basicDBVariable.get("files");
                    BasicDBObject basicDBEntries = (BasicDBObject)basicDBVariable.get("entries");

                    files.clear();
                    for (Object basicDBFile : basicDBFiles) {
                        files.add((String)basicDBFile);
                    }

                    replacements.clear();
                    Variable.Replacement replacement = new Variable.Replacement();
                    for (Map.Entry<String, Object> entry : basicDBEntries.entrySet()) {
                        if ("find".equals(entry.getKey())) {
                            replacement.setFind((String)entry.getValue());
                            continue;
                        }
                        if ("replace".equals(entry.getKey())) {
                            replacement.setReplace((String)entry.getValue());
                        }
                    }
                    replacements.add(replacement);

                    variables.add(new Variable(files, replacements));
                }
            }
        }

        return variables;
    }

    public static BasicDBList toBasicDBFormat(List<Variable> variables) {
        BasicDBList basicDBVariables = new BasicDBList();
        for (Variable variable : variables) {
            BasicDBList files = new BasicDBList();
            BasicDBObject replacements = new BasicDBObject();

            for (String file : variable.getFiles()) {
                files.add(file);
            }

            for (Variable.Replacement replacement : variable.getEntries()) {
                replacements.put("find", replacement.getFind());
                replacements.put("replace", replacement.getReplace());
            }

            BasicDBObject temporary = new BasicDBObject();
            temporary.put("files", files);
            temporary.put("entries", replacements);

            basicDBVariables.add(temporary);
        }

        return basicDBVariables;
    }
}
