package com.example.infinitecurse.curse;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 全局 Tick 驱动（§五/§十）：每游戏秒结算一次 Tick 类诅咒与跨日自动索债。
 */
public final class DebtScheduler {
	private static int counter = 0;

	private DebtScheduler() {
	}

	public static void tick(MinecraftServer server) {
		counter++;
		if (counter % 20 != 0) {
			return; // 约每游戏秒（20 ticks）结算一次
		}
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		long gameTime = overworld == null ? 0 : overworld.getGameTime();
		int day = (int) (gameTime / 24000L);
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			CurseManager.applyTickEffects(p);
			CurseManager.collectDebt(p, day);
			CurseManager.expireTemporary(p, gameTime);
			CurseZone.update(p, gameTime);
		}
	}
}
