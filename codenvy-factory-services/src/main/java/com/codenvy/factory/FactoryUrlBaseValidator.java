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
package com.codenvy.factory;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.*;
import com.codenvy.api.account.shared.dto.Member;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.factory.*;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.factory.dto.Restriction;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.shared.dto.*;
import com.codenvy.api.user.shared.dto.Attribute;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates values of factory parameters.
 *
 * @author Alexander Garagatyi
 */
public class FactoryUrlBaseValidator {



    private static final Pattern PROJECT_NAME_VALIDATOR = Pattern.compile("^[\\\\\\w\\\\\\d]+[\\\\\\w\\\\\\d_.-]*$");

    private AccountDao accountDao;

    private UserDao userDao;

    private UserProfileDao profileDao;

    public FactoryUrlBaseValidator(AccountDao accountDao, UserDao userDao, UserProfileDao profileDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }


    protected void validateVcs(Factory factory) throws FactoryUrlException{
        // check that vcs value is correct (only git is supported for now)
        if (!"git".equals(factory.getVcs())) {
            throw new FactoryUrlException("Parameter 'vcs' has illegal value. Only 'git' is supported for now.");
        }
        if (factory.getVcsurl() == null || factory.getVcsurl().isEmpty()) {
            throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, "vcsurl", factory.getVcsurl()));
        } else {
            try {
                URLDecoder.decode(factory.getVcsurl(), "UTF-8");
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, "vcsurl", factory.getVcsurl()));
            }
        }
    }


    protected void validateProjectName(Factory factory) throws FactoryUrlException {
        // validate project name
        String pname = null;
        if (factory.getV().equals("1.0")) {
            pname = factory.getPname();
        } else if (factory.getProjectattributes() != null) {
            pname = factory.getProjectattributes().getPname();
        }
        if (null != pname && !PROJECT_NAME_VALIDATOR.matcher(pname).matches()) {
            throw new FactoryUrlException(
                    "Project name must contain only Latin letters, digits or these following special characters -._.");
        }
    }


    protected void  validateOrgid(Factory factory) throws FactoryUrlException {
        // validate orgid
        String orgid = "".equals(factory.getOrgid()) ? null : factory.getOrgid();
        if (null != orgid) {
            if (factory.getUserid() != null) {
                try {
                    User user = userDao.getById(factory.getUserid());
                    Profile profile = profileDao.getById(factory.getUserid());
                    for (Attribute attribute : profile.getAttributes()) {
                        if (attribute.getName().equals("temporary") && Boolean.parseBoolean(attribute.getValue()))
                            throw new FactoryUrlException("Current user is not allowed for using this method.");
                    }
                    boolean isOwner = false;
                    List<Member> members = accountDao.getMembers(orgid);
                    if (members.isEmpty()) {
                        throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_ORGID_PARAMETER_MESSAGE, factory.getOrgid()));
                    }
                    for (Member accountMember : members) {
                        if (accountMember.getUserId().equals(user.getId()) && accountMember.getRoles().contains(
                                "account/owner")) {
                            isOwner = true;
                            break;
                        }
                    }
                    if (!isOwner) {
                        throw new FactoryUrlException("You are not authorized to use this orgid.");
                    }
                } catch (NotFoundException | ServerException e) {
                    throw new FactoryUrlException("You are not authorized to use this orgid.");
                }
            }
        }

    }


    protected void validateTrackedFactoryAndParams(Factory factory) throws FactoryUrlException {
        // validate tracked parameters
        Restriction restriction = factory.getRestriction();
        String orgid = "".equals(factory.getOrgid()) ? null : factory.getOrgid();

        try {
            List<Subscription> subscriptions = accountDao.getSubscriptions(factory.getOrgid());
            boolean isTracked = false;
            for (Subscription one : subscriptions) {
                if ("TrackedFactory".equals(one.getServiceId())) {
                    Date startTimeDate = new Date(one.getStartDate());
                    Date endTimeDate = new Date(one.getEndDate());
                    Date currentDate = new Date();
                    if (!startTimeDate.before(currentDate) || !endTimeDate.after(currentDate)) {
                        throw new FactoryUrlException(
                                String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_ORGID_PARAMETER_MESSAGE, factory.getOrgid()));
                    }
                    isTracked = true;
                    break;
                }
            }
            if (!isTracked)
                throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_ORGID_PARAMETER_MESSAGE, factory.getOrgid()));
        } catch (ServerException | NumberFormatException e) {
            throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_ORGID_PARAMETER_MESSAGE, factory.getOrgid()));
        }



        if (restriction != null) {
            if (0 != restriction.getValidsince()) {
                if (null == orgid) {
                    throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "validsince", null));
                }

                if (new Date().before(new Date(restriction.getValidsince()))) {
                    throw new FactoryUrlException(FactoryConstants.ILLEGAL_VALIDSINCE_MESSAGE);
                }
            }

            if (0 != restriction.getValiduntil()) {
                if (null == orgid) {
                    throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "validuntil", null));
                }

                if (new Date().after(new Date(restriction.getValiduntil()))) {
                    throw new FactoryUrlException(FactoryConstants.ILLEGAL_VALIDUNTIL_MESSAGE);
                }
            }



            if (restriction.getRestrictbypassword()) {
                if (null == orgid) {
                    throw new FactoryUrlException(
                            String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "restrictbypassword", null));
                }

                // TODO implement
            }

            if (null != restriction.getPassword()) {
                if (null == orgid) {
                    throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "password", null));
                }

                // TODO implement
            }

            if (0 != restriction.getMaxsessioncount()) {
                if (null == orgid) {
                    throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "maxsessioncount", null));
                }

                // TODO implement
            }
        }

        if (null != factory.getWelcome()) {
            if (null == orgid) {
                throw new FactoryUrlException(String.format(FactoryConstants.PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE, "welcome", null));
            }
        }
    }

}
