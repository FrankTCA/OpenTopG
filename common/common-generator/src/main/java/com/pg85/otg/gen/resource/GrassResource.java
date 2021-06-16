package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.gen.resource.util.PlantType;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class GrassResource  extends ResourceBase implements IBasicResource
{
	private static enum GroupOption
	{
		Grouped,
		NotGrouped
	}

	private final int frequency;
	private final double rarity;
	private GroupOption groupOption;
	private PlantType plant;
	private final MaterialSet sourceBlocks;

	public GrassResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(5, args);

		// The syntax for the first to arguments used to be blockId,blockData
		// Then it became plantType,unusedParam (plantType can still be
		// blockId:blockData)
		// Now it is plantType,groupOption
		this.groupOption = GroupOption.NotGrouped;
		String secondArgument = args.get(1);
		try
		{
			// Test whether the second argument is the data value (deprecated)
			readInt(secondArgument, 0, 16);
			// If so, parse it
			this.plant = PlantType.getPlant(args.get(0) + ":" + secondArgument, materialReader);
		} catch (InvalidConfigException e)
		{
			// Nope, second argument is not a number
			this.plant = PlantType.getPlant(args.get(0), materialReader);
			if (secondArgument.equalsIgnoreCase(GroupOption.Grouped.toString()))
			{
				this.groupOption = GroupOption.Grouped;
				// For backwards compatibility, the second argument is not checked further
			}
		}

		this.frequency = readInt(args.get(2), 1, 500);
		this.rarity = readRarity(args.get(3));
		this.sourceBlocks = readMaterials(args, 4, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
	{
		switch (this.groupOption)
		{
			case Grouped:
				spawnGrouped(worldGenRegion, random, chunkBeingPopulated);
				break;
			case NotGrouped:
				spawnNotGrouped(worldGenRegion, random, chunkBeingPopulated);
				break;
		}
	}
	
	private void spawnGrouped(IWorldGenRegion worldGenregion, Random random, ChunkCoordinate chunkBeingPopulated)
	{
		if (random.nextDouble() * 100.0 <= this.rarity)
		{
			// Passed Rarity test, place about Frequency grass in this chunk
			int centerX = chunkBeingPopulated.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			int centerZ = chunkBeingPopulated.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			int centerY = worldGenregion.getHighestBlockAboveYAt(centerX, centerZ, chunkBeingPopulated);
			
			if(centerY < Constants.WORLD_DEPTH)
			{
				return;
			}
			
			LocalMaterialData worldMaterial;

			// Fix y position
			while (
				(
					(centerY >= Constants.WORLD_DEPTH && centerY < Constants.WORLD_HEIGHT) &&
					(worldMaterial = worldGenregion.getMaterial(centerX, centerY, centerZ, chunkBeingPopulated)) != null &&
					(
						worldMaterial.isAir() || 
						worldMaterial.isLeaves()
					) &&
					(worldMaterial = worldGenregion.getMaterial(centerX, centerY - 1, centerZ, chunkBeingPopulated)) != null
				) && (
					centerY > 0
				)
			)
			{
				centerY--;
			}
			centerY++;

			// Try to place grass
			// Because of the changed y position, only one in four attempts
			// will have success
			int x;
			int y;
			int z;
			for (int i = 0; i < this.frequency * 4; i++)
			{
				x = centerX + random.nextInt(8) - random.nextInt(8);
				y = centerY + random.nextInt(4) - random.nextInt(4);
				z = centerZ + random.nextInt(8) - random.nextInt(8);
				if (
					(worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) != null && 
					worldMaterial.isAir() &&
					(
						(worldMaterial = worldGenregion.getMaterial(x, y - 1, z, chunkBeingPopulated)) != null && 
						this.sourceBlocks.contains(worldMaterial)
					)
				)
				{
					this.plant.spawn(worldGenregion, x, y, z, chunkBeingPopulated);
				}
			}
		}
	}

	private void spawnNotGrouped(IWorldGenRegion worldGenregion, Random random, ChunkCoordinate chunkBeingPopulated)
	{
		LocalMaterialData worldMaterial;
		int x;
		int z;
		int y;
		for (int t = 0; t < this.frequency; t++)
		{
			if (random.nextInt(100) >= this.rarity)
			{
				continue;
			}
			
			x = chunkBeingPopulated.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			z = chunkBeingPopulated.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			y = worldGenregion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);

			if(y < Constants.WORLD_DEPTH)
			{
				return;
			}
			
			while (
				(
					(worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) != null &&
					(
						worldMaterial.isAir() || 
						worldMaterial.isLeaves()
					) &&
					(worldMaterial = worldGenregion.getMaterial(x, y - 1, z, chunkBeingPopulated)) != null
				) && 
				y > 0
			)
			{
				y--;
			}

			if (					
				(
					(worldMaterial = worldGenregion.getMaterial(x, y + 1, z, chunkBeingPopulated)) == null ||
					!worldMaterial.isAir()
				) || (
					(worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) == null ||
					!this.sourceBlocks.contains(worldMaterial)
				)
			)
			{
				continue;
			}
			this.plant.spawn(worldGenregion, x, y + 1, z, chunkBeingPopulated);
		}
	}
	
	@Override
	public String toString()
	{
		return "Grass(" + this.plant.getName() + "," + this.groupOption + "," + this.frequency + "," + this.rarity + makeMaterials(this.sourceBlocks) + ")";
	}	
}