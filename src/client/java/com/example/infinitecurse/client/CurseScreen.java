package com.example.infinitecurse.client;

import com.example.infinitecurse.network.ModNetworking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.Unpooled;

/**
 * 千咒台 / 无限诅咒商店 的图形界面（§七/§六）。
 * 纯客户端 Screen：显示玩家咒点与诅咒列表，按钮通过 C2S 包请求服务端执行动作。
 */
public class CurseScreen extends Screen {
	private final String mode; // "altar" 或 "shop"

	private static final int PW = 256;
	private static final int PH = 248;

	private Button actionA;
	private Button actionB;
	private Button closeButton;

	public CurseScreen(String mode) {
		super(Component.literal(mode.equals("altar") ? "千咒台" : "无限诅咒商店"));
		this.mode = mode;
	}

	@Override
	protected void init() {
		int px = (this.width - PW) / 2;
		int py = (this.height - PH) / 2;
		int by = py + PH - 46;

		if ("altar".equals(mode)) {
			actionA = Button.builder(Component.literal("净化 (1锭+2尘)"),
							(b) -> send(ModNetworking.ALTAR_PURIFY))
					.bounds(px + 16, by, 104, 20).build();
			actionB = Button.builder(Component.literal("改写 (1锭+4尘+咒点)"),
							(b) -> send(ModNetworking.ALTAR_REWRITE))
					.bounds(px + 136, by, 104, 20).build();
		} else {
			actionA = Button.builder(Component.literal("签契返利 (+诅咒)"),
							(b) -> send(ModNetworking.SHOP_SIGN))
					.bounds(px + 16, by, 104, 20).build();
			actionB = Button.builder(Component.literal("增咒点 (1锭→+200)"),
							(b) -> send(ModNetworking.SHOP_ADD))
					.bounds(px + 136, by, 104, 20).build();
		}
		closeButton = Button.builder(Component.literal("关闭 (Esc)"),
						(b) -> Minecraft.getInstance().setScreen(null))
				.bounds(px + 16, by + 24, 224, 20).build();

		addRenderableWidget(actionA);
		addRenderableWidget(actionB);
		addRenderableWidget(closeButton);
	}

	private void send(ResourceLocation id) {
		ClientPlayNetworking.send(id, new FriendlyByteBuf(Unpooled.buffer()));
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		// 背景：压暗世界 + 面板
		gui.fill(0, 0, this.width, this.height, 0xCC000000);
		int px = (this.width - PW) / 2;
		int py = (this.height - PH) / 2;
		gui.fill(px, py, px + PW, py + PH, 0xE0101018);
		gui.fill(px, py, px + 4, py + PH, 0xFF7B2FBE);

		Font font = Minecraft.getInstance().font;
		gui.drawString(font, this.title, px + 12, py + 8, 0xFFE0A0, true);
		gui.drawString(font, Component.literal("§6咒点: " + ClientCurseData.points), px + 12, py + 28, 0xFFE0A0, true);
		if (!ClientCurseData.lastMessage.isEmpty()) {
			gui.drawString(font, Component.literal(ClientCurseData.lastMessage), px + 12, py + 46, 0xFFA0FFA0, true);
		}

		gui.drawString(font, Component.literal("— 诅咒列表 —"), px + 12, py + 66, 0xFFB0B0FF, true);
		int yy = py + 82;
		int shown = Math.min(ClientCurseData.curses.size(), 10);
		for (int i = 0; i < shown; i++) {
			gui.drawString(font, Component.literal(ClientCurseData.curses.get(i)), px + 12, yy, 0xFF9C9CFF, true);
			yy += 13;
		}
		if (ClientCurseData.curses.size() > shown) {
			gui.drawString(font, Component.literal("… 等 " + ClientCurseData.curses.size() + " 道"), px + 12, yy, 0xFF8080A0, true);
		}

		// 渲染按钮
		actionA.render(gui, mouseX, mouseY, partialTick);
		actionB.render(gui, mouseX, mouseY, partialTick);
		closeButton.render(gui, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
