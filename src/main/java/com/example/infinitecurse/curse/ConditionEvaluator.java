package com.example.infinitecurse.curse;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 条件判定（§四 条件类）：根据 condition.type 判断玩家当前是否满足生效条件。
 */
public final class ConditionEvaluator {

	private ConditionEvaluator() {
	}

	public static boolean test(String type, ServerPlayer player) {
		if (type == null) {
			return true;
		}
		Level level = player.level();
		switch (type) {
			case "raining":
				return level.isRaining();
			case "night": {
				long t = level.getDayTime() % 24000L;
				return t >= 13000L && t < 23000L;
			}
			case "day": {
				long t = level.getDayTime() % 24000L;
				return !(t >= 13000L && t < 23000L);
			}
			case "underwater":
				return player.isUnderWater();
			case "underground":
				return player.getBlockY() < 40;
			case "low_health":
				return player.getHealth() <= player.getMaxHealth() * 0.4f;
			case "nether":
				return level.dimension() == Level.NETHER;
			default:
				return true;
		}
	}
}
