package br.com.waldist.servlet;

import static br.com.waldist.commons.Property.ENCODING;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.waldist.commons.JsonHandler;
import br.com.waldist.commons.Utils;
import br.com.waldist.exception.InvalidFormatException;
import br.com.waldist.services.DistributionMapQueue.Content;
import br.com.waldist.services.DistributionServices;
import br.com.waldist.services.Services;

public class DistributionServlet extends HttpServlet
{
	private static final long serialVersionUID = -8991056015643472426L;
	private final Logger logger;
	private final DistributionServices distributionServices;
	private final JsonHandler jsonHandler;
	
	public DistributionServlet()
	{
		logger = LoggerFactory.getLogger(getClass());
		distributionServices = Services.distributionservices();
		jsonHandler = new JsonHandler();
	}
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
	{
		get(req, resp);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
	{
		add(req, resp);
	}

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
	{
		add(req, resp);
	}

	@Override
	protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
	{
		remove(req, resp);
	}
	
	
	private void get(final HttpServletRequest req, final HttpServletResponse resp)
	{
		final String mapName = req.getPathInfo().startsWith("/") ? req.getPathInfo().substring(1) : req.getPathInfo();
		try
		{
			createResponse(resp, Utils.isEmpty(mapName) ? jsonHandler.get(distributionServices.listMaps()) : jsonHandler.get(distributionServices.get(mapName)));
		} catch(Throwable t)
		{
			logger.error("check this...", t);
			createResponseError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new StringBuilder("error on get, mapName:[").append(mapName).append("], exception message:[").append(t.getMessage()).append("]").toString());
		}
		
	}
	
	private void add(final HttpServletRequest req, final HttpServletResponse resp)
	{
		final String mapName = req.getPathInfo().startsWith("/") ? req.getPathInfo().substring(1) : req.getPathInfo();
		try
		{
			distributionServices.addToQueue(new Content(mapName, new String(Utils.read(req.getInputStream()), ENCODING.value())));
		} catch(InvalidFormatException e)
		{
			createResponseError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch(IllegalArgumentException e)
		{
			createResponseError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch(Throwable t)
		{
			logger.error("check this...", t);
			createResponseError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new StringBuilder("error on get, mapName:[").append(mapName).append("], exception message:[").append(t.getMessage()).append("]").toString());
		}
	}
	
	private void remove(final HttpServletRequest req, final HttpServletResponse resp)
	{
		final String mapName = req.getPathInfo().startsWith("/") ? req.getPathInfo().substring(1) : req.getPathInfo();
		try
		{
			if(Utils.isEmpty(mapName))
				distributionServices.removeAll();
			else
				distributionServices.remove(mapName);
		} catch(Throwable t)
		{
			logger.error("check this...", t);
			createResponseError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new StringBuilder("error on get, mapName:[").append(mapName).append("], exception message:[").append(t.getMessage()).append("]").toString());
		}
	}
	
	private void createResponse(final HttpServletResponse resp, final String json)
	{
		if(json != null)
			Utils.writeResponse(resp, "application/json", json);
		else
			createResponseError(resp, HttpServletResponse.SC_NOT_FOUND, null);
	}
	
	private void createResponseError(final HttpServletResponse resp, final int error, final String message)
	{
		if(message != null)
			logger.error(message);
		resp.setStatus(error);
	}
}
