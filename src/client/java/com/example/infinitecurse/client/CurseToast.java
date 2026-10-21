package com.example.infinitecurse.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

/**
 * 新诅咒弹句（§12）：右下角短暂提示一句可读诅咒文本。
 */
public class CurseToast implements Toast {
	private final String title;
	private final String description;

	public CurseToast(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public Visibility render(GuiGraphics gui, ToastComponent toastComponent, long time) {
		int w = width();
		int h = height();
		gui.fill(0, 0, w, h, 0xCC2A0A3A);
		gui.fill(0, 0, 4, h, 0xFFB03070);
		Font font = Minecraft.getInstance().font;
		gui.drawString(font, Component.literal(title), 12, 6, 0xFFE0A0, true);
		gui.drawString(font, Component.literal(description), 12, 20, 0xFFDCD0E0, false);
		return time >= 4000 ? Visibility.HIDE : Visibility.SHOW;
	}
}
