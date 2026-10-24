package com.example.infinitecurse.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 源咒溺灵（§新增）：仅在诅咒源泉水体附近生成的诅咒怪。可游泳，命中施加「窒息」临时诅咒
 * （水下气息下降），并额外叠加一道随机 1 秒临时诅咒（与诅咒源泉同机制）。
 */
public class SourceDrowned extends AbstractCurseMob {

	public static final EntityType<SourceDrowned> TYPE = CurseEntities.build("source_drowned",
			(type, level) -> new SourceDrowned(type, level), 0.7F, 1.9F);

	public SourceDrowned(EntityType<? extends SourceDrowned> type, Level level) {
		super(type, level);
		this.xpReward = 6;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.7D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 3.5D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 22.0D);
	}

	@Override
	protected String curseAttr() {
		return "oxygen";
	}

	@Override
	protected String curseEff() {
		return "drown";
	}

	@Override
	protected int curseSeconds() {
		return 15;
	}

	@Override
	protected boolean extraRandomCurse() {
		return true;
	}
}
