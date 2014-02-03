package br.com.waldist.pojo;

import java.util.List;

public class Route
{
	private final List<DistributionCenter> distributionCenters;
	private final float distance;
	private final float price;
	
	public Route(final List<DistributionCenter> distributionCenters, final float distance, final float price)
	{
		this.distributionCenters = distributionCenters;
		this.distance = distance;
		this.price = price;
	}
	
	public List<DistributionCenter> distributionCenters()
	{
		return distributionCenters;
	}
	
	public float distance()
	{
		return distance;
	}
	
	public float price()
	{
		return price;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("Route [distributionCenters=").append(distributionCenters).append(", distance=").append(distance).append(", price=").append(price).append("]").toString();
	}

	
}
