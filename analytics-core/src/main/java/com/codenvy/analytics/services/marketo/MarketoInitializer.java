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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.view.CSVFileHolder;
import com.marketo.mktows.ArrayOfString;
import com.marketo.mktows.AuthenticationHeader;
import com.marketo.mktows.ImportToListModeEnum;
import com.marketo.mktows.ImportToListStatusEnum;
import com.marketo.mktows.MktMktowsApiService;
import com.marketo.mktows.MktowsPort;
import com.marketo.mktows.ParamsGetImportToListStatus;
import com.marketo.mktows.ParamsImportToList;
import com.marketo.mktows.SuccessGetImportToListStatus;
import com.marketo.mktows.SuccessImportToList;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class MarketoInitializer extends Feature {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final   String SOAP_END_POINT = "analytics.marketo.soap_end_point";
    private static final   String USER_ID        = "analytics.marketo.user_id";
    private static final   String SECRET_KEY     = "analytics.marketo.secret_key";
    private static final   String PROGRAM_NAME   = "analytics.marketo.program_name";
    protected static final String PAGE_SIZE      = "analytics.marketo.page_size";
    private static final   String LIST_NAME      = "analytics.marketo.list_name";
    private static final   String SERVICE_URL    = "analytics.marketo.service_url";
    private static final   String SERVICE_NAME   = "analytics.marketo.service_name";

    private String soapEndPoint;
    private String userId;
    private String secretKey;
    private String programName;
    private String serviceUrl;
    private String serviceName;
    private String listName;
    private int    pageSize;

    protected final Configurator           configurator;
    private final   MarketoReportGenerator reportGenerator;
    private final   CSVFileHolder          reportHolder;

    @Inject
    public MarketoInitializer(Configurator configurator,
                              MarketoReportGenerator reportGenerator,
                              CSVFileHolder cleaner) {
        this.configurator = configurator;
        this.reportGenerator = reportGenerator;
        this.reportHolder = cleaner;

        if (isAvailable()) {
            validateConfiguration();

            this.soapEndPoint = configurator.getString(SOAP_END_POINT);
            this.userId = configurator.getString(USER_ID);
            this.secretKey = configurator.getString(SECRET_KEY);
            this.programName = configurator.getString(PROGRAM_NAME);
            this.serviceUrl = configurator.getString(SERVICE_URL);
            this.serviceName = configurator.getString(SERVICE_NAME);
            this.listName = configurator.getString(LIST_NAME);
            this.pageSize = configurator.getInt(PAGE_SIZE, 1000);
        }
    }

    private void validateConfiguration() throws IllegalStateException {
        checkProperty(SOAP_END_POINT);
        checkProperty(USER_ID);
        checkProperty(SECRET_KEY);
        checkProperty(PROGRAM_NAME);
        checkProperty(SERVICE_URL);
        checkProperty(SERVICE_NAME);
        checkProperty(LIST_NAME);
    }


    private void checkProperty(String property) throws IllegalStateException {
        if (!configurator.exists(property)) {
            throw new IllegalStateException("The property  " + property + " does not exist in configuration.");
        }
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    protected boolean processActiveUsersOnly() {
        return false;
    }

    protected boolean cleanListBeforeImport() {
        return true;
    }

    protected Context prepareContext(Context context) {
        Context.Builder builder = new Context.Builder(context.getAll());
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);

        return builder.build();
    }

    protected Context prepareActiveUsersContext(Context context) {
        return context;
    }

    @Override
    protected void doExecute(Context context) throws IOException, ParseException {
        LOG.info(this.getClass().getName() + " is started");
        long start = System.currentTimeMillis();

        File report = reportHolder.createNewFile();

        try {
            reportGenerator.prepareReport(report,
                                          prepareContext(context),
                                          prepareActiveUsersContext(context),
                                          processActiveUsersOnly());
            importToListByPage(report);
        } catch (NoSuchAlgorithmException | InvalidKeyException | JAXBException | InterruptedException e) {
            throw new IOException("Can not import data in Marketo.", e);
        } finally {
            if (report != null) {
                report.delete();
            }

            LOG.info(this.getClass().getName() + " is finished in " + (System.currentTimeMillis() - start) / 1000 +
                     " sec.");
        }
    }

    private MktowsPort getPort() throws MalformedURLException {
        MktMktowsApiService service = new MktMktowsApiService(new URL(soapEndPoint),
                                                              new QName(serviceUrl, serviceName));
        return service.getMktowsApiSoapPort();
    }


    private void importToListByPage(File csv)
            throws IOException, JAXBException, InterruptedException, InvalidKeyException, NoSuchAlgorithmException {
        String reportHeader = createHeader(csv);

        ArrayOfString rows = new ArrayOfString();

        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            reader.readLine(); // skip header

            long currentPage = 0;
            long rowsImported = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");  // remove all '"'
                rows.getStringItems().add(line);

                if (rows.getStringItems().size() == pageSize) {
                    ++currentPage;
                    importData(reportHeader, rows, cleanListBeforeImport() && currentPage == 1);
                    LOG.info("Rows imported : " + (rowsImported += rows.getStringItems().size()));
                    rows = new ArrayOfString();
                }
            }

            if (rows.getStringItems().size() > 0) {
                ++currentPage;
                importData(reportHeader, rows, cleanListBeforeImport() && currentPage == 1);
                LOG.info("Rows imported : " + (rowsImported += rows.getStringItems().size()));
            }
        } finally {
            csv.delete();
        }
    }

    private void importData(String reportHeader,
                            ArrayOfString rows,
                            boolean isCleanList)
            throws NoSuchAlgorithmException,
                   InvalidKeyException,
                   IOException,
                   JAXBException,
                   InterruptedException {
        MktowsPort port = getPort();
        AuthenticationHeader header = createAuthenticationHeader();

        ImportToListStatusEnum importStatus = importToList(reportHeader, rows, port, header, isCleanList);

        if (importStatus == ImportToListStatusEnum.CANCELED
            || importStatus == ImportToListStatusEnum.FAILED) {
            throw new IOException("Can not import data in Marketo. Import status is " + importStatus);
        }

        if (importStatus == ImportToListStatusEnum.PROCESSING) {
            importStatus = waitUntilProcessing(port, header);
        }

        if (importStatus != ImportToListStatusEnum.COMPLETE) {
            throw new IOException("Can not import data in Marketo. Import status is " + importStatus);
        }
    }

    private ImportToListStatusEnum importToList(String reportHeader,
                                                ArrayOfString rows,
                                                MktowsPort port,
                                                AuthenticationHeader header,
                                                boolean isCleanList)
            throws NoSuchAlgorithmException,
                   InvalidKeyException,
                   IOException, JAXBException {
        ParamsImportToList request = new ParamsImportToList();

        request.setProgramName(programName);
        request.setListName(listName);
        request.setImportListMode(ImportToListModeEnum.UPSERTLEADS);
        request.setClearList(isCleanList);

        request.setImportFileHeader(reportHeader);
        request.setImportFileRows(rows);

        SuccessImportToList result = port.importToList(request, header);

        return ImportToListStatusEnum.valueOf(result.getResult().getImportStatus().value());
    }

    private String createHeader(File csv) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            return reader.readLine(); // read header
        }
    }

    private ImportToListStatusEnum waitUntilProcessing(MktowsPort port, AuthenticationHeader header)
            throws JAXBException, InterruptedException {
        ImportToListStatusEnum importStatus;
        do {
            Thread.sleep(5 * 1000);
            ParamsGetImportToListStatus request = new ParamsGetImportToListStatus();
            request.setProgramName(programName);
            request.setListName(listName);

            SuccessGetImportToListStatus result = port.getImportToListStatus(request, header);
            importStatus = ImportToListStatusEnum.valueOf(result.getResult().getStatus().value());
            LOG.info("SuccessGetImportToListStatus = " + result.getResult().getStatus().value());
        }
        while (importStatus == ImportToListStatusEnum.PROCESSING);

        return importStatus;
    }

    private AuthenticationHeader createAuthenticationHeader()
            throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException {
        // Create Signature
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String text = df.format(new Date());
        String requestTimestamp = text.substring(0, 22) + ":" + text.substring(22);
        String encryptString = requestTimestamp + userId;

        SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(encryptString.getBytes());
        char[] hexChars = Hex.encodeHex(rawHmac);
        String signature = new String(hexChars);

        // Set Authentication Header
        AuthenticationHeader header = new AuthenticationHeader();
        header.setMktowsUserId(userId);
        header.setRequestTimestamp(requestTimestamp);
        header.setRequestSignature(signature);

        return header;
    }
}
