package net.gtaun.shoebill.example.lvdm;

import net.gtaun.shoebill.SampObjectManager;
import net.gtaun.shoebill.common.command.Command;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.object.Menu;
import net.gtaun.shoebill.object.Player;

public class TestCommands
{
	public TestCommands()
	{
		
	}

	@Command
	public boolean drink(Player p)
	{
		p.setDrunkLevel(50000);
		return true;	
	}
	
	// @CommandHelp public String Pickup(Player p)		{ return "pick a pickup"; }
	
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
		Location location = p.getLocation();
		location.setX(location.getX() + 10);
		//Checkpoint checkpoint = new Checkpoint(location, 5);
		//Checkpoint usingCheckpoint = p.getCheckpoint();
		//if (usingCheckpoint != null) checkpoints.remove(usingCheckpoint);
		//checkpoints.add(checkpoint);
		//player.setCheckpoint(checkpoint);
		
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
