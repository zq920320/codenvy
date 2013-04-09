/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeCalculatedMetric extends AbstractMetric {

    private final MetricType addedType;
    private final MetricType removedType;

    CumulativeCalculatedMetric(MetricType metricType, MetricType addedType, MetricType removedType) {
        super(metricType);
        this.addedType = addedType;
        this.removedType = removedType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        Set<ScriptParameters> params = addedType.getInstance().getMandatoryParams();
        params.addAll(removedType.getInstance().getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        Set<ScriptParameters> params = addedType.getInstance().getAdditionalParams();
        params.addAll(removedType.getInstance().getAdditionalParams());
        params.removeAll(getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object queryValue(Map<String, String> context) throws IOException {
        long addedEntities = (Long)addedType.getInstance().getValue(context);
        long removedEntities = (Long)removedType.getInstance().getValue(context);

        try {
            shiftDateInterval(context);
        } catch (ParseException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }

        long previousEntities = (Long)metricType.getInstance().getValue(context);

        return new Long(previousEntities + addedEntities - removedEntities);
    }

    /**
     * Shift date interval forward depending on {@link TimeUnit}. Should also be placed in context.
     */
    public void shiftDateInterval(Map<String, String> context) throws IOException, ParseException {
        String fromDateParam = context.get(ScriptParameters.FROM_DATE.getName());
        String toDateParam = context.get(ScriptParameters.TO_DATE.getName());

        if (fromDateParam == null || toDateParam == null) {
            throw new IOException("Parameters " + ScriptParameters.FROM_DATE + " or " + ScriptParameters.TO_DATE
                                  + " were not found in context");
        }

        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();

        toDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(toDateParam));
        fromDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(fromDateParam));

        TimeUnit timeUnit = TimeUnit.valueOf(context.get(ScriptParameters.TIME_UNIT.getName()));

        switch (timeUnit) {
            case DAY:
                shiftByDay(fromDate, toDate, context);
                break;
            case WEEK:
                shiftByWeek(fromDate, toDate, context);
                break;
            case MONTH:
                shiftByMonth(fromDate, toDate, context);
                break;
            default:
                throw new IllegalArgumentException("Illegal time unit " + timeUnit);
        }
    }

    private void shiftByMonth(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1);

        toDate.add(Calendar.MONTH, 1);

        context.put(ScriptParameters.FROM_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(fromDate.getTime()));
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(toDate.getTime()));
    }

    private void shiftByWeek(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate.add(Calendar.DAY_OF_MONTH, 7);
        toDate.add(Calendar.DAY_OF_MONTH, 7);

        context.put(ScriptParameters.FROM_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(fromDate.getTime()));
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(toDate.getTime()));
    }

    private void shiftByDay(Calendar fromDate, Calendar toDate, Map<String, String> context) throws IOException {
        toDate.add(Calendar.DAY_OF_MONTH, 1);

        context.put(ScriptParameters.FROM_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(toDate.getTime()));
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(toDate.getTime()));
    }

}
