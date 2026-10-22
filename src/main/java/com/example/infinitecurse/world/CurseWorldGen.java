package com.example.infinitecurse.world;

import com.example.infinitecurse.block.CurseBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * 主世界诅咒地形自然生成（§新增）：在主世界地表散布"诅咒之森"与"诅咒岩地"地貌斑块，
 * 由诅咒方块构成；玩家踩上去即进入诅咒区域（见 CurseZone）。
 *
 * <p>注意（1.20.1 关键 API）：{@code CONFIGURED_FEATURE} / {@code PLACED_FEATURE} 是
 * <b>动态注册表（数据包注册表）</b>，并不存在于 {@link BuiltInRegistries} 中，
 * 不能在 mod 初始化阶段用 {@code Registry.register(...)} 写入（会得到 null 注册表并 NPE）。
 * 因此这里只把自定义 {@link Feature} 注册进内置的 {@code FEATURE} 注册表，
 * 而 configured / placed 特征通过 {@code data/infinite_curse/worldgen/} 下的 JSON 作为
 * 数据包注册表自动加载；{@link BiomeModifications#addFeature} 在 biome 构建时按
 * {@link ResourceKey} 查找 placed_feature。
 */
public final class CurseWorldGen {

	private CurseWorldGen() {
	}

	public static void register() {
		registerPatch("curse_forest", true);
		registerPatch("curse_rockland", false);
	}

	private static void registerPatch(String name, boolean forest) {
		// 自定义 Feature 注册到内置注册表（mod 初始化阶段可用）
		Feature<NoneFeatureConfiguration> feature = new CursePatchFeature(forest);
		Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation("infinite_curse", name), feature);

		// placed_feature 由 data/infinite_curse/worldgen/placed_feature/<name>_placed.json 提供，
		// 作为动态注册表自动加载；此处仅按 ResourceKey 将其加入主世界地表装饰步骤。
		ResourceKey<PlacedFeature> placedKey = ResourceKey.create(Registries.PLACED_FEATURE,
				new ResourceLocation("infinite_curse", name + "_placed"));
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
