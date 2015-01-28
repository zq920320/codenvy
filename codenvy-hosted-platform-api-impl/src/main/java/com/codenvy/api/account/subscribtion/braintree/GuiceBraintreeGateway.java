/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.account.subscribtion.braintree;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Braintree gateway that can be initialized with Guice.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class GuiceBraintreeGateway extends BraintreeGateway {

    public static final String MERCHANT_ID = "braintree.merchant_id";
    public static final String PUBLIC_KEY  = "braintree.public_key";
    public static final String PRIVATE_KEY = "braintree.private_key";
    public static final String ENVIRONMENT = "braintree.environment";

    @Inject
    public GuiceBraintreeGateway(@Named(ENVIRONMENT) String environmentName, @Named(MERCHANT_ID) String merchantId,
                                 @Named(PUBLIC_KEY) String publicKey,
                                 @Named(PRIVATE_KEY) String privateKey) {
        super("production".equals(environmentName) ? Environment.PRODUCTION : Environment.SANDBOX, merchantId, publicKey, privateKey);
    }
}
