package com.codenvy.migration.ldapImport;

public class LdapAttribute {
    private String name;
    private String value;

    public LdapAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse value that contains list of values separated commas
     *
     * @return list values
     */
    public String[] getValues() {
        return value.substring(1).split(",");
    }

    /**
     * Parse value that is saved in format pair key and value
     *
     * @return key and value in array
     */
    public String[] getKeyValue() {
        return value.split("<>");
    }

    @Override
    public String toString() {
        return name + " : " + value;
    }
}
