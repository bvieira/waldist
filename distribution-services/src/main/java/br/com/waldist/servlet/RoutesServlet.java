package br.com.waldist.servlet;
import static br.com.waldist.servlet.RoutesServlet.SearchKey.END;
import static br.com.waldist.servlet.RoutesServlet.SearchKey.MAP;
import static br.com.waldist.servlet.RoutesServlet.SearchKey.PERFORMANCE;
import static br.com.waldist.servlet.RoutesServlet.SearchKey.PRICE;
import static br.com.waldist.servlet.RoutesServlet.SearchKey.START;

import java.security.InvalidParameterException;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.waldist.commons.JsonHandler;
import br.com.waldist.commons.Utils;
import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.services.DistributionServices;
import br.com.waldist.services.Services;

public class RoutesServlet extends HttpServlet
{
	private static final long serialVersionUID = 396645669108350032L;
	private final Logger logger;
	private final DistributionServices distributionServices;
	private final JsonHandler jsonHandler;
	
	public RoutesServlet()
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
	
	@SuppressWarnings("unchecked")
	private void get(HttpServletRequest req, HttpServletResponse resp)
	{
		//routes?map=[nome da malha]&start=[origem]&end=[destino]&performance=[autonomia em km/l]&price=[preço da gasolina]
		try
		{
			validate(req.getParameterMap().keySet());
			createResponse(resp, jsonHandler.get(distributionServices.search(create(req.getParameter(MAP.key()), req.getParameter(START.key())), create(req.getParameter(MAP.key()), req.getParameter(END.key())), Utils.toFloat(req.getParameter(PRICE.key())), Utils.toFloat(req.getParameter(PERFORMANCE.key())))));
		}catch(InvalidParameterException e)
		{
			createResponseError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}catch(IllegalArgumentException e)
		{
			createResponseError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}catch(Throwable t)
		{
			logger.error("check this...", t);
			createResponseError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}
	
	private void validate(final Set<String> parameters)
	{
		for(SearchKey key : SearchKey.values())
			if(!parameters.contains(key.key()))
				throw new IllegalArgumentException(new StringBuilder("missing argument:[").append(key.key()).append("]").toString());
	}
	
	private DistributionCenter create(final String mapName, final String name)
	{
		if(Utils.isBlank(mapName) || Utils.isBlank(name))
			throw new IllegalArgumentException("mapName or name for distribution center is missing");
		return new DistributionCenter(mapName, name);
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
	
	enum SearchKey
	{
		MAP, START, END, PERFORMANCE, PRICE;
		
		String key()
		{
			return name().toLowerCase();
		}
	}
}