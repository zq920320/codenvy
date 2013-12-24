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
package com.codenvy.analytics.services.configuration;

import com.codenvy.analytics.collection.DBCollectionsConfiguration;
import com.codenvy.analytics.collection.CompoundIndexConfiguration;
import com.codenvy.analytics.collection.IndexFieldConfiguration;
import com.codenvy.analytics.collection.CollectionConfiguration;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestCollectionConfiguration {

    // test content of collections configuration file
    private static final String RESOURCE = 
            "<collections>" +
    		"   <collection>" +
    		"      <name>users_profiles_list</name>" +
    		"      <compound-index>" +
    		"         <name>all-metrix</name>" +
    		"         <field>user_first_name</field>" +
    		"         <field>user_last_name</field>" +
    		"         <field>user_company</field>" +
    		"      </compound-index>" +
    		"   </collection>" +
    		"</collections>";
   
    @Test
    public void testParsingIndexConfiguration() throws Exception {
        XmlConfigurationManager<DBCollectionsConfiguration> spyService = spy(new XmlConfigurationManager<>(DBCollectionsConfiguration.class));

        doReturn(new ByteArrayInputStream(RESOURCE.getBytes("UTF-8"))).when(spyService).openResource(anyString());

        DBCollectionsConfiguration configuration = spyService.loadConfiguration(anyString());

        assertNotNull(configuration);
        assertEquals(1, configuration.getCollections().size());

        CollectionConfiguration indexerConfiguration = configuration.getCollections().get(0);
        assertEquals("users_profiles_list", indexerConfiguration.getName());

        List<CompoundIndexConfiguration> compoundIndexes = indexerConfiguration.getCompoundIndexes();
        assertEquals(1, compoundIndexes.size());
        
        CompoundIndexConfiguration compoundIndex = compoundIndexes.get(0);
        assertEquals("all-metrix", compoundIndex.getName());
        
        List<IndexFieldConfiguration> compoundIndexFields = compoundIndex.getFields();
        assertEquals(3, compoundIndexFields.size());
        assertEquals("user_first_name", compoundIndexFields.get(0).getField());
        assertEquals("user_last_name", compoundIndexFields.get(1).getField());
        assertEquals("user_company", compoundIndexFields.get(2).getField());
    }
}
