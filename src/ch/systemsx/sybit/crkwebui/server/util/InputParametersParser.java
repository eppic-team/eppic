package ch.systemsx.sybit.crkwebui.server.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.RunParametersItem;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

public class InputParametersParser
{
	public static ApplicationSettings prepareApplicationSettings(InputStream inputParametersStream) throws Throwable
	{
		ApplicationSettings applicationSettings = new ApplicationSettings();
		List<SupportedMethod> supportedMethods = new ArrayList<SupportedMethod>();
		
		DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
		Document xmlDocument = xmlDocumentBuilder.parse(inputParametersStream);
		xmlDocument.getDocumentElement().normalize();
		
		Element documentRootNode = (Element)xmlDocument.getElementsByTagName("settings").item(0);
		
		NodeList supportedMethodsRootNodeList = documentRootNode.getElementsByTagName("supported_methods");
		Node supportedMethodsRootNode = (Node)supportedMethodsRootNodeList.item(0);
		
		NodeList supportedMethodsNodeList = supportedMethodsRootNode.getChildNodes();
		
		for(int i=0; i<supportedMethodsNodeList.getLength(); i++)
		{
			Node supportedMethodNode = supportedMethodsNodeList.item(i);
			
			if(supportedMethodNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element supportedMethodElement = (Element)supportedMethodNode;
				Node methodNameNode = supportedMethodElement.getElementsByTagName("name").item(0).getFirstChild();
				Node methodInputFieldNode = supportedMethodElement.getElementsByTagName("input_field").item(0).getFirstChild();
				
				SupportedMethod supportedMethod = new SupportedMethod();
				supportedMethod.setName(methodNameNode.getNodeValue());
				supportedMethod.setHasFieldSet(Boolean.parseBoolean(methodInputFieldNode.getNodeValue()));
				supportedMethods.add(supportedMethod);
			}
		}
		
		applicationSettings.setScoresTypes(supportedMethods);
		
		InputParameters inputParameters = new InputParameters();
		
		NodeList inputParametersRootNodeList = documentRootNode.getElementsByTagName("input_parameters");
		Node inputParametersRootNode = (Node)inputParametersRootNodeList.item(0);
		
		NodeList inputParametersNodeList = inputParametersRootNode.getChildNodes();
		
		for(int i=0; i<inputParametersNodeList.getLength(); i++)
		{
			Node inputParametersNode = (Node)inputParametersNodeList.item(i);
			
			if(inputParametersNode.getNodeType() == Node.ELEMENT_NODE)
			{
				if(inputParametersNode.getNodeName().equals("default_methods"))
				{
					List<String> defaultMethods = prepareList(inputParametersNode);
					inputParameters.setMethods(defaultMethods);
				}
				else if(inputParametersNode.getNodeName().equals("default_parameters"))
				{
					setDefaultParameters(inputParametersNode, inputParameters);
				}
				else if(inputParametersNode.getNodeName().equals("reduced_alphabet"))
				{
					List<Integer> reducedAlphabetList = prepareReducedAlphabetList(inputParametersNode);
					applicationSettings.setReducedAlphabetList(reducedAlphabetList);
				}
				else if(inputParametersNode.getNodeName().equals("search_mode"))
				{
					List<String> defaultMethods = prepareList(inputParametersNode);
					applicationSettings.setSearchModeList(defaultMethods);
				}
			}
		}
		
		applicationSettings.setDefaultParametersValues(inputParameters);
		
		Node notificationNode = documentRootNode.getElementsByTagName("notification").item(0);
		String notification = prepareNotificationText(notificationNode);
		applicationSettings.setNotificationOnStart(notification);
		
		Node runParametersNode = documentRootNode.getElementsByTagName("run_parameters").item(0);
		Map<String, String> runPropetiesMap = prepareRunParameters(runParametersNode);
		applicationSettings.setRunParametersNames(runPropetiesMap);
		
		return applicationSettings;
	}

	private static Map<String, String> prepareRunParameters(Node runParametersRootNode)
	{
		NodeList runParametersRootNodeList = runParametersRootNode.getChildNodes();
		
		Map<String, String> xmlParameters = new HashMap<String, String>();
		
		for(int i=0; i<runParametersRootNodeList.getLength(); i++)
		{
			Node runParameterNode = runParametersRootNodeList.item(i);
			
			if(runParameterNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element runParameterElement = (Element)runParameterNode;
				Node parameterNameNode = runParameterElement.getElementsByTagName("name").item(0).getFirstChild();
				Node parameterValueNode = runParameterElement.getElementsByTagName("value").item(0).getFirstChild();
				
				xmlParameters.put(parameterNameNode.getNodeValue(), parameterValueNode.getNodeValue());
			}
		}
		
		Map<String, String> runPropetiesMap = new HashMap<String, String>();
		for (Field field : RunParametersItem.class.getDeclaredFields())
		{
			if(xmlParameters.get(field.getName()) != null)
			{
				runPropetiesMap.put(field.getName(), (String) xmlParameters.get(field.getName()));
			}
		}
		
		return runPropetiesMap;
	}

	private static String prepareNotificationText(Node inputParametersNode) 
	{
		return inputParametersNode.getFirstChild().getNodeValue();
	}

	private static void setDefaultParameters(Node defaultInputParametersNodeRoot, 
											 InputParameters inputParameters) 
	{
		Element defaultInputParameterElement = (Element)defaultInputParametersNodeRoot;
		inputParameters.setAsaCalc(Integer.parseInt(defaultInputParameterElement.getElementsByTagName("asa_calc").item(0).getFirstChild().getNodeValue()));
		inputParameters.setMaxNrOfSequences(Integer.parseInt(defaultInputParameterElement.getElementsByTagName("max_nr_of_sequences").item(0).getFirstChild().getNodeValue()));
		inputParameters.setReducedAlphabet(Integer.parseInt(defaultInputParameterElement.getElementsByTagName("reduced_alphabet").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSoftIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("soft_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setHardIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("hard_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSelecton(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("selecton").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSearchMode(defaultInputParameterElement.getElementsByTagName("search_mode").item(0).getFirstChild().getNodeValue());
	}

	private static List<Integer> prepareReducedAlphabetList(Node reducedAlphabetNodeRoot) 
	{
		List<Integer> reducedAlphabetList = new ArrayList<Integer>();
		
		NodeList reducedAlphabetNodeList = reducedAlphabetNodeRoot.getChildNodes();
		
		for(int i=0; i<reducedAlphabetNodeList.getLength(); i++)
		{
			Node reducedAlphabetNode = reducedAlphabetNodeList.item(i);
			
			if(reducedAlphabetNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Node reducedAlphabetValueNode = reducedAlphabetNode.getFirstChild();
				reducedAlphabetList.add(Integer.parseInt(reducedAlphabetValueNode.getNodeValue()));
			}
		}
		
		return reducedAlphabetList;
	}

	private static List<String> prepareList(Node defaultMethodsNodeListRoot) 
	{
		List<String> defaultMethods = new ArrayList<String>();
		
		NodeList defaultMethodsNodeList = defaultMethodsNodeListRoot.getChildNodes();
		
		for(int i=0; i<defaultMethodsNodeList.getLength(); i++)
		{
			Node defaultMethodsNode = defaultMethodsNodeList.item(i);
			
			if(defaultMethodsNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Node methodNameNode = defaultMethodsNode.getFirstChild();
				defaultMethods.add(methodNameNode.getNodeValue());
			}
		}
		
		return defaultMethods;
	}
}
