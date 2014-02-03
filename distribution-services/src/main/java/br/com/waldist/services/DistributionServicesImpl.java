package br.com.waldist.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.com.waldist.commons.Utils;
import br.com.waldist.dao.DistributionMapDAO;
import br.com.waldist.exception.InvalidFormatException;
import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Path;
import br.com.waldist.pojo.Route;
import br.com.waldist.services.DistributionMapQueue.Content;

public class DistributionServicesImpl implements DistributionServices
{
	private final DistributionMapDAO distributionMapDAO;
	private final DistributionMapQueue queue;
	
	public DistributionServicesImpl(final DistributionMapDAO distributionMapDAO, final DistributionMapQueue queue)
	{
		this.distributionMapDAO = distributionMapDAO;
		this.queue = queue;
	}
	
	@Override
	public List<String> listMaps()
	{
		return distributionMapDAO.listMapNames();
	}
	
	@Override
	public DistributionMap get(final String mapName)
	{
		return distributionMapDAO.get(mapName);
	}
	
	@Override
	public void addToQueue(final Content content)
	{
		queue.add(content);
	}

	@Override
	public void add(final Content content)
	{
		if(content == null || Utils.isEmpty(content.mapName()))
			throw new IllegalArgumentException("distribution map name is missing");
		distributionMapDAO.save(parse(content.mapName(), content.content()));
	}
	
	@Override
	public void removeAll()
	{
		distributionMapDAO.remove();
	}
	
	@Override
	public void remove(final String mapName)
	{
		if(Utils.isEmpty(mapName))
			throw new IllegalArgumentException("distribution map name is missing");
		distributionMapDAO.remove(mapName);
	}
	
	@Override
	public Route search(final DistributionCenter start, final DistributionCenter end, final float price, final float performance)
	{
		final List<Path> paths = distributionMapDAO.getSmallestRoute(start, end);
		if(paths.isEmpty())
			return null;
		final float distance = calculateDistance(paths);
		return new Route(getDistributionCenters(paths), distance, calculatePrice(distance, price, performance));
	}
	
	/**
	 * lista todos os {@link DistributionCenter} presentes no conjunto de {@link Path}
	 * @param paths conjuntos de caminhos
	 * @return {@link DistributionCenter} presentes no conjunto de {@link Path}
	 */
	private List<DistributionCenter> getDistributionCenters(final List<Path> paths)
	{
		if(paths.isEmpty())
			return Collections.emptyList();
		final List<DistributionCenter> distributionCenters = new LinkedList<DistributionCenter>();
		distributionCenters.add(paths.get(0).start());
		for(Path path : paths)
			distributionCenters.add(path.end());
		return distributionCenters;
	}
	
	private float calculateDistance(final List<Path> paths)
	{
		float distance = 0;
		for(Path path : paths)
			distance += path.distance();
		return distance;
		
	}
	
	private float calculatePrice(final float distance, final float price, final float performance)
	{
		if(performance == 0)
			throw new IllegalArgumentException("performance is 0");
		return distance / performance * price;
	}
	
	DistributionMap parse(final String mapName, final String content)
	{
		if(Utils.isBlank(content))
			throw new IllegalArgumentException("content is empty");
		final List<Path> routes = new LinkedList<Path>();
		for(String line : content.split("\r?\n"))
		{
			if(line.isEmpty())
				continue;
			
			final String[] values = line.split("\\p{Blank}+");
			if(values.length != 3)
				throw new InvalidFormatException(new StringBuilder("invalid line content:[").append(line).append("] for map:[").append(mapName).append("]").toString());
			routes.add(new Path(new DistributionCenter(mapName, values[0]), new DistributionCenter(mapName, values[1]), Utils.toFloat(values[2])));
		}
		return new DistributionMap(mapName, routes);
	}
}
