package com.playmonumenta.epicstructures;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.playmonumenta.epicstructures.commands.ActivateSpecialStructure;
import com.playmonumenta.epicstructures.commands.AddRespawningStructure;
import com.playmonumenta.epicstructures.commands.CompassRespawn;
import com.playmonumenta.epicstructures.commands.ForceloadLazy;
import com.playmonumenta.epicstructures.commands.ListRespawningStructures;
import com.playmonumenta.epicstructures.commands.LoadStructure;
import com.playmonumenta.epicstructures.commands.ReloadStructures;
import com.playmonumenta.epicstructures.commands.RemoveRespawningStructure;
import com.playmonumenta.epicstructures.commands.RespawnStructure;
import com.playmonumenta.epicstructures.commands.SaveStructure;
import com.playmonumenta.epicstructures.commands.SetPostRespawnCommand;
import com.playmonumenta.epicstructures.commands.SetRespawnTimer;
import com.playmonumenta.epicstructures.commands.SetSpawnerBreakTrigger;
import com.playmonumenta.epicstructures.managers.EventListener;
import com.playmonumenta.epicstructures.managers.RespawnManager;
import com.playmonumenta.epicstructures.managers.StructureManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Plugin extends JavaPlugin {
	public StructureManager mStructureManager;

	public RespawnManager mRespawnManager = null;

	private File mConfigFile;
	private YamlConfiguration mConfig;

	@Override
	public void onLoad() {
		ActivateSpecialStructure.register(this);
		AddRespawningStructure.register(this);
		CompassRespawn.register(this);
		ForceloadLazy.register(this);
		ListRespawningStructures.register(this);
		LoadStructure.register(this);
		ReloadStructures.register(this);
		RemoveRespawningStructure.register(this);
		RespawnStructure.register(this);
		SaveStructure.register(this);
		SetPostRespawnCommand.register(this);
		SetRespawnTimer.register(this);
		SetSpawnerBreakTrigger.register(this);
	}

	@Override
	public void onEnable() {
		//TODO: Command to add an alternate generic structure

		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(new EventListener(this), this);

		reloadConfig();
	}

	@Override
	public void onDisable() {
		// Save current structure respawn times
		saveConfig();

		// Cancel structure respawning and clear list
		if (mRespawnManager != null) {
			mRespawnManager.cleanup();
			mRespawnManager = null;
		}

		// Cancel any remaining tasks
		getServer().getScheduler().cancelTasks(this);
	}

	@Override
	public void reloadConfig() {
		// Do not save first
		if (mRespawnManager != null) {
			mRespawnManager.cleanup();
			mRespawnManager = null;
		}

		if (mConfigFile == null) {
			mConfigFile = new File(getDataFolder(), "config.yml");
		}

		if (!mConfigFile.exists()) {
			try {
				// Create parent directories if they do not exist
				mConfigFile.getParentFile().mkdirs();

				// Create the file if it does not exist
				mConfigFile.createNewFile();
			} catch (IOException ex) {
				getLogger().log(Level.SEVERE, "Failed to create non-existent configuration file");
			}

			// TODO: Put sample config file in here also
		}

		mConfig = YamlConfiguration.loadConfiguration(mConfigFile);

		/* TODO: Non-hardcoded worlds! These should be saved into the respawning structure */
		mStructureManager = new StructureManager(this);
		mRespawnManager = new RespawnManager(this, Bukkit.getWorlds().get(0), mConfig);
	}

	@Override
	public void saveConfig() {
		if (mRespawnManager != null) {
			try {
				mConfig = mRespawnManager.getConfig();
				mConfig.save(mConfigFile);
			} catch (Exception ex) {
				getLogger().log(Level.SEVERE, "Could not save config to " + mConfigFile, ex);
			}
		}
	}

	public void asyncLog(Level level, String message) {
		new BukkitRunnable() {
			@Override
			public void run()
			{
				getLogger().log(level, message);
			}
		}.runTask(this);
	}

	public void asyncLog(Level level, String message, Exception ex) {
		new BukkitRunnable() {
			@Override
			public void run()
			{
				getLogger().log(level, message, ex);
			}
		}.runTask(this);
	}
}
