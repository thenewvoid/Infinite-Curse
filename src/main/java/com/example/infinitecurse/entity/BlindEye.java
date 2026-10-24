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
 * 瞽咒魔眼（§新增）：诅咒之森中的爬行魔眼。近战，命中施加「致盲」临时诅咒
 * （失明药水）。生命中等、速度较快，模型上以悬浮的眼瞳形态呈现。
 */
public class BlindEye extends AbstractCurseMob {

	public static final EntityType<BlindEye> TYPE = CurseEntities.build("blind_eye",
			(type, level) -> new BlindEye(type, level), 0.8F, 1.0F);

	public BlindEye(EntityType<? extends BlindEye> type, Level level) {
		super(type, level);
		this.xpReward = 8;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 16.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.32D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 28.0D);
	}

	@Override
	protected String curseAttr() {
		return "vision";
	}

	@Override
	protected String curseEff() {
		return "blind";
	}

	@Override
	protected int curseSeconds() {
		return 12;
	}
}
