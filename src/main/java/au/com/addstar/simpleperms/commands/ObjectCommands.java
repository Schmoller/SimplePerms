package au.com.addstar.simpleperms.commands;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@SuppressWarnings( "deprecation" )
public abstract class ObjectCommands
{
	protected final PermissionManager manager;
	
	public ObjectCommands(PermissionManager manager)
	{
		this.manager = manager;
	}
	
	public abstract String getName();
	
	public abstract PermissionBase getObject(String value) throws IllegalArgumentException;
	
	private void displayUsage(CommandSender sender, PermissionBase object, String... parts)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		builder.append(' ');
		builder.append(object.getName());
		
		for (String part : parts)
		{
			builder.append(' ');
			builder.append(part);
		}
		
		sender.sendMessage(builder.toString());
	}
	
	public final void onExecute(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length == 0)
		{
			displayUsage(sender, object, "<command> [<params>...]");
			return;
		}
		
		switch (args[0].toLowerCase())
		{
		case "check":
			onCheck(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "delete":
			onDelete(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "list":
			onList(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "add":
			onAdd(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "remove":
			onRemove(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "group":
		case "parent":
			onParent(sender, object, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length));
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown sub command " + args[0]);
			break;
		}
	}
	
	private void onCheck(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "check <permission>");
			return;
		}
		
		Boolean value = object.getLocalPermission(args[0]);
		boolean local = true;
		if (value == null)
		{
			local = false;
			value = object.getPermission(args[0]);
		}
		
		
		if (value == null)
			sender.sendMessage(String.format("%s doesnt have permission \"%s\" defined (inherited)", object.getName(), args[0]));
		else
		{
			if (local)
				sender.sendMessage(String.format("%s has \"%s\" = %s (self)", object.getName(), args[0], value.toString().toUpperCase()));
			else
				sender.sendMessage(String.format("%s has \"%s\" = %s (inherited)", object.getName(), args[0], value.toString().toUpperCase()));
		}
	}
	
	private void onDelete(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 0)
		{
			displayUsage(sender, object, "delete");
			return;
		}
		
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void onList(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length > 1)
		{
			displayUsage(sender, object, "list [<page>]");
			return;
		}
		
		int perPage = Integer.MAX_VALUE;
		if (sender instanceof ProxiedPlayer)
			perPage = 15;
		
		// Parse page number
		int page = 0;
		if (args.length == 1)
		{
			try
			{
				page = Integer.parseInt(args[0]);
				if (page <= 0)
				{
					sender.sendMessage(ChatColor.RED + "Page number must be 1 or higher");
					return;
				}
				
				--page;
			}
			catch (NumberFormatException e)
			{
				displayUsage(sender, object, "list [<page>]");
				return;
			}
		}
		
		// Display perms
		List<String> perms = object.getRawPermissions();
		int start = page * perPage;
		
		if (start >= perms.size() && start != 0)
			sender.sendMessage(ChatColor.RED + "Page number too high");
		else
		{
			for (int i = start; i < perms.size() && i < start + perPage; ++i)
				sender.sendMessage(String.format("%d) %s", i+1, perms.get(i)));
			
			// Show that there are more results
			if (perms.size() > start + perPage)
				sender.sendMessage("...");
		}
	}
	
	private void onAdd(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "add <permission>");
			return;
		}
		
		object.addPermission(args[0]);
		object.rebuildPermissions();
		
		sender.sendMessage(ChatColor.GREEN + args[0] + " was added to " + object.getName());
	}
	
	private void onRemove(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "remove <permission>");
			return;
		}

		object.removePermission(args[0]);
		object.rebuildPermissions();
		
		sender.sendMessage(ChatColor.GREEN + args[0] + " was removed from " + object.getName());
	}
	
	// ====================================
	//           Parent Commands
	// ====================================
	
	private void onParent(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length == 0)
		{
			displayUsage(sender, object, label, "<command> [<params>...]");
			return;
		}
		
		switch (args[0].toLowerCase())
		{
		case "list":
			onParentList(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "add":
			onParentAdd(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "remove":
			onParentRemove(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "set":
			onParentSet(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown sub command " + args[0]);
			break;
		}
	}
	
	private void onParentAdd(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "add <parent>");
			return;
		}
		
		PermissionGroup toAdd = manager.getGroup(args[0]);
		if (toAdd == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown group " + args[0]);
			return;
		}
		
		// Check that it doesnt already have this parent
		if (object.hasParent(toAdd))
		{
			sender.sendMessage(ChatColor.RED + object.getName() + " already inherits from " + toAdd.getName());
			return;
		}
		
		// Make sure we wont add a cycle
		if (object instanceof PermissionGroup && toAdd.hasParent((PermissionGroup)object))
		{
			sender.sendMessage(ChatColor.RED + "Adding that parent would create a cycle.");
			return;
		}
		
		object.addParent(toAdd);
		sender.sendMessage(ChatColor.GREEN + object.getName() + " now inherits from " + toAdd.getName());
	}
	
	private void onParentList(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 0)
		{
			displayUsage(sender, object, label, "list");
			return;
		}

		int index = 1;
		for (PermissionGroup parent : object.parents())
			sender.sendMessage(String.format("%d) %s", index++, parent.getName()));
	}
	
	private void onParentRemove(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "remove <parent>");
			return;
		}
		
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "add <parent>");
			return;
		}
		
		PermissionGroup toRemove = manager.getGroup(args[0]);
		if (toRemove == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown group " + args[0]);
			return;
		}
		
		object.removeParent(toRemove);
		sender.sendMessage(ChatColor.GOLD + toRemove.getName() + " is not longer a parent of " + object.getName());
	}
	
	private void onParentSet(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length == 0)
		{
			displayUsage(sender, object, label, "set <parent> [<parent>...]");
			return;
		}
		
		// Parse groups
		List<PermissionGroup> groups = Lists.newArrayList();
		for (String groupName : args)
		{
			PermissionGroup group = manager.getGroup(groupName);
			if (group == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown group " + groupName);
				return;
			}
			groups.add(group);
		}
		
		object.setParents(groups);
		sender.sendMessage(ChatColor.GOLD + object.getName() + " now inherits from " + groups);
	}
}
