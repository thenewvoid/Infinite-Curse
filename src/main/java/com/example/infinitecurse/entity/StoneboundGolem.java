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
 * 岩咒傀儡（§新增）：诅咒岩地的重型坦克怪。移动缓慢、生命高、击退抗性高，
 * 命中施加「孱弱」临时诅咒（护甲下降）。击杀额外掉落 1 个咒火锭。
 */
public class StoneboundGolem extends AbstractCurseMob {

	public static final EntityType<StoneboundGolem> TYPE = CurseEntities.build("stonebound_golem",
			(type, level) -> new StoneboundGolem(type, level), 1.4F, 2.4F);

	public StoneboundGolem(EntityType<? extends StoneboundGolem> type, Level level) {
		super(type, level);
		this.xpReward = 12;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.7D, true));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 60.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
				.add(Attributes.FOLLOW_RANGE, 18.0D);
	}

	@Override
	protected String curseAttr() {
		return "armor";
	}

	@Override
	protected String curseEff() {
		return "frail";
	}

	@Override
	protected int curseSeconds() {
		return 25;
	}

	@Override
	protected boolean dropIngot() {
		return true;
	}
}
