package br.com.waldist.pojo;

public class Path
{
	private final float distance;
	private final DistributionCenter start;
	private final DistributionCenter end;
	
	public Path(final DistributionCenter start, final DistributionCenter end, final float distance)
	{
		this.start = start;
		this.end = end;
		this.distance = distance;
	}
	
	public DistributionCenter start()
	{
		return start;
	}
	
	public DistributionCenter end()
	{
		return end;
	}
	
	public float distance()
	{
		return distance;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder("Path [start=").append(start).append(", end=").append(end).append(", distance=").append(distance).append("]").toString();
	}
}
