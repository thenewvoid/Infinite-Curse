package com.example.infinitecurse.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 咒丝游魂（§新增）：诅咒之森中的飞行骚扰怪。无重力，朝玩家三维移动并发起接触攻击，
 * 命中施加「迟滞」临时诅咒（移速下降）。生命低、速度中等。
 */
public class CurseWisp extends AbstractCurseMob {

	public static final EntityType<CurseWisp> TYPE = CurseEntities.build("curse_wisp",
			(type, level) -> new CurseWisp(type, level), 0.6F, 1.4F);

	public CurseWisp(EntityType<? extends CurseWisp> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.xpReward = 5;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new WispChaseGoal(this));
		this.goalSelector.addGoal(2, new WispDriftGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, net.minecraft.world.entity.player.Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 10.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected String curseAttr() {
		return "slow";
	}

	@Override
	protected String curseEff() {
		return "sluggish";
	}

	@Override
	protected int curseSeconds() {
		return 15;
	}

	/** 追猎：朝目标三维移动，贴近则接触攻击（自带冷却，避免每刻猛击）。 */
	private static class WispChaseGoal extends Goal {
		private final CurseWisp wisp;
		private int attackCd = 0;

		WispChaseGoal(CurseWisp wisp) {
			this.wisp = wisp;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.wisp.getTarget() != null;
		}

		@Override
		public void tick() {
			LivingEntity t = this.wisp.getTarget();
			if (t == null) {
				return;
			}
			if (this.attackCd > 0) {
				this.attackCd--;
			}
			Vec3 to = t.position().subtract(this.wisp.position());
			double dist = to.length();
			if (dist > 0.01D) {
				Vec3 dir = to.normalize().scale(0.18D);
				this.wisp.setDeltaMovement(dir.x, dir.y * 0.6D + (t.getY() > this.wisp.getY() ? 0.04D : -0.02D), dir.z);
			}
			this.wisp.lookAt(t, 360.0F, 360.0F);
			if (dist < 1.5D && this.attackCd == 0) {
				this.wisp.doHurtTarget(t);
				this.attackCd = 20;
			}
		}
	}

	/** 无目标时缓慢悬浮漂移，避免完全静止。 */
	private static class WispDriftGoal extends Goal {
		private final CurseWisp wisp;
		private int timer = 0;
		private Vec3 drift = Vec3.ZERO;

		WispDriftGoal(CurseWisp wisp) {
			this.wisp = wisp;
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return this.wisp.getTarget() == null;
		}

		@Override
		public void tick() {
			if (this.timer-- <= 0) {
				this.timer = 60 + this.wisp.level().random.nextInt(60);
				double a = this.wisp.level().random.nextDouble() * Math.PI * 2.0D;
				this.drift = new Vec3(Math.cos(a) * 0.05D, (this.wisp.level().random.nextDouble() - 0.5D) * 0.04D, Math.sin(a) * 0.05D);
			}
			this.wisp.setDeltaMovement(this.drift);
		}
	}
}
