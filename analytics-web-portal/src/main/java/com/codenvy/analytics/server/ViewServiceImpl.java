package com.codenvy.analytics.server;

import com.codenvy.analytics.client.ViewService;
import com.codenvy.analytics.metrics.TimeIntervalUtil;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.server.view.TimeLineView;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class ViewServiceImpl extends RemoteServiceServlet implements
                                                         ViewService {

    /**
     * {@inheritDoc}
     */
    public List<List<String>> getTimeLineView(Date date) throws IOException {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            Map<String, String> context = new HashMap<String, String>();
            context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());
            TimeIntervalUtil.initDateInterval(cal, context);
            TimeIntervalUtil.prevDateInterval(context);

            TimeLineView view = new TimeLineView(context);

            return view.getRows();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
