package com.example.infinitecurse.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 饥馑噬魂（§新增）：诅咒之森中骨瘦如柴的噬魂。近战，命中施加「饥馑」临时诅咒
 * （持续扣除饱食度）。生命中等、速度中等，掉落略多于普通怪。
 */
public class StarvingGhoul extends AbstractCurseMob {

	public static final EntityType<StarvingGhoul> TYPE = CurseEntities.build("starving_ghoul",
			(type, level) -> new StarvingGhoul(type, level), 0.7F, 1.9F);

	public StarvingGhoul(EntityType<? extends StarvingGhoul> type, Level level) {
		super(type, level);
		this.xpReward = 9;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 22.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.27D)
				.add(Attributes.ATTACK_DAMAGE, 3.5D)
				.add(Attributes.FOLLOW_RANGE, 20.0D);
	}

	@Override
	protected String curseAttr() {
		return "hunger";
	}

	@Override
	protected String curseEff() {
		return "starve";
	}

	@Override
	protected int curseSeconds() {
		return 20;
	}

	@Override
	protected int reverseDustCount(int looting) {
		int base = 1 + this.level().random.nextInt(2); // 1 或 2
		return Math.min(5, base + looting);
	}
}
