package com.example.infinitecurse.curse;

import com.example.infinitecurse.config.InfiniteCurseConfig;
import com.example.infinitecurse.entity.Archcurse;
import com.example.infinitecurse.entity.BlindEye;
import com.example.infinitecurse.entity.CurseWisp;
import com.example.infinitecurse.entity.RotLurker;
import com.example.infinitecurse.entity.SourceDrowned;
import com.example.infinitecurse.entity.StarvingGhoul;
import com.example.infinitecurse.entity.StoneboundGolem;
import com.example.infinitecurse.fluid.CurseFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.Map;

/**
 * 诅咒区域 / 诅咒源泉 计时与触发（§新增）：
 * - 玩家持续踩在"诅咒方块"上（即诅咒区域），累计超过阈值（默认 2 分半）后，被种下一道
 *   依据所踩方块而定的"固定临时诅咒"（不同方块对应不同诅咒，见 CurseBlocks.register）。
 * - 玩家泡在"诅咒源泉"液体中，每秒被种下一道随机的临时 1 秒诅咒。
 * 区域判定仅依赖方块（与地形自然生成配合），无需特殊生物群系注入。
 */
public final class CurseZone {

	/** 单个诅咒区域的固定诅咒定义。 */
	public static final class ZoneDef {
		public final Block block;
		public final String key;
		public final String attr;
		public final String eff;
		public final String label;

		public ZoneDef(Block block, String key, String attr, String eff, String label) {
			this.block = block;
			this.key = key;
			this.attr = attr;
			this.eff = eff;
			this.label = label;
		}
	}

	/** 方块 -> 区域定义。 */
	public static final Map<Block, ZoneDef> ZONE_BLOCKS = new HashMap<>();
	/** key -> 区域定义（用于按区域标识取回定义）。 */
	public static final Map<String, ZoneDef> ZONE_BY_KEY = new HashMap<>();

	private CurseZone() {
	}

	public static void addZone(Block block, String key, String attr, String eff, String label) {
		ZoneDef def = new ZoneDef(block, key, attr, eff, label);
		ZONE_BLOCKS.put(block, def);
		ZONE_BY_KEY.put(key, def);
	}

	/**
	 * 注册入口（由 InfiniteCurse.onInitialize 调用）。
	 * 区域映射（方块 -> 固定诅咒）已在 CurseBlocks.register() 中通过 addZone 登记，
	 * 故此处无需额外逻辑；保留方法以满足统一注册约定。
	 */
	public static void register() {
		// 区域定义见 CurseBlocks.register()
	}

	/** 每游戏秒调用一次：更新某玩家的区域/液体计时并触发诅咒。 */
	public static void update(ServerPlayer player, long gameTime) {
		if (player == null || player.level() == null) {
			return;
		}
		CurseManager.PlayerCurseData data = CurseManager.get(player.getUUID());
		Level level = player.level();
		BlockPos feet = player.blockPosition();
		BlockPos eye = new BlockPos((int) player.getX(), (int) player.getEyeY(), (int) player.getZ());

		// 1) 区域判定：脚下方块 / 身体所在方块 是否为诅咒方块
		BlockState below = level.getBlockState(feet.below());
		BlockState at = level.getBlockState(feet);
		ZoneDef def = ZONE_BLOCKS.get(below.getBlock());
		if (def == null) {
			def = ZONE_BLOCKS.get(at.getBlock());
		}

		// 2) 液体判定：脚/眼位置是否处于诅咒源泉流体
		boolean inLiquid = isCurseFluid(level, feet) || isCurseFluid(level, eye);

		InfiniteCurseConfig cfg = InfiniteCurseConfig.get();
		int threshold = cfg.zoneThresholdSeconds > 0 ? cfg.zoneThresholdSeconds : 150;
		int warn = cfg.zoneWarnSeconds > 0 ? cfg.zoneWarnSeconds : 120;

		// 3) 区域累计 -> 固定临时诅咒
		if (def != null) {
			if (def.key.equals(data.zoneKey)) {
				data.zoneTicks++;
			} else {
				data.zoneKey = def.key;
				data.zoneTicks = 1;
				data.zoneWarned = false;
			}
			if (!data.zoneWarned && data.zoneTicks >= warn) {
				player.sendSystemMessage(Component.literal("§5[诅咒之地] 你感到诅咒在脚下缓缓积聚……（已停留 " + data.zoneTicks + " 秒）"));
				data.zoneWarned = true;
			}
			if (data.zoneTicks >= threshold) {
				CurseInstance c = CurseGenerator.generateFixed(def.key, def.attr, def.eff);
				int dur = (cfg.zoneCurseSeconds > 0 ? cfg.zoneCurseSeconds : 60) * 20;
				CurseManager.addTemporaryCurse(player, c, dur, gameTime);
				player.sendSystemMessage(Component.literal(
						"§c[诅咒之地] 长久的停留唤醒了「" + c.displayText() + "」——踩在" + def.label + "之上的代价（临时诅咒）。"));
				data.zoneTicks = 0;
				data.zoneWarned = false;
			}
		} else {
			data.zoneKey = null;
			data.zoneTicks = 0;
			data.zoneWarned = false;
		}

		// 4) 液体：每秒随机临时 1 秒诅咒
		if (inLiquid) {
			CurseInstance c = CurseGenerator.generate(1).stream().findFirst().orElse(null);
			if (c != null) {
				int dur = (cfg.liquidCurseSeconds > 0 ? cfg.liquidCurseSeconds : 1) * 20;
				CurseManager.addTemporaryCurse(player, c, dur, gameTime);
			}
		}

		// 5) 诅咒系列怪物"涌现式"生成（§新增）：在诅咒区域/液体附近按概率召唤对应怪物
		maybeSpawnMob(player, level, data, def, inLiquid, cfg);
		// 6) 万咒之主（Boss）稀有唤醒：玩家诅咒累积到阈值后，按低概率降临（§新增）
		maybeSpawnBoss(player, level, data, cfg);
	}

	/**
	 * 在玩家附近的诅咒区域/诅咒源泉按配置概率生成一只对应怪物（§新增）。
	 * - 诅咒之森（草/泥/原木/木板/树叶）：咒丝游魂（飞行）/ 腐咒潜伏者（地面）/ 瞽咒魔眼（致盲）/ 饥馑噬魂（饥馑）四选一
	 * - 诅咒岩地（石/晶）：岩咒傀儡
	 * - 诅咒源泉液体：源咒溺灵
	 * 仅在 CurseZone.update 每游戏秒调用一次，配合 data.mobSpawnCooldown 控制频率。
	 */
	private static void maybeSpawnMob(ServerPlayer player, Level level, CurseManager.PlayerCurseData data,
									  ZoneDef def, boolean inLiquid, InfiniteCurseConfig cfg) {
		if (!cfg.mobSpawnEnabled) {
			return;
		}
		if (data.mobSpawnCooldown > 0) {
			data.mobSpawnCooldown--;
			return;
		}
		if (level.random.nextDouble() > cfg.mobSpawnChance) {
			return;
		}

		double ang = level.random.nextDouble() * Math.PI * 2.0D;
		int r = 3 + level.random.nextInt(Math.max(1, cfg.mobSpawnRadius - 3));
		BlockPos base = player.blockPosition().offset((int) (Math.cos(ang) * r), 0, (int) (Math.sin(ang) * r));

		Mob mob;
		if (inLiquid || def == null) {
			mob = new SourceDrowned(SourceDrowned.TYPE, level);
		} else if ("stone".equals(def.key) || "crystal".equals(def.key)) {
			mob = new StoneboundGolem(StoneboundGolem.TYPE, level);
		} else {
			// 诅咒之森：游魂 / 潜伏者 / 魔眼 / 噬魂 四选一
			int pick = level.random.nextInt(4);
			if (pick == 0) {
				mob = new CurseWisp(CurseWisp.TYPE, level);
			} else if (pick == 1) {
				mob = new RotLurker(RotLurker.TYPE, level);
			} else if (pick == 2) {
				mob = new BlindEye(BlindEye.TYPE, level);
			} else {
				mob = new StarvingGhoul(StarvingGhoul.TYPE, level);
			}
		}

		double px = base.getX() + 0.5D;
		double pz = base.getZ() + 0.5D;
		double py;
		if (mob instanceof CurseWisp) {
			py = player.getY() + 2.0D;
		} else {
			py = base.getY();
			for (int i = 0; i < 6; i++) {
				BlockState s = level.getBlockState(new BlockPos(base.getX(), base.getY() - i, base.getZ()));
				if (s.isSolid()) {
					py = base.getY() - i + 1;
					break;
				}
			}
		}
		mob.moveTo(px, py, pz, level.random.nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(mob);
		data.mobSpawnCooldown = cfg.mobSpawnCooldownSeconds;
		player.sendSystemMessage(Component.literal("§5[诅咒之地] 一股诅咒凝聚成形，向你袭来……"));
	}

	/**
	 * 万咒之主（Boss）稀有唤醒（§新增）：当玩家身上累积的诅咒数量达到阈值，按极低概率在最靠近的
	 * 诅咒区域/附近地面降临。受 bossCooldown 严格限频（默认 15 分钟），不会频繁骚扰。
	 */
	private static void maybeSpawnBoss(ServerPlayer player, Level level, CurseManager.PlayerCurseData data,
									  InfiniteCurseConfig cfg) {
		if (!cfg.mobSpawnEnabled) {
			return;
		}
		if (data.bossCooldown > 0) {
			data.bossCooldown--;
			return;
		}
		if (data.curses.size() < cfg.bossCurseThreshold) {
			return;
		}
		if (level.random.nextDouble() > cfg.bossSpawnChance) {
			return;
		}

		double ang = level.random.nextDouble() * Math.PI * 2.0D;
		int r = 4 + level.random.nextInt(Math.max(1, cfg.mobSpawnRadius - 2));
		BlockPos base = player.blockPosition().offset((int) (Math.cos(ang) * r), 0, (int) (Math.sin(ang) * r));

		Archcurse boss = new Archcurse(Archcurse.TYPE, level);
		double px = base.getX() + 0.5D;
		double pz = base.getZ() + 0.5D;
		double py = base.getY();
		for (int i = 0; i < 6; i++) {
			BlockState s = level.getBlockState(new BlockPos(base.getX(), base.getY() - i, base.getZ()));
			if (s.isSolid()) {
				py = base.getY() - i + 1;
				break;
			}
		}
		boss.moveTo(px, py, pz, level.random.nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(boss);
		data.bossCooldown = cfg.bossSpawnCooldownSeconds;
		player.sendSystemMessage(Component.literal("§4[万咒之主] 你身上的诅咒已凝成实体——万咒之主自虚空中降临！"));
	}

	private static boolean isCurseFluid(BlockGetter level, BlockPos pos) {
		FluidState fs = level.getFluidState(pos);
		return fs != null && (fs.getType() == CurseFluid.Source.INSTANCE || fs.getType() == CurseFluid.Flowing.INSTANCE);
	}
}
