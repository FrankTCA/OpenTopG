package com.pg85.otg.forge.materials;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ResourceLocationException;

public class ForgeMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{ 
		// If otg: or no domain was supplied, try OTG tags.

		// TODO: This might not be updated correctly, needs another look --auth
		TagKey<Block> optTag;
		if(!name.contains(":") || name.startsWith(Constants.MOD_ID_SHORT + ":"))
		{
			final ResourceLocation otgResourceLocation;
			try
			{
				otgResourceLocation = new ResourceLocation(Constants.MOD_ID_SHORT + ":" + name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", ""));
				optTag = BlockTags.create(otgResourceLocation);
				if(optTag != null)
				{
					return new ForgeMaterialTag(optTag, otgResourceLocation.toString());
				}
			} catch(ResourceLocationException ex) { }
		}
		
		final ResourceLocation resourceLocation;
		try
		{
			resourceLocation = new ResourceLocation(name.trim().toLowerCase());
			optTag = BlockTags.create(resourceLocation);
			if(optTag != null)
			{
				return new ForgeMaterialTag(optTag, resourceLocation.toString());
			}
		} catch(ResourceLocationException ex) { }
		
		return null;
	}

	private final String name;
	private final TagKey<Block> blockTag;

	private ForgeMaterialTag(TagKey<Block> blockTag, String name)
	{
		this.blockTag = blockTag;
		this.name = name;
	}

	public TagKey<Block> getTag()
	{
		return this.blockTag;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
