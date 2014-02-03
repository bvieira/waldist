package br.com.waldist.dao;

import org.neo4j.graphdb.GraphDatabaseService;


public class Neo4jTransactionManager implements TransactionManager
{
	private final ThreadLocal<Transaction> local;
	private final GraphDatabaseService database;

	Neo4jTransactionManager(final DatabaseManager database)
	{
		this.database = database.get();
		local = new ThreadLocal<Transaction>();
	}

	@Override
	public Transaction get()
	{
		Transaction t = local.get();
		if(t == null)
		{
			t =  new Neo4jTransaction(database);
			local.set(t);
		}
		return t;
	}

	@Override
	public void remove()
	{
		local.remove();
	}

}
