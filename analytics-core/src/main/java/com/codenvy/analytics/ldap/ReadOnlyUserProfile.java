package com.codenvy.analytics.ldap;

/**
 * Simple POJO to store user profile data
 */
public class ReadOnlyUserProfile {
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String company;

    public ReadOnlyUserProfile(String firstName, String lastName, String phoneNumber, String company) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.company = company;
    }

    public String getFirstName() {
        return firstName == null ? "" : firstName;
    }

    public String getLastName() {
        return lastName == null ? "" : lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber == null ? "" : phoneNumber;
    }

    public String getCompany() {
        return company == null ? "" : company;
    }
}
