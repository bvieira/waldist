package br.com.waldist.exception;

public class InvalidFormatException extends RuntimeException
{
	private static final long serialVersionUID = -820316845171935948L;

	public InvalidFormatException(final String msg)
	{
		super(msg);
	}
}
