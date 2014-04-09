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
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.workspaces.ActiveWorkspaces;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.DisplayConfiguration;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestExpandedMetric extends BaseTest {

    private static final String WS                  = "ws1";
    private static final String USER                = "user1@gmail.com";
    private static final String SESSION_ID          = "8AA06F22-3755-4BDD-9242-8A6371BAB53A";
    private static final String COLLECTION          = "users_activity_list";
    
    private ViewBuilder viewBuilder;
    
    private static final String ANALYSIS_VIEW_CONFIGURATION = BASE_DIR + "/classes/views/analysis.xml";
    
    @BeforeClass
    public void prepareDatabase() throws IOException, ParseException {
        List<Event> events = new ArrayList<>();

        // start main session
        events.add(
                Event.Builder.createSessionStartedEvent(USER, WS, "ide", SESSION_ID)
                             .withDate("2013-11-01").withTime("19:00:00,155").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(USER, "ws2", "project", "type", "id2")
                                .withDate("2013-11-01").withTime("19:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(USER, "ws2", "project", "type", "id2")
                                .withDate("2013-11-01").withTime("19:14:00").build());

        // event of another user in the another workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws3", "project2", "type", "id3")
                                .withDate("2013-11-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws3", "project2", "type", "id3")
                                .withDate("2013-11-01").withTime("19:10:00").build());

        // finish main session
        events.add(
                Event.Builder.createSessionFinishedEvent(USER, WS, "ide", SESSION_ID)
                             .withDate("2013-11-01").withTime("19:55:00,555").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());
    }
    
    @BeforeClass
    public void prepareViewBuilder() throws IOException {        
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);

        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, ANALYSIS_VIEW_CONFIGURATION);
            }
        });

        Configurator configurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{ANALYSIS_VIEW_CONFIGURATION}).when(configurator).getArray(anyString());

        viewBuilder = spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                          Injector.getInstance(CSVReportPersister.class),
                                          configurationManager,
                                          configurator));
    }

    @Test
    public void testExpandedAbstractActiveEntities() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, USER);
        
        AbstractActiveEntities metric = new ActiveWorkspaces();

        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 2);       
        
        // test expanded metric value
        ListValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = expandedValue.getAll();
        assertEquals(all.size(), 2);
        
        Map<String, ValueData> workspace1 = ((MapValueData) all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get("ws").toString(), "ws2");

        Map<String, ValueData> workspace2 = ((MapValueData) all.get(1)).getAll();
        assertEquals(workspace2.size(), 1);
        assertEquals(workspace2.get("ws").toString(), WS);
                
        // test expanded metric value pagination 
        builder.put(Parameters.PAGE, 2);
        builder.put(Parameters.PER_PAGE, 1);         
        
        expandedValue = metric.getExpandedValue(builder.build());
        assertEquals(expandedValue.size(), 1);
        
        all = expandedValue.getAll();
        assertEquals(all.size(), 1);
        
        Map<String, ValueData> workspace = ((MapValueData) all.get(0)).getAll();
        assertEquals(workspace.size(), 1);
        assertEquals(workspace.get("ws").toString(), WS);
    }
    
    @Test
    public void testConversionExpandedValueDataIntoViewData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, USER);
        
        AbstractActiveEntities metric = new ActiveWorkspaces();

        ListValueData expandedValue = metric.getExpandedValue(builder.build());
        
        // test view data builded on expanded metric value data
        ViewData viewData = viewBuilder.getViewData(expandedValue);
        assertEquals(viewData.size(), 1);
        
        SectionData sectionData = viewData.get(null);
        assertEquals(sectionData.size(), 3);
        
        List<ValueData> titleRow = sectionData.get(0);
        assertEquals(titleRow.size(), 1);
        assertEquals(titleRow.get(0).getAsString(), metric.getTrackedFields()[0]);

        List<ValueData> valueRow1 = sectionData.get(1);
        assertEquals(valueRow1.size(), 1);
        assertEquals(valueRow1.get(0).getAsString(), "ws2");
        
        List<ValueData> valueRow2 = sectionData.get(2);
        assertEquals(valueRow2.size(), 1);
        assertEquals(valueRow2.get(0).getAsString(), WS);
    }
}