package br.com.waldist.commons;

import static br.com.waldist.commons.Property.ENCODING;
import static br.com.waldist.commons.Property.FILE_READ_BLOCK_SIZE;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

public class Utils
{
	public static boolean isBlank(final String value)
	{
		return value == null || value.trim().isEmpty();
	}
	
	public static boolean isEmpty(final String value)
	{
		return value == null || value.isEmpty();
	}
	
	public static float toFloat(final String value)
	{
		try
		{
			return Float.valueOf(value);
		}catch(Throwable t)
		{
			throw new InvalidParameterException(new StringBuilder("cannot convert value:[").append(value).append("] to float").toString());
		}
	}
	
	public static byte[] read(final InputStream input)
	{
		if(input == null)
			return null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			final byte[] content = new byte[FILE_READ_BLOCK_SIZE.asInt()];
			int read = 0;
			while((read = input.read(content)) >= 0)
				baos.write(content, 0, read);
			return baos.toByteArray();
		}catch(Exception e)
		{
			LoggerFactory.getLogger(Utils.class).error("Cannot read inputstream [{}]", input, e);
			return null;
		}finally
		{
			close(baos);
		}
	}
	
	private static void close(final Closeable closeable)
	{
		try
		{
			if (closeable != null)
				closeable.close();
		} catch (IOException e)
		{
		}
	}
	
	public static void writeResponse(final HttpServletResponse resp, final String contentType, final String result)
	{
		try
		{
			final byte[] content = result.getBytes(ENCODING.value());
			resp.setHeader("Content-Type", new StringBuilder(contentType).append("; charset=").append(ENCODING.value()).toString());
			resp.setContentLength(content.length);
			resp.getOutputStream().write(content);
			resp.flushBuffer();
		} catch (IOException e)
		{
			LoggerFactory.getLogger(Utils.class).error("could not write on http response", e);
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
