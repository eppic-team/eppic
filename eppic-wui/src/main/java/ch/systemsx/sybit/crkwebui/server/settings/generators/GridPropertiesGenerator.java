package ch.systemsx.sybit.crkwebui.server.settings.generators;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GridPropertiesGenerator
{
	/**
	 * Generates grid settings.
	 * @param gridPropertiesStream input stream containing grid properties
	 */
	public static Map<String, String> prepareGridProperties(InputStream gridPropertiesStream) throws IOException
	{
		Properties gridProperties = new Properties();
		gridProperties.load(gridPropertiesStream);

		Map<String, String> gridPropetiesMap = new HashMap<String, String>();
		for (Object key : gridProperties.keySet())
		{
			gridPropetiesMap.put((String) key, (String) gridProperties.get(key));
		}
		
		return gridPropetiesMap;
	}
}
