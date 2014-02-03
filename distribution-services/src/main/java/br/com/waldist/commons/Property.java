package br.com.waldist.commons;

public enum Property
{
	ENCODING("UTF-8"),
	FILE_READ_BLOCK_SIZE(8192);
	
	private final String value;
	private final int intValue;
	
	private Property(final String value)
	{
		this.value = value;
		this.intValue = 0;
	}
	
	private Property(final int value)
	{
		this.intValue = value;
		this.value = null;
	}
	
	public String value()
	{
		return value;
	}
	
	public int asInt()
	{
		return intValue;
	}

}
