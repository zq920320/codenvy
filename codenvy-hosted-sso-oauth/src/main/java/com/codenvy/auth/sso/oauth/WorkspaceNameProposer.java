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
package com.codenvy.auth.sso.oauth;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.commons.lang.NameGenerator;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class allow find a suitable, not occupied workspace name */
public class WorkspaceNameProposer {
    private static final Pattern FORBIDDEN_INNER_CHARACTER    = Pattern.compile("[^a-z0-9-_.]");
    private static final Pattern FORBIDDEN_LEADING_CHARACTERS = Pattern.compile("^[^a-z0-9-_.]+");

    private final WorkspaceDao workspaceDao;

    @Inject
    public WorkspaceNameProposer(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    /**
     * Find a suitable, not occupied workspace name
     *
     * @param wsName
     *         - origin workspace name
     * @return - new workspace name that wasn't occupied yet. New name based on origin name.
     * Method normalize length and allowed symbols of ws name.
     * @throws com.codenvy.api.core.ServerException
     */
    public String propose(String wsName) throws ServerException {
        if (wsName == null) {
            throw new IllegalArgumentException("Workspace name can't be null.");
        }

        String workspaceName = normalizeWsName(wsName);

        String suffix = "";
        if (workspaceName.length() == 0) {
            suffix = NameGenerator.generate("", 6);
        } else if (workspaceName.length() < 3) {
            suffix = NameGenerator.generate("-", 6);
        }

        int counter = 0;


        while (counter++ < 100) {
            try {
                workspaceDao.getByName(workspaceName + suffix);
            } catch (NotFoundException e) {
                break;
            }
            if (workspaceName.length() == 0) {
                suffix = NameGenerator.generate("", 6);
            } else {
                suffix = NameGenerator.generate("-", 6);
            }
        }
        return workspaceName + suffix;
    }

    private String normalizeWsName(String origin) {
        // remove leading forbidden chars
        Matcher matcher = FORBIDDEN_LEADING_CHARACTERS.matcher(origin);
        origin = matcher.replaceFirst("");

        //replace forbidden chars by dash
        matcher = FORBIDDEN_INNER_CHARACTER.matcher(origin);
        origin = matcher.replaceAll("-");

        // limit ws name length
        if (origin.length() > 20) {
            return origin.substring(0, 20);
        } else {
            return origin;
        }
    }
}
