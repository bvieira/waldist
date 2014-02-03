package br.com.waldist.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.containers.TieringPicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.waldist.dao.DatabaseManager;
import br.com.waldist.dao.DatabaseServices;
import br.com.waldist.dao.DistributionMapDAOImpl;

public class Services
{
	private static final PicoContainer servicesContainer = createServicesContainer();
	
	private static final DistributionServices distributionServices = createService(DistributionServices.class);
	private static final DatabaseServices databaseServices = createService(DatabaseServices.class);
	
	private static PicoContainer createServicesContainer()
	{
		final DefaultPicoContainer daoContainer = new DefaultPicoContainer();
		daoContainer.as(Characteristics.CACHE).addComponent(DatabaseManager.class);
		daoContainer.as(Characteristics.CACHE).addComponent(DistributionMapDAOImpl.class);
		
		final TieringPicoContainer servicesContainer = new TieringPicoContainer(daoContainer);
		servicesContainer.as(Characteristics.CACHE).addComponent(DistributionServicesImpl.class);
		servicesContainer.as(Characteristics.CACHE).addComponent(DistributionMapQueue.class);
		return servicesContainer;
	}
	
	public static DistributionServices distributionservices()
	{
		return distributionServices;
	}
	
	public static DatabaseServices databaseServices()
	{
		return databaseServices;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T createService(final Class<T> clazz)
	{
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new ServiceInterceptor(servicesContainer.getComponent(clazz)));
	}
	
	private static class ServiceInterceptor implements InvocationHandler
	{
		private final Object obj;
		private final Logger logger;

		public ServiceInterceptor(Object obj)
		{
			logger = LoggerFactory.getLogger(getClass());
			this.obj = obj;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
		{
			final StringBuilder context = new StringBuilder(obj.getClass().getSimpleName()).append(".").append(method.getName());
			logger.debug(">>>> invoking {}", context);
			final long begin = System.currentTimeMillis();
			Object result = null;

			try
			{
				result = method.invoke(obj, args);
				logger.debug(">>>> done in {} {}", getDelta(begin), context);
			}catch(Throwable t)
			{
				logger.error(createExceptionLog(t, getDelta(begin), context), t);
				throw t;
			}
			return result;
		}
		
		private String createExceptionLog(final Throwable t, final long executionTime, final StringBuilder context)
		{
			return new StringBuilder(">>>> fail throwing ").append(t.getClass().getName()).append(" in ").append(executionTime).append("ms from ").append(context.toString()).append("\noriginal message:").append(t.getMessage()).toString();
		}

		private long getDelta(final long begin)
		{
			return System.currentTimeMillis() - begin;
		}
	}
}
