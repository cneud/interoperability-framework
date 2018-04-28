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

import eu.impact_project.iif.tw.gen.types.IOType;
import eu.impact_project.iif.tw.tmpl.GenericCode;
import eu.impact_project.iif.tw.tmpl.OperationCode;
import eu.impact_project.iif.tw.tmpl.OutputItemCode;
import eu.impact_project.iif.tw.tmpl.ResultElementCode;
import eu.impact_project.iif.tw.tmpl.SectionCode;
import eu.impact_project.iif.tw.tmpl.ServiceCode;
import eu.impact_project.iif.tw.tmpl.ServiceXml;
import eu.impact_project.iif.tw.tmpl.ServiceXmlOp;
import eu.impact_project.iif.tw.toolspec.InOut;
import eu.impact_project.iif.tw.toolspec.Input;
import eu.impact_project.iif.tw.toolspec.Operation;
import eu.impact_project.iif.tw.toolspec.Output;
import eu.impact_project.iif.tw.toolspec.Restriction;
import eu.impact_project.iif.tw.util.StringConverterUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.7
 */
public class ServiceCodeCreator {

    private static Logger logger = Logger.getLogger(ServiceCodeCreator.class.getName());
    PropertiesSubstitutor st;
    private List<Operation> operations;
    private ServiceCode sc;
    private ServiceXml sxml;

    /**
     * Constructor of the service code creator
     * @param st Substitutor, contains global project value-key pairs
     * @param sc Service code, the java code that is created for the service
     * @param sxml services xml, the axis2 web service definition file
     * @param operations The operations that are defined for the service
     */
    public ServiceCodeCreator(PropertiesSubstitutor st, ServiceCode sc, ServiceXml sxml, List<Operation> operations) {
        this.st = st;
        this.operations = operations;
        this.sc = sc;
        this.sxml = sxml;
    }
    

    /**
     * Create operations
     * @throws IOException
     * @throws GeneratorException
     */
    public void createOperations() throws IOException, GeneratorException {
        for (Operation operation : operations) {
            createOperationCode(operation);
        }
    }

    /**
     * Create the code for an operation
     * @param operation
     * @throws IOException
     * @throws GeneratorException
     */
    private void createOperationCode(Operation operation) throws IOException, GeneratorException {

        // Create service operation
        int opn = operation.getOid();
        boolean evaluationId = false;
        boolean has_inputdir = false;

        String operationTmpl = st.getProp("project.template.operation");
        OperationCode oc = new OperationCode(operationTmpl, opn);
        String operationName = operation.getName();
        oc.setOperationName(operationName);
        oc.put("operationname", oc.getOperationName());
        // add main project properties velocity context
        oc.put(st.getContext());

        List<Input> inputs = operation.getInputs().getInput();

        // count the number of real inputs, and skip the evaluationId
        int outputCounter = 0;

        for (Input input : inputs) {
            if (!input.getName().equals("evaluationId") && !input.getName().equals("inputdir")) {
                outputCounter = outputCounter + 1;
            } else if (input.getName().equals("evaluationId")) {
                evaluationId = true;
                logger.debug("evaluationId used in workflow");
            } else if (input.getName().equals("inputdir")) {
                has_inputdir = true;
                outputCounter = outputCounter + 1;
            }
        }

        if (evaluationId) {
                oc.put("evaluationId", "infolog(\"Evaluation-ID: \" + evaluationId);");
                oc.put("evaluationIdDir", "evaluationId +");
            } else {
                oc.put("evaluationId", "infolog(\"No evaluation-id\");");
                oc.put("evaluationIdDir", "");
            }


        // Handle inputdir
        if (has_inputdir) {
            oc.put("is_inputdir", "true");
        } else {
            oc.put("is_inputdir", "false");
        }

        int outputMax = outputCounter;
        outputCounter = 0;

        // generate an input data section in the service code for each input field
        // and add the evaluationId to the last element
        for (Input input : inputs) {
            if (!input.getName().equals("evaluationId")) {
                outputCounter = outputCounter + 1;
                if (outputCounter == outputMax && evaluationId) {
                    addDataSection(operation, oc, input, true);
                } else {
                    addDataSection(operation, oc, input, false);
                }
            }
        }

        // generate an output data section in the service code for each output field
        boolean has_outputstream = false;
        boolean has_outputdir = false;


        String tmpdir = System.getProperty("java.io.tmpdir");
        String outputdirname = Long.toString(System.currentTimeMillis());

        List<Output> outputs = operation.getOutputs().getOutput();
        for (Output output : outputs) {
            addDataSection(operation, oc, output, false);
            if (output.getName().equals("outputdir")) {
                has_outputdir = true;
            }
            if (output.getName().equals("outputstream")) {
                has_outputstream = true;
            }
        }

        // insert input and output data sections for the operation
        oc.put("inputsection", oc.getInputSection());
        oc.put("outputsection", oc.getOutputSection());

        // name of the service and project namespace
        oc.put("servicename", st.getContextProp("project_midfix"));
        oc.put("project_namespace", st.getContextProp("project_namespace"));

        // output files that are used as a command pattern variable
        // (CliMapping is set to a variable of the command pattern) have to
        // be defined before the command line process section.
        oc.put("outfileitems", oc.getOutFileItems());
        oc.put("resultelements", oc.getResultElements());

        // outputdir
        if (has_outputdir) {
            oc.put("outputdir", "\"" + tmpdir + File.separator + outputdirname + File.separator + "\"");
        } else {
            oc.put("outputdir", "\"\"");
        }

        // outputstream
        if (has_outputstream) {
            oc.put("outputstream", "outputstreamFileName");
        } else {
            oc.put("outputstream", "\"\"");
        }

        // operation id
        oc.put("opid", String.valueOf(opn));
        // velocity variable substitution for the operation
        oc.evaluate();

        // add the operation to the service code
        sc.addOperation(oc);

        // create services xml operation entry
        ServiceXmlOp sxmlop = new ServiceXmlOp("tmpl/servicexmlop.vm");
        sxmlop.put("operationname", oc.getOperationName());
        String clicmd = st.getProp("service.operation." + opn + ".clicmd");
        sxmlop.put("clicmd", clicmd);
        sxmlop.put("opid", String.valueOf(opn));
        // velocity variable substitution for the services xml
        sxmlop.evaluate();
        // add the operation to the services xml
        sxml.addOperation(sxmlop);
    }

    /**
     * Add an input/output data section to the operation
     * @param operation
     * @param oc
     * @param inout
     * @throws GeneratorException
     */
    protected void addDataSection(Operation operation, OperationCode oc,
            InOut inout, boolean evaluationId) throws GeneratorException {

        //delete temp flag
        String deleteTemp = st.getProp("deleteTemp");
        
        // input or output field?
        IOType iotype = (inout instanceof Input) ? IOType.INPUT : IOType.OUTPUT;
        // input/output fields
        String nodeName = inout.getName();
        String required = inout.getRequired();
        String dataType = inout.getDatatype();
        String cliMapping = inout.getCliMapping();
        // input specific fields
        Input input = null;
        boolean isInput = false;
        if (inout instanceof Input) {
            input = (Input) inout;
            isInput = true;
        }
        String cliReplacement = (isInput) ? ((Input) inout).getDefault().getClireplacement() : null;
        Restriction restriction = (isInput) ? ((Input) inout).getRestriction() : null;
        // output specific fields
        Output output = null;
        boolean isOutput = false;
        if (inout instanceof Output) {
            output = (Output) inout;
            isOutput = true;
        }
        boolean autoExtension = (isOutput && output.isAutoExtension() != null) && output.isAutoExtension();

        // overrides filename's output
        String outfilename = (isOutput) ? ((Output) inout).getOutFileName() : null;
        // filename prefix
        String prefixFromInput = (isOutput) ? ((Output) inout).getPrefixFromInput() : null;
        // filename extention
        String extension = (isOutput) ? ((Output) inout).getExtension() : null;

        boolean isRequired = (required != null && required.equalsIgnoreCase("true"));
        String opid = String.valueOf(operation.getOid());

        // code template for the the field depending on data type
        boolean isMultiple = restriction != null && restriction.isMultiple();
        String template = "tmpl/datatypes/" + iotype + "_"
                + StringConverterUtil.typeToFilename(dataType)
                + (isMultiple ? "_restricted_list" : "") // multiple string list
                + ((dataType.equals("xsd:anyURI") // URL with temporary file
                && !isRequired) ? "_opt" : "") // optional suffix
                + ".vm";

        logger.debug("Using template \"" + template + "\" for node \"" + nodeName + "\" in operation " + opid);

        File templateFile = new File(template);
        if (templateFile.canRead()) {
            try {
                SectionCode sectCode = new SectionCode(template);
                sectCode.put("opid", opid);
                sectCode.put("operationname", operation.getName());

                if (outfilename != null) {
                    sectCode.put("outfilename", "\"" + outfilename + "\"");
                } else {
                    sectCode.put("outfilename", "\"\"");
                }

                if (iotype == IOType.INPUT) {
                    sectCode.put("input_variable", nodeName);
                    String mapping = null;

                    if (cliReplacement == null) {
                        // Simple mapping
                        mapping = getCliMapping(inout);
                    } else {
                        // Mapping of a variable with replacement of null value
                        GenericCode cliReplCode = new GenericCode("tmpl/clireplacement.vm");
                        cliReplCode.put(sectCode.getCtx());
                        cliReplCode.put("clireplacement", cliReplacement);
                        cliReplCode.evaluate();
                        mapping = cliReplCode.getCode();
                    }

                    sectCode.put("mapping", mapping);
                    String parameter = getOperationParameter(input);
                    oc.addParameter(parameter);
                    String parList = oc.getParametersCsList();

                    if (evaluationId) {
                        parList = parList + ", String evaluationId";
                    }

                    oc.put("parameters", parList);
                    sectCode.evaluate();
                    oc.appendInputSection(sectCode.getCode());
                } else if (iotype == IOType.OUTPUT) {
                    sectCode.put("output_variable", nodeName); 
                    
                    // flag to delete temp file
                    sectCode.put("delete_temp", deleteTemp);
                    
                    // some tools automatically append the extension to the
                    // result file in this case it is attached again, so the
                    // result for a file with the extension .txt would be
                    // .txt.txt in order to match the output file produced
                    // by the tool.
                    if (autoExtension) {
                        sectCode.put("autoextension", "+\"." + extension + "\"");
                    } else {
                        sectCode.put("autoextension", "");
                    }
                    sectCode.evaluate();
                    if (dataType.equals("xsd:anyURI")) {
                        OutputItemCode oic = null;
                        // should the output file get the input file name as
                        // prefix? Different templates!
                        String outfileId = ((Output) inout).getOutfileId();
                        if (prefixFromInput == null || prefixFromInput.isEmpty()) {
                            if (outfileId != null && !outfileId.equals("")) {
                                oic = new OutputItemCode("tmpl/outfileitem_id.vm");
                                oic.put("outfileid", outfileId);
                            } else {
                                oic = new OutputItemCode("tmpl/outfileitem.vm");
                            }
                        } else {
                            if (outfileId != null && !outfileId.equals("")) {
                                oic = new OutputItemCode("tmpl/outfileitem_prefix_id.vm");
                                oic.put("outfileid", outfileId);
                            } else {
                                oic = new OutputItemCode("tmpl/outfileitem_prefix.vm");
                            }
                            oic.put("prefix", prefixFromInput);
                        }
                        // put the current context
                        oic.put(oc.getCtx());
                        oic.put("varname", nodeName);

                        String mapping = cliMapping;
                        oic.put("mapping", mapping);
                        oic.put("extension", extension);
                        oic.evaluate();
                        oc.addOutFileItem(oic.getCode());
                    }
                    ResultElementCode rec = new ResultElementCode("tmpl/resultelement.vm");
                    rec.put("varname", nodeName);
                    String serviceresult = null;
                    serviceresult = nodeName + "FileUrl.toString()";
                    rec.put("serviceresult", serviceresult);
                    rec.evaluate();
                    oc.addResultElement(rec.getCode());
                    oc.appendOutputSection(sectCode.getCode());
                }
            } catch (IOException ex) {
                logger.error("Unable to create code for template: " + template);
            }
        } else {
            throw new GeneratorException("Unable to read code template: " + template);
        }
    }

    /**
     * Get java data type definition for input/output datatype definition
     * @param dataType Input/output datatype definition
     * @param nodeName Current node name
     * @return Java data type definition
     */
    private String getOperationParameter(Input input) {
        String parameter = null;
        String dataType = input.getDatatype();
        String nodeName = input.getName();
        boolean isMultiple = (input.getRestriction() != null
                && input.getRestriction().isMultiple());
        if (dataType.equals("xsd:anyURI")) {
            parameter = "String " + nodeName;
        }
        if (dataType.equals("xsd:int")) {
            parameter = "Integer " + nodeName;
        }
        if (dataType.equals("xsd:boolean")) {
            parameter = "Boolean " + nodeName;
        }
        if (dataType.equals("xsd:string")) {
            if (isMultiple) {
                parameter = "OMElement " + nodeName;
            } else {
                parameter = "String " + nodeName;
            }
        }
        if (parameter == null) {
            parameter = "null";
        }
        return parameter;
    }

    /**
     * Get CLI mapping expression that assigns a value to the CLI replacement
     * variable.
     * @param currJsn Current Json node
     * @param dataType Data type
     * @param nodeName Node name
     * @return CLI mapping
     */
    private String getCliMapping(InOut inout) {

        String cliMappingVar = inout.getCliMapping();
        String dataType = inout.getDatatype();
        String nodeName = inout.getName();
        String mappingVal = null;
        if (cliMappingVar != null) {

            if (dataType.equals("xsd:anyURI")) {
                mappingVal = nodeName + "File.getAbsolutePath()";
            }
            if (dataType.equals("xsd:int")) {
                mappingVal = "Integer.toString(" + nodeName + ")";
            }
            if (dataType.equals("xsd:boolean")) {
                mappingVal = "Boolean.toString(" + nodeName + ")";
            }
            if (dataType.equals("xsd:string")) {
                mappingVal = nodeName;
            }
            if (mappingVal == null) {
                mappingVal = "\"TODO: Set value\"";
            }
            // assign the variable value to a command line interface pattern
            // variable if it is defined by the CliMapping type (only INPUT
            // types are mapped to command line interface pattern variables
            String mappingKeyVal = "";
            if (inout instanceof Input) {
                Input input = (Input) inout;
                Restriction restr = input.getRestriction();
                if (restr != null) {
                    boolean isMultiple = restr.isMultiple();
                    if (isMultiple) {
                        mappingVal += "Csv";
                    }
                }
                mappingKeyVal = "cliCmdKeyValPairs.put(\"" + cliMappingVar + "\", " + mappingVal + ");";
            }
            return mappingKeyVal;
        } else {
            return "// No CLI mapping defined for " + nodeName;
        }
    }
}
