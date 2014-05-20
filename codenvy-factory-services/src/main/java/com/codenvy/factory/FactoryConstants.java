/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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

/**
 * Message constants for factory validator.
 */
public class FactoryConstants {
    public static final String INVALID_PARAMETER_MESSAGE =
            "Passed in an invalid parameter.  You either provided a non-valid parameter, or that parameter is not " +
            "accepted for this Factory version.  For more information, please visit " +
            "http://docs.codenvy.com/user/creating-factories/factory-parameter-reference/.";


    public static final String ILLEGAL_HOSTNAME_MESSAGE =
            "This Factory has its access restricted by certain hostname. Your client does not match the specified " +
            "policy. Please contact the owner of this Factory for more information.";

    public static final String PARAMETRIZED_ILLEGAL_ORGID_PARAMETER_MESSAGE =
            "You have provided an invalid orgId %s. You could have provided the wrong code, " +
            "your subscription has expired, or you do not have a valid subscription account. Please contact " +
            "info@codenvy.com with any questions.";

    public static final String PARAMETRIZED_ILLEGAL_TRACKED_PARAMETER_MESSAGE =
            "You have provided a Tracked Factory parameter %s, and you do not have a valid orgId %s. You could have " +
            "provided the wrong code, your subscription has expired, or you do not have a valid subscription account." +
            " Please contact info@codenvy.com with any questions.";


    public static final String ILLEGAL_VALIDSINCE_MESSAGE =
            "This Factory is not yet valid due to time restrictions applied by its owner.  Please, " +
            "contact owner for more information.";

    public static final String ILLEGAL_VALIDUNTIL_MESSAGE =
            "This Factory has expired due to time restrictions applied by its owner.  Please, " +
            "contact owner for more information.";

    public static final String PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE =
            "The parameter %s has a value submitted %s with a value that is unexpected. For more information, " +
            "please visit: http://docs.codenvy.com/user/creating-factories/factory-parameter-reference/.";
}
