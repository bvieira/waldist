package br.com.waldist.dao;
import static br.com.waldist.dao.DatabaseManager.Index.DISTRIBUTION_CENTER;
import static br.com.waldist.dao.DatabaseManager.Index.MAPS;
import static br.com.waldist.dao.DatabaseManager.RelationshipType.CONTAINS;
import static br.com.waldist.dao.DatabaseManager.RelationshipType.PATH;
import static br.com.waldist.dao.DistributionMapDAOImpl.PropertyKey.DISTANCE;
import static br.com.waldist.dao.DistributionMapDAOImpl.PropertyKey.ID;
import static br.com.waldist.dao.DistributionMapDAOImpl.PropertyKey.MAP;
import static br.com.waldist.dao.DistributionMapDAOImpl.PropertyKey.NAME;

import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import br.com.waldist.dao.DatabaseManager.IndexEntry;
import br.com.waldist.dao.DatabaseManager.IndexInfo;
import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Path;

public class DistributionMapDAOImpl implements DistributionMapDAO
{
	private final DatabaseManager db;
	private final TransactionManager transactionManager;
	
	public DistributionMapDAOImpl(final DatabaseManager db)
	{
		this.db = db;
		this.transactionManager = new Neo4jTransactionManager(db);
	}

	@Override
	public List<String> listMapNames()
	{
		final Transaction tx = transactionManager.get().begin();
		List<String> mapNames = null;
		try
		{
			mapNames = db.listNodeContent(new IndexInfo(MAPS, NAME.key(), "*"));
			tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}
		return mapNames;
	}

	@Override
	public void remove()
	{
		final Transaction tx = transactionManager.get().begin();
		try
		{
			db.clear();
			tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}
		
	}

	@Override
	public DistributionMap get(String mapName)
	{
		final Transaction tx = transactionManager.get().begin();
		DistributionMap distributionMap = null;
		try
		{
			final Node node = db.get(new IndexInfo(MAPS, NAME.key(), mapName));
			distributionMap = node == null ? null : toDistributionMap(node);
		tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}
		return distributionMap;
	}

	@Override
	public void save(final DistributionMap distributionMap)
	{
		final Transaction tx = transactionManager.get().begin();
		try
		{
			Node node = db.get(new IndexInfo(MAPS, NAME.key(), distributionMap.name()));
			if(node != null)
				removeDistributionMap(distributionMap.name());
			node = db.createNode();
			node.setProperty(NAME.key(), distributionMap.name());
			db.index(node, new IndexInfo(MAPS, NAME.key(), distributionMap.name()));
				
			for(Path path : distributionMap.paths())
				save(node, path);
		tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}

	}
	
	@Override
	public void remove(String mapName)
	{
		final Transaction tx = transactionManager.get().begin();
		try
		{
			removeDistributionMap(mapName);
			tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}
	}
	
	private void removeDistributionMap(final String mapName)
	{
		final Node map = db.get(new IndexInfo(MAPS, NAME.key(), mapName));
		for(Relationship relationship : map.getRelationships(CONTAINS))
			remove(relationship.getEndNode());
		remove(map);
	}
	
	private void remove(final Node node)
	{
		for(Relationship relationship : node.getRelationships())
			relationship.delete();
		db.removeFromIndex(node);
		node.delete();
	}

	@Override
	public List<Path> getSmallestRoute(final DistributionCenter start, final DistributionCenter end)
	{
		final List<Path> paths = new LinkedList<Path>();
		final Transaction tx = transactionManager.get().begin();
		try
		{
			for(Relationship relationship : db.findSmallestPath(new IndexInfo(DISTRIBUTION_CENTER, ID.key(), start.id()), new IndexInfo(DISTRIBUTION_CENTER, ID.key(), end.id()), PATH, Direction.OUTGOING, DISTANCE.key()))
				paths.add(new Path(toDistributionCenter(relationship.getStartNode()), toDistributionCenter(relationship.getEndNode()), (Float) relationship.getProperty(DISTANCE.key())));
		tx.commit();
		}catch(Throwable t)
		{
			tx.rollback();
		}
		return paths;
	}
	
	
	//----------converters
	private DistributionMap toDistributionMap(final Node node)
	{
		final List<Path> paths =  new LinkedList<Path>();
		for(Relationship relationship : node.getRelationships(CONTAINS))
			paths.addAll(toPaths(relationship.getEndNode()));
		return new DistributionMap(String.valueOf(node.getProperty(NAME.key())), paths);
	}
	
	private List<Path> toPaths(final Node node)
	{
		final List<Path> paths = new LinkedList<Path>();
		for(Relationship relationship : node.getRelationships(Direction.OUTGOING, PATH))
			paths.add(new Path(toDistributionCenter(relationship.getStartNode()), toDistributionCenter(relationship.getEndNode()), (Float) relationship.getProperty(DISTANCE.key())));
		return paths;
	}
	
	private DistributionCenter toDistributionCenter(final Node node)
	{
		return new DistributionCenter(String.valueOf(node.getProperty(MAP.key())), String.valueOf(node.getProperty(NAME.key())));
	}
	
	//----------save
	private void save(final Node distributionMap, final Path path)
	{
		final Relationship relationship = save(distributionMap, path.start()).createRelationshipTo(save(distributionMap, path.end()), PATH);
		relationship.setProperty(DISTANCE.key(), path.distance());
	}
	
	private Node save(final Node distributionMap, final DistributionCenter distributionCenter)
	{
		Node node = db.get(new IndexInfo(DISTRIBUTION_CENTER, ID.key(), distributionCenter.id()));
		if(node != null)
			return node;
		
		node = db.createNode();
		node.setProperty(ID.key(), distributionCenter.id());
		node.setProperty(NAME.key(), distributionCenter.name());
		node.setProperty(MAP.key(), distributionCenter.mapName());
		db.index(node, new IndexInfo(DISTRIBUTION_CENTER, new IndexEntry(ID.key(), distributionCenter.id()), new IndexEntry(MAP.key(), distributionCenter.mapName())));
		distributionMap.createRelationshipTo(node, CONTAINS);
		return node;
	}

	
	//----------mapping
	enum PropertyKey
	{
		ID, NAME, DISTANCE, MAP;
		
		String key()
		{
			return this.name().toLowerCase();
		}
	}
}
