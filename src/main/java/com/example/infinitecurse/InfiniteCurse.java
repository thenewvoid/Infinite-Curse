package com.example.infinitecurse;

import com.example.infinitecurse.block.CurseBlocks;
import com.example.infinitecurse.block.CurseShopBlock;
import com.example.infinitecurse.block.CurseTableBlock;
import com.example.infinitecurse.curse.CurseDataLoader;
import com.example.infinitecurse.curse.CurseManager;
import com.example.infinitecurse.curse.CurseZone;
import com.example.infinitecurse.curse.DebtScheduler;
import com.example.infinitecurse.entity.CurseEntities;
import com.example.infinitecurse.fluid.CurseFluid;
import com.example.infinitecurse.network.ModNetworking;
import com.example.infinitecurse.item.CurseRingItem;
import com.example.infinitecurse.item.CursefireIngotItem;
import com.example.infinitecurse.item.ReverseDustItem;
import com.example.infinitecurse.world.CurseWorldGen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

/**
 * 无限诅咒 主入口（§16 命名统一：mod id = infinite_curse，包名 = com.example.infinitecurse）。
 */
public class InfiniteCurse implements ModInitializer {

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "curse_ring"), CurseRingItem.INSTANCE);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "reverse_dust"), ReverseDustItem.INSTANCE);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "cursefire_ingot"), CursefireIngotItem.INSTANCE);
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("infinite_curse", "curse_table"), CurseTableBlock.INSTANCE);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "curse_table"), CurseTableBlock.ITEM);
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("infinite_curse", "curse_shop"), CurseShopBlock.INSTANCE);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("infinite_curse", "curse_shop"), CurseShopBlock.ITEM);

		CurseDataLoader.register();

		// 诅咒方块系列 + 诅咒源泉（液体）+ 主世界诅咒地形自然生成（§新增）
		CurseBlocks.register();
		CurseFluid.register();
		CurseZone.register();
		CurseWorldGen.register();

		// 诅咒系列怪物：注册实体类型与默认属性（§新增）
		CurseEntities.register();

		// 玩家进入：授予戒指 + 10 诅咒 / 重新应用已有诅咒
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				CurseManager.onPlayerJoin(p);
			}
		});

		// 死亡/重生：保留诅咒数据
		ServerPlayerEvents.COPY_FROM.register((oldP, newP, alive) -> CurseManager.copyData(oldP, newP));

		// 全局 Tick：Tick 诅咒 + 自动索债
		ServerTickEvents.END_SERVER_TICK.register((server) -> DebtScheduler.tick(server));

		// 存档 / 读档
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> CurseManager.loadAll());
		ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer server) -> CurseManager.saveAll());
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CurseManager.saveAll());

		// 界面 C2S 动作包（§七/§六）：客户端按钮 -> 服务端执行
		registerReceiver(ModNetworking.ALTAR_PURIFY, CurseManager::altarPurify);
		registerReceiver(ModNetworking.ALTAR_REWRITE, CurseManager::altarRewrite);
		registerReceiver(ModNetworking.SHOP_SIGN, CurseManager::shopSign);
		registerReceiver(ModNetworking.SHOP_ADD, CurseManager::shopAdd);
	}

	private static void registerReceiver(ResourceLocation id,
										java.util.function.Consumer<ServerPlayer> action) {
		ServerPlayNetworking.registerGlobalReceiver(id,
				(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
				 FriendlyByteBuf buf, PacketSender sender) -> action.accept(player));
	}
}
