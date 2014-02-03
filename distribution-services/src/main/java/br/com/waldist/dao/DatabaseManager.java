package br.com.waldist.dao;

import static br.com.waldist.dao.DatabaseManager.DatasourceProperty.PATH;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.waldist.exception.DatabaseException;
import br.com.waldist.exception.MultipleEntriesException;


public class DatabaseManager implements DatabaseServices
{
	private static final String INDEX_PROPERTY = "index"; 
	private final GraphDatabaseService database;
	private final GlobalGraphOperations globalOperations;
	private final Logger logger;
	
	public DatabaseManager()
	{
		this.logger = LoggerFactory.getLogger(getClass());
		database = new GraphDatabaseFactory().newEmbeddedDatabase(PATH.getValue());
		globalOperations = GlobalGraphOperations.at(database);
	}
	
	GraphDatabaseService get()
	{
		return database;
	}
	
	/**
	 * lista o conteudo da chave para todos os nós presentes do índice
	 * @param indexInfo {@link IndexInfo}
	 * @return lista com o conteudo do nó cadastrado na chave presente no {@link IndexInfo}
	 */
	List<String> listNodeContent(final IndexInfo indexInfo)
	{
		final List<String> results = new LinkedList<String>();
		for(Node node : database.index().forNodes(indexInfo.index().name()).query(indexInfo.key(), indexInfo.value()))
			results.add(String.valueOf(node.getProperty(indexInfo.key(), "")));
		return results;
	}
	
	/**
	 * apaga todo o conteúdo da base de dados
	 */
	void clear()
	{
		for(Index index : Index.values())
			database.index().forNodes(index.name()).delete();
		for(Relationship relationship : globalOperations.getAllRelationships())
			relationship.delete();
		for(Node node : globalOperations.getAllNodes())
			node.delete();
	}
	
	/**
	 * obtém o no de acordo com o que for enviado pelo {@link IndexInfo}
	 * @param info {@link IndexInfo} informações para busca do conteúdo
	 * @return nó da base que atende os requisitos do {@link IndexInfo}
	 */
	Node get(final IndexInfo info)
	{
		try
		{
			return database.index().forNodes(info.index().name()).get(info.key(), info.value()).getSingle();
		}catch(NoSuchElementException e)
		{
			throw new DatabaseException(e.getMessage(), e);
		}
	}
	
	/**
	 * lista os nós de acorco com o {@link IndexInfo}
	 * @param indexInfo informações para a busca do conteúdo
	 * @return lista com os nós da base que atende os requisitos do {@link IndexInfo}
	 */
	List<Node> list(final IndexInfo indexInfo)
	{
		final List<Node> nodes = new LinkedList<Node>();
		for(Node node : database.index().forNodes(indexInfo.index().name()).get(indexInfo.key(), indexInfo.value()))
			nodes.add(node);
		return nodes;
	}
	
	/**
	 * Remove o nó do índice que ele pertence
	 * @param node
	 */
	void removeFromIndex(final Node node)
	{
		database.index().forNodes(String.valueOf(node.getProperty(INDEX_PROPERTY))).remove(node);
	}
	
	/**
	 * encontra o menor custo entre a origem e o destino.
	 * @param start {@link IndexInfo} para encontrar o nó de origem
	 * @param end {@link IndexInfo} para encontrar o nó de destino
	 * @param type {@link RelationshipType} tipo de relação que deve ser considerado durante a busca
	 * @param direction {@link Direction} direção da relação entre os nós
	 * @param costKey chave que está presente nas {@link Relationship} e que representa o custo
	 * @return lista de {@link Relationship} com o menor custo para chegar da origem ao destino
	 */
	List<Relationship> findSmallestPath(final IndexInfo start, final IndexInfo end, final RelationshipType type, final Direction direction, final String costKey)
	{
		final PathFinder<WeightedPath> pathFinder = GraphAlgoFactory.dijkstra(Traversal.expanderForTypes(type, direction), costKey);
		final List<Relationship> relationships = new LinkedList<Relationship>();
		for(Relationship relationship : pathFinder.findSinglePath(getNode(start), getNode(end)).relationships())
			relationships.add(relationship);
		return relationships;
	}
	
	Node createNode()
	{
		return database.createNode();
	}
	
	Node getNode(final IndexInfo indexInfo)
	{
		try
		{
			return database.index().forNodes(indexInfo.index().name()).get(indexInfo.key(), indexInfo.value()).getSingle();
		}catch(NoSuchElementException e)
		{
			throw new DatabaseException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * indexa o nó de acordo com o {@link IndexInfo}
	 * @param node nó que será indexado
	 * @param info {@link IndexInfo}
	 */
	void index(final Node node, final IndexInfo info)
	{
		node.setProperty(INDEX_PROPERTY, info.index().name());
		final org.neo4j.graphdb.index.Index<Node> index = database.index().forNodes(info.index().name()); 
		for(IndexEntry entry : info.entries())
			index.putIfAbsent(node, entry.key(), entry.value());
	}
	
	static class IndexInfo
	{
		private final Index index;
		private final IndexEntry[] entries;
		
		IndexInfo(final Index index, final String key, final String value)
		{
			this.index = index;
			this.entries = new IndexEntry[]{new IndexEntry(key, value)};
		}
		
		IndexInfo(final Index index, final IndexEntry... entries)
		{
			this.index = index;
			this.entries = entries;
		}
		
		Index index()
		{
			return index;
		}
		
		IndexEntry[] entries()
		{
			return entries;
		}
		
		String key()
		{
			if(entries.length > 1)
				throw new MultipleEntriesException(entries.length);
			return entries[0].key();
		}
		
		String value()
		{
			if(entries.length > 1)
				throw new MultipleEntriesException(entries.length); 
			return entries[0].value();
		}
		
	}
	
	static class IndexEntry
	{
		private final String key, value;
		public IndexEntry(final String key, final String value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String key()
		{
			return key;
		}
		
		public String value()
		{
			return value;
		}
	}
	
	enum Index
	{
		DISTRIBUTION_CENTER, MAPS;
	}
	
	enum RelationshipType implements org.neo4j.graphdb.RelationshipType
	{
		PATH, CONTAINS;
	}
	
	
	enum DatasourceProperty
	{
		PATH("datasource.path", "/tmp/waldist/database/neo4j-store");
		
		final String property;
		final String defaultValue;
		DatasourceProperty(final String property, final String defaultValue)
		{
			this.property = property;
			this.defaultValue = defaultValue;
		}
		
		public String getValue()
		{
			return System.getProperty(getProperty(), getDefaultValue());
		}
		
		public String getProperty()
		{
			return property;
		}
		
		public String getDefaultValue()
		{
			return defaultValue;
		}
	}


	//------------service
	@Override
	public void start()
	{
		logger.info("starting database...");
	}

	@Override
	public void stop()
	{
		logger.info("shutdown database...");
		database.shutdown();
	}
}
