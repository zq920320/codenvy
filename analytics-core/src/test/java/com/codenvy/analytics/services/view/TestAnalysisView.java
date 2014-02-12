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
package com.codenvy.analytics.services.view;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

/** @author Dmytro Nochevnov */
public class TestAnalysisView extends BaseTest {

    private static final String ANALYSIS_VIEW_CONFIGURATION = BASE_DIR + "/classes/views/analysis.xml";
     
    private ViewBuilder viewBuilder;    

    private static final String DATE1 = "2013-11-01";
    private static final String DATE2 = "2013-12-02";
    
    private static final List<String> monthLabels = Arrays.asList(new String[]{"", "Jan 2014", "Dec 2013",
           "Nov 2013", "Oct 2013", "Sep 2013", "Aug 2013", "Jul 2013", "Jun 2013", "May 2013", "Apr 2013",
           "Mar 2013", "Feb 2013", "Jan 2013", "Dec 2012"});
    
    private static final List<String> metricLabels = Arrays.asList(new String[]{"", "Total Users",
            "Total Number of Users We Track", "Created Projects", "& Built", "& Run", "& Deployed to PAAS",
            "Sent Invites", "Shell Launched", "Have Complete Profile"});
    
    
    @BeforeMethod
    public void prepare() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.USER.put(context, Parameters.USER_TYPES.REGISTERED.toString());

        Parameters.LOG.put(context, prepareLog().getAbsolutePath());
       
        Parameters.FROM_DATE.put(context, DATE1.replace("-", ""));
        Parameters.TO_DATE.put(context, DATE1.replace("-", ""));
        extractDataFromLog(context);

        Parameters.FROM_DATE.put(context, DATE2.replace("-", ""));
        Parameters.TO_DATE.put(context, DATE2.replace("-", ""));
        extractDataFromLog(context);
        
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
    public void testSpecificDayPeriod() throws Exception {
        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Map> context = ArgumentCaptor.forClass(Map.class);

        Map<String, String> executionContext = Utils.newContext();
        Parameters.TO_DATE.put(executionContext, "20140101");

        viewBuilder.computeDisplayData(executionContext);
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        ViewData actualData = viewData.getAllValues().get(0);
        assertEquals(actualData.size(), 1);
        
        SectionData analysisReport = actualData.get("analysis_month");
        assertEquals(analysisReport.size(), 10);

        // test metrics
        for (int i = 0; i < analysisReport.size(); i++) {
            String metricLabel = analysisReport.get(i).get(0).getAsString();
            assertEquals(metricLabel, metricLabels.get(i));
        } 
        
        // test headers
        List<ValueData> headers = analysisReport.get(0);
        assertEquals(headers.size(), 15);
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).getAsString();
            assertEquals(header, monthLabels.get(i));
        }        

        // test data on Oct 2013
        Map<String, String> oct2013Data = getColumn(4, analysisReport, metricLabels, true);
        assertEquals(oct2013Data.get("Total Users"), "20");
        assertEquals(oct2013Data.get("Total Number of Users We Track"), "");
        assertEquals(oct2013Data.get("Created Projects"), "");
        assertEquals(oct2013Data.get("& Built"), "");
        assertEquals(oct2013Data.get("& Run"), "");
        assertEquals(oct2013Data.get("& Deployed to PAAS"), "");
        assertEquals(oct2013Data.get("Sent Invites"), "");
        assertEquals(oct2013Data.get("Shell Launched"), "");
        assertEquals(oct2013Data.get("Have Complete Profile"), "");
        
        // test data on Nov 2013
        Map<String, String> nov2013Data = getColumn(3, analysisReport, metricLabels, true);
        assertEquals(nov2013Data.get("Total Users"), "22");
        assertEquals(nov2013Data.get("Total Number of Users We Track"), "2");
        assertEquals(nov2013Data.get("Created Projects"), "2");
        assertEquals(nov2013Data.get("& Built"), "2");
        assertEquals(nov2013Data.get("& Run"), "2");
        assertEquals(nov2013Data.get("& Deployed to PAAS"), "1");
        assertEquals(nov2013Data.get("Sent Invites"), "1");
        assertEquals(nov2013Data.get("Shell Launched"), "1");
//         assertEquals(nov2013Data.get("Have Complete Profile"), "1");  // TODO there is a bug in analytics which returns ""
        
        // test data on Dec 2013
        Map<String, String> dec2013Data = getColumn(2, analysisReport, metricLabels, true);
        assertEquals(dec2013Data.get("Total Users"), "23");
        assertEquals(dec2013Data.get("Total Number of Users We Track"), "3");
        assertEquals(dec2013Data.get("Created Projects"), "3");
        assertEquals(dec2013Data.get("& Built"), "3");
        assertEquals(dec2013Data.get("& Run"), "3");
        assertEquals(dec2013Data.get("& Deployed to PAAS"), "3");
        assertEquals(dec2013Data.get("Sent Invites"), "2");
        assertEquals(dec2013Data.get("Shell Launched"), "2");
//         assertEquals(dec2013Data.get("Have Complete Profile"), "2");  // TODO there is a bug in analytics which returns ""
        
        // test data on Jan 2014
        Map<String, String> jan2014Data = getColumn(1, analysisReport, metricLabels, true);
        assertEquals(jan2014Data.get("Total Users"), "23");
        assertEquals(jan2014Data.get("Total Number of Users We Track"), "3");
        assertEquals(jan2014Data.get("Created Projects"), "3");
        assertEquals(jan2014Data.get("& Built"), "3");
        assertEquals(jan2014Data.get("& Run"), "3");
        assertEquals(jan2014Data.get("& Deployed to PAAS"), "3");
        assertEquals(jan2014Data.get("Sent Invites"), "2");
        assertEquals(jan2014Data.get("Shell Launched"), "2");
//         assertEquals(jan2014Data.get("Have Complete Profile"), "2");  // TODO there is a bug in analytics which returns ""
    }

    /**
     * Returns column from section table of view
     */
    private Map<String, String> getColumn(int columnIndex, SectionData report, List<String> rowLabels, boolean passFirstRow) {
        LinkedHashMap<String, String> columnData = new LinkedHashMap<>();
        assertEquals(report.size(), rowLabels.size());
 
        int i = (passFirstRow) ? 1 : 0;   // don't taking into account first row which consists of column labels
        for (; i < rowLabels.size(); i++) {
            columnData.put(rowLabels.get(i), report.get(i).get(columnIndex).getAsString());
        }
        
        return columnData;
    }

    private File prepareLog() throws IOException {
        List<Event> events = new ArrayList<>();

        /** events at the 2013-11-01 */
        // create users
        events.add(Event.Builder.createUserCreatedEvent("user1-id", "user1")
                        .withDate(DATE1).build());
        events.add(Event.Builder.createUserCreatedEvent("user2-id", "user2")
                   .withDate(DATE1).build());

        // update users' profiles
        events.add(Event.Builder.createUserUpdateProfile("user1", "f1", "l1", "company1", "phone1", "jobtitle1")
                        .withDate(DATE1).build());
        events.add(Event.Builder.createUserUpdateProfile("user2", "", "", "", "", "")
                        .withDate(DATE1).build());

        // active users [user1, user2]
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withTime("09:00:00").withDate(DATE1)
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withTime("09:00:00").withDate(DATE1)
                        .build());

        // add shell launched events
        events.add(Event.Builder.createShellLaunchedEvent("user2", "ws2", "2").withTime("09:00:00").withDate(DATE1)
                   .build());
        events.add(Event.Builder.createShellLaunchedEvent("user2", "ws2", "2").withTime("10:00:00").withDate(DATE1)
                   .build());
        
        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate(DATE1)
                     .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate(DATE1)
                     .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate(DATE1)
                     .withTime("10:03:00").build());

        // projects deployed to LOCAL
        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws2", "", "project1", "type1", "LOCAL")
                        .withTime("10:10:00,000")
                        .withDate(DATE1).build());

        // projects deployed to PaaS
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "", "project1", "type1", "paas1")
                        .withTime("10:10:00,000")
                        .withDate(DATE1).build());        
        
        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withTime("10:06:00")
                        .withDate(DATE1).build());


        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1", "", "", "", "", "", "")
                        .withDate(DATE1)
                        .withTime("20:03:00").build());
       
        events.add(Event.Builder.createDebugStartedEvent("user2", "ws1", "", "", "id1")
                        .withDate(DATE1)
                        .withTime("20:06:00").build());        

        // invite users
        events.add(Event.Builder.createUserInviteEvent("user1", "ws1", "email1")
                        .withDate(DATE1).build());

        events.add(Event.Builder.createRunStartedEvent("user2", "ws2", "project", "type", "id1")
                         .withDate(DATE1)
                         .withTime("20:59:00").build());        
        events.add(Event.Builder.createRunFinishedEvent("user2", "ws2", "project", "type", "id1")
                         .withDate(DATE1)
                         .withTime("21:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user1", "ws1", "project", "type", "id2")
                         .withDate(DATE1)
                         .withTime("21:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user1", "ws1", "project", "type", "id2")
                         .withDate(DATE1)
                         .withTime("21:14:00").build());

        
        /** events at the 2013-12-02 */
        // create user
        events.add(Event.Builder.createUserCreatedEvent("user3-id", "user3")
                   .withDate(DATE2).build());   

        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withTime("09:00:00").withDate(DATE2)
                   .build());

        // update user's profile
        events.add(Event.Builder.createUserUpdateProfile("user3", "f3", "l3", "company3", "phone3", "jobtitle3")
                   .withDate(DATE2).build());
        
        // add shell launched events
        events.add(Event.Builder.createShellLaunchedEvent("user3", "ws3", "3").withTime("09:00:00").withDate(DATE2)
                   .build());
        events.add(Event.Builder.createShellLaunchedEvent("user2", "ws2", "2").withTime("10:00:00").withDate(DATE2)
                   .build());        
        
        // projects created
        events.add(
                   Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project22", "type1").withDate(DATE2)
                        .withTime("10:03:00").build());
        events.add(
                   Event.Builder.createProjectCreatedEvent("user3", "ws3", "", "project33", "type1").withDate(DATE2)
                        .withTime("10:03:00").build());

        // projects deployed to LOCAL
        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws2", "", "project1", "type1", "LOCAL")
                        .withTime("10:10:00,000")
                        .withDate(DATE2).build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws3", "", "project33", "type1", "LOCAL")
                        .withTime("10:10:00,000")
                        .withDate(DATE2).build());
        
        
        // projects deployed to PaaS
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                        .withTime("10:10:00,000")
                        .withDate(DATE2).build());
        
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                        .withTime("10:00:00")
                        .withDate(DATE2).build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project2", "type1", "paas1")
                        .withTime("10:11:00,100")
                        .withDate(DATE2).build());


        // invite users
        events.add(Event.Builder.createUserInviteEvent("user1", "ws1", "email2")
                   .withDate(DATE2).build());
        events.add(Event.Builder.createUserInviteEvent("user3", "ws3", "email3")
                   .withDate(DATE2).build());
        
        return LogGenerator.generateLog(events);
    }    

    private void extractDataFromLog(Map<String, String> context) throws IOException, ParseException {
        /** create collections "created_users", "removed_users" to calculate "total_users" metric */
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.toString());
        Parameters.EVENT.put(context, "user-created");
        Parameters.STORAGE_TABLE.put(context, "created_users");
        pigServer.execute(ScriptType.EVENTS, context);

        Parameters.EVENT.put(context, "user-removed");
        Parameters.STORAGE_TABLE.put(context, "removed_users");
        pigServer.execute(ScriptType.EVENTS, context);

        /** create collections "active_users_set" to calculate "active_users" metric */
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.toString());
        Parameters.EVENT.put(context, "*");
        Parameters.PARAM.put(context, "user");
        Parameters.STORAGE_TABLE.put(context, "active_users_set");
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, context);

        /** create collections "created_project" to calculate "users_who_created_project" metric */
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "project-created");
        Parameters.STORAGE_TABLE.put(context, "created_projects");
        pigServer.execute(ScriptType.EVENTS, context);        

        /** create collections "builds" to calculate "users_who_built" metric */
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "project-built,project-deployed,application-created");
        Parameters.STORAGE_TABLE.put(context, "builds");
        pigServer.execute(ScriptType.EVENTS, context);  
        
        /** create collections "deploys" to calculate "users_who_deployed" metric */        
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "project-deployed,application-created");
        Parameters.STORAGE_TABLE.put(context, "deploys");
        pigServer.execute(ScriptType.EVENTS, context);          
        
        /** create collections "deploys_to_paas" to calculate "users_who_deployed_to_paas" metric */        
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "application-created");
        Parameters.STORAGE_TABLE.put(context, "deploys_to_paas");
        pigServer.execute(ScriptType.EVENTS, context);     

        /** create collections "user_invite" to calculate "users_who_invited" metric */
        // Parameters.USER.put(context, Parameters.USER_TYPES.REGISTERED.toString());  // TODO
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "user-invite");
        Parameters.STORAGE_TABLE.put(context, "user_invite");
        pigServer.execute(ScriptType.EVENTS, context);         

        /** create collections "shell_launched" to calculate "users_who_launched_shell" metric */        
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.toString());
        Parameters.EVENT.put(context, "shell-launched");
        Parameters.STORAGE_TABLE.put(context, "shell_launched");
        pigServer.execute(ScriptType.EVENTS, context);           
        
        /** create collections "users_profiles_list" to calculate "completed_profiles" metric */
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.toString());        
        Parameters.STORAGE_TABLE.put(context, "users_profiles_list");
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, context);
    }
}
