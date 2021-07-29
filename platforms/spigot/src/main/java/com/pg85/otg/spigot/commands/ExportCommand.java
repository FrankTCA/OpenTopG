package com.pg85.otg.spigot.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.MCWorldGenRegion;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.server.v1_16_R3.ArgumentTile;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;

public class ExportCommand extends BaseCommand
{
	protected static final HashMap<Player, Region> playerSelectionMap = new HashMap<>();
	
	public ExportCommand() {
		this.name = "export";
		this.helpMessage = "Allows you to export an area as a BO3 or BO4.";
		this.usage = "Please see /otg help export.";
	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command"); return true;}
		Player player = (Player) sender;

		if (args.length == 0) {help(player); return true;}
		String objectName =  args[0];
		IBlockData centerBlockState = null;
		try
		{
			// Get the material to use for center block
			 if (args.length >= 2) centerBlockState = ArgumentTile.a().parse(new StringReader(args[1])).a();
		}
		catch (CommandSyntaxException e)
		{
			sender.sendMessage("Could not find material "+args[1]);
			return true;
		}
		String presetName = args.length > 2 && !args[2].equalsIgnoreCase("global") ? args[2] : null;
		boolean isGlobal = presetName == null;
		String templateName = args.length >= 4 ? args[3] : "default";
		String flags = args.length >= 5 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
		boolean overwrite = flags.contains("-o");
		boolean branching = flags.contains("-b");
		boolean includeAir = flags.contains("-a");

		// Get region
		Region region = playerSelectionMap.get(player);
		if (region == null || region.getLow() == null || region.getHigh() == null) {
			sender.sendMessage("Please mark two corners with /otg region mark"); return true;}

		// Get preset
		Preset preset = getPresetOrDefault(presetName);
		if (preset == null) {
			sender.sendMessage("Could not find preset "+presetName); return true; }

		// Get object path
		Path objectPath = getObjectPath(isGlobal ? null : preset.getPresetFolder());

		// Check for existing file
		if (!overwrite)
		{
			if (new File(objectPath.toFile(), objectName + ".bo3").exists())
			{
				sender.sendMessage("File already exists, run command with flag '-o' to overwrite"); 
				return true;
			}
		}
		
		// Get required pieces
		SpigotWorldGenRegion genRegion;
		if((((CraftWorld)((Player)sender).getWorld()).getGenerator() instanceof OTGSpigotChunkGen))
		{
			genRegion = new SpigotWorldGenRegion(
				preset.getFolderName(),
				preset.getWorldConfig(),
				((CraftWorld)player.getWorld()).getHandle(),
				((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).generator
			);
		} else {
			genRegion = new MCWorldGenRegion(
				preset.getFolderName(),
				preset.getWorldConfig(),
				((CraftWorld) player.getWorld()).getHandle()
			);
		}

		LocalNBTHelper nbtHelper = new SpigotNBTHelper();
		Corner lowCorner = region.getLow();
		Corner highCorner = region.getHigh();
		Corner center = new Corner((highCorner.x - lowCorner.x) / 2 + lowCorner.x, lowCorner.y, (highCorner.z - lowCorner.z) / 2 + lowCorner.z);

		// Fetch template or default settings
		BO3 template = (BO3) OTG.getEngine().getCustomObjectManager().getObjectLoaders().get("bo3")
			.loadFromFile(templateName, new File(objectPath.toFile(), templateName + ".BO3Template"), OTG.getEngine().getLogger());

		// Initialize the settings
		template.onEnable(
			preset.getFolderName(),
			OTG.getEngine().getOTGRootFolder(),
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker()
		);

		BO3 bo3;

		if (branching)
		{
			try
			{
				bo3 = BO3Creator.createStructure(
					lowCorner,
					highCorner,
					center,
					objectName,
					includeAir,
					objectPath,
					genRegion,
					nbtHelper,
					template.getConfig(),
					preset.getFolderName(),
					OTG.getEngine().getOTGRootFolder(),
					OTG.getEngine().getLogger(),
					OTG.getEngine().getCustomObjectManager(),
					OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
					OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()
				);
			}
			catch (Exception e)
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, String.format("Error during export command: ", (Object[])e.getStackTrace()));
				return true;
			}
		} else {
			// Create a new BO3 from our settings
			LocalMaterialData centerBlock = centerBlockState == null ? null : SpigotMaterialData.ofBlockData(centerBlockState);
			bo3 = BO3Creator.create(
				lowCorner,
				highCorner,
				center,
				centerBlock,
				objectName,
				includeAir,
				objectPath,
				genRegion,
				nbtHelper,
				null,
				template.getConfig(),
				preset.getFolderName(),
				OTG.getEngine().getOTGRootFolder(),
				OTG.getEngine().getLogger(),
				OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
				OTG.getEngine().getCustomObjectResourcesManager(),
				OTG.getEngine().getModLoadedChecker()
			);
		}

		// Send feedback, and register the BO3 for immediate use
		if (bo3 != null)
		{
			sender.sendMessage("Successfully created BO3 " + objectName);
			if (isGlobal)
			{
				OTG.getEngine().getCustomObjectManager().registerGlobalObject(bo3, bo3.getConfig().getFile());
			} else {
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(preset.getFolderName(), bo3.getName().toLowerCase(Locale.ROOT), bo3.getConfig().getFile(), bo3);
			}
		} else {
			sender.sendMessage("Failed to create BO3 " + objectName);
		}

		return true;
	}

	public static void help(Player player)
	{
		player.sendMessage("To use the export command:");
		player.sendMessage("/otg export <name> [center block] [preset] [template] [-a, -b, -o]");
		player.sendMessage(" * Name is the name of the object");
		player.sendMessage(" * Center block is where to place 0,0. It chooses the first it finds from below.");
		player.sendMessage(" * Preset is which preset to fetch template from, and to save the BO3 to");
		player.sendMessage(" * Template is what default settings to apply to the BO3");
		player.sendMessage(" * -a flag is to include air blocks, -b is to export branching, and -o is overwrite file");
	}

	protected static Preset getPresetOrDefault(String presetFolderName)
	{
		if (presetFolderName == null)
		{
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		} else {
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(presetFolderName);
		}
	}

	protected static Path getObjectPath(Path presetFolder)
	{
		Path objectPath;
		if (presetFolder == null)
		{
			objectPath = OTG.getEngine().getGlobalObjectsFolder();
		} else {
			objectPath = presetFolder.resolve(Constants.WORLD_OBJECTS_FOLDER);
		}

		if (!objectPath.toFile().exists())
		{
			if (objectPath.resolve("..").resolve("WorldObjects").toFile().exists())
			{
				objectPath = objectPath.resolve("..").resolve("WorldObjects");
			}
		}
		return objectPath;
	}

	public static boolean region(CommandSender source, String[] args)
	{
		if (!(source instanceof Player))
		{
			source.sendMessage("Only players can execute this command");
			return false;
		}
		Player player = ((Player) source);
		if (!playerSelectionMap.containsKey(player))
		{
			playerSelectionMap.put(player, new Region());
		}

		if (args.length == 0)
		{
			source.sendMessage("placeholder help message");
			return true;
		}

		Region region = playerSelectionMap.get(player);
		switch (args[0])
		{
			case "mark":
				region.setPos(player.getLocation());
				source.sendMessage("Position marked");
				return true;
			case "clear":
				region.clear();
				player.sendMessage("Position cleared");
				return true;
			case "shrink":
			case "expand":
				if (region.getLow() == null) {
					source.sendMessage("Please mark two positions before modifying or exporting the region");return true; }
				if (args.length < 3) {
					source.sendMessage("Please specify a direction and an amount to expand by"); return true;}
				String direction = args[1];
				int value = Integer.parseInt(args[2]);
				if (args[0].equalsIgnoreCase("shrink")) value = -value;
				expand(player, direction, value);
				return true;
			default:
				return false;
		}
	}

	public static void expand(Player source, String direction, Integer value)
	{
		Region region = playerSelectionMap.get(source);
		if (region.getLow() == null)
		{
			source.sendMessage("Please mark two positions before modifying or exporting the region");
			return;
		}
		switch (direction)
		{
			case "south": // positive Z
				region.setHighCorner(new Corner(region.high.x, region.high.y, region.high.z + value));
				break;
			case "north": // negative Z
				region.setLowCorner(new Corner(region.low.x, region.low.y, region.low.z - value));
				break;
			case "east": // positive X
				region.setHighCorner(new Corner(region.high.x + value, region.high.y, region.high.z));
				break;
			case "west": // negative X
				region.setLowCorner(new Corner(region.low.x - value, region.low.y, region.low.z));
				break;
			case "up": // positive y
				region.setHighCorner(new Corner(region.high.x, region.high.y + value, region.high.z));
				break;
			case "down": // negative y
				region.setLowCorner(new Corner(region.low.x, region.low.y - value, region.low.z));
				break;
			default:
				source.sendMessage("Unrecognized direction " + direction);
				return;
		}
		source.sendMessage("Region modified");
	}

	protected static Region getRegionFromObject(int x, int y, int z, BO3 bo3)
	{
		ExportCommand.Region region = new ExportCommand.Region();
		BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
		region.setPos(new BlockPosition(x + box.getMinX(), y + box.getMinY(), z + box.getMinZ()));
		region.setPos(new BlockPosition(
			x + box.getMinX() + box.getWidth(),
			y + box.getMinY() + box.getHeight(),
			z + box.getMinZ() + box.getDepth()));
		return region;
	}

	private static final List<String> flags = Arrays.asList("-a", "-b", "-o");

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		Map<String, String> strings = CommandUtil.parseArgs(args, true);
		
		if (strings.size() > 4) {
			// Return flags
			return flags;
		}
		if (strings.size() == 4)
		{ // Template
			String preset = strings.get("3");
			List<String> list;
			if (preset.equalsIgnoreCase("global"))
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects()
					.getGlobalTemplates(OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
			} else {
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects()
					.getTemplatesForPreset(preset, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
			}
			if (list == null) list = new ArrayList<>();
			list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
			list.add("default");
			String s = strings.get("4");
			return StringUtil.copyPartialMatches(s == null ? "" : s, list, new ArrayList<>());
		}
		if (strings.size() == 3)
		{ // Preset
			String s = strings.get("3");
			return StringUtil.copyPartialMatches(s == null ? "" : s, presetNames, new ArrayList<>());
		}
		//if (strings.size() == 2)
		{ // Center block
			// TODO: tab complete the block parameter
			//return StringUtil.copyPartialMatches(strings.get("2"), );
		}
		// Name - no suggestions
		return new ArrayList<>();
	}

	// if a name includes a space, we wrap it in quotes
	protected static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	protected static Set<String> presetNames = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream()
		.map(filterNamesWithSpaces).collect(Collectors.toSet());

	static {
		presetNames.add("global");
	}

	private static final List<String> directions = Arrays.asList("down", "east", "north", "south", "up", "west");
	private static final List<String> regionSubCommands = Arrays.asList("clear","expand", "mark", "shrink");

	public static List<String> tabCompleteRegion(String[] strings)
	{
		if (strings.length == 2)
		{
			return StringUtil.copyPartialMatches(strings[1], regionSubCommands, new ArrayList<>());
		}

		if (strings[1].equalsIgnoreCase("expand") || strings[1].equalsIgnoreCase("shrink")) {
			if (strings.length == 3)
			{
				return StringUtil.copyPartialMatches(strings[2], directions, new ArrayList<>());
			}
		}

		return new ArrayList<>();
	}

	public static class Region
	{
		private Corner low = null;
		private Corner high = null;
		private final BlockPosition[] posArr = new BlockPosition[2];

		public Region()
		{
			posArr[0] = null;
			posArr[1] = null;
		}

		public void setPos(Location loc)
		{
			setPos(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}

		public void setPos(BlockPosition blockPos)
		{
			if (posArr[0] == null) posArr[0] = blockPos;
			else if (posArr[1] == null)
			{
				posArr[1] = blockPos;
				updateCorners();
			}
			else
			{
				posArr[0] = posArr[1];
				posArr[1] = blockPos;
				updateCorners();
			}
		}

		public void clear()
		{
			posArr[0] = null;
			posArr[1] = null;
			low = null;
			high = null;
		}

		public Corner getLow()
		{
			return low;
		}

		public Corner getHigh()
		{
			return high;
		}

		protected void setLowCorner(Corner newCorner)
		{
			this.low = newCorner;
		}

		protected void setHighCorner(Corner newCorner)
		{
			this.high = newCorner;
		}

		private void updateCorners()
		{
			low = new Corner(
				Math.min(posArr[0].getX(), posArr[1].getX()),
				Math.min(posArr[0].getY(), posArr[1].getY()),
				Math.min(posArr[0].getZ(), posArr[1].getZ())
			);
			high = new Corner(
				Math.max(posArr[0].getX(), posArr[1].getX()),
				Math.max(posArr[0].getY(), posArr[1].getY()),
				Math.max(posArr[0].getZ(), posArr[1].getZ())
			);
		}
	}
}
