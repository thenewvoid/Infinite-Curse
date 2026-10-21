package com.example.infinitecurse.network;

import net.minecraft.resources.ResourceLocation;

/**
 * 网络通道：
 * - SYNC：服务端 → 客户端 的诅咒数据同步（§12 HUD/Toast）。
 * - 以下为 客户端 → 服务端 的界面动作包（§七/§六 图形界面）。
 */
public final class ModNetworking {
	/** 服务端 → 客户端：诅咒列表 + 咒点 + 新诅咒提示 */
	public static final ResourceLocation SYNC = new ResourceLocation("infinite_curse", "sync");
	/** 客户端 → 服务端：千咒台净化 */
	public static final ResourceLocation ALTAR_PURIFY = new ResourceLocation("infinite_curse", "altar_purify");
	/** 客户端 → 服务端：千咒台改写 */
	public static final ResourceLocation ALTAR_REWRITE = new ResourceLocation("infinite_curse", "altar_rewrite");
	/** 客户端 → 服务端：商店签契返利 */
	public static final ResourceLocation SHOP_SIGN = new ResourceLocation("infinite_curse", "shop_sign");
	/** 客户端 → 服务端：商店增咒点 */
	public static final ResourceLocation SHOP_ADD = new ResourceLocation("infinite_curse", "shop_add");

	private ModNetworking() {
	}
}
