package br.com.waldist.dao;

import org.neo4j.graphdb.GraphDatabaseService;

class Neo4jTransaction implements Transaction
{
	private final GraphDatabaseService graphDatabase;
	private org.neo4j.graphdb.Transaction transaction;

	Neo4jTransaction(final GraphDatabaseService graphDatabase)
	{
		this.graphDatabase = graphDatabase;
	}

	@Override
	public Transaction begin()
	{
		transaction = graphDatabase.beginTx();
		return this;
	}

	@Override
	public void commit()
	{
		transaction.success();
		transaction.finish();
	}

	@Override
	public void rollback()
	{
		transaction.failure();
		transaction.finish();
	}
}
