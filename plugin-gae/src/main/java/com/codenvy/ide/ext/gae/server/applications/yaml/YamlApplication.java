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
package com.codenvy.ide.ext.gae.server.applications.yaml;

import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlConfig;
import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlException;
import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlReader;
import com.google.appengine.tools.admin.AppAdminFactory;
import com.google.appengine.tools.admin.Application;
import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.ResourceLimits;
import com.google.appengine.tools.admin.UpdateListener;
import com.google.apphosting.utils.config.AppEngineConfigException;
import com.google.apphosting.utils.config.BackendsXml;
import com.google.apphosting.utils.config.BackendsYamlReader;
import com.google.apphosting.utils.config.CronXml;
import com.google.apphosting.utils.config.CronYamlReader;
import com.google.apphosting.utils.config.DispatchXml;
import com.google.apphosting.utils.config.DosXml;
import com.google.apphosting.utils.config.DosYamlReader;
import com.google.apphosting.utils.config.IndexYamlReader.IndexYaml;
import com.google.apphosting.utils.config.IndexesXml;
import com.google.apphosting.utils.config.QueueXml;
import com.google.apphosting.utils.config.QueueYamlReader;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * The abstract representation of GAE application that contains yaml file.
 *
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 */
public abstract class YamlApplication implements GenericApplication {
    private final File                 applicationDirectory;
    private final String               applicationDirectoryPath;
    private final String               sourceLanguage;
    private final Map<String, Pattern> staticFilesPatterns;
    private final YamlAppInfo          appInfo;
    private final CronXml              cronXml;
    private final QueueXml             queueXml;
    private final DosXml               dosXml;
    private final IndexesXml           indexesXml;
    private final BackendsXml          backendsXml;
    private       String               appInfoString;

    public YamlApplication(@NotNull File applicationDirectory, @NotNull String sourceLanguage) {
        this.applicationDirectory = applicationDirectory;
        applicationDirectoryPath = applicationDirectory.getAbsolutePath();

        this.sourceLanguage = sourceLanguage;

        staticFilesPatterns = new HashMap<>();

        appInfo = readAppYaml(getPath() + "/app.yaml");

        CronYamlReader cronReader = new CronYamlReader(applicationDirectoryPath);
        cronXml = cronReader.parse();

        QueueYamlReader queueYamlReader = new QueueYamlReader(applicationDirectoryPath);
        queueXml = queueYamlReader.parse();

        DosYamlReader dosYamlReader = new DosYamlReader(applicationDirectoryPath);
        dosXml = dosYamlReader.parse();

        indexesXml = readIndexYaml(getPath() + "/index.yaml");

        BackendsYamlReader backendsYaml = new BackendsYamlReader(applicationDirectoryPath);
        backendsXml = backendsYaml.parse();
    }

    /**
     * Transform index YAML file configuration to abstract Java object format.
     *
     * @param path
     *         path where YAML file is located
     * @return an instance with YAML configuration
     */
    @Nullable
    public static IndexesXml readIndexYaml(@NotNull String path) {
        Path indexFilePath = Paths.get(path);
        if (!Files.exists(indexFilePath)) {
            return null;
        }

        Reader fileReader = null;
        try {
            fileReader = new FileReader(indexFilePath.toFile());
            YamlReader reader = new YamlReader(fileReader);

            YamlConfig readerConfig = reader.getConfig();
            readerConfig.setPropertyElementType(IndexYaml.class, "indexes", IndexYaml.Index.class);
            readerConfig.setPropertyElementType(IndexYaml.Index.class, "properties", IndexYaml.Property.class);

            IndexYaml indexYaml = reader.read(IndexYaml.class);

            if (indexYaml == null || indexYaml.getIndexes() == null || indexYaml.getIndexes().isEmpty()) {
                // No index configured but file exists.
                // It looks like legal for python sdk but java sdk fails for the same situation.
                // Return null instead of empty index if index is not configured at all.
                return null;
            }

            return indexYaml.toXml(null);
        } catch (YamlException | IOException e) {
            throw new AppEngineConfigException(e.getMessage(), e);
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException ignored) {
                // do nothing
            }
        }
    }

    /**
     * Transform app YAML file configuration to abstract Java object format.
     *
     * @param path
     *         path where YAML file is located
     * @return an instance with YAML configuration
     */
    @NotNull
    public static YamlAppInfo readAppYaml(@NotNull String path) {
        Reader fileReader = null;
        try {
            fileReader = new FileReader(path);
            YamlAppInfo appInfo = YamlAppInfo.parse(fileReader);

            List<Map<String, String>> yamlErrorHandlers = appInfo.error_handlers;
            if (yamlErrorHandlers == null) {
                return appInfo;
            }

            for (Map<String, String> yamlErrorHandler : yamlErrorHandlers) {
                if (yamlErrorHandler.get("mime_type") == null) {
                    yamlErrorHandler.put("mime_type", Application.guessContentTypeFromName(yamlErrorHandler.get("file")));
                }

                if (yamlErrorHandler.get("error_code") == null) {
                    yamlErrorHandler.put("error_code", "default");
                }
            }

            return appInfo;
        } catch (IOException e) {
            throw new AppEngineConfigException(e.getMessage(), e);
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException ignored) {
                // do nothing
            }
        }
    }

    /** {@inheritDoc } */
    @Override
    public String getAppId() {
        return appInfo.application;
    }

    /** {@inheritDoc } */
    @Override
    public String getVersion() {
        return appInfo.version;
    }

    /** {@inheritDoc } */
    @Override
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /** {@inheritDoc } */
    @Override
    public String getModule() {
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public boolean isPrecompilationEnabled() {
        return false;
    }

    /** {@inheritDoc } */
    @Override
    public List<ErrorHandler> getErrorHandlers() {
        List<ErrorHandler> errorHandlers = new ArrayList<>();
        List<Map<String, String>> yamlErrorHandlers = appInfo.error_handlers;

        if (yamlErrorHandlers != null) {
            for (Map<String, String> yamlErrorHandler : yamlErrorHandlers) {
                ErrorHandler appErrorHandler = new ErrorHandlerImpl(yamlErrorHandler.get("file"),
                                                                    yamlErrorHandler.get("error_code"),
                                                                    yamlErrorHandler.get("mime_type"));
                errorHandlers.add(appErrorHandler);
            }
        }

        return errorHandlers;
    }

    /** {@inheritDoc } */
    @Override
    public String getMimeTypeIfStatic(String path) {
        for (Map<String, String> handler : appInfo.handlers) {
            String staticDir = handler.get("static_dir");
            String regex = staticDir == null ? handler.get("upload") : staticDir + ".*";

            if (regex == null) {
                continue;
            }

            Pattern pattern = staticFilesPatterns.get(regex);
            if (pattern == null) {
                pattern = Pattern.compile(regex);
                staticFilesPatterns.put(regex, pattern);
            }

            if (pattern.matcher(path).matches()) {
                String mimeType = handler.get("mime_type");
                if (mimeType == null) {
                    mimeType = Application.guessContentTypeFromName(path);
                }

                return mimeType;
            }
        }

        return null;
    }

    /** {@inheritDoc } */
    @Override
    public CronXml getCronXml() {
        return cronXml;
    }

    /** {@inheritDoc } */
    @Override
    public QueueXml getQueueXml() {
        return queueXml;
    }

    /** {@inheritDoc } */
    @Override
    public DispatchXml getDispatchXml() {
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public DosXml getDosXml() {
        return dosXml;
    }

    /** {@inheritDoc } */
    @Override
    public String getPagespeedYaml() {
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public IndexesXml getIndexesXml() {
        return indexesXml;
    }

    /** {@inheritDoc } */
    @Override
    public BackendsXml getBackendsXml() {
        return backendsXml;
    }

    /** {@inheritDoc } */
    @Override
    public String getApiVersion() {
        return appInfo.api_version;
    }

    /** {@inheritDoc } */
    @Override
    public String getPath() {
        return applicationDirectoryPath;
    }

    /** {@inheritDoc } */
    @Override
    public File getStagingDir() {
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public void resetProgress() {
        // do nothing
    }

    /** {@inheritDoc } */
    @Override
    public File createStagingDirectory(AppAdminFactory.ApplicationProcessingOptions applicationProcessingOptions,
                                       ResourceLimits resourceLimits)
            throws IOException {
        // Do not create staging directory.
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public void cleanStagingDirectory() {
        // Delete original application directory. Always get fresh copy of sources from IDE.
        deleteRecursive(applicationDirectory);
    }

    /** {@inheritDoc } */
    @Override
    public void setListener(UpdateListener updateListener) {
        // do nothing
    }

    /** {@inheritDoc } */
    @Override
    public void setDetailsWriter(PrintWriter printWriter) {
        // do nothing
    }

    /** {@inheritDoc } */
    @Override
    public void statusUpdate(String s, int i) {
        // do nothing
    }

    /** {@inheritDoc } */
    @Override
    public void statusUpdate(String s) {
        // do nothing
    }

    /** {@inheritDoc } */
    @Override
    public String getAppYaml() {
        if (appInfoString == null) {
            appInfoString = appInfo.toYaml();
        }

        return appInfoString;
    }

    /** {@inheritDoc } */
    @Override
    public String getInstanceClass() {
        // Not implement yet. Don't use. Always return null
        return null;
    }

    /**
     * The dummy implementation of error handler. It's provide the information about error handler.
     */
    public static class ErrorHandlerImpl implements ErrorHandler {
        private final String file;
        private final String errorCode;
        private final String mimeType;

        public ErrorHandlerImpl(@NotNull String file, @NotNull String errorCode, @NotNull String mimeType) {
            this.file = file;
            this.errorCode = errorCode;
            this.mimeType = mimeType;
        }

        /** {@inheritDoc } */
        @Override
        public String getFile() {
            return file;
        }

        /** {@inheritDoc } */
        @Override
        public String getErrorCode() {
            return errorCode;
        }

        /** {@inheritDoc } */
        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

}