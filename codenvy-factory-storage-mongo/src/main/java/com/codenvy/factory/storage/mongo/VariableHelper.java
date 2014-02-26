package com.codenvy.factory.storage.mongo;

import com.codenvy.api.factory.dto.Replacement;
import com.codenvy.api.factory.dto.Variable;
import com.codenvy.dto.server.DtoFactory;
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
        List<Replacement> replacements = new ArrayList<>();
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
                    Replacement replacement = DtoFactory.getInstance().createDto(Replacement.class);
                    for (Object o1 : basicDBEntries) {
                        BasicDBObject entries = (BasicDBObject)o1;
                        for (Map.Entry<String, Object> entry : entries.entrySet()) {
                            if ("find".equals(entry.getKey())) {
                                replacement.setFind((String)entry.getValue());
                                continue;
                            }
                            if ("replace".equals(entry.getKey())) {
                                replacement.setReplace((String)entry.getValue());
                                continue;
                            }

                            if ("replacemode".equals(entry.getKey())) {
                                replacement.setReplacemode((String)entry.getValue());
                            }
                        }
                        replacements.add(replacement);
                    }

                    Variable variable = DtoFactory.getInstance().createDto(Variable.class);
                    variable.setFiles(files);
                    variable.setEntries(replacements);
                    variables.add(variable);
                }
            }
        }

        return variables;
    }

    /** Transform List of Variables into Mongo DB Basic Object to allow save list in database. */
    public static BasicDBList toBasicDBFormat(List<Variable> variables) {
        BasicDBList basicDBVariables = new BasicDBList();

        if (variables != null) {
            for (Variable variable : variables) {
                BasicDBList files = new BasicDBList();
                BasicDBList basicDBReplacements = new BasicDBList();

                for (String file : variable.getFiles()) {
                    files.add(file);
                }

                for (Replacement replacement : variable.getEntries()) {
                    BasicDBObject BasicDBReplacement = new BasicDBObject();
                    BasicDBReplacement.put("find", replacement.getFind());
                    BasicDBReplacement.put("replace", replacement.getReplace());
                    BasicDBReplacement.put("replacemode", replacement.getReplacemode());
                    basicDBReplacements.add(BasicDBReplacement);
                }

                BasicDBObject temporary = new BasicDBObject();
                temporary.put("files", files);
                temporary.put("entries", basicDBReplacements);

                basicDBVariables.add(temporary);
            }
        }

        return basicDBVariables;
    }
}
