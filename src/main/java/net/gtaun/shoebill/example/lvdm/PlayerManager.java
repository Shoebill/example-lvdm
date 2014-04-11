package net.gtaun.shoebill.example.lvdm;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import net.gtaun.shoebill.SampObjectManager;
import net.gtaun.shoebill.data.Checkpoint;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.data.Vector3D;
import net.gtaun.shoebill.event.checkpoint.CheckpointEnterEvent;
import net.gtaun.shoebill.event.player.PlayerCommandEvent;
import net.gtaun.shoebill.event.player.PlayerConnectEvent;
import net.gtaun.shoebill.event.player.PlayerDeathEvent;
import net.gtaun.shoebill.event.player.PlayerRequestClassEvent;
import net.gtaun.shoebill.event.player.PlayerSpawnEvent;
import net.gtaun.shoebill.event.player.PlayerUpdateEvent;
import net.gtaun.shoebill.object.Menu;
import net.gtaun.shoebill.object.Player;
import net.gtaun.util.event.EventManager;
import net.gtaun.util.event.EventManagerNode;
import net.gtaun.util.event.HandlerPriority;

import org.apache.commons.lang3.math.NumberUtils;

public class PlayerManager
{
	private static final int INITIAL_MONEY = 50000;
	private static final Vector3D[] RANDOM_SPAWNS = 
	{
		new Vector3D(1958.3783f, 1343.1572f, 15.3746f),
		new Vector3D(2199.6531f, 1393.3678f, 10.8203f),
		new Vector3D(2483.5977f, 1222.0825f, 10.8203f),
		new Vector3D(2637.2712f, 1129.2743f, 11.1797f),
		new Vector3D(2000.0106f, 1521.1111f, 17.0625f),
		new Vector3D(2024.8190f, 1917.9425f, 12.3386f),
		new Vector3D(2261.9048f, 2035.9547f, 10.8203f),
		new Vector3D(2262.0986f, 2398.6572f, 10.8203f),
		new Vector3D(2244.2566f, 2523.7280f, 10.8203f),
		new Vector3D(2335.3228f, 2786.4478f, 10.8203f),
		new Vector3D(2150.0186f, 2734.2297f, 11.1763f),
		new Vector3D(2158.0811f, 2797.5488f, 10.8203f),
		new Vector3D(1969.8301f, 2722.8564f, 10.8203f),
		new Vector3D(1652.0555f, 2709.4072f, 10.8265f),
		new Vector3D(1564.0052f, 2756.9463f, 10.8203f),
		new Vector3D(1271.5452f, 2554.0227f, 10.8203f),
		new Vector3D(1441.5894f, 2567.9099f, 10.8203f),
		new Vector3D(1480.6473f, 2213.5718f, 11.0234f),
		new Vector3D(1400.5906f, 2225.6960f, 11.0234f),
		new Vector3D(1598.8419f, 2221.5676f, 11.0625f),
		new Vector3D(1318.7759f, 1251.3580f, 10.8203f),
		new Vector3D(1558.0731f, 1007.8292f, 10.8125f),
		new Vector3D(-857.0551f, 1536.6832f, 22.5870f),		// Out of Town Spawns
		new Vector3D(817.3494f, 856.5039f, 12.7891f),
		new Vector3D(116.9315f, 1110.1823f, 13.6094f),
		new Vector3D(-18.8529f, 1176.0159f, 19.5634f),
		new Vector3D(-315.0575f, 1774.0636f, 43.6406f),
		new Vector3D(1705.2347f, 1025.6808f, 10.8203f)
	};

//	private static final Vector3D[] COP_SPAWNS =
//	{
//		new Vector3D(2297.1064f, 2452.0115f, 10.8203f),
//		new Vector3D(2297.0452f, 2468.6743f, 10.8203f)
//	};
	
	
	private EventManagerNode eventManagerNode;
	private Collection<Checkpoint> checkpoints;
	private Random random;
	
	
	public PlayerManager(EventManager rootEventManager)
	{
		checkpoints = new LinkedList<>();
		random = new Random();
		
		eventManagerNode = rootEventManager.createChildNode();
		
		eventManagerNode.registerHandler(PlayerUpdateEvent.class, HandlerPriority.NORMAL, (PlayerUpdateEvent e) ->
		{
			// getUpdateCount() Example:
			Player player = e.getPlayer();
			if (player.getUpdateCount() % 100 == 0)
			{
				player.setScore(player.getMoney());
			}
		});
		
		eventManagerNode.registerHandler(PlayerConnectEvent.class, HandlerPriority.NORMAL, (PlayerConnectEvent e) ->
		{
			Player player = e.getPlayer();
			player.sendGameText(5000, 5, "~w~SA-MP: ~r~Las Venturas ~g~MoneyGrub");
			player.sendMessage(Color.PURPLE, "Welcome to Las Venturas MoneyGrub, For help type /help.");
			
			Color color = new Color(random.nextInt() << 8 | 0xFF);
			while (color.getY()<128) color = new Color(random.nextInt() << 8 | 0xFF);
			player.setColor(color);
		});
		
		eventManagerNode.registerHandler(PlayerSpawnEvent.class, HandlerPriority.NORMAL, (PlayerSpawnEvent e) ->
		{
			Player player = e.getPlayer();
			player.giveMoney(INITIAL_MONEY);
			player.toggleClock(true);
			setRandomSpawn(player);
		});
		
		eventManagerNode.registerHandler(PlayerDeathEvent.class, HandlerPriority.NORMAL, (PlayerDeathEvent e) ->
		{
			Player player = e.getPlayer();
			Player killer = e.getKiller();
			
			player.sendDeathMessage(killer, e.getReason());
			if (killer != null)
			{
				killer.giveMoney(player.getMoney());
			}
			
			player.setMoney(0);
		});
		
		eventManagerNode.registerHandler(PlayerRequestClassEvent.class, HandlerPriority.NORMAL, (PlayerRequestClassEvent e) ->
		{
			Player player = e.getPlayer();
			setupForClassSelection(player);
		});
		
		eventManagerNode.registerHandler(PlayerCommandEvent.class, HandlerPriority.NORMAL, (PlayerCommandEvent e) ->
		{
			Player player = e.getPlayer();
			
			String command = e.getCommand();
			String[] splits = command.split(" ", 2);
			
			String operation = splits[0].toLowerCase();
			Queue<String> args = new LinkedList<>();
			
			if (splits.length > 1)
			{
				String[] argsArray = splits[1].split(" ");
				args.addAll(Arrays.asList(argsArray));
			}
			
			switch (operation)
			{
			case "/pickup":
				Location location = player.getLocation();
				location.setY(location.getY()+10);
				SampObjectManager.get().createPickup(351, 15, location);
				e.setProcessed();
				return;
				
			case "/menu":
				Menu menu = SampObjectManager.get().createMenu("test1", 1, 0, 0, 100, 100);
				menu.setColumnHeader(0, "test2");
				menu.addItem(0, "hi");
				menu.addItem(0, "hey");
				menu.show(player);
				e.setProcessed();
				return;
				
			case "/cp":
				location = player.getLocation();
				location.setX(location.getX() + 10);
				Checkpoint checkpoint = new Checkpoint(location, 5);
				Checkpoint usingCheckpoint = player.getCheckpoint();
				if (usingCheckpoint != null) checkpoints.remove(usingCheckpoint);
				checkpoints.add(checkpoint);
				player.setCheckpoint(checkpoint);
				e.setProcessed();
				return;
				
			case "/tp":
				if (args.size() < 3)
				{
					player.sendMessage(Color.WHITE, "Usage: /tp [x] [y] [z]");
					e.setProcessed();
					return;
				}
				
				float x = NumberUtils.toFloat(args.poll());
				float y = NumberUtils.toFloat(args.poll());
				float z = NumberUtils.toFloat(args.poll());
				player.setLocation(x, y, z);
				e.setProcessed();
				return;
				
			case "/world":
				if (args.size() < 1)
				{
					player.sendMessage(Color.WHITE, "Usage: /world [id]");
					e.setProcessed();
					return;
				}
				
				int worldId = NumberUtils.toInt(args.poll());
				player.setWorld(worldId);
				e.setProcessed();
				return;
				
			case "/interior":
				if (args.size() < 1)
				{
					player.sendMessage(Color.WHITE, "Usage: /interior [id]");
					e.setProcessed();
					return;
				}
				
				int interior = NumberUtils.toInt(args.poll());
				player.setInterior(interior);
				e.setProcessed();
				return;
				
			case "/angle":
				if (args.size() < 1)
				{
					player.sendMessage(Color.WHITE, "Usage: /angle [val]");
					e.setProcessed();
					return;
				}

				float angle = NumberUtils.toFloat(args.poll());
				player.setAngle(angle);
				e.setProcessed();
				return;
				
			case "/kill":
				player.setHealth(0.0f);
				e.setProcessed();
				return;
				
			case "/codepage":
				if (args.size() < 1)
				{
					player.sendMessage(Color.WHITE, "Usage: /codepage [val]");
					e.setProcessed();
					return;
				}
				
				int codepage = NumberUtils.toInt(args.poll());
				player.setCodepage(codepage);
				e.setProcessed();
				return;
				
			case "/givecash":
				if (args.size() != 2)
				{
					player.sendMessage(Color.WHITE, "Usage: /givecash [playerid] [amount]");
					e.setProcessed();
					return;
				}
				
				int playerId = NumberUtils.toInt(args.poll());
				int money = NumberUtils.toInt(args.poll());
				
				Player givePlayer = SampObjectManager.get().getPlayer(playerId);
				if (givePlayer == null || givePlayer == player)
				{
					player.sendMessage(Color.RED, "Invalid player id.");
					e.setProcessed();
					return;
				}
				
				if (money <= 0 || money > player.getMoney())
				{
					player.sendMessage(Color.WHITE, "Invalid transaction amount.");
					e.setProcessed();
					return;
				}
				
				player.giveMoney(-money);
				givePlayer.giveMoney(money);
				
				player.sendMessage(Color.YELLOW, "You have sent " + givePlayer.getName() + "(" + givePlayer.getId() + "), $" + money);
				givePlayer.sendMessage(Color.YELLOW, "You have recieved $" + money + " from " + player.getName() + "(" + player.getId() + ").");
				
				LvdmGamemode.logger().info("{}({}) has transfered {} to {}({})\n", player.getName(), player.getId(), money, givePlayer.getName(), givePlayer.getId());
				e.setProcessed();
				return;
				
			case "/objective":
				player.sendMessage(Color.YELLOW, "This gamemode is faily open, there's no specific win / endgame conditions to meet.");
				player.sendMessage(Color.YELLOW, "In LVDM:Money Grub, when you kill a player, you will receive whatever money they have.");
				player.sendMessage(Color.YELLOW, "Consequently, if you have lots of money, and you die, your killer gets your cash.");
				player.sendMessage(Color.YELLOW, "However, you're not forced to kill players for money, you can always gamble in the");
				player.sendMessage(Color.YELLOW, "Casino's.");
				e.setProcessed();
				return;
				
			case "/tips":
				player.sendMessage(Color.YELLOW, "Spawning with just a desert eagle might sound lame, however the idea of this");
				player.sendMessage(Color.YELLOW, "gamemode is to get some cash, get better guns, then go after whoever has the");
				player.sendMessage(Color.YELLOW, "most cash. Once you've got the most cash, the idea is to stay alive(with the");
				player.sendMessage(Color.YELLOW, "cash intact)until the game ends, simple right?");
				e.setProcessed();
				return;
				
			case "/help":
				player.sendMessage(Color.YELLOW, "Las Venturas Deathmatch: Money Grub Coded By Jax and the SA-MP Team.");
				player.sendMessage(Color.YELLOW, "Type: /objective : to find out what to do in this gamemode.");
				player.sendMessage(Color.YELLOW, "Type: /givecash [playerid] [money-amount] to send money to other players.");
				player.sendMessage(Color.YELLOW, "Type: /tips : to see some tips from the creator of the gamemode.");
				e.setProcessed();
				return;
			}
		});
		
		eventManagerNode.registerHandler(CheckpointEnterEvent.class, HandlerPriority.NORMAL, (CheckpointEnterEvent e) ->
		{
			Player player = e.getPlayer();
			Checkpoint checkpoint = e.getCheckpoint();
			
			if(checkpoints.contains(checkpoint))
			{
				player.disableCheckpoint();
				player.playSound(1057, player.getLocation());
				checkpoints.remove(checkpoint);
			}
		});
		
		eventManagerNode.registerHandler(PlayerCommandEvent.class, HandlerPriority.BOTTOM, (PlayerCommandEvent e) ->
		{
			Player player = e.getPlayer();
			player.sendMessage(Color.RED, "Unknown command. Type /help to see help.");
			e.setProcessed();
		});
	}
	
	public void uninitialize()
	{
		eventManagerNode.destroy();
	}
	
	private void setRandomSpawn(Player player)
	{
		int rand = random.nextInt(RANDOM_SPAWNS.length);
		player.setLocation(RANDOM_SPAWNS[rand]);
		player.setInterior(0);
	}

	private void setupForClassSelection(Player player)
	{
		player.setInterior(14);
		player.setLocation(258.4893f, -41.4008f, 1002.0234f);
		player.setAngle(270.0f);
		player.setCameraPosition(256.0815f, -43.0475f, 1004.0234f);
		player.setCameraLookAt(258.4893f, -41.4008f, 1002.0234f);
	}
}
