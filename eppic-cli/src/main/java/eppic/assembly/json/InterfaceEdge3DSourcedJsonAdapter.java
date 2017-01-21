package eppic.assembly.json;

import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.gui.InterfaceEdge3DSourced;

public class InterfaceEdge3DSourcedJsonAdapter implements JsonSerializer<InterfaceEdge3DSourced<? extends ChainVertex3D>>{
	private InterfaceEdge3DJsonAdapter adapter;
	public InterfaceEdge3DSourcedJsonAdapter() {
		adapter = new InterfaceEdge3DJsonAdapter();
	}
	@Override
	public JsonObject serialize(InterfaceEdge3DSourced<? extends ChainVertex3D> src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = adapter.serialize(src, typeOfSrc, context);
		json.addProperty("source",src.getSource().getUniqueName());
		json.addProperty("target",src.getTarget().getUniqueName());
		return json;
	}

}
