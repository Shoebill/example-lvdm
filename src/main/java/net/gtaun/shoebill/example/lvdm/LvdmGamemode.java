package net.gtaun.shoebill.example.lvdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;

import net.gtaun.shoebill.SampObjectFactory;
import net.gtaun.shoebill.SampObjectStore;
import net.gtaun.shoebill.constant.PlayerMarkerMode;
import net.gtaun.shoebill.event.TimerEventHandler;
import net.gtaun.shoebill.event.timer.TimerTickEvent;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.object.Server;
import net.gtaun.shoebill.object.Timer;
import net.gtaun.shoebill.object.World;
import net.gtaun.shoebill.resource.Gamemode;
import net.gtaun.util.event.EventManager;
import net.gtaun.util.event.EventManager.HandlerPriority;

import org.slf4j.Logger;

public class LvdmGamemode extends Gamemode
{
	private static Logger logger;
	public static Logger logger()
	{
		return logger;
	}
	
	
	private PlayerManager playerManager;
	private Timer timer;
	
	
	public LvdmGamemode()
	{
		
	}
	
	@Override
	protected void onEnable() throws Throwable
	{
		logger = getLogger();
		
		final SampObjectStore store = getShoebill().getSampObjectStore();
		final SampObjectFactory factory = getShoebill().getSampObjectFactory();
		final EventManager eventManager = getEventManager();
		
		Server server = store.getServer();
		World world = store.getWorld();
		
		server.setGamemodeText(getDescription().getName());
		world.showPlayerMarkers(PlayerMarkerMode.GLOBAL);
		world.showNameTags(true);
		world.enableStuntBonusForAll(false);
		
		TimerEventHandler timerEventHandler = new TimerEventHandler()
		{
			@Override
			public void onTimerTick(TimerTickEvent event)
			{
				Collection<Player> players = store.getPlayers();
				for (Player player : players)
				{
					player.setScore(player.getMoney());
				}
			}
		};
		
		timer = factory.createTimer(5000);
		timer.start();
		
		eventManager.registerHandler(TimerTickEvent.class, timer, timerEventHandler, HandlerPriority.NORMAL);	//Bind timer object for this EventHandler
		
		factory.createPickup(371, 15, 1710.3359f, 1614.3585f, 10.1191f, 0);
		factory.createPickup(371, 15, 1964.4523f, 1917.0341f, 130.9375f, 0);
		factory.createPickup(371, 15, 2055.7258f, 2395.8589f, 150.4766f, 0);
		factory.createPickup(371, 15, 2265.0120f, 1672.3837f, 94.9219f, 0);
		factory.createPickup(371, 15, 2265.9739f, 1623.4060f, 94.9219f, 0);
		
		playerManager = new PlayerManager(eventManager, factory, store);
		
		File playerClassFile = new File(getDataDir(), "class.txt");
		loadClass(world, playerClassFile);
		
		File vehicleFilesDir = new File(getDataDir(), "vehicles/");
		loadVehicle(factory, vehicleFilesDir);
	}

	@Override
	protected void onDisable() throws Throwable
	{
		playerManager.uninitialize();
		playerManager = null;
	}

	private void loadClass(World world, File file)
	{
		BufferedReader reader;
		try
		{
			logger.info("loading " + file);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
			
			int count = 0;
			while (reader.ready())
			{
				String data = reader.readLine().trim();
				String[] datas = data.split(",");

				if (data.length() == 0 || data.charAt(0) == '/' || datas.length < 11) continue;
				
				try
				{
					int i = 0;
					int modelId = Integer.parseInt(datas[i++].trim());
					float x = Float.parseFloat(datas[i++].trim());
					float y = Float.parseFloat(datas[i++].trim());
					float z = Float.parseFloat(datas[i++].trim());
					float angle = Float.parseFloat(datas[i++].trim());
					int weapon1 = Integer.parseInt(datas[i++].trim());
					int ammo1 = Integer.parseInt(datas[i++].trim());
					int weapon2 = Integer.parseInt(datas[i++].trim());
					int ammo2 = Integer.parseInt(datas[i++].trim());
					int weapon3 = Integer.parseInt(datas[i++].trim());
					int ammo3 = Integer.parseInt(datas[i++].trim());
					world.addPlayerClass(modelId, x, y, z, angle, weapon1, ammo1, weapon2, ammo2, weapon3, ammo3);
					
					count++;
				}
				catch (NumberFormatException e)
				{
					logger.info("Skip: " + data);
				}
			}
			
			logger.info("Created " + count + " classes.");
			reader.close();
		}
		catch (IOException e)
		{
			logger.info("Can't initialize classes, please check your " + file);
		}
	}

	private void loadVehicle(SampObjectFactory factory, File dir)
	{
		BufferedReader reader;

		File files[] = dir.listFiles();
			
		int count = 0;
		for (File file : files)
		{
			try
			{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
				logger.info("loading " + file);
					
				while (reader.ready())
				{
					String data = reader.readLine().trim();
					String[] datas = data.split("[, ]");
					
					if (data.length() == 0 || data.charAt(0) == '/' || datas.length < 7) continue;
					
					try
					{
						int i = 0;
						int modelId = Integer.parseInt(datas[i++].trim());
						float x = Float.parseFloat(datas[i++].trim());
						float y = Float.parseFloat(datas[i++].trim());
						float z = Float.parseFloat(datas[i++].trim());
						float angle = Float.parseFloat(datas[i++].trim());
						int color1 = Integer.parseInt(datas[i++].trim());
						int color2 = Integer.parseInt(datas[i++].trim());
						factory.createVehicle(modelId, x, y, z, angle, color1, color2, 0);
						
						count++;
					}
					catch (NumberFormatException e)
					{
						logger.info("Skip: " + data);
					}
				}
			}
			catch (IOException e)
			{
				System.out.println("Can't initialize vehicles, please check your " + file + " file.");
			}
		}

		System.out.println("Created " + count + " vehicles.");
	}
}
