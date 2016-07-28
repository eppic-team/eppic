package ch.systemsx.sybit.crkwebui.server.jmol.generators.json;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eppic.assembly.ChainVertex3D;

public class ChainVertex3DJsonAdapter implements JsonSerializer<ChainVertex3D>{
	//private static final Logger logger = LoggerFactory.getLogger(ChainVertex3DJsonAdapter.class);
	@Override
	public JsonElement serialize(ChainVertex3D src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("opId", src.getOpId());
		json.addProperty("chainId", src.getChainId());
		json.addProperty("color",src.getColorStr());
		json.add("center",context.serialize(src.getCenter()));
		json.addProperty("uniqueName",src.getUniqueName());
		json.addProperty("label", src.toString());
		return json;
	}
}
