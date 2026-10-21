package com.example.infinitecurse.item;

import com.example.infinitecurse.curse.CurseManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 诅咒之戒（§八）：开局戴入触发 10 道诅咒；右击可再次引动戒指（仅一次）。
 */
public class CurseRingItem extends Item {
	public static final CurseRingItem INSTANCE = new CurseRingItem();

	public CurseRingItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.pass(held);
		}
		CurseManager.onRingUsed((net.minecraft.server.level.ServerPlayer) player);
		return InteractionResultHolder.success(held);
	}
}
