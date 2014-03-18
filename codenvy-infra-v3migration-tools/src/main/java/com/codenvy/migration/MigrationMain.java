package com.codenvy.migration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.codenvy.migration.daoExport.DaoExporter;
import com.codenvy.migration.daoExport.DaoManager;
import com.codenvy.migration.ldapImport.LdapImporter;
import com.codenvy.migration.ldapImport.factory.AccountFactory;
import com.codenvy.migration.ldapImport.factory.ObjectFactory;
import com.codenvy.migration.ldapImport.factory.UserFactory;
import com.codenvy.migration.ldapImport.factory.WorkspaceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class MigrationMain {
    static {
        System.setProperty("logback.configurationFile", "conf/logback.xml");  //To avoid logback DEBUG's
    }

    private static final Logger LOG = LoggerFactory.getLogger(MigrationMain.class);

    public static void main(String[] args) {
        MigrationParamsSet paramsSet = new MigrationParamsSet();
        JCommander jCommander = new JCommander(paramsSet);
        jCommander.setCaseSensitiveOptions(false);
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage() + ". Exiting now.");
            paramsSet.setHelp(true);
        }

        if (paramsSet.isEnabledHelp()) {
            showHelp();
        }

        if (paramsSet.isEnabledExport() && (paramsSet.getLdapConf() == null || paramsSet.getMongoConf() == null)) {
            System.out.println("Optional parameter '--export' is present but required parameter for it is missing");
            showHelp();
        }

        MemoryStorage storage = new MemoryStorage();
        LdapImporter ldapReader = new LdapImporter(storage, new HashSet<ObjectFactory>(Arrays.asList(
                new UserFactory(),
                new AccountFactory(),
                new WorkspaceFactory())
        ));

        LOG.info("Started reading");
        try {
            ldapReader.importFromFile(paramsSet.getDumpFile());
        } catch (IOException e) {
            LOG.error("Error reading dump file. " + e.getMessage(), e);
            System.exit(1);
        }
        LOG.info("Finished reading");

        LOG.info("Count users = " + storage.getUsers().size());
        LOG.info("Count accounts = " + storage.getAccounts().size());
        LOG.info("Count workspaces = " + storage.getWorkspaces().size());

        LOG.info("Started integrity checking");

        IntegrityChecker checker = new IntegrityChecker(storage);
        checker.check();

        LOG.info("Finished integrity checking");

        if (paramsSet.isEnabledExport()) {
            LOG.info("Started exporting");

            Properties ldapProperties = new Properties();
            Properties mongoProperties = new Properties();
            try {
                ldapProperties.load(new FileInputStream(paramsSet.getLdapConf()));
                mongoProperties.load(new FileInputStream(paramsSet.getMongoConf()));
            } catch (IOException e) {
                LOG.error("Error reading of config files");
                System.exit(1);
            }

            DaoManager daoManager = new DaoManager(ldapProperties, mongoProperties);
            DaoExporter daoExporter = new DaoExporter(storage, daoManager);
            daoExporter.export();

            LOG.info("Finished exporting");
        }
    }

    private static void showHelp() {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Usage :  java -jar codenvy-organization-migration-{version}.jar [options]");
        System.out.println("Required parameters:");
        System.out.println("        -dumpFile          : Path to LDAP dump file");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Optional parameters:");
        System.out.println("        --export           : Export objects to new schema");
        System.out.println("        -help              : Display this help message.");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Required parameters if present optional parameter '--export':");
        System.out.println("        -ldapConf          : Path to LDAP configuration file");
        System.out.println("        -mongoConf         : Path to mongo configuration file");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Reading objects with checking examples:");
        System.out.println("java -jar codenvy-organization-migration-0.10.0-SNAPSHOT.jar -dumpfile ~/Dump.txt");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~OR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Reading objects without checking examples:");
        System.out.println("java -jar codenvy-organization-migration-0.10.0-SNAPSHOT.jar -dumpfile ~/Dump.txt " +
                           "--export -ldapConf conf/ldap.properties -mongoConf conf/mongo.properties");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.exit(1);
    }
}
