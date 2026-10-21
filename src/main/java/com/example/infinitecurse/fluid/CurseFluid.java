package com.example.infinitecurse.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * 液态诅咒——诅咒源泉（§新增）：踩/泡在其中每秒获得一道随机临时 1 秒诅咒。
 * 使用 vanilla FlowingFluid 的常规自定义流体写法（Source / Flowing 双态）。
 * 贴图由客户端 CurseFluidRenderHandler 提供，故此处不重写 getStillTexture / getFlowingTexture。
 */
public abstract class CurseFluid extends FlowingFluid {

	public static final ResourceLocation STILL_TEX = new ResourceLocation("infinite_curse", "fluid/curse_source_still");
	public static final ResourceLocation FLOW_TEX = new ResourceLocation("infinite_curse", "fluid/curse_source_flow");

	public static LiquidBlock BLOCK;
	public static BucketItem BUCKET;

	@Override
	public Fluid getSource() {
		return Source.INSTANCE;
	}

	@Override
	public Fluid getFlowing() {
		return Flowing.INSTANCE;
	}

	@Override
	public Item getBucket() {
		return BUCKET;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, 0);
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	protected boolean canConvertToSource(Level world) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
		// 诅咒源泉不会在流动时破坏方块
	}

	@Override
	public float getExplosionResistance() {
		return 100F;
	}

	@Override
	public int getTickDelay(LevelReader world) {
		return 5;
	}

	@Override
	protected int getSlopeFindDistance(LevelReader world) {
		return 4;
	}

	@Override
	protected int getDropOff(LevelReader world) {
		return 1;
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == Source.INSTANCE || fluid == Flowing.INSTANCE;
	}

	public static class Source extends CurseFluid {
		public static final Source INSTANCE = new Source();

		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends CurseFluid {
		public static final Flowing INSTANCE = new Flowing();

		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(FlowingFluid.LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(FlowingFluid.LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static void register() {
		BLOCK = new LiquidBlock(Source.INSTANCE, BlockBehaviour.Properties.of()
				.noCollission().strength(100F).replaceable().noLootTable());
		BUCKET = new BucketItem(Source.INSTANCE, new Item.Properties()
				.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1));
		Registry.register(BuiltInRegistries.FLUID, new ResourceLocation("infinite_curse", "curse_source"), Source.INSTANCE);
		Registry.register(BuiltInRegistries.FLUID, new ResourceLocation("infinite_curse", "flowing_curse_source"), Flowing.INSTANCE);
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("infinite_curse", "curse_source"), BLOCK);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "curse_source_bucket"), BUCKET);
	}
}
