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
package com.codenvy.subscription;

import com.codenvy.api.account.shared.dto.NewBilling;
import com.codenvy.api.account.shared.dto.NewSubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.dto.server.DtoFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Tests for {@link com.codenvy.subscription.SubscriptionAttributesValidatorImpl]}
 */
public class SubscriptionAttributesValidatorImplTest {
    private NewSubscriptionAttributes           defaultSubscriptionAttributes;
    private SubscriptionAttributesValidatorImpl validator;

    @BeforeMethod
    public void setUp() throws Exception {
        validator = new SubscriptionAttributesValidatorImpl();

        defaultSubscriptionAttributes = DtoFactory.getInstance().createDto(NewSubscriptionAttributes.class)
                                                  .withTrialDuration(7)
                                                  .withStartDate("11/12/2014")
                                                  .withEndDate("11/12/2015")
                                                  .withDescription("description")
                                                  .withCustom(Collections.singletonMap("key", "value"))
                                                  .withBilling(DtoFactory.getInstance().createDto(NewBilling.class)
                                                                         .withStartDate("11/12/2014")
                                                                         .withEndDate("11/12/2015")
                                                                         .withUsePaymentSystem("true")
                                                                         .withCycleType(1)
                                                                         .withCycle(1)
                                                                         .withContractTerm(1)
                                                                         .withPaymentToken("token"));
    }

    @Test
    public void shouldBeAbleToValidateValidSubscriptionAttributes() throws Exception {
        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(dataProvider = "validDataProvider")
    public void shouldBeAbleToValidateValidDates(String date)
            throws Exception {
        defaultSubscriptionAttributes.withStartDate(date)
                                     .withEndDate(date)
                                     .getBilling()
                                     .withUsePaymentSystem("false")
                                     .withStartDate(date)
                                     .withEndDate(date);

        validator.validate(defaultSubscriptionAttributes);
    }

    @DataProvider(name = "validDataProvider")
    public String[][] validDataProvider() {
        return new String[][]{
                {"01/01/2015"},
                {"1/1/2015"},
                {"01/1/2015"},
                {"1/01/2015"},
                {"01/01/15"},
                {"12/31/2015"},
                {"02/29/2012"},
                {"02/28/2011"},

        };
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attributes required")
    public void shouldFailsIfSubscriptionAttributesAreNull() throws Exception {
        validator.validate(null);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute billing required")
    public void shouldFailsIfBillingIsNull() throws Exception {
        defaultSubscriptionAttributes.setBilling(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute usePaymentSystem required")
    public void shouldFailsIfUsePaymentSystemIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling().withUsePaymentSystem(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute usePaymentSystem required")
    public void shouldFailsIfUsePaymentSystemIsEmpty() throws Exception {
        defaultSubscriptionAttributes.getBilling().withUsePaymentSystem("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Value of billing attribute usePaymentSystem is invalid",
          dataProvider = "usePaymentSystemProvider")
    public void shouldFailsIfUsePaymentSystemIsNotTrueOrFalse(String usePaymentSystem) throws Exception {
        defaultSubscriptionAttributes.getBilling().setUsePaymentSystem(usePaymentSystem);

        validator.validate(defaultSubscriptionAttributes);
    }

    @DataProvider(name = "usePaymentSystemProvider")
    public Object[][] usePaymentSystemProvider() {
        return new String[][]{
                {"123"},
                {"True"},
                {"False"},
                {"tru"},
                {"11/10/2019"}
        };
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute description required")
    public void shouldFailsIfDescriptionIsNull() throws Exception {
        defaultSubscriptionAttributes.setDescription(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute description required")
    public void shouldFailsIfDescriptionIsEmpty() throws Exception {
        defaultSubscriptionAttributes.setDescription("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute trial duration required")
    public void shouldFailsIfUsePaymentSystemIsFalseAndTrialDurationIsNull() throws Exception {
        defaultSubscriptionAttributes.withTrialDuration(null).getBilling().setUsePaymentSystem("false");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute startDate required")
    public void shouldFailsIfStartDateIsNull() throws Exception {
        defaultSubscriptionAttributes.setStartDate(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute endDate required")
    public void shouldFailsIfEndDateIsNull() throws Exception {
        defaultSubscriptionAttributes.setEndDate(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute startDate required")
    public void shouldFailsIfStartDateIsEmpty() throws Exception {
        defaultSubscriptionAttributes.setStartDate("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription attribute endDate required")
    public void shouldFailsIfEndDateIsEmpty() throws Exception {
        defaultSubscriptionAttributes.setEndDate("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Value of subscription attribute startDate is invalid",
          dataProvider = "illegalDateProvider")
    public void shouldFailsIfStartDateFormatIsIllegal(String date) throws Exception {
        defaultSubscriptionAttributes.setStartDate(date);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Value of subscription attribute endDate is invalid",
          dataProvider = "illegalDateProvider")
    public void shouldFailsIfEndDateFormatIsIllegal(String date) throws Exception {
        defaultSubscriptionAttributes.setEndDate(date);

        validator.validate(defaultSubscriptionAttributes);
    }

    @DataProvider(name = "illegalDateProvider")
    public String[][] illegalStartDateProvider() {
        return new String[][]{
                {"sgas"},
                {"13456"},
                {"#&*D*"},
                {"1025/1554/46496"},
                {"15/15/2015"},
                {"12/32/2015"},
                {"02/30/2012"},
                {"02/29/2011"}
        };
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute paymentToken required")
    public void shouldFailsIfUsePaymentIsTrueAndPaymentTokenIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling()
                                     .withUsePaymentSystem("false")
                                     .withPaymentToken(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute paymentToken required")
    public void shouldFailsIfUsePaymentIsTrueAndPaymentTokenIsEmpty() throws Exception {
        defaultSubscriptionAttributes.getBilling()
                                     .withUsePaymentSystem("false")
                                     .withPaymentToken("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute startDate required")
    public void shouldFailsIfUsePaymentIsTrueAndBillingStartDateIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling()
                                     .withUsePaymentSystem("false")
                                     .withStartDate(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute startDate required")
    public void shouldFailsIfUsePaymentIsTrueAndBillingStartDateIsEmpty() throws Exception {
        defaultSubscriptionAttributes.getBilling()
                                     .withUsePaymentSystem("false")
                                     .withStartDate("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Value of billing attribute startDate is invalid",
          dataProvider = "illegalDateProvider")
    public void shouldFailsIfUsePaymentSystemIsTrueAndBillingStartDateFormatIsIllegal(String date) throws Exception {
        defaultSubscriptionAttributes.getBilling()
                                     .withUsePaymentSystem("false")
                                     .withStartDate(date);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute endDate required")
    public void shouldFailsIfBillingEndDateIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling().setEndDate(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute endDate required")
    public void shouldFailsIfBillingEndDateIsEmpty() throws Exception {
        defaultSubscriptionAttributes.getBilling().setEndDate("");

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Value of billing attribute endDate is invalid",
          dataProvider = "illegalDateProvider")
    public void shouldFailsIfBillingEndDateFormatIsIllegal(String date) throws Exception {
        defaultSubscriptionAttributes.getBilling().withEndDate(date);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute cycle required")
    public void shouldFailsIfCycleIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling().setCycle(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute cycleType required")
    public void shouldFailsIfCycleTypeIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling().setCycleType(null);

        validator.validate(defaultSubscriptionAttributes);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Billing attribute contractTerm required")
    public void shouldFailsIfContactTermIsNull() throws Exception {
        defaultSubscriptionAttributes.getBilling().setContractTerm(null);

        validator.validate(defaultSubscriptionAttributes);
    }
}