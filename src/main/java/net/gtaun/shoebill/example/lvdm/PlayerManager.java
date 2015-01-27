package net.gtaun.shoebill.example.lvdm;

import java.util.Random;

import net.gtaun.shoebill.common.command.CommandGroup;
import net.gtaun.shoebill.common.command.PlayerCommandManager;
import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.data.Vector3D;
import net.gtaun.shoebill.event.player.PlayerCommandEvent;
import net.gtaun.shoebill.event.player.PlayerConnectEvent;
import net.gtaun.shoebill.event.player.PlayerDeathEvent;
import net.gtaun.shoebill.event.player.PlayerDisconnectEvent;
import net.gtaun.shoebill.event.player.PlayerRequestClassEvent;
import net.gtaun.shoebill.event.player.PlayerSpawnEvent;
import net.gtaun.shoebill.event.player.PlayerUpdateEvent;
import net.gtaun.shoebill.event.player.PlayerWeaponShotEvent;
import net.gtaun.shoebill.object.Player;
import net.gtaun.util.event.EventManager;
import net.gtaun.util.event.EventManagerNode;
import net.gtaun.util.event.HandlerPriority;

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
	private PlayerCommandManager commandManager;

	private Random random;


	public PlayerManager(EventManager rootEventManager)
	{
		random = new Random();

		eventManagerNode = rootEventManager.createChildNode();

		commandManager = new PlayerCommandManager(eventManagerNode);
		commandManager.installCommandHandler(HandlerPriority.NORMAL);

		commandManager.registerCommands(new LvdmCommands());

		// Example: register /test [command] ...
		CommandGroup testGroup = new CommandGroup();
		testGroup.registerCommands(new TestCommands());
		commandManager.registerChildGroup(testGroup, "test");

		eventManagerNode.registerHandler(PlayerUpdateEvent.class, (e) ->
		{
			Player player = e.getPlayer();

			// getUpdateCount() Example
			if (player.getUpdateCount() % 100 == 0)
			{
				player.setScore(player.getMoney());
			}
		});

		eventManagerNode.registerHandler(PlayerWeaponShotEvent.class, (e) ->
		{
			e.getPlayer().sendMessage(Color.LIGHTBLUE, String.format("WeaponShot: hittype: %1$s, weapon: %2$s, pos: %3$s", e.getHitType(), e.getWeapon(), e.getPosition()));
		});

		eventManagerNode.registerHandler(PlayerConnectEvent.class, (e) ->
		{
			Player player = e.getPlayer();
			player.sendGameText(5000, 5, "~w~SA-MP: ~r~Las Venturas ~g~MoneyGrub");
			player.sendMessage(Color.PURPLE, "Welcome to Las Venturas MoneyGrub, For help type /help.");

			Player.sendDeathMessageToAll(player, null, WeaponModel.CONNECT);

			Color color = new Color();
			do color.setValue(random.nextInt()); while (color.getY() < 128);
			player.setColor(color);
		});

		eventManagerNode.registerHandler(PlayerDisconnectEvent.class, (e) ->
		{
			Player player = e.getPlayer();
			Player.sendDeathMessageToAll(player, null, WeaponModel.DISCONNECT);
		});

		eventManagerNode.registerHandler(PlayerSpawnEvent.class, (e) ->
		{
			Player player = e.getPlayer();
			player.giveMoney(INITIAL_MONEY);
			player.toggleClock(true);
			setRandomSpawnPos(player);
		});

		eventManagerNode.registerHandler(PlayerDeathEvent.class, (e) ->
		{
			Player player = e.getPlayer();
			Player killer = e.getKiller();

			Player.sendDeathMessageToAll(killer, player, e.getReason());
			if (killer != null) killer.giveMoney(player.getMoney());

			player.setMoney(0);
		});

		eventManagerNode.registerHandler(PlayerRequestClassEvent.class, (e) ->
		{
			Player player = e.getPlayer();
			setupForClassSelection(player);
		});

		eventManagerNode.registerHandler(PlayerCommandEvent.class, HandlerPriority.BOTTOM, (e) ->
		{
			Player player = e.getPlayer();
			player.sendMessage(Color.RED, "Unknown command. Type /help to see help.");
			e.setProcessed();
		});
	}

	public void uninitialize()
	{
		commandManager.destroy();
		eventManagerNode.destroy();
	}

	private void setRandomSpawnPos(Player player)
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
