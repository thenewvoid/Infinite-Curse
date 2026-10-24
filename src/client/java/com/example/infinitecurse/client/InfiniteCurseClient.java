package com.example.infinitecurse.client;

import com.example.infinitecurse.block.CurseShopBlock;
import com.example.infinitecurse.block.CurseTableBlock;
import com.example.infinitecurse.client.entity.CurseEntityRenderers;
import com.example.infinitecurse.network.ModNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端入口：注册 HUD 渲染、S2C 同步包接收、以及右击方块打开图形界面（§12/§七/§六）。
 */
public class InfiniteCurseClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// 诅咒系列怪物（§新增）：几何模型层 + 实体渲染器
		CurseEntityRenderers.register();

		// 诅咒源泉流体渲染（§新增）
		CurseFluidRenderHandler fluidRenderHandler = new CurseFluidRenderHandler();
		FluidRenderHandlerRegistry.INSTANCE.register(com.example.infinitecurse.fluid.CurseFluid.Source.INSTANCE, fluidRenderHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(com.example.infinitecurse.fluid.CurseFluid.Flowing.INSTANCE, fluidRenderHandler);

		// 接收服务端同步：写入客户端缓存，必要时弹出 Toast
		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SYNC, (client, handler, buf, sender) -> {
			int points = buf.readVarInt();
			int n = buf.readVarInt();
			List<String> list = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				list.add(buf.readUtf());
			}
			String toast = buf.readUtf();
			ClientCurseData.set(points, list, toast);
			if (!toast.isEmpty()) {
				Minecraft.getInstance().getToasts().addToast(new CurseToast("§5新诅咒降临", toast));
			}
		});

		// 右击 千咒台 / 商店 打开图形界面（客户端拦截，避免服务端直接执行）
		UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hit) -> {
			BlockState state = world.getBlockState(hit.getBlockPos());
			// 必须在渲染线程上打开屏幕：Minecraft.setScreen 会先把 this.screen 指向新屏幕、
			// 再调用其 init()（fabric 的事件即在 init 的 mixin 中初始化）。若此回调不在渲染线程
			// 执行，渲染线程可能在 init() 跑完前就渲染该屏幕，导致 beforeRenderEvent 为 null 而崩溃。
			// 用 execute(...) 把开屏推迟到渲染线程下一帧，确保 init() 先完成（§修复 2026-10-23）。
			Minecraft client = Minecraft.getInstance();
			if (state.getBlock() == CurseTableBlock.INSTANCE) {
				client.execute(() -> client.setScreen(new CurseScreen("altar")));
				return InteractionResult.SUCCESS;
			}
			if (state.getBlock() == CurseShopBlock.INSTANCE) {
				client.execute(() -> client.setScreen(new CurseScreen("shop")));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		// 常驻 HUD：右上角咒点徽标 + 诅咒列表
		HudRenderCallback.EVENT.register((GuiGraphics gui, float tickDelta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			Font font = mc.font;
			int width = mc.getWindow().getGuiScaledWidth();
			int x = width - 176;
			int y = 10;
			int w = 168;
			List<String> curses = ClientCurseData.curses;
			int rows = Math.min(curses.size(), 12);
			int h = 18 + rows * 11 + 6;
			gui.fill(x, y, x + w, y + h, 0x90000000);
			gui.fill(x, y, x + 4, y + h, 0xFF7B2FBE);
			gui.drawString(font, Component.literal("§6咒点: " + ClientCurseData.points), x + 8, y + 4, 0xFFE0A0, true);
			int yy = y + 18;
			for (int i = 0; i < rows; i++) {
				gui.drawString(font, Component.literal(curses.get(i)), x + 8, yy, 0xFF9C9CFF, true);
				yy += 11;
			}
		});
	}
}
