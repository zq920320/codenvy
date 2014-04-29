/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.api.core.util.Pair;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class DummyHTTPMetricTransport implements  MetricTransport {
    @Override
    public <DTO> DTO getResource(Class<DTO> dtoInterface, String method, String path) throws IOException {

        return (DTO)dtoInterface.getInterfaces();
    }

    @Override
    public <DTO> List<DTO> getResources(Class<DTO> dtoInterface, String method, String path) throws IOException {
        List<DTO> list = new ArrayList<>();
        list.add((DTO)dtoInterface.getInterfaces());

        return list;
    }

    @Override
    public <DTO> DTO getResource(Class<DTO> dtoInterface, String method, String path, Object body, Pair... parameters)
            throws IOException {
        return (DTO)dtoInterface.getInterfaces();
    }

    @Override
    public <DTO> List<DTO> getResources(Class<DTO> dtoInterface, String method, String path, Object body,
                                        Pair... parameters) throws IOException {
        List<DTO> list = new ArrayList<>();
        list.add((DTO)dtoInterface.getInterfaces());

        return list;
    }
}
