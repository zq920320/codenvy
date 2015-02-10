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
package com.codenvy.api.dao.sql;

import static com.google.common.collect.Iterables.get;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.MonthlyBillingPeriod;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.shared.dto.MemoryChargeDetails;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;


public class SqlBillingServiceTest extends AbstractSQLTest {


    @DataProvider(name = "storage")
    public Object[][] createDS() throws SQLException {

        Object[][] result = new Object[sources.length][];
        for (int i = 0; i < sources.length; i++) {
            DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(sources[i]);
            result[i] = new Object[]{new SqlMeterBasedStorage(connectionFactory, new MonthlyBillingPeriod()),
                                     new SqlBillingService(connectionFactory, 0.15, 10.0)};
        }
        return result;
    }



    @Test(dataProvider = "storage")
    public void shouldCalculateSimpleReceipt(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given


        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Invoice> ac3 = billingService.getInvoices("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 30.9);
        //free + paid
        assertEquals(invoice.getCharges().size(), 2);
        assertTrue(invoice.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(10.0)
                                                           .withPrice(0.0)
                                                           .withServiceId("Saas")
                                                           .withType("Free")));

        assertTrue(invoice.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(206.0)
                                                           .withPrice(0.15)
                                                           .withServiceId("Saas")
                                                           .withType("Paid")));

        assertEquals(invoice.getMemoryChargeDetails().size(), 1);
        assertTrue(invoice.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                                                       .withAmount(216.0)
                                                                       .withWorkspaceId("ws-235423")))


        ;
    }

    @Test(dataProvider = "storage")
    public void shouldCalculateFreeHours(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ServerException, ParseException {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Invoice> ac3 = billingService.getInvoices("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.0);
        //free + paid
        assertEquals(invoice.getCharges().size(), 1);
        assertTrue(invoice.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(0.52306)
                                                           .withPrice(0.0)
                                                           .withServiceId("Saas")
                                                           .withType("Free")));

        assertEquals(invoice.getMemoryChargeDetails().size(), 1);
        assertTrue(invoice.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                                                       .withAmount(0.52306)
                                                                       .withWorkspaceId("ws-235423")));


    }

    @Test(dataProvider = "storage")
    public void shouldCalculateBetweenSeveralWorkspaces(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ServerException, ParseException {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("02-01-2015 10:00:00").getTime(),
                                     sdf.parse("02-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-2",
                                     "run-234"));

        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Invoice> ac3 = billingService.getInvoices("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.0);
        //free + paid
        assertEquals(invoice.getCharges().size(), 1);
        assertTrue(invoice.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(1.04612)
                                                           .withPrice(0.0)
                                                           .withServiceId("Saas")
                                                           .withType("Free")));

        assertEquals(invoice.getMemoryChargeDetails().size(), 2);
        assertTrue(invoice.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                                                       .withAmount(0.52306)
                                                                       .withWorkspaceId("ws-235423")));
        assertTrue(invoice.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                                                       .withAmount(0.52306)
                                                                       .withWorkspaceId("ws-2")));


    }


    @Test(dataProvider = "storage")
    public void shouldCalculateWithMultipleAccounts(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 18:20:56").getTime(),
                                     "usr-123",
                                     "ac-1",
                                     "ws-235423",
                                     "run-234"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 11:18:35").getTime(),
                                     "usr-4358634",
                                     "ac-1",
                                     "ws-4356",
                                     "run-435876"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Invoice> ac3 = billingService.getInvoices("ac-3");
        List<Invoice> ac5 = billingService.getInvoices("ac-5");
        List<Invoice> ac1 = billingService.getInvoices("ac-1");
        //then
        assertEquals(ac3.size(), 1);
        assertEquals(get(ac3, 0).getTotal(), 30.9);


        assertEquals(ac5.size(), 1);
        assertEquals(get(ac5, 0).getTotal(), 2.1);

        assertEquals(ac1.size(), 1);
        assertEquals(get(ac1, 0).getTotal(), 0.0);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByPaymentState(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        //then
        assertEquals(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5).size(), 2);
        assertEquals(billingService.getInvoices(PaymentState.EXECUTING, 5).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAYMENT_FAIL, 5).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.CREDIT_CARD_MISSING, 5).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAID_SUCCESSFULLY, 5).size(), 0);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetPaymentState(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        Invoice invoice = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5), 1);
        billingService.setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL);
        //then
        assertEquals(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5).size(), 1);
        assertEquals(billingService.getInvoices(PaymentState.EXECUTING, 5).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAYMENT_FAIL, 5).size(), 1);
        assertEquals(get(billingService.getInvoices(PaymentState.PAYMENT_FAIL, 5), 0).getId(), invoice.getId());
        assertEquals(billingService.getInvoices(PaymentState.CREDIT_CARD_MISSING, 5).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAID_SUCCESSFULLY, 5).size(), 0);

    }


    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingFailPaymentReceipt(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAYMENT_FAIL);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(5);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingPaidSuccessfulyReceipt(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAID_SUCCESSFULLY);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(5);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingCreditCardMissingReceipt(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(5);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }


    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetReceiptMailState(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        //when
        billingService.generateInvoices(Long.MIN_VALUE, Long.MAX_VALUE);
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, 5), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING);
        billingService.markInvoiceAsSent(id);
        //then
        assertEquals(billingService.getNotSendInvoices(5).size(), 0);
    }

}