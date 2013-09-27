package com.maestrodev;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.io.IOUtils;
import org.fusesource.stomp.client.BlockingConnection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.maestrodev.maestro.plugins.StompConnectionFactory;


/**
 * Tests for Maestro Jira plugin.
 */
public class JiraWorkerTest
{
    private static final JSONParser parser = new JSONParser();
    private static List<String> issueKeys;

    HashMap<String, Object> stompConfig;
    StompConnectionFactory stompConnectionFactory;
    BlockingConnection blockingConnection;
    JiraWorker worker;

    @Before
    public void setUp() throws Exception {
        stompConfig = new HashMap<String, Object>();
        stompConfig.put("host", "localhost");
        stompConfig.put("port", "61613");
        stompConfig.put("queue", "test");

        // Setup the mock stomp connection
        stompConnectionFactory = mock(StompConnectionFactory.class);
        blockingConnection = mock(BlockingConnection.class);
        when(stompConnectionFactory.getConnection(Matchers.anyString(),
                       Matchers.anyInt())).thenReturn(blockingConnection);

        worker = new JiraWorker();   
        worker.setStompConnectionFactory(stompConnectionFactory);
        worker.setStompConfig(stompConfig);
    }

    /**
     * Test for Jira
     */
    @Test
    public void createIssue()
        throws Exception
    {
        JiraWorker spy = spy(worker);
        spy.setWorkitem( loadJson( "create" ) );

        spy.createIssue();

        assertNull( spy.getError(), worker.getError() );
        
        Map response = (Map) spy.getFields().get("jira");
        
        assertNotNull(response.get("key"));
        
        issueKeys = new ArrayList<String>();
        issueKeys.add((String)response.get("key"));
    }

    
    /**
     * Test for Jira
     */
    @Test
    public void transitIssue()
        throws Exception
    {
        JiraWorker spy = spy(worker);
        spy.setWorkitem( loadJson( "transition" ) );
        
        spy.setField("issue_keys", issueKeys);

        spy.transitionIssues();

        assertNull( spy.getError(), spy.getError() );
        
    }
    
    public JSONObject loadJson( String name )
        throws IOException, ParseException
    {
        InputStream is = null;
        try
        {
            String f = name + ".json";
            is = getClass().getClassLoader().getResourceAsStream( f );
            if ( is == null )
            {
                throw new IllegalStateException( "File not found " + f );
            }
            else
            {
                return (JSONObject) parser.parse( IOUtils.toString( is ) );
            }
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }
    }

}
