/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.collector;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class TestLogCollector {

    /** Logger. */
    protected static final Logger LOG = LoggerFactory.getLogger(TestLogCollector.class);

    /** The threads count to be launched. */
    private static final int THREAD_COUNT = 1000;

    /** Time between events generation; */
    private static final int EVENT_INTERVAL = 50;                                             // 100 ms

    private static final int MAX_EVENT_COUNT_PER_THREAD = 5000;

    private CountDownLatch latch = new CountDownLatch(1);

    private AtomicLong count = new AtomicLong(0);

    private Logger log;

    @BeforeTest
    public void setUp() throws Exception {
        LoggerContext context = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        URL configURL = getClass().getClassLoader().getResource("logback-appenders.xml");
        configurator.doConfigure(configURL);

        log = context.getLogger(TestLogCollector.class);
    }

    /** Test throughput. */
    @Test
    public void testThroughput() throws Exception {
        ThreadGroup group = new ThreadGroup("Writers");

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Writer(group, "thread " + i);
            thread.start();
        }

        latch.countDown();

        while (group.activeCount() != 0) {
            Thread.sleep(5000);
            LOG.info("Has been wrote " + count.get() + " from " + (MAX_EVENT_COUNT_PER_THREAD * THREAD_COUNT)
                     + " events");
        }

        LOG.info("Has been wrote " + count.get() + " from " + (MAX_EVENT_COUNT_PER_THREAD * THREAD_COUNT)
                 + " events");
    }

    /** Is responsible to writer messages to the syslog server. */
    private class Writer extends Thread {
        /** Writer constructor. */
        Writer(ThreadGroup group, String name) {
            super(group, name);
        }

        /** {@inheritDoc} */
        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e1) {
                return;
            }

            for (int i = 0; i < MAX_EVENT_COUNT_PER_THREAD; i++) {
                try {
                    doWrite();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        /** @throws InterruptedException */
        private void doWrite() throws InterruptedException {
            log.info(UUID.randomUUID().toString());
            count.incrementAndGet();

            Thread.sleep(EVENT_INTERVAL);
        }
    }
}
