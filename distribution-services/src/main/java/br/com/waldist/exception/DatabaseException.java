package br.com.waldist.exception;

public class DatabaseException extends RuntimeException
{
	private static final long serialVersionUID = 4076709852408334730L;

	public DatabaseException(final String msg, final Throwable t)
	{
		super(msg, t);
	}
}
