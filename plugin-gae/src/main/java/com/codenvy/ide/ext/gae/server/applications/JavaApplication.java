/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server.applications;

import com.google.appengine.tools.admin.AppAdminFactory;
import com.google.appengine.tools.admin.Application;
import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.ResourceLimits;
import com.google.appengine.tools.admin.UpdateListener;
import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.BackendsXml;
import com.google.apphosting.utils.config.CronXml;
import com.google.apphosting.utils.config.DispatchXml;
import com.google.apphosting.utils.config.DosXml;
import com.google.apphosting.utils.config.IndexesXml;
import com.google.apphosting.utils.config.QueueXml;
import com.google.apphosting.utils.config.WebXml;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;
import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.ZipUtils.unzip;

/**
 * Wrapper for com.google.appengine.tools.admin.Application to make possible cleanup temporary files.
 *
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Sergii Leschenko
 */
public class JavaApplication implements GenericApplication {
    private static final Logger LOG = LoggerFactory.getLogger(JavaApplication.class);

    private final Application delegate;

    @Inject
    public JavaApplication(@Assisted URL url) throws IOException {
        String path = getApplicationBinaries(url).getPath();

        this.delegate = Application.readApplication(path);
    }

    @Nonnull
    private File getApplicationBinaries(@Nonnull URL url) throws IOException {
        File tempFile = downloadFile(null, "ide-appengine", null, url);

        Path appPath = Paths.get(tempFile.getParentFile().getPath(), tempFile.getName() + "_dir");
        Path appDir = Files.createDirectories(appPath);

        File dir = appDir.toFile();

        unzip(tempFile, dir);
        Files.delete(tempFile.toPath());

        return dir;
    }

    /** {@inheritDoc } */
    @Override
    public String getAppId() {
        return delegate.getAppId();
    }

    /** {@inheritDoc } */
    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    /** {@inheritDoc } */
    @Override
    public String getSourceLanguage() {
        return "Java";
    }

    /** {@inheritDoc } */
    @Override
    public String getModule() {
        return delegate.getModule();
    }

    /** {@inheritDoc } */
    @Override
    public boolean isPrecompilationEnabled() {
        return delegate.isPrecompilationEnabled();
    }

    /** {@inheritDoc } */
    @Override
    public List<ErrorHandler> getErrorHandlers() {
        return delegate.getErrorHandlers();
    }

    /** {@inheritDoc } */
    @Override
    public String getMimeTypeIfStatic(String path) {
        return delegate.getMimeTypeIfStatic(path);
    }

    /** @return an instance of abstract representation of appengine web xml configuration file */
    public AppEngineWebXml getAppEngineWebXml() {
        return delegate.getAppEngineWebXml();
    }

    /** {@inheritDoc } */
    @Override
    public CronXml getCronXml() {
        return delegate.getCronXml();
    }

    /** {@inheritDoc } */
    @Override
    public QueueXml getQueueXml() {
        return delegate.getQueueXml();
    }

    /** {@inheritDoc } */
    @Override
    public DispatchXml getDispatchXml() {
        return delegate.getDispatchXml();
    }

    /** {@inheritDoc } */
    @Override
    public DosXml getDosXml() {
        return delegate.getDosXml();
    }

    /** {@inheritDoc } */
    @Override
    public String getPagespeedYaml() {
        return delegate.getPagespeedYaml();
    }

    /** {@inheritDoc } */
    @Override
    public IndexesXml getIndexesXml() {
        return delegate.getIndexesXml();
    }

    /** @return an instance of abstract representation of web xml configuration file */
    public WebXml getWebXml() {
        return delegate.getWebXml();
    }

    /** {@inheritDoc } */
    @Override
    public BackendsXml getBackendsXml() {
        return delegate.getBackendsXml();
    }

    /** {@inheritDoc } */
    @Override
    public String getApiVersion() {
        return delegate.getApiVersion();
    }

    /** {@inheritDoc } */
    @Override
    public String getPath() {
        return delegate.getPath();
    }

    /** {@inheritDoc } */
    @Override
    public File getStagingDir() {
        return delegate.getStagingDir();
    }

    /** {@inheritDoc } */
    @Override
    public void resetProgress() {
        delegate.resetProgress();
    }

    /** {@inheritDoc } */
    @Override
    public File createStagingDirectory(AppAdminFactory.ApplicationProcessingOptions opts, ResourceLimits resourceLimits)
            throws IOException {
        try {
            String java7Path = System.getProperty("appengine.java");

            String classpath = Joiner.on(":").join(((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs());

            ProcessBuilder processBuilder = new ProcessBuilder(java7Path,
                                                               "-Dappengine.sdk.root=" + System.getProperty("appengine.sdk.root"),
                                                               "-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"),
                                                               "-cp",
                                                               classpath,
                                                               StagingDirectoryCreator.class.getCanonicalName(),
                                                               delegate.getPath());
            Process process = processBuilder.start();
            process.waitFor();
            if (process.exitValue() != 0) {
                LOG.error("Fail of compiling gae application via java 7: {}", IoUtil.readStream(process.getErrorStream()));
                throw new IOException("Fail to create staging directory");
            }

            String processOutput = IoUtil.readStream(process.getInputStream());
            final String[] outputLines = processOutput.split("\\n");

            String stagDirectoryPath = new String(Base64.decodeBase64(outputLines[0]));
            File stagingDirectory = new File(stagDirectoryPath);

            updatePrivateField(Application.class, delegate, "stageDir", stagingDirectory);
            updatePrivateField(Application.class, delegate, "apiVersion", new String(Base64.decodeBase64(outputLines[1])));
            updatePrivateField(Application.class, delegate, "appYaml", new String(Base64.decodeBase64(outputLines[2])));

            return stagingDirectory;
        } catch (Exception e) {
            LOG.error("Fail of compiling gae application via java 7: " + e.getLocalizedMessage(), e);
            throw new IOException(e.getLocalizedMessage());
        }
    }

    private void updatePrivateField(Class destinationClass, Object destination, String fieldName, Object value) throws NoSuchFieldException,
                                                                                                                       IllegalAccessException {
        final Field field = destinationClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(destination, value);
    }

    /** {@inheritDoc } */
    @Override
    public void cleanStagingDirectory() {
        delegate.cleanStagingDirectory();
        deleteRecursive(new File(getPath()));
    }

    /** {@inheritDoc } */
    @Override
    public void setListener(UpdateListener listener) {
        delegate.setListener(listener);
    }

    /** {@inheritDoc } */
    @Override
    public void setDetailsWriter(PrintWriter detailsWriter) {
        delegate.setDetailsWriter(detailsWriter);
    }

    /** {@inheritDoc } */
    @Override
    public void statusUpdate(String message, int amount) {
        delegate.statusUpdate(message, amount);
    }

    /** {@inheritDoc } */
    @Override
    public void statusUpdate(String message) {
        delegate.statusUpdate(message);
    }

    /** {@inheritDoc } */
    @Override
    public String getAppYaml() {
        return delegate.getAppYaml();
    }

    /** {@inheritDoc } */
    @Override
    public String getInstanceClass() {
        // Not implement yet. Don't use. Always return null
        return null;
    }

}