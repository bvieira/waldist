package br.com.waldist.exception;

public class MultipleEntriesException extends RuntimeException
{
	private static final long serialVersionUID = -6411292130094781688L;

	public MultipleEntriesException(final int count)
	{
		super(new StringBuilder("multiple entries, count:[").append(count).append("]").toString());
	}
}
