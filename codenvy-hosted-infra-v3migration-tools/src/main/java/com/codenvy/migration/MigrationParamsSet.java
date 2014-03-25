package com.codenvy.migration;

import com.beust.jcommander.Parameter;

public class MigrationParamsSet {
    @Parameter(names = "-dumpFile", required = true, description = "Path to file that contain ldap dump")
    private String dumpFile;

    @Parameter(names = "-ldapConf", required = false, description = "Path to file with ldap configuration")
    private String ldapConf;

    @Parameter(names = "-mongoConf", required = false, description = "Path to file with mongo configuration")
    private String mongoConf;

    @Parameter(names = "--export", required = false)
    private boolean enabledExport;

    @Parameter(names = "-help", help = true)
    private boolean help;

    public String getDumpFile() {
        return dumpFile;
    }

    public boolean isEnabledExport() {
        return enabledExport;
    }

    public boolean isEnabledHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public String getLdapConf() {
        return ldapConf;
    }

    public String getMongoConf() {
        return mongoConf;
    }
}
