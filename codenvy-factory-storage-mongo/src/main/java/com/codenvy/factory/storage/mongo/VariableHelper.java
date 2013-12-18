package com.codenvy.factory.storage.mongo;

import com.codenvy.api.factory.Variable;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Variable util class to operate with parametrized variables. */
public class VariableHelper {

    /** Transform Basic DB Object from Mongo DB representation into List of Variables. */
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
                    BasicDBList basicDBEntries = (BasicDBList)basicDBVariable.get("entries");

                    files.clear();
                    for (Object basicDBFile : basicDBFiles) {
                        files.add((String)basicDBFile);
                    }

                    replacements.clear();
                    Variable.Replacement replacement = new Variable.Replacement();
                    for (Object o1 : basicDBEntries) {
                        BasicDBObject entries = (BasicDBObject)o1;
                        for (Map.Entry<String, Object> entry : entries.entrySet()) {
                            if ("find".equals(entry.getKey())) {
                                replacement.setFind((String)entry.getValue());
                                continue;
                            }
                            if ("replace".equals(entry.getKey())) {
                                replacement.setReplace((String)entry.getValue());
                            }
                        }
                        replacements.add(replacement);
                    }

                    variables.add(new Variable(files, replacements));
                }
            }
        }

        return variables;
    }

    /** Transform List of Variables into Mongo DB Basic Object to allow save list in database. */
    public static BasicDBList toBasicDBFormat(List<Variable> variables) {
        BasicDBList basicDBVariables = new BasicDBList();
        for (Variable variable : variables) {
            BasicDBList files = new BasicDBList();
            BasicDBList basicDBReplacements = new BasicDBList();

            for (String file : variable.getFiles()) {
                files.add(file);
            }

            for (Variable.Replacement replacement : variable.getEntries()) {
                BasicDBObject BasicDBReplacement = new BasicDBObject();
                BasicDBReplacement.put("find", replacement.getFind());
                BasicDBReplacement.put("replace", replacement.getReplace());
                basicDBReplacements.add(BasicDBReplacement);
            }

            BasicDBObject temporary = new BasicDBObject();
            temporary.put("files", files);
            temporary.put("entries", basicDBReplacements);

            basicDBVariables.add(temporary);
        }

        return basicDBVariables;
    }
}
