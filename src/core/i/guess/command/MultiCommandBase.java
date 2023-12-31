package core.i.guess.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import core.i.guess.MiniPlugin;
import core.i.guess.common.C;
import core.i.guess.common.NautHashMap;
import core.i.guess.permission.Permission;
import core.i.guess.permission.PermissionManager;

public abstract class MultiCommandBase<PluginType extends MiniPlugin> extends CommandBase<PluginType>
{
	private NautHashMap<String, ICommand> Commands;

	public MultiCommandBase(PluginType plugin, Permission permission, String... aliases)
	{
		super(plugin, permission, aliases);

		Commands = new NautHashMap<>();
	}

	public void AddCommand(ICommand command)
	{
		for (String commandRoot : command.Aliases())
		{
			Commands.put(commandRoot.toLowerCase(Locale.ENGLISH), command);
			command.SetCommandCenter(_commandCenter);
		}
	}

	@Override
	public void SetCommandCenter(CommandCenter commandCenter)
	{
		super.SetCommandCenter(commandCenter);
		for (ICommand iCommand : Commands.values())
		{
			iCommand.SetCommandCenter(commandCenter);
		}
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		String commandName = null;
		String[] newArgs = new String[]{};

		if (args != null && args.length > 0)
		{
			commandName = args[0];

			if (args.length > 1)
			{
				newArgs = new String[args.length - 1];

				for (int i = 0; i < newArgs.length; i++)
				{
					newArgs[i] = args[i + 1];
				}
			}
		}

		ICommand command = Commands.get(commandName);

		if (command != null)
		{
			if (PermissionManager.Instance.hasPermission(caller.getUniqueId(), command.getPermission()))
			{
				command.SetAliasUsed(commandName);

				command.Execute(caller, newArgs);
			} else
			{
				caller.sendMessage(C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}
		else
		{
			Help(caller, args);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args.length > 1)
		{
			String commandName = args[0].toLowerCase(Locale.ENGLISH);

			ICommand command = Commands.get(commandName);

			if (command != null)
			{
				String[] newArgs = new String[args.length - 1];
				System.arraycopy(args, 1, newArgs, 0, newArgs.length);
				return command.onTabComplete(sender, args[0], newArgs);
			}
		}
		else if (args.length == 1)
		{
			Stream<ICommand> stream = Commands.values().stream();
			if (sender instanceof Player)
			{
				stream = stream.filter(command -> PermissionManager.Instance.hasPermission(((Player) sender).getUniqueId(), command.getPermission()));
			}
			return getMatches(args[0], stream.map(ICommand::Aliases).flatMap(Collection::stream));
		}

		return null;
	}

	protected abstract void Help(Player caller, String[] args);
}