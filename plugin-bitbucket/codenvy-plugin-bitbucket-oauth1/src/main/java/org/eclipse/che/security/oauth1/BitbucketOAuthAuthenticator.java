/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package org.eclipse.che.security.oauth1;

import org.eclipse.che.security.oauth1.shared.User;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * OAuth authentication for bitbucket account.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketOAuthAuthenticator extends OAuthAuthenticator {
    @Inject
    public BitbucketOAuthAuthenticator(@NotNull @Named("oauth.bitbucket.clientid") String clientId,
                                       @NotNull @Named("oauth.bitbucket.clientsecret") String clientSecret,
                                       @NotNull @Named("oauth.bitbucket.authuri") String authUri,
                                       @NotNull @Named("oauth.bitbucket.requesttokenuri") String requestTokenUri,
                                       @NotNull @Named("oauth.bitbucket.requestaccesstokenuri") String requestAccessTokenUri,
                                       @NotNull @Named("oauth.bitbucket.verifyaccesstokenuri") String verifyAccessTokenUri,
                                       @NotNull @Named("oauth.bitbucket.redirecturis") String redirectUri) {
        super(clientId,
              clientSecret,
              authUri,
              requestTokenUri,
              requestAccessTokenUri,
              verifyAccessTokenUri,
              redirectUri);
    }

    @Override
    public User getUser(final String token, final String tokenSecret) throws OAuthAuthenticationException {
        final BitbucketUser user = getJson("https://api.bitbucket.org/2.0/user", token, tokenSecret, BitbucketUser.class);
        final BitbucketEmail[] emails = getJson("https://api.bitbucket.org/1.0/emails", token, tokenSecret, BitbucketEmail[].class);

        BitbucketEmail primaryEmail = null;
        for (final BitbucketEmail oneEmail : emails) {
            if (oneEmail.isPrimary()) {
                primaryEmail = oneEmail;
                break;
            }
        }

        if (primaryEmail == null || primaryEmail.getEmail() == null || primaryEmail.getEmail().isEmpty()) {
            throw new OAuthAuthenticationException("Sorry, we failed to find any primary emails associated with your Bitbucket account.");
        }

        user.setEmail(primaryEmail.getEmail());

        try {

            new InternetAddress(user.getEmail()).validate();

        } catch (final AddressException e) {
            throw new OAuthAuthenticationException(e);
        }

        return user;
    }

    @Override
    public String getOAuthProvider() {
        return "bitbucket";
    }

    /**
     * Information for each email address indicating if the address.
     */
    public static class BitbucketEmail {
        private boolean primary;
        private String  email;

        public boolean isPrimary() {
            return primary;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public String getEmail() {
            return email;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setEmail(String email) {
            this.email = email;
        }
    }
}
