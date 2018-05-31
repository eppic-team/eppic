package eppic.assembly.json;

import java.lang.reflect.Type;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eppic.assembly.LatticeGraph3D;

public class LatticeGraph3DJson implements JsonSerializer<LatticeGraph3D>{
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph3DJson.class);
	@Override
	public JsonElement serialize(LatticeGraph3D src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.add("vertices", context.serialize(src.getGraph().vertexSet(),Set.class));
		json.add("edges", context.serialize(src.getGraph().edgeSet(),Set.class));
		json.add("unitCellTransforms",context.serialize(src.getUnitCellTransforms(), Set.class));

		return json;
	}
}
