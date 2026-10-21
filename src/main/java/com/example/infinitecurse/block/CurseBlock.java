package com.example.infinitecurse.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 诅咒方块基类：仅作为普通方块，诅咒语义（区域判定）由 CurseZone 通过方块映射维护。
 */
public class CurseBlock extends Block {
	public CurseBlock(BlockBehaviour.Properties props) {
		super(props);
	}
}
