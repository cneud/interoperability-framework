/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*/


package eu.impact_project.resultsrepository;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 
 * Contains all the workflow results.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Results {

	private ArrayList<ToolResults> toolResultsList = new ArrayList<ToolResults>();

	public ArrayList<ToolResults> getToolResultsList() {
		return toolResultsList;
	}
	
	public void addToolResults(ToolResults results) {
		if (toolResultsList == null){
			toolResultsList = new ArrayList<ToolResults>();
		}
		toolResultsList.add(results);
	}
	
	public boolean notEmpty() {
		return toolResultsList.size() > 0;
	}
	
}
