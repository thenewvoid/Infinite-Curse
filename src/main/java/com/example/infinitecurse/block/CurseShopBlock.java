package com.example.infinitecurse.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 无限诅咒商店（§六）：右击打开图形界面（客户端 UseBlockCallback 处理），服务端不再直接执行。
 * 签契返利 / 增咒点 由界面按钮通过 C2S 包请求（见 CurseManager.shopSign / shopAdd）。
 */
public class CurseShopBlock extends Block {
	public static final CurseShopBlock INSTANCE = new CurseShopBlock();
	public static final net.minecraft.world.item.BlockItem ITEM =
			new net.minecraft.world.item.BlockItem(INSTANCE, new net.minecraft.world.item.Item.Properties());

	public CurseShopBlock() {
		super(BlockBehaviour.Properties.of().strength(3.0f, 6.0f));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
								InteractionHand hand, BlockHitResult hit) {
		// 交互由客户端 UseBlockCallback 拦截并打开界面；此处直接返回成功，避免服务端额外处理。
		return InteractionResult.SUCCESS;
	}
}
