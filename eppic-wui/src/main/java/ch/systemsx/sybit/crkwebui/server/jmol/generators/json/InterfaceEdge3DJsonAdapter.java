package ch.systemsx.sybit.crkwebui.server.jmol.generators.json;

import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eppic.assembly.InterfaceEdge3D;

public class InterfaceEdge3DJsonAdapter implements JsonSerializer<InterfaceEdge3D>{
	//private static final Logger logger = LoggerFactory.getLogger(ChainVertex3DJsonAdapter.class);
	@Override
	public JsonObject serialize(InterfaceEdge3D src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("interfaceId", src.getInterfaceId());
		json.addProperty("clusterId", src.getClusterId());
		json.addProperty("xtalTrans", src.getXtalTransString());
		json.addProperty("name", src.getUniqueName());
		json.addProperty("color", src.getColorStr());
		json.add("circles", context.serialize(src.getCircles()));
		json.add("segments", context.serialize(src.getSegments()));
		return json;
	}
}
