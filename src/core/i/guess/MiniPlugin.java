package core.i.guess;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import core.i.guess.command.CommandCenter;
import core.i.guess.command.ICommand;
import core.i.guess.common.F;
import core.i.guess.common.UtilServer;
import core.i.guess.common.UtilTime;
import core.i.guess.common.UtilTime.TimeUnit;
import core.i.guess.lifetimes.Lifetime;
import core.i.guess.lifetimes.Lifetimed;
import core.i.guess.lifetimes.SimpleLifetime;
import core.i.guess.thread.ThreadPool;

public abstract class MiniPlugin implements Listener, Lifetimed
{
	
	// As MiniPlugins can technically be disabled at any time, each
		// has its own unique Lifetime.  If MiniPlugins are declared
		// to never be able to be disabled, then a "Singleton" Lifetime
		// could be shared between all of them.
		private final SimpleLifetime _lifetime = new SimpleLifetime();
		protected String _moduleName = "Unknown Module";
		protected JavaPlugin _plugin;

		protected long _initializedTime;

		public MiniPlugin(String moduleName)
		{
			this(moduleName, UtilServer.getPlugin());
		}

		public MiniPlugin(String moduleName, JavaPlugin plugin)
		{
			_moduleName = moduleName;
			_plugin = plugin;

			_initializedTime = System.currentTimeMillis();

			_lifetime.start();
	        onEnable();
	        
	        registerEvents(this);

			Managers.put(this);
		}

		public PluginManager getPluginManager()
		{
			return _plugin.getServer().getPluginManager();
		}

		public BukkitScheduler getScheduler()
		{
			return _plugin.getServer().getScheduler();
		}

		public JavaPlugin getPlugin()
		{
			return _plugin;
		}

		public void registerEvents(Listener listener)
		{
			_plugin.getServer().getPluginManager().registerEvents(listener, _plugin);
		}

		public void registerSelf()
		{
			registerEvents(this);
		}

		public void deregisterSelf()
		{
			HandlerList.unregisterAll(this);
		}

		public final void onEnable()
		{
			long epoch = System.currentTimeMillis();
			log("Booting module..");
			enable();
			addCommands();
			log("Module booted in " + UtilTime.convertString(System.currentTimeMillis() - epoch, 1, TimeUnit.FIT) + ".");
		}

		public final void onDisable()
		{
			disable();

			log("Disabled.");
		}

		public void enable()
		{
		}

		public void disable()
		{
		}

		public void addCommands()
		{
		}

		public final String getName()
		{
			return _moduleName;
		}

		public final void addCommand(ICommand command)
		{
			CommandCenter.Instance.addCommand(command);
		}

		public final void removeCommand(ICommand command)
		{
			CommandCenter.Instance.removeCommand(command);
		}

		public void log(String message)
		{
			Bukkit.getConsoleSender().sendMessage(F.main(_moduleName, message));
		}

		public void runAsync(Runnable runnable)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			ThreadPool.ASYNC.execute(() ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing module task", exception);
				}
			});
		}

		public BukkitTask runAsync(Runnable runnable, long time)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			return _plugin.getServer().getScheduler().runTaskLaterAsynchronously(_plugin, () ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing MiniPlugin task", exception);
				}
			}, time);
		}

		public BukkitTask runAsyncTimer(Runnable runnable, long time, long period)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			return _plugin.getServer().getScheduler().runTaskTimerAsynchronously(_plugin, () ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing module task", exception);
				}
			}, time, period);
		}

		public BukkitTask runSync(Runnable runnable)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			return _plugin.getServer().getScheduler().runTask(_plugin, () ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing module task", exception);
				}
			});
		}

		public BukkitTask runSyncLater(Runnable runnable, long delay)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			return _plugin.getServer().getScheduler().runTaskLater(_plugin, () ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing module task", exception);
				}
			}, delay);
		}

		public BukkitTask runSyncLater(BukkitRunnable runnable, long delay)
		{
			return runnable.runTaskLater(_plugin, delay);
		}

		public BukkitTask runSyncTimer(Runnable runnable, long delay, long period)
		{
			Exception exception = new Exception();
			exception.fillInStackTrace();
			return _plugin.getServer().getScheduler().runTaskTimer(_plugin, () ->
			{
				try
				{
					runnable.run();
				}
				catch (Throwable t)
				{
					exception.initCause(t);
					throw new RuntimeException("Exception while executing module task", exception);
				}
			}, delay, period);
		}

		public BukkitTask runSyncTimer(BukkitRunnable runnable, long delay, long period)
		{
			return runnable.runTaskTimer(_plugin, delay, period);
		}

		protected <T extends MiniPlugin> T require(Class<T> clazz)
		{
			return Managers.require(clazz);
		}

		@Override
		public Lifetime getLifetime()
		{
			return _lifetime;
		}

}
