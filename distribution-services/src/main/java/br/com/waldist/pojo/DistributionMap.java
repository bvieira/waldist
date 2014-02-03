package br.com.waldist.pojo;

import java.util.List;

public class DistributionMap
{
	private final String name;
	private final List<Path> paths;
	
	public DistributionMap(final String name, final List<Path> paths)
	{
		this.name = name;
		this.paths = paths;
	}
	
	public String name()
	{
		return name;
	}
	
	public List<Path> paths()
	{
		return paths;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("DistributionMap [name=").append(name).append(", paths=").append(paths).append("]").toString();
	}
}
