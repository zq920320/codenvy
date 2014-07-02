/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All users' emails are be stored into different files. The paths to the files are placed in the configuration under
 * parameters with 'file' key.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class FileStoredRecipientGroup extends AbstractRecipientGroup {

    private static final String FILE = "file";

    public FileStoredRecipientGroup(List<ParameterConfiguration> parameters) {
        super(parameters);
    }

    @Override
    public Set<String> getEmails(Context context) throws IOException {
        Set<String> emails = new HashSet<>();
        Set<String> files = getParameters(FILE);

        for (String file : files) {
            emails.addAll(readFromFile(file));
        }

        return emails;
    }

    private Set<String> readFromFile(String file) throws IOException {
        Set<String> emails = new HashSet<>();

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty()) {
                    emails.add(line);
                }
            }
        }

        return emails;
    }
}
