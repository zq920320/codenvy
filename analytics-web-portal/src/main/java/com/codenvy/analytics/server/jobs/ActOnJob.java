/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;

import org.apache.commons.net.ftp.FTPSClient;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActOnJob implements Job {

    /** The value of {@value #ANALYTICS_ACTON_FTP_PROPERTIES_PARAM} runtime parameter. */
    private static final String ACTON_FTP_PROPERTIES  =
                                                        System.getProperty("analytics.acton.ftp.properties");
    private static final String ACTON_LAYOUT_RESOURCE = "acton-file-layout.properties";
    private static final Logger LOGGER                = LoggerFactory.getLogger(ActOnJob.class);

    /**
     * @return initialized job
     */
    public static JobDetail createJob() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(ActOnJob.class.getName()));
        jobDetail.setJobClass(ActOnJob.class);

        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("ActOnJob is started");

        long start = System.currentTimeMillis();

        try {
            File file = prepareFile();
            send(file);

            LOGGER.info("File " + file.getName() + " was transfered successfully");

            if (!file.delete()) {
                LOGGER.warn("File " + file.getName() + " can not be removed");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        } finally {
            LOGGER.info("ActOnJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void send(File file) throws SocketException, IOException {
        Properties ftpProps = readFTPProperties();

        FTPSClient ftp = new FTPSClient(false);

        ftp.connect(ftpProps.getProperty("server"), Integer.valueOf(ftpProps.getProperty("port")));
        try {
            ftp.login(ftpProps.getProperty("login"), ftpProps.getProperty("password"));

            ftp.execPBSZ(0);
            ftp.execPROT("P");
            ftp.enterLocalPassiveMode();

            transferFile(file, ftp);

            ftp.logout();
        } finally {
            ftp.disconnect();
        }
    }

    private void transferFile(File file, FTPSClient ftp) throws FileNotFoundException, IOException {
        InputStream in = new FileInputStream(file);
        try {
            if (!ftp.storeFile(file.getName(), in)) {
                throw new IOException("File " + file.getName() + " was not transfered to the server");
            }
        } finally {
            in.close();
        }
    }

    private Properties readFTPProperties() throws FileNotFoundException, IOException {
        Properties ftpProps = new Properties();
        InputStream in = new BufferedInputStream(new FileInputStream(ACTON_FTP_PROPERTIES));
        try {
            ftpProps.load(in);
        } finally {
            in.close();
        }

        return ftpProps;
    }

    private File prepareFile() throws IOException {
        Map<String, String> context = Utils.initilizeContext(TimeUnit.DAY, new Date());

        Set<StringValueData> activeUsers = getActiveUsers(context);
        LinkedHashMap<String, String> metrics = readMetric();
        
        File file = new File(System.getProperty("java.io.tmpdir"), Utils.getToDateParam(context) + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            writeTitle(out, metrics);
            for (StringValueData userVD : activeUsers) {
                writeEmail(out, userVD);

                for (String metricName : metrics.values()) {
                    out.write(",");
                    writeMetricValue(out, userVD, metricName, context);
                }

                out.newLine();
            }
        } finally {
            out.close();
        }

        return file;
    }

    private void writeMetricValue(BufferedWriter out, StringValueData userVD, String metricName, Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(metricName);

        context = Utils.newContext(context);
        context.put(Metric.USER_FILTER_PARAM, userVD.getAsString());

        String value = metric.getValue(context).getAsString();
        out.write(value);
    }

    private void writeEmail(BufferedWriter out, StringValueData userVD) throws IOException {
        out.write(userVD.getAsString());
    }

    private void writeTitle(BufferedWriter out, LinkedHashMap<String, String> metrics) throws IOException {
        out.write("email");
        for (String key : metrics.keySet()) {
            out.write(",");
            out.write(key);
        }
        out.newLine();
    }

    private LinkedHashMap<String, String> readMetric() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ACTON_LAYOUT_RESOURCE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] entry = line.split("=");
                result.put(entry[0], entry[1]);
            }

            return result;
        } finally {
            in.close();
        }
    }

    private Set<StringValueData> getActiveUsers(Map<String, String> context) throws IOException {
        Metric activeUsersSetMetric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);
        SetStringValueData valueData = (SetStringValueData)activeUsersSetMetric.getValue(context);

        return valueData.getAll();
    }
}
