package net.gtaun.shoebill.example.lvdm;

import net.gtaun.shoebill.SampObjectManager;
import net.gtaun.shoebill.common.command.BeforeCheck;
import net.gtaun.shoebill.common.command.Command;
import net.gtaun.shoebill.common.command.CustomCommand;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.data.Radius;
import net.gtaun.shoebill.object.Checkpoint;
import net.gtaun.shoebill.object.Menu;
import net.gtaun.shoebill.object.Player;

public class TestCommands
{
	@BeforeCheck
	public boolean checkPremission(Player p, String cmd, String params)
	{
		return p.isAdmin();
	}
	
	@CustomCommand
	public boolean customHandler(Player p, String cmd, String params)
	{
		// If the command has been processed, return true
		return false;
	}

	@Command
	public boolean drink(Player p)
	{
		p.setDrunkLevel(50000);
		return true;
	}

	@Command
	public boolean pickup(Player p)
	{
		Location location = p.getLocation();
		location.y += 10;
		SampObjectManager.get().createPickup(351, 15, location);
		return true;
	}

	@Command
	public boolean menu(Player p)
	{
		Menu menu = SampObjectManager.get().createMenu("test1", 1, 0, 0, 100, 100);
		menu.setColumnHeader(0, "test2");
		menu.addItem(0, "hi");
		menu.addItem(0, "hey");
		menu.show(p);

		return true;
	}

	@Command
	public boolean checkpoint(Player p)
	{
		Radius location = new Radius(p.getLocation(), 10);
		location.x += 10;

		p.setCheckpoint(new Checkpoint()
		{
			@Override
			public Radius getLocation()
			{
				return location;
			}

			@Override
			public void onEnter(Player p)
			{
				p.disableCheckpoint();
				p.playSound(1057);
			}
		});

		return true;
	}

	@Command
	public boolean tp(Player p, float x, float y, float z)
	{
		p.setLocation(x, y, z);
		return true;
	}

	@Command
	public boolean world(Player p, int worldId)
	{
		p.setWorld(worldId);
		return true;
	}

	@Command
	public boolean interior(Player p, int interiorId)
	{
		p.setInterior(interiorId);
		return true;
	}

	@Command
	public boolean angle(Player p, float angle)
	{
		p.setAngle(angle);
		return true;
	}

	@Command
	public boolean codepage(Player p, int code)
	{
		p.setCodepage(code);
		return true;
	}
}
