package br.com.waldist.services;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import br.com.waldist.dao.DistributionMapDAO;
import br.com.waldist.exception.InvalidFormatException;
import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Path;
import br.com.waldist.pojo.Route;

public class DistributionServicesTest
{
	@Mock
	private DistributionMapDAO distributionMapDAO;
	private final DistributionMapQueue queue = new DistributionMapQueue();
	
	@Before
	public void setUp() throws Exception
	{
		initMocks(this);
	}
	
	@Test
	public void testRouteSearch()
	{
		final String mapName = "map1";
		
		final List<DistributionCenter> distributionCenters = new LinkedList<DistributionCenter>();
		distributionCenters.add(createDistributionCenter(mapName, "A"));
		distributionCenters.add(createDistributionCenter(mapName, "B"));
		distributionCenters.add(createDistributionCenter(mapName, "C"));
		final Route resultRoute = new Route(distributionCenters, 15f, 3.75f);
		
		final DistributionCenter start = createDistributionCenter(mapName, "A");
		final DistributionCenter end = createDistributionCenter(mapName, "C");
		when(distributionMapDAO.getSmallestRoute(start, end)).thenReturn(createPaths(mapName));
		final Route route = new DistributionServicesImpl(distributionMapDAO, queue).search(start, end, 2.5f, 10f);
		assertTrue(route.toString().equals(resultRoute.toString()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRouteInvalidPerformanceSearch()
	{
		final String mapName = "map1";
		final DistributionCenter start = createDistributionCenter(mapName, "A");
		final DistributionCenter end = createDistributionCenter(mapName, "C");
		when(distributionMapDAO.getSmallestRoute(start, end)).thenReturn(createPaths(mapName));
		new DistributionServicesImpl(distributionMapDAO, queue).search(start, end, 2.5f, 0);
	}
	
	@Test
	public void testNoRouteSearch()
	{
		final String mapName = "map1";
		final DistributionCenter start = createDistributionCenter(mapName, "A");
		final DistributionCenter end = createDistributionCenter(mapName, "Z");
		when(distributionMapDAO.getSmallestRoute(start, end)).thenReturn(new LinkedList<Path>());
		assertNull(new DistributionServicesImpl(distributionMapDAO, queue).search(start, end, 2.5f, 10f));
	}
	
	@Test
	public void testParseContentSuccess()
	{
		final String mapName = "map1";
		
		final DistributionMap result = new DistributionMap(mapName, createPaths(mapName));
		final DistributionMap distributionMap = new DistributionServicesImpl(distributionMapDAO, queue).parse("map1", "A B 10\nB C 5");
		
		assertTrue(distributionMap.toString().equals(result.toString()));
	}
	
	@Test(expected = InvalidFormatException.class)
	public void testParseContentInvalidFormatError()
	{
		new DistributionServicesImpl(distributionMapDAO, queue).parse("map1", "A B 10\nB  5");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseContentIllegalArgumentError()
	{
		new DistributionServicesImpl(distributionMapDAO, queue).parse("map1", "A B 10\nB C D");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseContentIllegalArgumentError2()
	{
		new DistributionServicesImpl(distributionMapDAO, queue).parse("map1", "  ");
	}
	
	private DistributionCenter createDistributionCenter(final String mapName, final String name)
	{
		return new DistributionCenter(mapName, name);
	}
	
	private List<Path> createPaths(final String mapName)
	{
		final List<Path> paths = new LinkedList<Path>();
		paths.add(new Path(createDistributionCenter(mapName, "A"), createDistributionCenter(mapName, "B"), 10f));
		paths.add(new Path(createDistributionCenter(mapName, "B"), createDistributionCenter(mapName, "C"), 5f));
		return paths;
	}

}
