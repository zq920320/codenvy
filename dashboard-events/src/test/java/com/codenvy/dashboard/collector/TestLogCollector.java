/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.dashboard.collector;

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

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestLogCollector
{

    /**
     * Logger.
     */
    protected static final Logger LOG                        = LoggerFactory.getLogger(TestLogCollector.class);

    /**
     * The threads count to be launched.
     */
    private static final int      THREAD_COUNT               = 1000;

    /**
     * Time between events generation;
     */
    private static final int      EVENT_INTERVAL             = 50;                                             // 100 ms

    private static final int      MAX_EVENT_COUNT_PER_THREAD = 5000;

    private CountDownLatch        latch                      = new CountDownLatch(1);

    private AtomicLong            count                      = new AtomicLong(0);

    private Logger                log;

    @BeforeTest
    public void setUp() throws Exception
    {
        LoggerContext context = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        URL configURL = getClass().getClassLoader().getResource("logback-appenders.xml");
        configurator.doConfigure(configURL);

        log = context.getLogger(TestLogCollector.class);
    }

    /**
     * Test throughput.
     */
    @Test
    public void testThroughput() throws Exception
    {
        ThreadGroup group = new ThreadGroup("Writers");

        for (int i = 0; i < THREAD_COUNT; i++)
        {
            Thread thread = new Writer(group, "thread " + i);
            thread.start();
        }

        latch.countDown();

        while (group.activeCount() != 0)
        {
            Thread.sleep(5000);
            LOG.info("Has been wrote " + count.get() + " from " + (MAX_EVENT_COUNT_PER_THREAD * THREAD_COUNT)
                     + " events");
        }

        LOG.info("Has been wrote " + count.get() + " from " + (MAX_EVENT_COUNT_PER_THREAD * THREAD_COUNT)
                 + " events");
    }

    /**
     * Is responsible to writer messages to the syslog server.
     */
    private class Writer extends Thread
    {
        /**
         * Writer constructor.
         */
        Writer(ThreadGroup group, String name)
        {
            super(group, name);
        }

        /**
         * {@inheritDoc}
         */
        public void run()
        {
            try
            {
                latch.await();
            } catch (InterruptedException e1)
            {
                return;
            }

            for (int i = 0; i < MAX_EVENT_COUNT_PER_THREAD; i++)
            {
                try
                {
                    doWrite();
                } catch (InterruptedException e)
                {
                    break;
                }
            }
        }

        /**
         * @throws InterruptedException
         */
        private void doWrite() throws InterruptedException
        {
            log.info(UUID.randomUUID().toString());
            count.incrementAndGet();

            Thread.sleep(EVENT_INTERVAL);
        }
    }
}
