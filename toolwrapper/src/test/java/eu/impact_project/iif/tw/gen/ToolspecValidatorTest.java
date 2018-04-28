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
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import eu.impact_project.iif.tw.Constants;
import eu.impact_project.iif.tw.conf.Configuration;
import eu.impact_project.iif.tw.gen.types.ErrType;
import eu.impact_project.iif.tw.toolspec.Services;
import eu.impact_project.iif.tw.toolspec.Toolspec;
import java.net.URL;
import java.util.logging.Level;
import static org.junit.Assert.fail;

/**
 *
 * @author onbscs
 */
public class ToolspecValidatorTest {

	private ArrayList<String> toolspecs;

	private static Logger logger = Logger
			.getLogger(ToolspecValidatorTest.class.getName());

	/**
	 * Creates a List of tool specifications for testing
	 * 
	 * @throws GeneratorException
	 * @throws URISyntaxException
	 */
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

	private ToolspecValidator getToolspecValidator(String toospecXml)
			throws GeneratorException {
		ToolspecValidator tv;
		try {
			Configuration ioc = new Configuration();
			ioc.setXmlConf(new File(Constants.DEFAULT_TOOLSPEC));
			ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
			JAXBContext context;
			context = JAXBContext
					.newInstance("eu.impact_project.iif.tw.toolspec");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc
					.getXmlConf()));
			tv = new ToolspecValidator(toolspec, ioc);
			return tv;
		} catch (JAXBException ex) {
			//logger.error("JAXBException", ex);
			throw new GeneratorException("JAXBException occurred.");
		}
	}

	/**
	 * Test of validate method, of class ToolspecValidator.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testValidate() throws Exception {
		for (String toolspec : toolspecs) {
			ToolspecValidator tv = getToolspecValidator(toolspec);
			tv.validateWithXMLSchema();
			tv.validate();
                        tv.isValid();
		}
                
	}   
        
        @Test
        public void testValidateError()
        {            
            try
            {   
                Configuration ioc = new Configuration();
                URL url = this.getClass().getResource("/Errortoolspec.xml");        
                File testXml = new File(url.getFile());
                ioc.setXmlConf(testXml);                
                ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
                Toolspec toolspec = new Toolspec();
                ToolspecValidator tv = new ToolspecValidator(toolspec, ioc);

                tv.validateWithXMLSchema();
                fail("Should raise exception");
            }
            catch (GeneratorException ex)
            {
                //System.out.println(ex.toString());
            }
        }
        
        @Test
        public void testValidateError2()
        {            
            try
            {   
                Configuration ioc = new Configuration();
                URL url = this.getClass().getResource("/Errortoolspec.xml");        
                File testXml = new File(url.getFile());
                ioc.setXmlConf(testXml);                
                ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));
                Toolspec toolspec = null;
                ToolspecValidator tv = new ToolspecValidator(toolspec, ioc);

                Toolspec pru = new Toolspec();
                pru.setServices(new Services());
                
                
                tv.validate();
                fail("Should raise exception");
            }
            catch (GeneratorException ex)
            {
                
            }
        }
        
        @Test
        public void testCheck()
        {
            try
            {   
                Configuration ioc = new Configuration();
                URL url = this.getClass().getResource("/layoutevaluation.xml");        
                File testXml = new File(url.getFile());
                ioc.setXmlConf(testXml);                
                ioc.setProjConf(new File(Constants.DEFAULT_PROJECT_PROPERTIES));                
                JAXBContext context;
			context = JAXBContext
					.newInstance("eu.impact_project.iif.tw.toolspec");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc
					.getXmlConf()));                        
			ToolspecValidator tv = new ToolspecValidator(toolspec, ioc);                
                
                tv.validate();                 
                fail("Should raise exception");
            }
            catch (GeneratorException ex)
            {
                
            } catch (JAXBException ex)
            {
                
            }
        }
        
        @Test
        public void testWarning()
        {
            ToolspecValidator tv = new ToolspecValidator(null, null);
            tv.addError(new Error(ErrType.WARNING, "Test error"));
            try
            {
                tv.checkpoint();
                
            } catch (GeneratorException ex)
            {
                fail("Should not raise exception");
            }
        }
        
        @Test
        public void testErrorType()
        {
            ErrType err = ErrType.WARNING;
            err.toString();
            ErrType err2 = ErrType.ERROR;
            err2.toString();
        }
        
        

    private void addToolspecFilesFromResourceDir(String dirName) throws URISyntaxException {
    	// OK get the resource directory as a file from the URL
        File dir = new File(("src/main/resources/toolspec.xsd"));
    }

	private void addToolspecFilesFromDir(File dir) {
		// if it's not a good dir
		if ((dir == null) || (!dir.exists()) || (!dir.isDirectory())) {
			throw new IllegalArgumentException("Argument dirname:"
					+ dir.getAbsolutePath() + " is not an existing directory.");
		}
		// Get it's file children
		File[] files = dir.listFiles();
		if (files != null) {
			// If not null then
			for (File file : files) {
				if (file.getName().endsWith(".xml")) {
					logger.info("Tool specification file \"" + file.getName()
							+ "\" found");
					toolspecs.add(file.getAbsolutePath());
				}
			}
		}

	}
}
