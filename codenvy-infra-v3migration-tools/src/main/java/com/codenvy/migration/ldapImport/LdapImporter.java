package com.codenvy.migration.ldapImport;

import com.codenvy.migration.MemoryStorage;
import com.codenvy.migration.ldapImport.factory.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LdapImporter {
    private static final Logger LOG = LoggerFactory.getLogger(LdapImporter.class);

    private static final String BEGIN_PARAMETER_OBJECT_CLASS = "structuralObjectClass: ";

    /** Set of object classes that will be ignored */
    private static final Set<String> IGNORED_OBJECT_CLASSES = new HashSet<>(Arrays.asList(
            "organizationalUnit",
            "inetOrgPerson",
            "cloudIdeInvitation")
    );

    private MemoryStorage memoryStorage;

    /** Factories for parse ldap object in text format */
    private Set<ObjectFactory> factories;

    public LdapImporter(MemoryStorage memoryStorage, Set<ObjectFactory> factories) {
        this.memoryStorage = memoryStorage;
        this.factories = factories;
    }

    public MemoryStorage getMemoryStorage() {
        return memoryStorage;
    }

    /**
     * @param item
     *         String representation of item
     * @return class of LDAP item
     */
    private String getObjectClass(List<String> item) {
        for (String line : item) {
            if (line.startsWith(BEGIN_PARAMETER_OBJECT_CLASS)) {
                return line.substring(BEGIN_PARAMETER_OBJECT_CLASS.length());
            }
        }
        return null;
    }

    /**
     * @return LDAP object in format list strings
     * @throws IOException
     *         when some error reading file
     */
    private List<String> readItem(BufferedReader reader) throws IOException {
        List<String> item = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            item.add(line);
        }

        return item;
    }

    /**
     * Reads objects that are saved in a dump LDAP file
     *
     * @param path
     *         to LDAP dump file
     * @throws IOException
     */
    public void importFromFile(String path) throws IOException {
        List<String> item;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            while (!(item = readItem(bufferedReader)).isEmpty()) {
                String objectClass = getObjectClass(item);
                boolean isCreated = false;

                if (!IGNORED_OBJECT_CLASSES.contains(objectClass)) {
                    for (ObjectFactory objectFactory : factories) {
                        if (objectFactory.isSuitableClass(objectClass)) {
                            try {
                                isCreated = true;
                                memoryStorage.add(objectFactory.create(item));
                                break;
                            } catch (Exception e) {
                                LOG.error("Error creating object. " + e.getMessage(), e);
                            }
                        }
                    }

                    if (!isCreated) {
                        LOG.warn("Class " + objectClass + " has not a handler and is not ignored");
                    }
                }
            }
        }
    }
}
