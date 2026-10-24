package com.example.infinitecurse.item;

import com.example.infinitecurse.block.CurseBlocks;
import com.example.infinitecurse.block.CurseShopBlock;
import com.example.infinitecurse.block.CurseTableBlock;
import com.example.infinitecurse.fluid.CurseFluid;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * 模组的创造模式物品栏（§建议 1/2 修复）：把全部模组物品与方块集中到一个独立标签页，
 * 不再散落到原版标签页里、也避免"在创造模式找不到诅咒台"的问题。
 */
public final class CurseItemGroups {

	/** 模组专属创造标签页。 */
	public static final CreativeModeTab TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			new ResourceLocation("infinite_curse", "general"),
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 100)
					.title(Component.translatable("itemGroup.infinite_curse.general"))
					.icon(() -> new ItemStack(CurseTableBlock.ITEM))
					.displayItems((params, output) -> {
						// 功能道具
						output.accept(CurseRingItem.INSTANCE);
						output.accept(ReverseDustItem.INSTANCE);
						output.accept(CursefireIngotItem.INSTANCE);
						// 功能方块
						output.accept(CurseTableBlock.ITEM);
						output.accept(CurseShopBlock.ITEM);
						// 诅咒方块系列
						output.accept(CurseBlocks.GRASS.asItem());
						output.accept(CurseBlocks.DIRT.asItem());
						output.accept(CurseBlocks.STONE.asItem());
						output.accept(CurseBlocks.LOG.asItem());
						output.accept(CurseBlocks.PLANKS.asItem());
						output.accept(CurseBlocks.LEAVES.asItem());
						output.accept(CurseBlocks.CRYSTAL.asItem());
						// 诅咒源泉（液体）桶
						if (CurseFluid.BUCKET != null) {
							output.accept(CurseFluid.BUCKET);
						}
					})
					.build());

	private CurseItemGroups() {
	}

	/** 在 onInitialize 显式调用一次，确保标签页在物品/方块注册之后完成登记。 */
	public static void register() {
		// TAB 的静态初始化块已执行 Registry.register，这里仅确保本类被加载。
	}
}
