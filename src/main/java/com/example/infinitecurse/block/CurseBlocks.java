package com.example.infinitecurse.block;

import com.example.infinitecurse.curse.CurseZone;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

/**
 * 诅咒方块系列（§新增）：诅咒草 / 诅咒泥 / 诅咒石 / 诅咒原木 / 诅咒木板 / 诅咒树叶 / 诅咒晶。
 * 同时作为"诅咒区域"的地基——玩家踩在其上累计停留会被种下固定临时诅咒（见 CurseZone）。
 * 自然生成见 CurseWorldGen（诅咒之森 / 诅咒岩地）。
 */
public final class CurseBlocks {

	public static final CurseBlock GRASS = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(0.6F).sound(SoundType.GRASS));
	public static final CurseBlock DIRT = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(0.5F).sound(SoundType.GRAVEL));
	public static final CurseBlock STONE = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(1.5F).sound(SoundType.STONE));
	public static final CurseBlock LOG = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(2.0F).sound(SoundType.WOOD));
	public static final CurseBlock PLANKS = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(2.0F).sound(SoundType.WOOD));
	public static final CurseBlock LEAVES = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(0.2F).sound(SoundType.GRASS).noOcclusion());
	public static final CurseBlock CRYSTAL = new CurseBlock(BlockBehaviour.Properties.of()
			.strength(1.5F).sound(SoundType.AMETHYST).lightLevel(state -> 10));

	private CurseBlocks() {
	}

	public static void register() {
		registerBlock("curse_grass", GRASS);
		registerBlock("curse_dirt", DIRT);
		registerBlock("curse_stone", STONE);
		registerBlock("curse_log", LOG);
		registerBlock("curse_planks", PLANKS);
		registerBlock("curse_leaves", LEAVES);
		registerBlock("curse_crystal", CRYSTAL);
		// 区域映射：踩在不同方块上会种下不同的"固定"临时诅咒
		CurseZone.addZone(GRASS, "grass", "move_speed", "sluggish", "迟缓诅咒");
		CurseZone.addZone(DIRT, "dirt", "mining_speed", "fatigue", "乏力诅咒");
		CurseZone.addZone(STONE, "stone", "armor", "frail", "孱弱诅咒");
		CurseZone.addZone(LOG, "log", "max_health", "dec", "衰减诅咒");
		CurseZone.addZone(PLANKS, "planks", "attack", "frail", "孱弱诅咒");
		CurseZone.addZone(LEAVES, "leaves", "vision", "blind", "致盲诅咒");
		CurseZone.addZone(CRYSTAL, "crystal", "attack", "zero", "归零诅咒");
	}

	private static void registerBlock(String id, Block block) {
		ResourceLocation rl = new ResourceLocation("infinite_curse", id);
		Registry.register(BuiltInRegistries.BLOCK, rl, block);
		Registry.register(BuiltInRegistries.ITEM, rl, new BlockItem(block, new Item.Properties()));
	}
}
