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

package com.codenvy.analytics.impl;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.util.MetricDTOFactory;
import com.codenvy.api.analytics.MetricHandler;
import com.codenvy.api.analytics.dto.MetricInfoDTO;
import com.codenvy.api.analytics.dto.MetricInfoListDTO;
import com.codenvy.api.analytics.dto.MetricValueDTO;
import com.codenvy.api.analytics.exception.MetricNotFoundException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Metric handler implementation base on data stored in files on file system. Which should be preliminary prepared by
 * calling appropriate scripts.
 *
 * @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a>
 */
@Singleton
public class FileBasedMetricHandler implements MetricHandler {

    @Override
    public MetricValueDTO getValue(String metricName,
                                   Map<String, String> executionContext,
                                   UriInfo uriInfo) throws MetricNotFoundException {
        MetricValueDTO metricValueDTO = DtoFactory.getInstance().createDto(MetricValueDTO.class);
        try {
            ValueData vd = getMetricValue(metricName, executionContext);
            metricValueDTO.setType(vd.getType());
            metricValueDTO.setValue(vd.getAsString());
        } catch (IOException e) {
            throw new IllegalStateException("Inappropriate metric state or metric context to evaluate metric ");
        } catch (IllegalArgumentException e) {
            throw new MetricNotFoundException();
        }
        return metricValueDTO;
    }

    @Override
    public MetricInfoDTO getInfo(String metricName, UriInfo uriInfo) throws MetricNotFoundException {
        try {
            Metric metric = MetricFactory.getMetric(metricName);
            return MetricDTOFactory.createMetricDTO(metric, metricName, uriInfo);
        } catch (IllegalArgumentException e) {
            throw new MetricNotFoundException();
        }
    }

    @Override
    public MetricInfoListDTO getAllInfo(UriInfo uriInfo) {
        List<MetricInfoDTO> metricInfoDTOs = new ArrayList<>();

        for (Metric metric : MetricFactory.getAllMetrics()) {
            metricInfoDTOs.add(MetricDTOFactory.createMetricDTO(metric, metric.getName(), uriInfo));
        }

        MetricInfoListDTO metricInfoListDTO = DtoFactory.getInstance().createDto(MetricInfoListDTO.class);
        metricInfoListDTO.setMetrics(metricInfoDTOs);
        return metricInfoListDTO;
    }

    protected ValueData getMetricValue(String metricName, Map<String, String> executionContext) throws IOException {
        if (executionContext.get(Parameters.FROM_DATE.name()) == null) {
            Parameters.FROM_DATE.putDefaultValue(executionContext);
        }

        if (executionContext.get(Parameters.TO_DATE.name()) == null) {
            Parameters.TO_DATE.putDefaultValue(executionContext);
        }

        return MetricFactory.getMetric(metricName).getValue(executionContext);
    }
}
