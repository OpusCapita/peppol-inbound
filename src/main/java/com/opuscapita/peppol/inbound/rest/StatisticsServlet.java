package com.opuscapita.peppol.inbound.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.statistics.api.StatisticsGranularity;
import no.difi.oxalis.statistics.inbound.StatisticsProducer;
import org.joda.time.DateTime;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * There are only 3 parameters, start, end and granularity - like this:
 * {@literal /statistics?start=2019-05-01T00&end=2019-06-01T00&granularity=M}
 * <p>
 * The start/end are dates are ISO formatted like : yyyy-mm-ddThh
 * The granularity can be H (hour), D (day), M (month) and Y (year)
 */
@Singleton
public class StatisticsServlet extends HttpServlet {

    @Inject
    private StatisticsProducer statisticsProducer;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getOutputStream().write("Hello!\nStatistics endpoint does not support http POST".getBytes());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Params params = parseParams(request.getParameterMap());

        ServletOutputStream servletOutputStream = response.getOutputStream();
        statisticsProducer.emitData(servletOutputStream, params.start, params.end, params.granularity);
        servletOutputStream.close();
    }

    private Params parseParams(Map<String, String[]> parameterMap) {
        Params result = new Params();
        parseGranularity(parameterMap, result);
        parseDates(parameterMap, result);
        return result;
    }

    private void parseDates(Map<String, String[]> parameterMap, Params result) {
        result.start = parseDate(getParamFromMultiValues(parameterMap, "start"));
        result.end = parseDate(getParamFromMultiValues(parameterMap, "end"));
    }

    private void parseGranularity(Map<String, String[]> parameterMap, Params result) {
        String granularity = getParamFromMultiValues(parameterMap, "g");
        if (granularity == null) {
            granularity = getParamFromMultiValues(parameterMap, "granularity");
        }
        if (granularity == null) {
            throw new IllegalArgumentException("Missing request parameter: 'granularity' (Y,M,D or H)");
        } else {
            result.granularity = StatisticsGranularity.valueForAbbreviation(granularity);
        }
    }

    private String getParamFromMultiValues(Map<String, String[]> parameterMap, String key) {
        String[] values = parameterMap.get(key);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    private Date parseDate(String dateAsString) {
        if (dateAsString != null) {
            try {
                DateTime date = DateTime.parse(dateAsString);
                return date.toDate();
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Unable to parseMultipart '%s'", dateAsString));
            }
        }
        return null;
    }

    static class Params {
        Date start;
        Date end;
        StatisticsGranularity granularity;
    }
}
