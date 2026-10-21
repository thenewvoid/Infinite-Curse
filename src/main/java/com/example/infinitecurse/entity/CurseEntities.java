package com.example.infinitecurse.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * 诅咒系列怪物的注册中心（§新增）：构建并注册 4 个 EntityType，并登记各自默认属性。
 * 生成（"涌现式"）由 CurseZone 在诅咒区域/诅咒源泉按配置概率触发，无需额外 SpawnRestriction。
 */
public final class CurseEntities {

	private CurseEntities() {
	}

	/** 构建并注册一个怪物 EntityType（MONSTER 类别，可自然/程序化生成）。 */
	static <T extends Entity> EntityType<T> build(String id, EntityType.EntityFactory<T> factory, float width, float height) {
		EntityType<T> type = FabricEntityTypeBuilder.create(MobCategory.MONSTER, factory)
				.dimensions(EntityDimensions.fixed(width, height))
				.build();
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation("infinite_curse", id), type);
	}

	/** 登记默认属性（必须在实体首次生成前完成，故在 onInitialize 显式调用）。 */
	public static void register() {
		FabricDefaultAttributeRegistry.register(CurseWisp.TYPE, CurseWisp.createAttributes().build());
		FabricDefaultAttributeRegistry.register(RotLurker.TYPE, RotLurker.createAttributes().build());
		FabricDefaultAttributeRegistry.register(StoneboundGolem.TYPE, StoneboundGolem.createAttributes().build());
		FabricDefaultAttributeRegistry.register(SourceDrowned.TYPE, SourceDrowned.createAttributes().build());
		FabricDefaultAttributeRegistry.register(BlindEye.TYPE, BlindEye.createAttributes().build());
		FabricDefaultAttributeRegistry.register(StarvingGhoul.TYPE, StarvingGhoul.createAttributes().build());
		FabricDefaultAttributeRegistry.register(Archcurse.TYPE, Archcurse.createAttributes().build());
	}
}
