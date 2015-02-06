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
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.shared.dto.Charge;
import com.codenvy.api.account.shared.dto.MemoryChargeDetails;
import com.codenvy.api.account.shared.dto.Receipt;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;


public class SqlBillingServiceTest extends AbstractSQLTest{



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
        billingService.generateReceipts(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Receipt> ac3 = billingService.getReceipts("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Receipt receipt = get(ac3, 0);
        assertEquals(receipt.getTotal(), 30.9);
        //free + paid
        assertEquals(receipt.getCharges().size(), 2);
        assertTrue(receipt.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                               .withAmount(10.0)
                                                               .withPrice(0.0)
                                                               .withServiceId("Saas")
                                                               .withType("Free")));

        assertTrue(receipt.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(206.0)
                                                           .withPrice(0.15)
                                                           .withServiceId("Saas")
                                                           .withType("Paid")));

        assertEquals(receipt.getMemoryChargeDetails().size(), 1);
        assertTrue(receipt.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
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
        billingService.generateReceipts(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Receipt> ac3 = billingService.getReceipts("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Receipt receipt = get(ac3, 0);
        assertEquals(receipt.getTotal(), 0.0);
        //free + paid
        assertEquals(receipt.getCharges().size(), 1);
        assertTrue(receipt.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(0.52306)
                                                           .withPrice(0.0)
                                                           .withServiceId("Saas")
                                                           .withType("Free")));

        assertEquals(receipt.getMemoryChargeDetails().size(), 1);
        assertTrue(receipt.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
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
        billingService.generateReceipts(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Receipt> ac3 = billingService.getReceipts("ac-3");
        //then
        assertEquals(ac3.size(), 1);
        Receipt receipt = get(ac3, 0);
        assertEquals(receipt.getTotal(), 0.0);
        //free + paid
        assertEquals(receipt.getCharges().size(), 1);
        assertTrue(receipt.getCharges().contains(DtoFactory.getInstance().createDto(Charge.class)
                                                           .withAmount(1.04612)
                                                           .withPrice(0.0)
                                                           .withServiceId("Saas")
                                                           .withType("Free")));

        assertEquals(receipt.getMemoryChargeDetails().size(), 2);
        assertTrue(receipt.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                                                       .withAmount(0.52306)
                                                                       .withWorkspaceId("ws-235423")));
        assertTrue(receipt.getMemoryChargeDetails().contains(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
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
        billingService.generateReceipts(Long.MIN_VALUE, Long.MAX_VALUE);
        List<Receipt> ac3 = billingService.getReceipts("ac-3");
        List<Receipt> ac5 = billingService.getReceipts("ac-5");
        List<Receipt> ac1 = billingService.getReceipts("ac-1");
        //then
        assertEquals(ac3.size(), 1);
        assertEquals(get(ac3, 0).getTotal(), 30.9);


        assertEquals(ac5.size(), 1);
        assertEquals(get(ac5, 0).getTotal(), 2.1);

        assertEquals(ac1.size(), 1);
        assertEquals(get(ac1, 0).getTotal(), 0.0);







    }
}