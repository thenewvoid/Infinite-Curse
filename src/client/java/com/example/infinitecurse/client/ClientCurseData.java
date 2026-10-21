package com.example.infinitecurse.client;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端缓存：HUD 与 Toast 读取的诅咒数据（由 S2C 同步包写入）。
 * lastMessage 为最近一次操作反馈，供图形界面展示。
 */
public final class ClientCurseData {
	public static int points = 0;
	public static List<String> curses = new ArrayList<>();
	public static String lastMessage = "";

	private ClientCurseData() {
	}

	public static void set(int points, List<String> curses, String lastMessage) {
		ClientCurseData.points = points;
		ClientCurseData.curses = curses;
		ClientCurseData.lastMessage = lastMessage == null ? "" : lastMessage;
	}
}
