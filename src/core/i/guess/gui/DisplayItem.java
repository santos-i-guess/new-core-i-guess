package core.i.guess.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DisplayItem implements GuiItem
{
	private ItemStack _item;

	public DisplayItem(ItemStack item)
	{
		_item = item;
	}

	@Override
	public void setup()
	{

	}

	@Override
	public void close()
	{

	}

	@Override
	public void click(ClickType clickType)
	{

	}

	@Override
	public ItemStack getObject()
	{
		return _item;
	}
}
