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
package com.codenvy.analytics.impl;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricNotFoundException;
import com.codenvy.analytics.metrics.MetricRestrictionException;

import org.eclipse.che.api.analytics.MetricHandler;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoListDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueListDTO;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.util.MetricDTOFactory.createMetricDTO;

/**
 * Metric handler implementation base on data stored in files on file system. Which should be preliminary prepared by
 * calling appropriate scripts.
 *
 * @author Dmitry Kuleshov
 * @author Anatoliy Bazko
 */
@Singleton
public class FileBasedMetricHandler implements MetricHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedMetricHandler.class);

    /** {@inheritDoc} */
    @Override
    public MetricValueDTO getValue(String metricName,
                                   Map<String, String> context,
                                   UriInfo uriInfo) throws MetricNotFoundException,
                                                           MetricRestrictionException {
        validateInternalMetric(metricName);
        return getMetricValueDTO(metricName, context);
    }

    /** {@inheritDoc} */
    @Override
    public MetricValueListDTO getListValues(String metricName,
                                            List<Map<String, String>> parameters,
                                            Map<String, String> context,
                                            UriInfo uriInfo) throws MetricNotFoundException, MetricRestrictionException {
        validateInternalMetric(metricName);

        MetricValueListDTO metricValueListDTO = DtoFactory.newDto(MetricValueListDTO.class);

        List<MetricValueDTO> metricValues = new ArrayList<>();
        metricValueListDTO.setMetrics(metricValues);

        if (parameters != null) {
            for (Map<String, String> parameter : parameters) {
                Map<String, String> mergedContext = merge(parameter, context);
                metricValues.add(getMetricValueDTO(metricName, mergedContext));
            }
        }

        return metricValueListDTO;
    }


    /** {@inheritDoc} */
    @Override
    public MetricValueDTO getValueByJson(String metricName,
                                         Map<String, String> parameters,
                                         Map<String, String> context,
                                         UriInfo uriInfo) throws MetricNotFoundException, MetricRestrictionException {
        validateInternalMetric(metricName);
        return parameters != null ? getMetricValueDTO(metricName, merge(parameters, context))
                                  : getMetricValueDTO(metricName, context);
    }

    /** {@inheritDoc} */
    public MetricValueDTO getPublicValue(String metricName,
                                         Map<String, String> context,
                                         UriInfo uriInfo) throws MetricNotFoundException, MetricRestrictionException {
        validateInternalMetric(metricName);
        return getMetricValueDTO(metricName, context);
    }

    /** {@inheritDoc} */
    @Override
    public MetricValueListDTO getUserValues(List<String> metricNames,
                                            Map<String, String> context,
                                            UriInfo uriInfo) throws MetricNotFoundException, MetricRestrictionException {
        MetricValueListDTO metricValueListDTO = DtoFactory.newDto(MetricValueListDTO.class);

        List<MetricValueDTO> metricValues = new ArrayList<>();
        metricValueListDTO.setMetrics(metricValues);

        for (String metricName : metricNames) {
            if (!isInternal(metricName)) {
                metricValues.add(getMetricValueDTO(metricName, context));
            }
        }

        return metricValueListDTO;
    }

    /** {@inheritDoc} */
    @Override
    public MetricInfoDTO getInfo(String metricName, UriInfo uriInfo) throws MetricNotFoundException {
        validateInternalMetric(metricName);

        Metric metric = MetricFactory.getMetric(metricName);
        return createMetricDTO(metric, metricName, uriInfo);
    }

    /** {@inheritDoc} */
    @Override
    public MetricInfoListDTO getAllInfo(UriInfo uriInfo) throws MetricNotFoundException, MetricRestrictionException {
        List<MetricInfoDTO> metricInfoDTOs = new ArrayList<>();

        for (Metric metric : MetricFactory.getAllMetrics()) {
            if (!isInternal(metric)) {
                metricInfoDTOs.add(createMetricDTO(metric, metric.getName(), uriInfo));
            }
        }

        MetricInfoListDTO metricInfoListDTO = DtoFactory.getInstance().createDto(MetricInfoListDTO.class);
        metricInfoListDTO.setMetrics(metricInfoDTOs);
        return metricInfoListDTO;
    }

    protected ValueData getMetricValue(String metricName, Context context) throws IOException {
        return MetricFactory.getMetric(metricName).getValue(context);
    }

    protected Map<String, String> merge(Map<String, String> parameters, Map<String, String> context) {
        Map<String, String> mergedContext = new HashMap<>(context);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey().toUpperCase();
            if (!mergedContext.containsKey(key)) {
                mergedContext.put(key, entry.getValue());
            }
        }
        return mergedContext;
    }

    /**
     * Indicates if metric internal. In this case it won't be available through API.
     */
    protected boolean isInternal(Metric metric) {
        return metric.getClass().isAnnotationPresent(InternalMetric.class);
    }

    protected boolean isInternal(String metricName) {
        return isInternal(MetricFactory.getMetric(metricName));
    }

    protected void validateInternalMetric(String metricName) throws MetricNotFoundException {
        Metric metric = MetricFactory.getMetric(metricName);
        if (isInternal(metric)) {
            throw new MetricNotFoundException(metricName);
        }
    }

    protected MetricValueDTO getMetricValueDTO(String metricName,
                                               Map<String, String> context) throws MetricNotFoundException,
                                                                                   MetricRestrictionException {
        try {
            ValueData vd = getMetricValue(metricName, Context.valueOf(context));
            MetricValueDTO metricValueDTO = DtoFactory.newDto(MetricValueDTO.class);
            metricValueDTO.setName(metricName);
            metricValueDTO.setType(vd.getType());
            metricValueDTO.setValue(vd.getAsString());
            return metricValueDTO;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException("Inappropriate metric state or metric context to evaluate metric ");
        }
    }
}
