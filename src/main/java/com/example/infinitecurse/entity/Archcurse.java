package com.example.infinitecurse.entity;

import com.example.infinitecurse.curse.CurseGenerator;
import com.example.infinitecurse.curse.CurseInstance;
import com.example.infinitecurse.curse.CurseManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.EnumSet;

/**
 * 万咒之主（§新增）：诅咒谱系的顶点 Boss。体型庞大、生命极高、攻击沉重、抗击退，
 * 命中时按序列轮替施加不同临时诅咒（移速减半/生命衰减/护甲孱弱/致盲/饥馑/气力/窒息/腐坏），
 * 并周期性释放"诅咒新星"对附近玩家随机下咒。仅在玩家诅咒累积到阈值后于诅咒之地 rare 唤醒。
 */
public class Archcurse extends AbstractCurseMob {

	/** 命中轮替的诅咒序列（属性键, 效果键），全部取自 variables.json。 */
	private static final String[][] CURSE_CYCLE = {
			{"move_speed", "half"},
			{"max_health", "dec"},
			{"armor", "frail"},
			{"vision", "blind"},
			{"hunger", "starve"},
			{"attack", "weak"},
			{"oxygen", "drown"},
			{"regen", "rot"},
	};

	private int curseIndex = 0;

	public static final EntityType<Archcurse> TYPE = CurseEntities.build("archcurse",
			(type, level) -> new Archcurse(type, level), 1.8F, 3.2F);

	public Archcurse(EntityType<? extends Archcurse> type, Level level) {
		super(type, level);
		this.xpReward = 60;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.8D, true));
		this.goalSelector.addGoal(2, new CurseNovaGoal(this));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.4D));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
				this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 200.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 8.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
				.add(Attributes.FOLLOW_RANGE, 28.0D);
	}

	@Override
	protected String curseAttr() {
		return CURSE_CYCLE[curseIndex][0];
	}

	@Override
	protected String curseEff() {
		return CURSE_CYCLE[curseIndex][1];
	}

	@Override
	protected int curseSeconds() {
		return 20;
	}

	@Override
	protected boolean extraRandomCurse() {
		return true;
	}

	@Override
	protected int reverseDustCount(int looting) {
		return 2 + this.level().random.nextInt(3) + looting; // 2~4 + 掠夺
	}

	@Override
	protected boolean dropIngot() {
		return true;
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit) {
			curseIndex = (curseIndex + 1) % CURSE_CYCLE.length;
		}
		return hit;
	}

	/** 诅咒新星：周期性对附近玩家施加一道随机的 3 秒临时诅咒（§新增）。 */
	private static class CurseNovaGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final Archcurse boss;
		private int cooldown = 80;

		CurseNovaGoal(Archcurse boss) {
			this.boss = boss;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.boss.getTarget() instanceof ServerPlayer;
		}

		@Override
		public void tick() {
			if (this.cooldown-- > 0) {
				return;
			}
			this.cooldown = 80;
			LivingEntity t = this.boss.getTarget();
			if (!(t instanceof ServerPlayer sp)) {
				return;
			}
			if (this.boss.distanceTo(t) > 12.0D) {
				return;
			}
			CurseInstance r = CurseGenerator.generate(1).stream().findFirst().orElse(null);
			if (r != null) {
				CurseManager.addTemporaryCurse(sp, r, 60L, sp.level().getGameTime());
				sp.sendSystemMessage(Component.literal("§5[万咒之主] 诅咒新星掠过，你被「" + r.displayText() + "」波及！"));
			}
		}
	}
}
