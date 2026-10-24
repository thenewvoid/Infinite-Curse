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
 * 腐咒潜伏者（§新增）：诅咒之森地面的伏击怪。近战，命中施加「腐坏」临时诅咒（持续掉血）。
 * 中等生命、中速。
 */
public class RotLurker extends AbstractCurseMob {

	public static final EntityType<RotLurker> TYPE = CurseEntities.build("rot_lurker",
			(type, level) -> new RotLurker(type, level), 0.8F, 1.8F);

	public RotLurker(EntityType<? extends RotLurker> type, Level level) {
		super(type, level);
		this.xpReward = 7;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, true));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 18.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.26D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 20.0D);
	}

	@Override
	protected String curseAttr() {
		return "regen";
	}

	@Override
	protected String curseEff() {
		return "rot";
	}

	@Override
	protected int curseSeconds() {
		return 20;
	}
}
