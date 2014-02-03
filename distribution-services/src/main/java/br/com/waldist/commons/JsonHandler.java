package br.com.waldist.commons;

import java.util.List;

import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Path;
import br.com.waldist.pojo.Route;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
/**
 * reponsável pela conversão dos objetos para formato json 
 * @author bvieira
 *
 */
public class JsonHandler
{
	private final Gson gson;
	
	public JsonHandler()
	{
		gson = new Gson();
	}
	
	public String get(final List<String> values)
	{
		return values == null ? null : gson.toJson(values);
	}
	
	public String get(final Route route)
	{
		if(route == null)
			return null;
		final JsonObject json = new JsonObject();
		json.add("route", getDistributionCentersJson(route.distributionCenters()));
		json.addProperty("distance", route.distance());
		json.addProperty("price", route.price());
		return json.toString();
	}
	
	public String get(final DistributionMap distributionMap)
	{
		if(distributionMap == null)
			return null;
		final JsonObject json = new JsonObject();
		json.addProperty("map", distributionMap.name());
		json.add("paths", getPathsJson(distributionMap.paths()));
		return json.toString();
	}
	
	private JsonElement getPathsJson(final List<Path> paths)
	{
		if(paths == null)
			return null;
		final JsonArray json = new JsonArray();
		for(Path path : paths)
			json.add(getJson(path));
		return json;
	}
	
	private JsonElement getDistributionCentersJson(final List<DistributionCenter> distributionCenters)
	{
		if(distributionCenters == null)
			return null;
		final JsonArray json = new JsonArray();
		for(DistributionCenter distributionCenter : distributionCenters)
			json.add(new JsonPrimitive(distributionCenter.name()));
		return json;
	}
	
	private JsonElement getJson(final Path path)
	{
		if(path == null)
			return null;
		final JsonObject json = new JsonObject();
		json.addProperty("start", path.start().name());
		json.addProperty("end", path.end().name());
		json.addProperty("distance", path.distance());
		return json;
	}
}
