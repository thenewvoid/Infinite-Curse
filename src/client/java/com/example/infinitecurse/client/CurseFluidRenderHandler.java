package com.example.infinitecurse.client;

import com.example.infinitecurse.fluid.CurseFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

/**
 * 诅咒源泉的客户端流体渲染：紫色调，使用自定义流体贴图（§新增）。
 */
public class CurseFluidRenderHandler implements FluidRenderHandler {

	private TextureAtlasSprite[] sprites;

	private TextureAtlasSprite[] getSprites() {
		if (sprites == null) {
			var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
			sprites = new TextureAtlasSprite[] {
					atlas.apply(CurseFluid.STILL_TEX),
					atlas.apply(CurseFluid.FLOW_TEX)
			};
		}
		return sprites;
	}

	@Override
	public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		return 0xFF6A0DAD; // 诅咒紫
	}

	@Override
	public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		return getSprites();
	}
}
