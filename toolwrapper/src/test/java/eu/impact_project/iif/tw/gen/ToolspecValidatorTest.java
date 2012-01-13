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

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.impact_project.iif.tw.Constants;
import eu.impact_project.iif.tw.conf.Configuration;
import eu.impact_project.iif.tw.toolspec.Toolspec;

/**
 *
 * @author onbscs
 */
public class ToolspecValidatorTest {

    private ArrayList<String> toolspecs;

    private static Logger logger = LoggerFactory.getLogger(ToolspecValidatorTest.class.getName());

    public ToolspecValidatorTest() {
    }

    @Before
    public void setUp() throws GeneratorException, URISyntaxException {
        toolspecs = new ArrayList<String>();
        toolspecs.add(Constants.DEFAULT_TOOLSPEC);
        // All tool specification instances from the examples directory
        // will be validated
        try {
            addToolspecFilesFromResourceDir("/examples");
        } catch (IllegalArgumentException excep) {
        	System.out.println(excep.getLocalizedMessage());
        	excep.printStackTrace();
        }
        
        if (this.toolspecs.isEmpty()) {
        	throw new IllegalStateException("No toolspecs found to test.");
        }
    }

    private ToolspecValidator getToolspecValidator(String toospecXml) throws GeneratorException {
        ToolspecValidator tv;
        try {
            Configuration ioc = new Configuration();
            ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
            ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
            JAXBContext context;
            context = JAXBContext.newInstance("eu.impact_project.iif.tw.toolspec");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc.getXmlConf()));
            tv = new ToolspecValidator(toolspec, ioc);
            return tv;
        } catch (JAXBException ex) {
            logger.error("JAXBException", ex);
            throw new GeneratorException("JAXBException occurred.");
        }
    }


     /**
     * Test of validate method, of class ToolspecValidator.
     */
    @Test
    public void testValidate() throws Exception {
        for(String toolspec : toolspecs) {
            ToolspecValidator tv = getToolspecValidator(toolspec);
            tv.validateWithXMLSchema();
            tv.validate();
        }
    }

    private void addToolspecFilesFromResourceDir(String dirName) throws URISyntaxException {
    	// OK get the resource directory as a file from the URL
        File dir = new File(this.getClass().getResource(dirName).toURI());
        this.addToolspecFilesFromDir(dir);
    }

    private void addToolspecFilesFromDir(File dir) {
        // if it's not a good dir
        if ((dir == null) || (!dir.exists()) || (!dir.isDirectory())) {
        	throw new IllegalArgumentException("Argument dirname:" + dir.getAbsolutePath() + " is not an existing directory.");
        }
        // Get it's file children
        File[] files = dir.listFiles();
        if (files != null) {
        	// If not null then
            for (File file : files) {
                if(file.getName().endsWith(".xml")) {
                    logger.info("Tool specification file \"" + file.getName() + "\" found");
                    toolspecs.add(file.getAbsolutePath());
                }
            }
        }
    	
    }
}
