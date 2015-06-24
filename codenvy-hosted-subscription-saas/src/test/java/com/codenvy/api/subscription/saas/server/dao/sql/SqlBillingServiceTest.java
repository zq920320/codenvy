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
package com.codenvy.api.subscription.saas.server.dao.sql;

import com.codenvy.api.metrics.server.MemoryUsedMetric;
import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.dao.sql.SqlMeterBasedStorage;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.period.MonthlyMetricPeriod;
import com.codenvy.api.metrics.server.period.Period;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.PaymentState;
import com.codenvy.api.subscription.saas.server.billing.ResourcesFilter;
import com.codenvy.api.subscription.saas.server.billing.bonus.Bonus;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceFilter;
import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.api.subscription.saas.shared.dto.AccountResources;
import com.codenvy.api.subscription.saas.shared.dto.Charge;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.shared.dto.Resources;
import com.codenvy.sql.DataSourceConnectionFactory;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static com.google.common.collect.Iterables.get;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;


public class SqlBillingServiceTest extends AbstractSQLTest {
    private MetricPeriod metricPeriod = new MonthlyMetricPeriod();

    private MeterBasedStorage meterBasedStorage;
    private BillingService    billingService;
    private BonusDao          bonusDao;

    @BeforeTest
    public void initT() throws SQLException {
        DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(source);
        meterBasedStorage = new SqlMeterBasedStorage(connectionFactory);
        billingService = new SqlBillingService(connectionFactory, 0.15, 10.0);
        bonusDao = new SqlBonusDao(connectionFactory, Mockito.mock(AccountDao.class));
    }

    @Test
    public void shouldCalculateSimpleInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 30.9);

        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), SAAS_SUBSCRIPTION_ID);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 0.0);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPaidAmount(), 206.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "216.0");
    }

    @Test(enabled = false)
    public void shouldBeAbleToFilterInvoiceByDates() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-02-2015 10:00:00").getTime(),
                                     sdf.parse("10-02-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-2343"));


        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime() - 1);
        billingService.generateInvoices(sdf.parse("01-02-2015 00:00:00").getTime(),
                                        sdf.parse("01-03-2015 00:00:00").getTime() - 1);
        List<Invoice> ac3 = billingService.getInvoices(
                InvoiceFilter.builder()
                             .withAccountId("ac-3")
                             .withFromDate(sdf.parse("01-02-2015 00:00:00").getTime())
                             .withTillDate(sdf.parse("01-03-2015 00:00:00").getTime() - 1).build());
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 30.9);
    }

    @Test
    public void shouldCalculateFreeHours() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.0);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), SAAS_SUBSCRIPTION_ID);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 0.0);
        assertEquals(saasCharge.getFreeAmount(), 0.535609);
        assertEquals(saasCharge.getPaidAmount(), 0.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "0.535609");
    }

    @Test
    public void shouldCalculateBonusHours() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(5000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        bonusDao.create(new Bonus().withAccountId("ac-3")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withResources(2D));
        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.0);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), SAAS_SUBSCRIPTION_ID);
        assertEquals(saasCharge.getProvidedFreeAmount(), 12.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 0.0);
        assertEquals(saasCharge.getFreeAmount(), 10.461111);
        assertEquals(saasCharge.getPaidAmount(), 0.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "10.461111");
    }

    @Test
    public void shouldCalculateSumOfBonusesHours() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(7500,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 12:05:32").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-3", 1, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        bonusDao.create(new Bonus().withAccountId("ac-3")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-01-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withResources(1D));
        bonusDao.create(new Bonus().withAccountId("ac-3")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-01-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-01-2016 00:00:00").getTime())
                                   .withResources(1D));
        bonusDao.create(new Bonus().withAccountId("ac-3")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-03-2015 00:00:00").getTime())
                                   .withResources(1D));
        bonusDao.create(new Bonus().withAccountId("ac-3")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-03-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-10-2015 00:00:00").getTime())
                                   .withResources(1D));
        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);
        assertEquals(invoice.getTotal(), 0.25);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), SAAS_SUBSCRIPTION_ID);
        assertEquals(saasCharge.getProvidedFreeAmount(), 13.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 1.0);
        assertEquals(saasCharge.getFreeAmount(), 13.0);
        assertEquals(saasCharge.getPrePaidAmount(), 1.0);
        assertEquals(saasCharge.getPaidAmount(), 1.691667);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 1);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "15.691667");
    }

    @Test
    public void shouldCalculateBetweenSeveralWorkspaces() throws Exception {
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
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        //then
        assertEquals(ac3.size(), 1);
        Invoice invoice = get(ac3, 0);

        assertEquals(invoice.getTotal(), 0.0);
        assertEquals(invoice.getCharges().size(), 1);
        Charge saasCharge = get(invoice.getCharges(), 0);
        assertEquals(saasCharge.getServiceId(), SAAS_SUBSCRIPTION_ID);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 0.0);
        assertEquals(saasCharge.getFreeAmount(), 1.071218);
        assertEquals(saasCharge.getPaidAmount(), 0.0);
        assertEquals(saasCharge.getPaidPrice(), 0.15);
        assertEquals(saasCharge.getPrePaidAmount(), 0.0);
        assertNotNull(saasCharge.getDetails());
        assertEquals(saasCharge.getDetails().size(), 2);
        assertEquals(saasCharge.getDetails().get("ws-235423"), "0.535609");
        assertEquals(saasCharge.getDetails().get("ws-2"), "0.535609");
    }

    @Test
    public void shouldCalculateWithMultipleAccounts() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
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
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-1", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());


        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        List<Invoice> ac3 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-3"));
        List<Invoice> ac5 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5"));
        List<Invoice> ac1 = billingService.getInvoices(getInvoiceFilterWithAccountId("ac-1"));
        //then
        assertEquals(ac3.size(), 1);
        assertEquals(get(ac3, 0).getTotal(), 30.9);


        assertEquals(ac5.size(), 1);
        assertEquals(get(ac5, 0).getTotal(), 2.1);

        assertEquals(ac1.size(), 1);
        assertEquals(get(ac1, 0).getTotal(), 0.0);
    }

    @Test
    public void shouldBeAbleToGetByPaymentState() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("01-01-2015 11:00:00").getTime(),
                                     "usr-34",
                                     "ac-4",
                                     "ws-4567845",
                                     "run-345634"));

        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-4", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        //then
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)).size(), 2);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.NOT_REQUIRED)).size(), 1);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.EXECUTING)).size(), 0);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.PAYMENT_FAIL)).size(), 0);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.CREDIT_CARD_MISSING)).size(), 0);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.PAID_SUCCESSFULLY)).size(), 0);
    }

    @Test
    public void shouldBeAbleToSetPaymentState() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());


        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Invoice invoice = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1);
        billingService.setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL, "cc111");

        //then
        assertEquals(invoice.getPaymentDate().longValue(), 0L);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)).size(), 1);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.EXECUTING)).size(), 0);
        List<Invoice> invoices = billingService.getInvoices(getInvoicesFilterWithState(PaymentState.PAYMENT_FAIL));
        assertEquals(invoices.size(), 1);

        Invoice invoice1 = get(invoices, 0);
        Assert.assertTrue(invoice1.getPaymentDate() > 0);
        assertEquals(invoice1.getCreditCardId(), "cc111");
        assertEquals(get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.PAYMENT_FAIL)), 0).getId(), invoice.getId());
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.CREDIT_CARD_MISSING)).size(), 0);
        assertEquals(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.PAID_SUCCESSFULLY)).size(), 0);
    }

    @Test
    public void shouldBeAbleToSetGetByMailingFailPaymentInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAYMENT_FAIL, "cc-234356");

        //then
        List<Invoice> notSendInvoice = billingService.getInvoices(getInvoiceFilterWithNotSendInvoices());
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);
    }

    @Test
    public void shouldBeAbleToSetGetByMailingPaidSuccessfullyInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1).getId();
        billingService.setPaymentState(id, PaymentState.PAID_SUCCESSFULLY, "cc-445");

        //then
        List<Invoice> notSendInvoice = billingService.getInvoices(getInvoiceFilterWithNotSendInvoices());
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);
    }

    @Test
    public void shouldBeAbleToSetGetByMailingNotRequiredInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1).getId();
        billingService.setPaymentState(id, PaymentState.NOT_REQUIRED, null);

        //then
        List<Invoice> notSendInvoice = billingService.getInvoices(getInvoiceFilterWithNotSendInvoices());
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);
    }

    @Test
    public void shouldBeAbleToSetGetByMailingCreditCardMissingInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING, null);

        //then
        List<Invoice> notSendInvoice = billingService.getInvoices(getInvoiceFilterWithNotSendInvoices());
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getId(), id);
    }

    @Test
    public void shouldBeAbleToGetByMailingPaymentNotRequiredInvoice() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 11:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));


        billingService.addSubscription("ac-5",
                                       0,
                                       sdf.parse("01-01-2014 00:00:00").getTime(),
                                       sdf.parse("01-02-2016 00:00:00").getTime());
        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        //then
        List<Invoice> notSendInvoice = billingService.getInvoices(getInvoiceFilterWithNotSendInvoices());
        assertEquals(notSendInvoice.size(), 1);
        assertEquals(get(notSendInvoice, 0).getAccountId(), "ac-5");
    }


    @Test
    public void shouldBeAbleToSetInvoiceMailState() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());


        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Long id = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1).getId();
        billingService.setPaymentState(id, PaymentState.CREDIT_CARD_MISSING, null);
        billingService.markInvoiceAsSent(id);
        //then
        assertTrue(billingService.getInvoices(getInvoiceFilterWithNotSendInvoices()).isEmpty());
    }


    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp =
            "Invoice with id " +
            "498509 is not found")
    public void shouldFailIfInvoiceIsNotFound() throws Exception {
        //given
        //when
        billingService.getInvoice(498509);
    }

    @Test
    public void shouldBeAbleToGetInvoicesById() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        Invoice expected = get(billingService.getInvoices(getInvoicesFilterWithState(PaymentState.WAITING_EXECUTOR)), 1);
        Invoice actual = billingService.getInvoice(expected.getId());
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Not able to generate invoices. Result overlaps with existed invoices.")
    public void shouldFailToCalculateInvoicesTwiceWithOverlappingPeriod() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 10:00:00").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));

        billingService.addSubscription("ac-3", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-5", 0, sdf.parse("01-01-2015 00:00:00").getTime(), sdf.parse("01-02-2015 00:00:00").getTime());

        //when
        billingService.generateInvoices(sdf.parse("01-01-2015 00:00:00").getTime(),
                                        sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.generateInvoices(sdf.parse("09-01-2015 00:00:00").getTime(),
                                        sdf.parse("15-02-2015 00:00:00").getTime());
    }

    @Test
    public void shouldBeAbleToAddPrepaidTime() throws Exception {
        //when
        billingService.addSubscription("ac-1", 34.34,
                                       sdf.parse("01-01-2015 00:00:00").getTime(),
                                       sdf.parse("01-02-2015 00:00:00").getTime());
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Unable to add new prepaid time since it overlapping with existed period")
    public void shouldNoBeAbleToAddPrepaidTimeForIntersectionPeriod() throws Exception {
        //when
        billingService.addSubscription("ac-1", 34.34,
                                       sdf.parse("01-01-2015 00:00:00").getTime(),
                                       sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.addSubscription("ac-1", 34.34,
                                       sdf.parse("15-01-2015 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime());
    }

    @Test
    public void shouldBeAbleToClosePrepaidPeriod() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("01-01-2015 00:00:00").getTime(),
                                                                      sdf.parse("30-01-2015 00:00:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-1",
                                                                      "ws-7",
                                                                      "run-1254"));
        billingService.addSubscription("ac-1",
                                       100,
                                       sdf.parse("01-01-2015 00:00:00").getTime(),
                                       sdf.parse("01-02-2015 00:00:00").getTime());
        billingService.removeSubscription("ac-1", sdf.parse("15-01-2015 00:00:00").getTime());
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-1")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 45.16129);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 45.16129);
        assertEquals(saasCharge.getPaidAmount(), 640.83871);
    }

    @Test
    public void shouldBeAbleToAddNewPrepaidTimeAfterClosingOldPrepaidPeriod() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("01-01-2015 00:00:00").getTime(),
                                                                      sdf.parse("30-01-2015 00:00:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-1",
                                                                      "ws-7",
                                                                      "run-1254"));
        billingService.addSubscription("ac-1",
                                       100,
                                       sdf.parse("01-01-2015 00:00:00").getTime(),
                                       sdf.parse("01-02-2015 00:00:00").getTime());

        billingService.removeSubscription("ac-1", sdf.parse("15-01-2015 00:00:00").getTime());

        billingService.addSubscription("ac-1",
                                       100,
                                       sdf.parse("15-01-2015 00:00:00").getTime(),
                                       sdf.parse("01-02-2015 00:00:00").getTime());
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-1")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 100.0);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 100.0);
        assertEquals(saasCharge.getPaidAmount(), 586.0);
    }

    @Test
    public void shouldNotGenerateInvoiceWithoutPrepaid() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        assertTrue(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")).isEmpty());
    }

    @Test
    public void shouldBeAbleToAddPrepaidTimeForInvoicePrepaidAddFromTheMiddleOfTheMonth() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-01-2015 00:00:00").getTime(),
                                       sdf.parse("15-05-2015 00:00:00").getTime());


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 54.83871);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 54.83871);
        assertEquals(saasCharge.getPaidAmount(), 219.16129);
    }

    @Test
    public void shouldBeAbleToAddPrepaidTimeForInvoicePrepaidAddTillTheMiddleOfTheMonth() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-01-2015 00:00:00").getTime());


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 45.16129);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 45.16129);
        assertEquals(saasCharge.getPaidAmount(), 228.83871);
    }

    @Test
    public void shouldBeAbleToAddPrepaidTimeForInvoicePrepaidAddForTheFullMonth() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-05-2015 00:00:00").getTime());


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 100.0);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 100.0);
        assertEquals(saasCharge.getPaidAmount(), 174.0);
    }

    @Test
    public void shouldBeAbleToAddPrepaidTimeForInvoiceFromTwoClosePeriods() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-01-2015 00:00:00").getTime() - 1);
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-01-2015 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime());


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 100.0);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 100.0);
        assertEquals(saasCharge.getPaidAmount(), 174.0);
    }

    @Test
    public void shouldBeAbleToAddPrepaidTimeForInvoiceFromTwoSeparatePeriods() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-01-2015 00:00:00").getTime() - 1);
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("20-01-2015 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime());


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        billingService.generateInvoices(period.getStartDate().getTime(), period.getEndDate().getTime());
        //then
        Invoice actual = get(billingService.getInvoices(getInvoiceFilterWithAccountId("ac-5")), 0);
        assertNotNull(actual);
        Charge saasCharge = get(actual.getCharges(), 0);
        assertNotNull(saasCharge);
        assertEquals(saasCharge.getProvidedFreeAmount(), 10.0);
        assertEquals(saasCharge.getProvidedPrepaidAmount(), 83.870968);
        assertEquals(saasCharge.getFreeAmount(), 10.0);
        assertEquals(saasCharge.getPrePaidAmount(), 83.870968);
        assertEquals(saasCharge.getPaidAmount(), 190.129032);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter is missing for states  PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateSuccessfulWithoutCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.PAID_SUCCESSFULLY, null);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter is missing for states  PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateSuccessfulWithEmptyCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.PAID_SUCCESSFULLY, "");
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter is missing for states  PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateFailWithoutCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.PAYMENT_FAIL, null);

    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter is missing for states  PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateFailWithEmptyCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.PAYMENT_FAIL, "");
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter should be null for states different when PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateNotRequiredWithCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.NOT_REQUIRED, "CC");
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Credit card parameter should be null for states different when PAYMENT_FAIL or PAID_SUCCESSFULLY")
    public void shouldNotAllowToSetPaymentStateCCMissingWithCC() throws Exception {
        //given
        billingService.setPaymentState(1, PaymentState.CREDIT_CARD_MISSING, "CC");
    }

    @Test
    public void shouldGetEstimationByAccountWithAllDatesBetweenPeriod() throws Exception {
        //given
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:01:00").getTime(),
                                                                      "usr-123453",
                                                                      "ac-348798",
                                                                      "ws-235675423",
                                                                      "run-2344567"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        //when
        List<AccountResources> usage = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(sdf.parse("10-01-2014 09:00:00").getTime())
                                                           .withTillDate(sdf.parse("10-01-2014 14:00:00").getTime())
                                                           .withAccountId("ac-46534")
                                                           .build());

        //then
        assertEquals(usage.size(), 1);
        AccountResources resources = get(usage, 0);
        assertEquals(resources.getFreeAmount(), 0.384533);
    }

    @Test
    public void shouldGetEstimateByAccountWithDatesBetweenPeriod() throws Exception {
        //given
        //when
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:01:00").getTime(),
                                                                      "usr-123453",
                                                                      "ac-348798",
                                                                      "ws-235675423",
                                                                      "run-2344567"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2013 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2013 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 09:55:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2015 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2015 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        //when
        List<AccountResources> usage = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(sdf.parse("10-01-2014 10:00:00").getTime())
                                                           .withTillDate(sdf.parse("10-01-2014 12:10:00").getTime())
                                                           .withAccountId("ac-46534")
                                                           .build());

        //then
        assertEquals(usage.size(), 1);
        AccountResources resources = get(usage, 0);
        assertEquals(resources.getFreeAmount(), 0.217867);
    }

    @Test
    public void shouldBeAbleToEstimateUsageWithFreePrepaidAndPaidTime() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-01-2015 00:00:00").getTime() - 1);
        billingService.addSubscription("ac-5", 100,
                                       sdf.parse("20-01-2015 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime());

        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        List<AccountResources> usage = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(period.getStartDate().getTime())
                                                           .withTillDate(period.getEndDate().getTime())
                                                           .withAccountId("ac-5")
                                                           .withFreeGbHMoreThan(4.12)
                                                           .withPrePaidGbHMoreThan(15.0)
                                                           .withPaidGbHMoreThan(100.0)
                                                           .withMaxItems(1)
                                                           .withSkipCount(0)
                                                           .build());

        //then
        assertEquals(usage.size(), 1);
        AccountResources resources = get(usage, 0);
        assertEquals(resources.getFreeAmount(), 10.0);
        assertEquals(resources.getPrePaidAmount(), 83.870968);
        assertEquals(resources.getPaidAmount(), 190.129032);
    }

    @Test
    public void shouldBeAbleToLimitAndSkipEstimateUsageWithFreePrepaidAndPaidTime() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("21-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("01-01-2015 08:23:00").getTime(),
                                     sdf.parse("02-01-2015 18:00:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        billingService.addSubscription("ac-6", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-01-2015 00:00:00").getTime() - 1);


        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));
        List<AccountResources> usage = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(period.getStartDate().getTime())
                                                           .withTillDate(period.getEndDate().getTime())
                                                           .withMaxItems(1)
                                                           .withSkipCount(1)
                                                           .build());

        //then
        assertEquals(usage.size(), 1);
        AccountResources resources = get(usage, 0);
        assertEquals(resources.getAccountId(), "ac-6");
        assertEquals(resources.getFreeAmount(), 10.0);
        assertEquals(resources.getPrePaidAmount(), 45.161290);
        assertEquals(resources.getPaidAmount(), 228.838710);
    }

    @Test
    public void shouldNoReturnPaidGbHForCommunityAccount() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1256"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1252"));

        billingService.addSubscription("ac-5", 10,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime() - 1);

        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        Resources usage = billingService
                .getEstimatedUsage(period.getStartDate().getTime(), period.getEndDate().getTime());
        List<AccountResources> usageAccount = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(period.getStartDate().getTime())
                                                           .withTillDate(period.getEndDate().getTime())
                                                           .build());

        //then
        assertEquals(usage.getFreeAmount(), 20.0);
        assertEquals(usage.getPrePaidAmount(), 10.0);
        assertEquals(usage.getPaidAmount(), 8.0);
        assertEquals(usageAccount.size(), 2);
        AccountResources ac5 = get(usageAccount, 0);
        AccountResources ac6 = get(usageAccount, 1);

        assertEquals(ac5.getFreeAmount(), 10.0);
        assertEquals(ac5.getPrePaidAmount(), 10.0);
        assertEquals(ac5.getPaidAmount(), 8.0);

        assertEquals(ac6.getFreeAmount(), 10.0);
        assertEquals(ac6.getPrePaidAmount(), 0.0);
        assertEquals(ac6.getPaidAmount(), 0.0);
    }

    @Test
    public void shouldBeAbleToGetEstimatedUsage() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("12-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1256"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-7",
                                     "ws-7",
                                     "run-1252"));

        billingService.addSubscription("ac-5", 0,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime() - 1);
        billingService.addSubscription("ac-6", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime() - 1);

        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        Resources usage = billingService
                .getEstimatedUsage(period.getStartDate().getTime(), period.getEndDate().getTime());
        List<AccountResources> usageAccount = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(period.getStartDate().getTime())
                                                           .withTillDate(period.getEndDate().getTime())
                                                           .build());

        //then
        assertEquals(usage.getFreeAmount(), 30.0);
        assertEquals(usage.getPrePaidAmount(), 58.0);
        assertEquals(usage.getPaidAmount(), 18.0);
        assertEquals(usageAccount.size(), 3);
        AccountResources ac5 = get(usageAccount, 0);
        AccountResources ac6 = get(usageAccount, 1);
        AccountResources ac7 = get(usageAccount, 2);

        assertEquals(ac5.getFreeAmount(), 10.0);
        assertEquals(ac5.getPrePaidAmount(), 0.0);
        assertEquals(ac5.getPaidAmount(), 18.0);

        assertEquals(ac6.getFreeAmount(), 10.0);
        assertEquals(ac6.getPrePaidAmount(), 58.0);
        assertEquals(ac6.getPaidAmount(), 0.0);

        assertEquals(ac7.getFreeAmount(), 10.0);
        assertEquals(ac7.getPrePaidAmount(), 0.0);
        assertEquals(ac7.getPaidAmount(), 0.0);
    }

    @Test
    public void shouldBeAbleToGetEstimatedUsageWithPrepaid() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("12-01-2015 21:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1256"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 08:23:00").getTime(),
                                     sdf.parse("11-01-2015 12:23:00").getTime(),
                                     "usr-123",
                                     "ac-7",
                                     "ws-7",
                                     "run-1252"));

        billingService.addSubscription("ac-5", 0,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime() - 1);
        billingService.addSubscription("ac-6", 100,
                                       sdf.parse("15-12-2014 00:00:00").getTime(),
                                       sdf.parse("15-02-2015 00:00:00").getTime() - 1);

        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        Resources usage = billingService
                .getEstimatedUsage(period.getStartDate().getTime(), period.getEndDate().getTime());
        List<AccountResources> usageAccount = billingService
                .getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                           .withFromDate(period.getStartDate().getTime())
                                                           .withTillDate(period.getEndDate().getTime())
                                                           .build());

        //then
        assertEquals(usage.getFreeAmount(), 30.0);
        assertEquals(usage.getPrePaidAmount(), 58.0);
        assertEquals(usage.getPaidAmount(), 18.0);
        assertEquals(usageAccount.size(), 3);
        AccountResources ac5 = get(usageAccount, 0);
        AccountResources ac6 = get(usageAccount, 1);
        AccountResources ac7 = get(usageAccount, 2);

        assertEquals(ac5.getFreeAmount(), 10.0);
        assertEquals(ac5.getPrePaidAmount(), 0.0);
        assertEquals(ac5.getPaidAmount(), 18.0);

        assertEquals(ac6.getFreeAmount(), 10.0);
        assertEquals(ac6.getPrePaidAmount(), 58.0);
        assertEquals(ac6.getPaidAmount(), 0.0);

        assertEquals(ac7.getFreeAmount(), 10.0);
        assertEquals(ac7.getPrePaidAmount(), 0.0);
        assertEquals(ac7.getPaidAmount(), 0.0);
    }

    @Test
    public void shouldBeAbleToGetEstimatedUsageWithBonuses() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2015 01:00:00").getTime(),
                                                                      sdf.parse("12-01-2015 21:00:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-6",
                                                                      "ws-7",
                                                                      "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2015 08:23:00").getTime(),
                                                                      sdf.parse("11-01-2015 12:23:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-5",
                                                                      "ws-7",
                                                                      "run-1256"));

        bonusDao.create(new Bonus().withAccountId("ac-5")
                                   .withFromDate(sdf.parse("15-12-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("15-12-2015 00:00:00").getTime())
                                   .withResources(20D)
                                   .withCause("Bonus"));

        bonusDao.create(new Bonus().withAccountId("ac-7")
                                   .withFromDate(sdf.parse("15-12-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("15-12-2015 00:00:00").getTime())
                                   .withResources(10D)
                                   .withCause("Bonus"));

        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        Resources usage = billingService.getEstimatedUsage(period.getStartDate().getTime(), period.getEndDate().getTime());
        List<AccountResources> usageAccount =
                billingService.getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                                         .withFromDate(period.getStartDate().getTime())
                                                                         .withTillDate(period.getEndDate().getTime())
                                                                         .build());

        //then
        assertEquals(usage.getFreeAmount(), 38.0);
        assertEquals(usage.getPrePaidAmount(), 0.0);
        assertEquals(usage.getPaidAmount(), 0.0);
        assertEquals(usageAccount.size(), 2);
        AccountResources ac5 = get(usageAccount, 0);
        AccountResources ac6 = get(usageAccount, 1);

        assertEquals(ac5.getFreeAmount(), 28.0);
        assertEquals(ac5.getPrePaidAmount(), 0.0);
        assertEquals(ac5.getPaidAmount(), 0.0);

        assertEquals(ac6.getFreeAmount(), 10.0);
        assertEquals(ac6.getPrePaidAmount(), 0.0);
        assertEquals(ac6.getPaidAmount(), 0.0);
    }

    @Test
    public void testCheckingAvailableResourcesWhenAccountDoNotUseFullFreeAmount() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("10-01-2015 05:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        assertTrue(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(), period.getEndDate().getTime()));
    }

    @Test
    public void testCheckingAvailableResourcesWhenAccountUseFullFreeAmount() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("10-01-2015 12:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        assertFalse(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(), period.getEndDate().getTime()));
    }

    @Test
    public void testCheckingAvailableResourcesWhenAccountUseFullFreeAmountButHasBonus() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 00:00:00").getTime(),
                                     sdf.parse("10-01-2015 12:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));
        bonusDao.create(new Bonus().withAccountId("ac-6")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-02-2015 00:00:00").getTime())
                                   .withResources(3D));
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        assertTrue(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(), period.getEndDate().getTime()));
    }


    @Test
    public void testCheckingAvailableResourcesWhenAccountUseFullFreeAmountAndHasSubscription() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("10-01-2015 12:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        billingService.addSubscription("ac-6", 0, period.getStartDate().getTime(), period.getEndDate().getTime());

        assertTrue(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(), period.getEndDate().getTime()));
    }

    @Test
    public void testCheckingAvailableResourcesWhenAccountDidNotUseResources() throws Exception {
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        assertTrue(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(), period.getEndDate().getTime()));
    }

    @Test
    public void testCheckingAvailableResourcesWhenAccountUseFullFreeAmountAndHasInactiveClosePrepaidPeriod() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 01:00:00").getTime(),
                                     sdf.parse("10-01-2015 12:00:00").getTime(),
                                     "usr-123",
                                     "ac-6",
                                     "ws-7",
                                     "run-1254"));
        billingService.addSubscription("ac-6", 0, sdf.parse("10-01-2015 01:00:00").getTime(),
                                       sdf.parse("10-01-2015 12:00:00").getTime());
        billingService.removeSubscription("ac-6", sdf.parse("10-01-2015 02:00:00").getTime());
        //when
        Period period = metricPeriod.get(sdf.parse("01-01-2015 00:00:00"));

        assertFalse(billingService.hasAvailableResources("ac-6", period.getStartDate().getTime(),
                                                         sdf.parse("10-01-2015 12:00:00").getTime()));
    }

    @Test
    public void shouldBeAbleToGetProvidedFreeResources() throws Exception {
        //when
        double providedResources = billingService.getProvidedFreeResources("account-5", sdf.parse("10-01-2015 01:00:00").getTime(),
                                                                           sdf.parse("10-01-2015 12:00:00").getTime());
        //then
        assertEquals(providedResources, 10D);
    }

    @Test
    public void shouldSumBonusesToProvidedFreeAmount() throws Exception {
        //given
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("01-06-2014 00:00:00").getTime())
                                   .withTillDate(sdf.parse("01-06-2015 00:00:00").getTime())
                                   .withResources(2D));
        bonusDao.create(new Bonus().withAccountId("ac-1")
                                   .withCause("Bonus")
                                   .withFromDate(sdf.parse("30-01-2015 00:00:00").getTime())
                                   .withTillDate(sdf.parse("30-05-2015 00:00:00").getTime())
                                   .withResources(1D));
        //when
        double providedResources = billingService.getProvidedFreeResources("ac-1", sdf.parse("01-01-2015 00:00:00").getTime(),
                                                                           sdf.parse("31-01-2015 00:00:00").getTime());
        //then
        assertEquals(providedResources, 13D);
    }

    private InvoiceFilter getInvoiceFilterWithAccountId(String accountId) throws ServerException {
        return InvoiceFilter.builder()
                            .withAccountId(accountId)
                            .build();
    }

    private InvoiceFilter getInvoicesFilterWithState(PaymentState state) throws ServerException {
        return InvoiceFilter.builder()
                            .withPaymentStates(state)
                            .build();
    }

    private InvoiceFilter getInvoiceFilterWithNotSendInvoices() throws ServerException {
        return InvoiceFilter.builder()
                            .withIsMailNotSend()
                            .withPaymentStates(PaymentState.NOT_REQUIRED,
                                               PaymentState.PAYMENT_FAIL,
                                               PaymentState.PAID_SUCCESSFULLY,
                                               PaymentState.CREDIT_CARD_MISSING)
                            .build();
    }
}
