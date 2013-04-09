/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import junit.framework.Assert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestValueManager {

    private BufferedReader reader;
    private BufferedWriter writer;

    @BeforeMethod
    public void setUp() throws Exception {
        File file = new File(ScriptExecutor.RESULT_DIRECTORY, UUID.randomUUID().toString());
        file.createNewFile();
        
        reader = new BufferedReader(new FileReader(file));
        writer = new BufferedWriter(new FileWriter(file));
    }

    @Test
    public void testListValueManger() throws Exception {
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        
        ValueManager manager = new ListValueManager();
        manager.store(writer, value);

        List<String> newValue = (List<String>)manager.load(reader);

        Assert.assertEquals(newValue.get(0), "1");
        Assert.assertEquals(newValue.get(1), "2");
    }

    @Test
    public void testMapValueManger() throws Exception {
        Map<String, Long> value = new HashMap<String, Long>();
        value.put("1", 2L);
        value.put("3", 4L);

        ValueManager manager = new MapValueManager();
        manager.store(writer, value);

        Map<String, Long> newValue = (Map<String, Long>)manager.load(reader);

        Assert.assertEquals(newValue.get("1"), Long.valueOf(2));
        Assert.assertEquals(newValue.get("3"), Long.valueOf(4));
    }

    @Test
    public void testLongValueManger() throws Exception {
        ValueManager manager = new LongValueManager();
        manager.store(writer, 1L);

        Long newValue = (Long)manager.load(reader);

        Assert.assertEquals(newValue, Long.valueOf(1));
    }
}
