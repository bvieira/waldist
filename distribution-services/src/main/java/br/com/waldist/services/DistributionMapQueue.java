package br.com.waldist.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.waldist.exception.DatabaseException;

public class DistributionMapQueue
{
	private final ConcurrentMap<String, ThreadProcessor> channels;

	public DistributionMapQueue()
	{
		channels = new ConcurrentHashMap<String, ThreadProcessor>();
	}

	public void add(final Content content)
	{
		if(content != null && content.mapName() != null)
			getProcessor(content.mapName()).add(content);
	}

	private ThreadProcessor getProcessor(final String channel)
	{
		final ThreadProcessor processor = channels.get(channel);
		if(processor != null)
			return processor;

		final ThreadProcessor newProcessor = new ThreadProcessor();
		final ThreadProcessor mapProcessor = channels.putIfAbsent(channel, newProcessor);
		if(mapProcessor != null)
			return mapProcessor;
		
		new Thread(newProcessor, channel).start();
		return newProcessor;
	}

	private static class ThreadProcessor implements Runnable
	{
		private final BlockingQueue<Content> contents;
		private final Logger logger;

		public ThreadProcessor()
		{
			this.logger = LoggerFactory.getLogger(getClass());
			contents = new LinkedBlockingDeque<Content>();
		}

		@Override
		public void run()
		{
			while(true)
				try
				{
					execute(contents.take(), 3);
				}catch(InterruptedException e)
				{
					logger.error("interrupt thread error", e);
					break;
				}
		}

		private void execute(final Content content, final int retries)
		{
			if(retries == 0)
			{
				logger.error("unable to procces distribution map:[{}]", content.mapName());
				return ;
			}

			try
			{
				Services.distributionservices().add(content);
			}catch(DatabaseException e)
			{
				logger.info("database exception {}", e.getCause().getMessage());
				execute(content, retries - 1);
			}catch(Throwable t)
			{
				logger.error("event on add map:[{}]", content.mapName(), t);
			}
		}

		void add(final Content content)
		{
			contents.add(content);
		}

	}
	
	public static final class Content
	{
		private final String mapName;
		private final String content;
		
		public Content(final String mapName, final String content)
		{
			this.mapName = mapName;
			this.content = content;
		}
		
		public String mapName()
		{
			return mapName;
		}
		
		public String content()
		{
			return content;
		}
	}
}
