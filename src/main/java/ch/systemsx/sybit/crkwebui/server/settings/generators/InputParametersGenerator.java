package ch.systemsx.sybit.crkwebui.server.settings.generators;

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
import ch.systemsx.sybit.crkwebui.shared.model.ScreenSettings;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

/**
 * This class is used to parse input parameters file.
 * @author AS
 */
public class InputParametersGenerator
{
	/**
	 * Reads application settings from input stream
	 * @param inputParametersStream stream from which parameters are going to be read
	 * @return retrieved application settings
	 * @throws Throwable when can not properly parse input parameters file
	 */
	public static ApplicationSettings prepareApplicationSettings(InputStream inputParametersStream) throws Throwable
	{
		ApplicationSettings applicationSettings = new ApplicationSettings();
		
		DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
		Document xmlDocument = xmlDocumentBuilder.parse(inputParametersStream);
		xmlDocument.getDocumentElement().normalize();
		
		Element documentRootNode = (Element)xmlDocument.getElementsByTagName("settings").item(0);
		
		NodeList supportedMethodsRootNodeList = documentRootNode.getElementsByTagName("supported_methods");
		Node supportedMethodsRootNode = (Node)supportedMethodsRootNodeList.item(0);
		List<SupportedMethod> supportedMethods = prepareSupportedMethods(supportedMethodsRootNode);
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
		
		String notification = getNodeText(documentRootNode, "notification");
		applicationSettings.setNotificationOnStart(notification);
		
		String newsMessage = getNodeText(documentRootNode, "news");
		applicationSettings.setNewsMessage(newsMessage);
		
		Node runParametersNode = documentRootNode.getElementsByTagName("run_parameters").item(0);
		Map<String, String> runPropetiesMap = prepareRunParameters(runParametersNode);
		applicationSettings.setRunParametersNames(runPropetiesMap);
		
		Node screenSettingsNode = documentRootNode.getElementsByTagName("screen_settings").item(0);
		ScreenSettings screenSettings = prepareScreenSettings(screenSettingsNode);
		applicationSettings.setScreenSettings(screenSettings);
		
		return applicationSettings;
	}
	
	/**
	 * Retrieves list of supported methods.
	 * @param supportedMethodsRootNode methods root node
	 * @return list of supported methods
	 */
	private static List<SupportedMethod> prepareSupportedMethods(Node supportedMethodsRootNode)
	{
		List<SupportedMethod> supportedMethods = new ArrayList<SupportedMethod>();
		
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
		
		return supportedMethods;
	}

	/**
	 * Retrieves human readable names of run parameters.
	 * @param runParametersRootNode run parameters node
	 * @return human readable names of run parameters
	 */
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
	
	/**
	 * Retrieves screen settings.
	 * @param screenSettingsNode screen settings node
	 * @return screen settings
	 */
	private static ScreenSettings prepareScreenSettings(Node screenSettingsNode)
	{
		ScreenSettings screenSettings = new ScreenSettings();
		
		NodeList screenSettingsNodeList = screenSettingsNode.getChildNodes();
		
		for(int i=0; i<screenSettingsNodeList.getLength(); i++)
		{
			Node settingNode = (Node)screenSettingsNodeList.item(i);
			
			if(settingNode.getNodeType() == Node.ELEMENT_NODE)
			{
				if(settingNode.getNodeName().equals("min_screen_width"))
				{
					screenSettings.getMinWindowData().setWindowWidth(Integer.parseInt(settingNode.getFirstChild().getNodeValue()));
				}
				else if(settingNode.getNodeName().equals("min_screen_height"))
				{
					screenSettings.getMinWindowData().setWindowHeight(Integer.parseInt(settingNode.getFirstChild().getNodeValue()));
				}
			}
		}
		
		return screenSettings;
	}

	private static void setDefaultParameters(Node defaultInputParametersNodeRoot, 
											 InputParameters inputParameters) 
	{
		Element defaultInputParameterElement = (Element)defaultInputParametersNodeRoot;
		inputParameters.setMaxNrOfSequences(Integer.parseInt(defaultInputParameterElement.getElementsByTagName("max_nr_of_sequences").item(0).getFirstChild().getNodeValue()));
		inputParameters.setReducedAlphabet(Integer.parseInt(defaultInputParameterElement.getElementsByTagName("reduced_alphabet").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSoftIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("soft_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setHardIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("hard_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSearchMode(defaultInputParameterElement.getElementsByTagName("search_mode").item(0).getFirstChild().getNodeValue());
	}

	/**
	 * Retrieves list of values for reduced alphabet.
	 * @param reducedAlphabetNodeRoot reduced alphabet node root
	 * @return list of values for reduced alphabet
	 */
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

	/**
	 * Prepares list of strings for specified node list root.
	 * @param nodeListRoot
	 * @return list of retrieved strings
	 */
	private static List<String> prepareList(Node nodeListRoot) 
	{
		List<String> nodesValues = new ArrayList<String>();
		
		NodeList nodeList = nodeListRoot.getChildNodes();
		
		for(int i=0; i<nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			
			if(node.getNodeType() == Node.ELEMENT_NODE)
			{
				Node elementNode = node.getFirstChild();
				nodesValues.add(elementNode.getNodeValue());
			}
		}
		
		return nodesValues;
	}
	
	/**
	 * Retrieves text specified in node.
	 * @param rootNode root node
	 * @param nodeName name of the node for which text is to be retrieved
	 * @return text stored in node
	 */
	private static String getNodeText(Element rootNode,
									  String nodeName)
	{
		Node node = rootNode.getElementsByTagName(nodeName).item(0);
		String nodeText = null;
		
		if(node.hasChildNodes())
		{
			nodeText = node.getFirstChild().getNodeValue();
		}
		return nodeText;	
	}
}
