package com.example.infinitecurse.entity;

import com.example.infinitecurse.curse.CurseGenerator;
import com.example.infinitecurse.curse.CurseInstance;
import com.example.infinitecurse.curse.CurseManager;
import com.example.infinitecurse.item.CursefireIngotItem;
import com.example.infinitecurse.item.ReverseDustItem;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

/**
 * 诅咒系列怪物的基类（§新增）：所有怪物命中玩家时，通过既有的诅咒引擎
 * 给玩家种下一道"临时诅咒"（属性/效果键取自 variables.json，零新增数据）。
 * 击杀掉落逆咒尘 / 咒火锭——即玩家在千咒台净化诅咒所需的材料，形成闭环。
 */
public abstract class AbstractCurseMob extends Monster {

	/** 怪物命中玩家时施加的诅咒属性键（必须在 variables.json 中存在）。 */
	protected abstract String curseAttr();

	/** 怪物命中玩家时施加的诅咒效果键（必须在 variables.json 中存在）。 */
	protected abstract String curseEff();

	/** 该临时诅咒的持续秒数。 */
	protected abstract int curseSeconds();

	/** 是否在主诅咒之外额外施加一道随机 1 秒临时诅咒（如源咒溺灵）。 */
	protected boolean extraRandomCurse() {
		return false;
	}

	protected AbstractCurseMob(EntityType<? extends AbstractCurseMob> type, net.minecraft.world.level.Level level) {
		super(type, level);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof ServerPlayer sp && !sp.level().isClientSide()) {
			long gameTime = sp.level().getGameTime();
			CurseInstance c = CurseGenerator.generateFixed(this.getType().toString(), curseAttr(), curseEff());
			CurseManager.addTemporaryCurse(sp, c, (long) curseSeconds() * 20L, gameTime);
			sp.sendSystemMessage(Component.literal("§c[咒] 你被「" + c.displayText() + "」缠身！"));
			if (extraRandomCurse()) {
				CurseInstance r = CurseGenerator.generate(1).stream().findFirst().orElse(null);
				if (r != null) {
					CurseManager.addTemporaryCurse(sp, r, 20L, gameTime);
				}
			}
		}
		return hit;
	}

	/** 击杀掉落净化材料（逆咒尘 / 咒火锭），概率由子类微调。 */
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
		super.dropCustomDeathLoot(source, looting, recentlyHit);
		if (!recentlyHit) {
			return;
		}
		int dust = reverseDustCount(looting);
		if (dust > 0) {
			this.spawnAtLocation(new ItemStack(ReverseDustItem.INSTANCE, dust));
		}
		if (dropIngot()) {
			this.spawnAtLocation(new ItemStack(CursefireIngotItem.INSTANCE, 1));
		}
	}

	/** 逆咒尘掉落数量（默认 0~1，可被掠夺附魔加成）。 */
	protected int reverseDustCount(int looting) {
		int base = this.level().random.nextInt(2); // 0 或 1
		return Math.min(4, base + looting);
	}

	/** 是否额外掉落 1 个咒火锭（重型怪物开启）。 */
	protected boolean dropIngot() {
		return false;
	}
}
