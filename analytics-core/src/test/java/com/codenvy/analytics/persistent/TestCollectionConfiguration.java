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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestCollectionConfiguration extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<collections>" +
                                                "   <collection name=\"users_profiles_list\">" +
                                                "      <indexes>" +
                                                "         <index name=\"index name\">" +
                                                "            <field>user_first_name</field>" +
                                                "            <field>user_last_name</field>" +
                                                "            <field>user_company</field>" +
                                                "         </index>" +
                                                "      </indexes>" +
                                                "   </collection>" +
                                                "</collections>";

    private XmlConfigurationManager<CollectionsConfiguration> configurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        configurationManager = new XmlConfigurationManager<>(CollectionsConfiguration.class, FILE);
    }

    @Test
    public void testParsingConfiguration() throws Exception {
        CollectionsConfiguration configuration = configurationManager.loadConfiguration();

        assertNotNull(configuration);
        assertEquals(1, configuration.getCollections().size());

        CollectionConfiguration collectionConfiguration = configuration.getCollections().get(0);
        assertEquals("users_profiles_list", collectionConfiguration.getName());

        IndexesConfiguration indexesConfiguration = collectionConfiguration.getIndexes();

        List<IndexConfiguration> indexes = indexesConfiguration.getIndexes();
        assertEquals(1, indexes.size());

        IndexConfiguration index = indexes.get(0);
        assertEquals("index name", index.getName());

        List<FieldConfiguration> fields = index.getFields();
        assertEquals(3, fields.size());
        assertEquals("user_first_name", fields.get(0).getField());
        assertEquals("user_last_name", fields.get(1).getField());
        assertEquals("user_company", fields.get(2).getField());
    }
}
