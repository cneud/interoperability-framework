/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.impact_project.iif.t2.client;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Impact
 */
public class WorkflowDetailsTest
{
    
    /**
     * Test of getTitle method, of class WorkflowDetails.
     */
    @Test
    public void testGets()
    {       
        WorkflowDetails instance = new WorkflowDetails();
        
        instance.setTitle("test");
        instance.setDescription("test");
        instance.setImageUrl("test");
        
        assertEquals("test", instance.getTitle());
        assertEquals("test", instance.getDescription());
        assertEquals("test", instance.getImageUrl());
    }

    
    
}
