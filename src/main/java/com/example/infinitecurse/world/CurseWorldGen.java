package com.example.infinitecurse.world;

import com.example.infinitecurse.block.CurseBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

/**
 * 主世界诅咒地形自然生成（§新增）：在主世界地表散布"诅咒之森"与"诅咒岩地"地貌斑块，
 * 由诅咒方块构成；玩家踩上去即进入诅咒区域（见 CurseZone）。
 */
public final class CurseWorldGen {

	private CurseWorldGen() {
	}

	public static void register() {
		registerPatch("curse_forest", true);
		registerPatch("curse_rockland", false);
	}

	@SuppressWarnings("unchecked")
	private static void registerPatch(String name, boolean forest) {
		Feature<NoneFeatureConfiguration> feature = new CursePatchFeature(forest);
		Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation("infinite_curse", name), feature);

		ResourceKey<ConfiguredFeature<?, ?>> cfgKey = ResourceKey.create(Registries.CONFIGURED_FEATURE,
				new ResourceLocation("infinite_curse", name + "_cfg"));
		ConfiguredFeature<?, ?> configured = new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE);

		ResourceKey<PlacedFeature> placedKey = ResourceKey.create(Registries.PLACED_FEATURE,
				new ResourceLocation("infinite_curse", name + "_placed"));

		// 该映射下 BuiltInRegistries 未暴露 CONFIGURED_FEATURE / PLACED_FEATURE 静态字段，
		// 但二者作为内置注册表存在于 REGISTRY（注册表之注册表）中，可通过 ResourceKey 取得。
		// 使用裸类型避免 ResourceKey<Registry<...>> 与 ResourceKey<Registry<?>> 的不变性冲突。
		@SuppressWarnings("rawtypes")
		Registry rootReg = (Registry) BuiltInRegistries.REGISTRY;
		@SuppressWarnings("rawtypes")
		Registry confReg = (Registry) rootReg.get(Registries.CONFIGURED_FEATURE);
		@SuppressWarnings("rawtypes")
		Registry placedReg = (Registry) rootReg.get(Registries.PLACED_FEATURE);

		Registry.register(confReg, cfgKey, configured);

		List<PlacementModifier> modifiers = List.of(
				RarityFilter.onAverageOnceEvery(forest ? 220 : 320),
				InSquarePlacement.spread(),
				HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
				BiomeFilter.biome());
		PlacedFeature placed = new PlacedFeature(Holder.direct(configured), modifiers);
		Registry.register(placedReg, placedKey, placed);

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.SURFACE_STRUCTURES,
				placedKey);
	}

	/** 在指定位置生成一片诅咒地表（森林 / 岩地 两种形态）。 */
	public static class CursePatchFeature extends Feature<NoneFeatureConfiguration> {
		private final boolean forest;

		public CursePatchFeature(boolean forest) {
			super(NoneFeatureConfiguration.CODEC);
			this.forest = forest;
		}

		@Override
		public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
			WorldGenLevel level = context.level();
			BlockPos origin = context.origin();
			RandomSource random = context.random();
			int radius = forest ? 5 : 6;
			BlockState surfaceBlock = forest ? CurseBlocks.GRASS.defaultBlockState() : CurseBlocks.STONE.defaultBlockState();
			BlockState underBlock = forest ? CurseBlocks.DIRT.defaultBlockState() : CurseBlocks.STONE.defaultBlockState();

			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dz * dz > radius * radius) {
						continue;
					}
					BlockPos column = new BlockPos(origin.getX() + dx, 0, origin.getZ() + dz);
					BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, column);
					if (top.getY() < level.getMinBuildHeight() + 1) {
						continue;
					}
					BlockState existing = level.getBlockState(top);
					if (existing.isAir() || existing.getBlock() == Blocks.WATER || existing.getBlock() == Blocks.LAVA) {
						continue;
					}
					level.setBlock(top, surfaceBlock, 2);
					level.setBlock(top.below(), underBlock, 2);
					level.setBlock(top.below(2), CurseBlocks.STONE.defaultBlockState(), 2);
				}
			}

			if (forest) {
				int trees = 2 + random.nextInt(4);
				for (int i = 0; i < trees; i++) {
					int tx = origin.getX() + random.nextInt(radius * 2) - radius;
					int tz = origin.getZ() + random.nextInt(radius * 2) - radius;
					BlockPos base = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG,
							new BlockPos(tx, 0, tz)).above();
					int h = 4 + random.nextInt(3);
					for (int y = 0; y < h; y++) {
						level.setBlock(base.above(y), CurseBlocks.LOG.defaultBlockState(), 2);
					}
					BlockPos crown = base.above(h);
					for (int cx = -2; cx <= 2; cx++) {
						for (int cz = -2; cz <= 2; cz++) {
							if (cx * cx + cz * cz <= 4) {
								level.setBlock(crown.offset(cx, 0, cz), CurseBlocks.LEAVES.defaultBlockState(), 2);
							}
						}
					}
				}
			}
			return true;
		}
	}
}
