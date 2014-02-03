package br.com.waldist.dao;

public interface Transaction
{
	Transaction begin();

	void commit();

	void rollback();
}
