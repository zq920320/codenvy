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

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import static com.google.common.collect.Iterables.get;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class SqlBillingServiceTest extends AbstractSQLTest {


    @DataProvider(name = "storage")
    public Object[][] createDS() throws SQLException {

        Object[][] result = new Object[sources.length][];
        for (int i = 0; i < sources.length; i++) {
            DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(sources[i]);
            result[i] = new Object[]{new SqlMeterBasedStorage(connectionFactory),
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices("ac-3", -1, 0);
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 30.9);

        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), "Saas");
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPaidAmount(), 206.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "216.0");

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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices("ac-3", -1, 0);
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.0);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), "Saas");
        assertEquals(saasCharge.getFreeAmount(), 0.523056);
        assertEquals(saasCharge.getPaidAmount(), 0.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "0.523056");
    }

    @Test(dataProvider = "storage")
    public void shouldCalculateBetweenSeveralWorkspaces(MeterBasedStorage meterBasedStorage,
                                                        BillingService billingService)
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices("ac-3", -1, 0);
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);

        assertEquals(invoice.getTotal(), 0.0);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), "Saas");
        assertEquals(saasCharge.getFreeAmount(), 1.046112);
        assertEquals(saasCharge.getPaidAmount(), 0.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 2);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "0.523056");
        assertEquals(saasCharge.getDetails().get("ws-2"), "0.523056");
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices("ac-3", -1, 0);
        List<Invoice> ac5 = billingService.getInvoices("ac-5", -1, 0);
        List<Invoice> ac1 = billingService.getInvoices("ac-1", -1, 0);
        //then
        assertEquals(ac3.size(), 1);
        assertEquals(get(ac3, 0).getTotal(), 30.9);


        assertEquals(ac5.size(), 1);
        assertEquals(get(ac5, 0).getTotal(), 2.1);

        assertEquals(ac1.size(), 1);
        assertEquals(get(ac1, 0).getTotal(), 0.0);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToGetByPaymentState(MeterBasedStorage meterBasedStorage, BillingService billingService)
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

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 11:00:00").getTime(),
                                     "usr-34",
                                     "ac-4",
                                     "ws-4567845",
                                     "run-345634"));


        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        //then
        assertEquals(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0).size(), 2);
        assertEquals(billingService.getInvoices(PaymentState.NOT_REQUIRED, -1, 0).size(), 1);
        assertEquals(billingService.getInvoices(PaymentState.EXECUTING, -1, 0).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAYMENT_FAIL, -1, 0).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.CREDIT_CARD_MISSING, -1, 0).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAID_SUCCESSFULLY, -1, 0).size(), 0);

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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Invoice invoice = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1);
        billingService.setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL, "cc111");

        //then
        assertEquals(invoice.getPaymentDate().longValue(), 0L);
        assertEquals(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0).size(), 1);
        assertEquals(billingService.getInvoices(PaymentState.EXECUTING, -1, 0).size(), 0);
        List<Invoice> invoices = billingService.getInvoices(PaymentState.PAYMENT_FAIL, -1, 0);
        assertEquals(invoices.size(), 1);

        Invoice invoice1 = get(invoices, 0);
        Assert.assertTrue(invoice1.getPaymentDate() > 0);
        assertEquals(invoice1.getCreditCardId(), "cc111");
        assertEquals(get(billingService.getInvoices(PaymentState.PAYMENT_FAIL, -1, 0), 0).getId(), invoice.getId());
        assertEquals(billingService.getInvoices(PaymentState.CREDIT_CARD_MISSING, -1, 0).size(), 0);
        assertEquals(billingService.getInvoices(PaymentState.PAID_SUCCESSFULLY, -1, 0).size(), 0);

    }


    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingFailPaymentReceipt(MeterBasedStorage meterBasedStorage,
                                                                BillingService billingService)
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAYMENT_FAIL, null);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(-1, 0);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingPaidSuccessfulyReceipt(MeterBasedStorage meterBasedStorage,
                                                                    BillingService billingService)
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAID_SUCCESSFULLY, null);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(-1, 0);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToSetGetByMailingCreditCardMissingReceipt(MeterBasedStorage meterBasedStorage,
                                                                      BillingService billingService)
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING, null);

        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(-1, 0);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);

    }


    @Test(dataProvider = "storage")
    public void shouldBeAbleToGetByMailingPaymentNotRequiredReceipt(MeterBasedStorage meterBasedStorage,
                                                                    BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 11:20:56").getTime(),
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        //then
        List<Invoice> notSendInvoice = billingService.getNotSendInvoices(-1, 0);
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getAccountId(), "ac-5");

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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING, null);
        billingService.markInvoiceAsSent(id);
        //then
        assertEquals(billingService.getNotSendInvoices(-1, 0).size(), 0);
    }


    @Test(dataProvider = "storage", expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp =
            "Invoice with id " +
            "498509 is not found")
    public void shouldFailIfInvoiceIsNotFound(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ServerException, NotFoundException {
        //given
        //when
        billingService.getInvoice(498509);
    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToGetInvoicesById(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException, NotFoundException {
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Invoice expected = get(billingService.getInvoices(PaymentState.WAITING_EXECUTOR, -1, 0), 1);
        Invoice actual = billingService.getInvoice(expected.getId());
        assertEquals(actual, expected);


    }

    @Test(dataProvider = "storage", expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Not able to generate invoices. Result overlaps with existed invoices.")
    public void shouldFailToCalculateInvoicesTwiceWithOverlappingPeriod(MeterBasedStorage meterBasedStorage,
                                                                        BillingService billingService)
            throws ParseException, ServerException, NotFoundException {
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
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.generateInvoices(sdf.parse("09-01-2015 00:00:00").getTime(),
                                        sdf.parse("15-02-2015 00:00:00").getTime());

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToAddPrepaidTime(MeterBasedStorage meterBasedStorage,
                                             BillingService billingService)
            throws ParseException, ServerException, NotFoundException {
        //when
        billingService.addPrepaid("ac-1", 34.34,
                                  sdf.parse("01-01-2015 00:00:00").getTime(),
                                  sdf.parse("01-02-2015 00:00:00").getTime());
    }

    @Test(dataProvider = "storage", expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Unable to add new prepaid time since it overlapping with existed period")
    public void shouldNoBeAbleToAddPrepaidTimeForIntersectionPeriod(MeterBasedStorage meterBasedStorage,
                                                                    BillingService billingService)
            throws ParseException, ServerException, NotFoundException {
        //when
        billingService.addPrepaid("ac-1", 34.34,
                                  sdf.parse("01-01-2015 00:00:00").getTime(),
                                  sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addPrepaid("ac-1", 34.34,
                                  sdf.parse("15-01-2015 00:00:00").getTime(),
                                  sdf.parse("15-02-2015 00:00:00").getTime());

    }
}