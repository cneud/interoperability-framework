/*
 *  Copyright 2011 The IMPACT Project Consortium.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package eu.impact_project.iif.tw.gen;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import eu.impact_project.iif.tw.Constants;
import eu.impact_project.iif.tw.conf.Configuration;
import eu.impact_project.iif.tw.util.FileUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author onbscs
 */
public class PropertiesSubstitutorTest {

    private PropertiesSubstitutor st;

    /**
     * Reads the config and sets up substitutor
     * @throws GeneratorException
     */
    @Before
    public void setUp() throws GeneratorException {
        Configuration ioc = new Configuration();
        ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
        ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
        st = new PropertiesSubstitutor(ioc.getProjConf());
    }

    /**
     * Test of addVariable method, of class PropertiesSubstitutor.
     */
    @Test
    public void testAddVariable() {
        st.addVariable("testvar", "testval");
        String val = st.getContextProp("testvar");
        assertEquals("Test variable not created successfully.", "testval", val);
        st.applySubstitution(val);
    }

    /**
     * Test of deriveVariables method, of class PropertiesSubstitutor.
     * @throws Exception 
     */
    @Test
    public void testDeriveVariables() throws Exception {

        String projectTitle = "Test Project";
        double toolVersion = 1.0D;

        ServiceDef sdef = new ServiceDef(projectTitle, Double.toString(toolVersion));
        st.setServiceDef(sdef);

        st.addVariable("project_title", sdef.getName());
        st.addVariable("tool_version", sdef.getVersion());
        st.addVariable("global_package_name", "org.apache.axis2");

        st.deriveVariables();

        assertEquals("Variable is not derived correctly.", "TestProject10", st.getContextProp("project_midfix"));
        assertEquals("Variable is not derived correctly.", "testproject10", st.getContextProp("project_midfix_lc"));
        assertEquals("Variable is not derived correctly.", "org/apache/axis2", st.getContextProp("project_package_path"));
        assertEquals("Variable is not derived correctly.", "http://apache.org/axis2", st.getContextProp("project_namespace"));
        
        ServiceDef pruSdef = st.getServiceDef();
        assertEquals(sdef, pruSdef);
    }
    
    @Test
    public void testErrorUnableLoad()
    {
        try
        {
            PropertiesSubstitutor ps = new PropertiesSubstitutor("");
            fail("Must raise, GeneratorException");
        } catch (GeneratorException ex)
        {
            //Test OK
        }        
    }
    
    @Test
    public void testServiceRefNull() 
    {        
        try
        {
            Configuration ioc = new Configuration();
            ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
            ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
            PropertiesSubstitutor ps = new PropertiesSubstitutor(ioc.getProjConf());
            ps.setServiceDef(null);
            ps.deriveVariables();
            fail("Must raise, GeneratorException");
        } catch (GeneratorException ex)
        {
            //Test OK
        }        
    }
    
    @Test
    public void testApplySubstitutionSourceFail() 
    {        
        try
        {
            Configuration ioc = new Configuration();
            ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
            ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
            PropertiesSubstitutor ps = new PropertiesSubstitutor(ioc.getProjConf());
            ps.applySubstitution("", "");
            fail("Must raise, GeneratorException");
        } catch (GeneratorException ex)
        {
            //Test OK
        }      
    }   
    
    @Test
    public void testApplySubstitutionTargetFail() 
    {        
        try
        {
            Configuration ioc = new Configuration();
            ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
            ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
            PropertiesSubstitutor ps = new PropertiesSubstitutor(ioc.getProjConf());
            String wsdlSourcePath = FileUtil.makePath("tmpl") + "Template.wsdl";  
            ps.applySubstitution(wsdlSourcePath, "");
            fail("Must raise, GeneratorException");
        } catch (GeneratorException ex)
        {
            //Test OK
        }      
    } 

}
