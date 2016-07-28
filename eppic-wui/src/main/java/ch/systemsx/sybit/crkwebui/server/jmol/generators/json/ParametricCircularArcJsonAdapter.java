package ch.systemsx.sybit.crkwebui.server.jmol.generators.json;

import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eppic.assembly.ParametricCircularArc;

public class ParametricCircularArcJsonAdapter implements JsonSerializer<ParametricCircularArc>{
	//private static final Logger logger = LoggerFactory.getLogger(ChainVertex3DJsonAdapter.class);
	@Override
	public JsonObject serialize(ParametricCircularArc src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();

		json.addProperty("radius", src.getRadius());
		json.addProperty("startAngle", src.getStartAngle());
		json.addProperty("endAngle", src.getEndAngle());
		json.add("start", context.serialize(src.getStart()));
		json.add("mid", context.serialize(src.getMid()));
		json.add("end", context.serialize(src.getEnd()));
		json.addProperty("uniqueName", src.getUniqueName());

		return json;
	}
}
