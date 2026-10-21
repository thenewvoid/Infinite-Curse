package com.example.infinitecurse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 模组配置（§11.5）。从 <游戏目录>/config/infinite_curse.json 读取，缺失则写入默认值。
 * difficultyMultiplier 默认 1.0 / autoDebtEnabled 默认 true / altarCostMultiplier 默认 1.0 / maxCursesPerDay 默认 2。
 */
public final class InfiniteCurseConfig {
	public double difficultyMultiplier = 1.0;
	public boolean autoDebtEnabled = true;
	public double altarCostMultiplier = 1.0;
	public int maxCursesPerDay = 2;

	// ===== 诅咒之地 / 诅咒源泉（§新增） =====
	/** 在诅咒区域连续停留多少秒后被种下固定临时诅咒（默认 150 秒 = 2 分半）。 */
	public int zoneThresholdSeconds = 150;
	/** 停留到第几秒时给出"诅咒积聚"预警（默认 120 秒）。 */
	public int zoneWarnSeconds = 120;
	/** 区域固定临时诅咒持续秒数（默认 60 秒）。 */
	public int zoneCurseSeconds = 60;
	/** 泡在诅咒源泉中每秒获得的随机临时诅咒持续秒数（默认 1 秒）。 */
	public int liquidCurseSeconds = 1;

	// ===== 诅咒系列怪物（§新增） =====
	/** 是否在诅咒区域/诅咒源泉开启"涌现式"生成怪物。 */
	public boolean mobSpawnEnabled = true;
	/** 玩家处于诅咒区域时，每游戏秒生成一只怪物的概率（默认 2%）。 */
	public double mobSpawnChance = 0.02;
	/** 同一玩家两次生成之间的最小间隔秒数（默认 6 秒）。 */
	public int mobSpawnCooldownSeconds = 6;
	/** 怪物在玩家周围的生成半径（默认 6 米）。 */
	public int mobSpawnRadius = 6;

	// ===== 万咒之主（Boss）稀有唤醒（§新增） =====
	/** 玩家身上累积的诅咒数量达到该阈值后，Boss 才有概率被唤醒（默认 14）。 */
	public int bossCurseThreshold = 14;
	/** 达到阈值后，每游戏秒唤醒 Boss 的概率（默认 0.8%）。 */
	public double bossSpawnChance = 0.008;
	/** 两次 Boss 唤醒之间的最小间隔秒数（默认 900 秒 = 15 分钟）。 */
	public int bossSpawnCooldownSeconds = 900;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static InfiniteCurseConfig INSTANCE;

	public static InfiniteCurseConfig get() {
		if (INSTANCE == null) INSTANCE = load();
		return INSTANCE;
	}

	private static InfiniteCurseConfig load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		Path file = dir.resolve("infinite_curse.json");
		InfiniteCurseConfig cfg = new InfiniteCurseConfig();
		if (Files.exists(file)) {
			try {
				InfiniteCurseConfig parsed = GSON.fromJson(Files.readString(file), InfiniteCurseConfig.class);
				if (parsed != null) cfg = parsed;
			} catch (Exception ignored) {
				// 解析失败则保留默认值
			}
		}
		try {
			Files.writeString(file, GSON.toJson(cfg));
		} catch (IOException ignored) {
			// 写入失败不影响运行
		}
		return cfg;
	}

	/** 返回经过难度系数缩放后的数值。 */
	public double scaled(double base) {
		return base * difficultyMultiplier;
	}
}
