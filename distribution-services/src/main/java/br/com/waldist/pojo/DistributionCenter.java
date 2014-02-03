package br.com.waldist.pojo;

import com.google.gson.annotations.Expose;

public class DistributionCenter
{
	@Expose(serialize = false, deserialize = false)
	private final String id;
	private final String name;
	private final String mapName;
	
	public DistributionCenter(final String mapName, final String name)
	{
		this.id = new StringBuilder(mapName).append("-").append(name).toString();
		this.mapName = mapName;
		this.name = name;
	}
	
	public String id()
	{
		return id;
	}
	
	public String name()
	{
		return name;
	}
	
	public String mapName()
	{
		return mapName;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("DistributionCenter [id=").append(id).append(", name=").append(name).append(", mapName=").append(mapName).append("]").toString();
	}
}
