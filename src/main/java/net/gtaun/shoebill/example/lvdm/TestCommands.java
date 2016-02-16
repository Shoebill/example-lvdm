package net.gtaun.shoebill.example.lvdm;

import net.gtaun.shoebill.SampObjectManager;
import net.gtaun.shoebill.common.command.*;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.data.Radius;
import net.gtaun.shoebill.object.Checkpoint;
import net.gtaun.shoebill.object.Menu;
import net.gtaun.shoebill.object.Player;

import java.util.function.Consumer;

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

	@CommandHelp(value = "Changes your drunk level to 50000 and makes you drunk.")
	@Command
	public boolean drink(Player p)
	{
		p.setDrunkLevel(50000);
		return true;
	}

	@CommandHelp(value = "Creates a sample pickup.")
	@Command
	public boolean pickup(Player p)
	{
		Location location = p.getLocation();
		location.y += 10;
		SampObjectManager.get().createPickup(351, 15, location);
		return true;
	}

	@CommandHelp(value = "Opens a sample Menu.")
	@Command
	public boolean menu(Player p)
	{
		Menu menu = SampObjectManager.get().createMenu("test1", 1, 0, 0, 100, 100);
		menu.setColumnHeader(0, "test2");
		menu.addItem(0, "Hello!");
		menu.addItem(0, "Hi!");
		menu.show(p);

		return true;
	}

	@CommandHelp(value = "Creates a sample checkpoint.")
	@Command
	public boolean checkpoint(Player p)
	{
		Radius location = new Radius(p.getLocation(), 10);
		location.x += 10;

		p.setCheckpoint(Checkpoint.create(location, player -> {
            player.disableCheckpoint();
            player.playSound(1057);
        }, null));

		return true;
	}

	@CommandHelp(value = "Teleports you to a specific position on the map.")
	@Command
	public boolean tp(Player p, float x, float y, float z)
	{
		p.setLocation(x, y, z);
		return true;
	}

	@CommandHelp(value = "Teleports you to a target player.")
	@Command
	public boolean tp(Player p, Player target) {
		p.setLocation(target.getLocation());
		return true;
	}

	@CommandHelp(value = "Changes your worldId to a specific one.")
	@Command
	public boolean world(Player p,
						 @CommandParameter(name = "World ID", description = "The target world id.") int worldId)
	{
		p.setWorld(worldId);
		return true;
	}

	@CommandHelp(value = "Changes your interior to a specific one.")
	@Command
	public boolean interior(Player p,
							@CommandParameter(name = "Interior ID", description = "The target interior id") int interiorId)
	{
		p.setInterior(interiorId);
		return true;
	}

	@CommandHelp(value = "Changes your characters facing angle.")
	@Command
	public boolean angle(Player p,
						 @CommandParameter(name = "Angle", description = "The target angle in degrees.") float angle)
	{
		p.setAngle(angle);
		return true;
	}

	@CommandHelp(value = "Sets the codepage of the player to a specific one.")
	@Command
	public boolean codepage(Player p,
							@CommandParameter(name = "Codepage ID", description = "The target codepage code.") int code)
	{
		p.setCodepage(code);
		return true;
	}
}
