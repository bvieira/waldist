package br.com.waldist.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.com.waldist.services.Services;

public class WalDistServletContextListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent arg0)
	{
		Services.databaseServices().start();
	}


	@Override
	public void contextDestroyed(ServletContextEvent arg0)
	{
		Services.databaseServices().stop();
	}
}
