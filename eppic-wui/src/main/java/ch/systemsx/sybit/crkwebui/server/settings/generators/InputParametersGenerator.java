package ch.systemsx.sybit.crkwebui.server.settings.generators;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eppic.model.dto.ApplicationSettings;
import eppic.model.dto.InputParameters;
import eppic.model.dto.ScreenSettings;

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
		
		InputParameters inputParameters = new InputParameters();
		
		NodeList inputParametersRootNodeList = documentRootNode.getElementsByTagName("input_parameters");
		Node inputParametersRootNode = (Node)inputParametersRootNodeList.item(0);
		
		NodeList inputParametersNodeList = inputParametersRootNode.getChildNodes();
		
		for(int i=0; i<inputParametersNodeList.getLength(); i++)
		{
			Node inputParametersNode = (Node)inputParametersNodeList.item(i);
			
			if(inputParametersNode.getNodeType() == Node.ELEMENT_NODE)
			{
				if(inputParametersNode.getNodeName().equals("default_parameters"))
				{
					setDefaultParameters(inputParametersNode, inputParameters);
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
		
		Node screenSettingsNode = documentRootNode.getElementsByTagName("screen_settings").item(0);
		ScreenSettings screenSettings = prepareScreenSettings(screenSettingsNode);
		applicationSettings.setScreenSettings(screenSettings);
		
		return applicationSettings;
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
		inputParameters.setSoftIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("soft_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setHardIdentityCutoff(Float.parseFloat(defaultInputParameterElement.getElementsByTagName("hard_identity_cutoff").item(0).getFirstChild().getNodeValue()));
		inputParameters.setSearchMode(defaultInputParameterElement.getElementsByTagName("search_mode").item(0).getFirstChild().getNodeValue());
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
