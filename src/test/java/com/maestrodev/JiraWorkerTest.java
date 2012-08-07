package com.maestrodev;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

/**
 * Tests for Maestro Cloud plugin.
 */
public class JiraWorkerTest
{
    private static final JSONParser parser = new JSONParser();
    private static List issueKeys;
    /**
     * Test for Alm
     */
    @Test
    public void createIssue()
        throws Exception
    {
        JiraWorker worker = new JiraWorker();

        worker.setWorkitem( loadJson( "create" ) );

        // provision
        worker.createIssue();

        assertNull( worker.getError(), worker.getError() );
        
        Map response = (Map) worker.getFields().get("jira");
        
        issueKeys = new ArrayList();
        issueKeys.add(response.get("key")); 
    }

    
    /**
     * Test for Alm
     */
    @Test
    public void transitIssue()
        throws Exception
    {
        JiraWorker worker = new JiraWorker();

        worker.setWorkitem( loadJson( "transition" ) );
        
        worker.setField("issue_keys", issueKeys);
        // provision
        worker.transitionIssues();

        assertNull( worker.getError(), worker.getError() );
        
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
