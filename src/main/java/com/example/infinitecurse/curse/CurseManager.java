package com.example.infinitecurse.curse;

import com.example.infinitecurse.config.InfiniteCurseConfig;
import com.example.infinitecurse.curse.pool.AttributeDef;
import com.example.infinitecurse.curse.pool.EffectDef;
import com.example.infinitecurse.item.CurseRingItem;
import com.example.infinitecurse.item.CursefireIngotItem;
import com.example.infinitecurse.item.ReverseDustItem;
import com.example.infinitecurse.network.ModNetworking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;

/**
 * 玩家诅咒容器 + 机制应用（§四/§五/§七/§八/§十/§11.4）。
 * 同时承担 咒点经济、千咒台净化/改写、商店签契/增咒点、自动索债、存档/读档。
 * 条件类诅咒（§四）由 updateConditional 按运行时条件动态启停。
 */
public final class CurseManager {

	private static final Map<UUID, PlayerCurseData> PLAYERS = new HashMap<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type SAVE_TYPE = new TypeToken<Map<String, PlayerCurseData>>() {}.getType();

	private CurseManager() {
	}

	public static PlayerCurseData get(UUID id) {
		return PLAYERS.computeIfAbsent(id, k -> new PlayerCurseData());
	}

	public static PlayerCurseData get(ServerPlayer p) {
		return get(p.getUUID());
	}

	/** 玩家级数据。curses 为全部诅咒；appliedAttr/tickEffectCounts/appliedConditional 为运行时派生，读档后清空重算。 */
	public static class PlayerCurseData {
		public List<CurseInstance> curses = new ArrayList<>();
		public int points = 0;
		public boolean ringGranted = false;
		public boolean ringUsed = false;
		public int lastDebtDay = -1;
		public final Map<String, String> appliedAttr = new HashMap<>();
		/** tick 类诅咒目标 -> 引用计数（支持同目标多诅咒） */
		public final Map<String, Integer> tickEffectCounts = new HashMap<>();
		/** 当前已生效的条件类诅咒 id 集合（updateConditional 维护） */
		public final Set<String> appliedConditional = new HashSet<>();
		// ===== 诅咒之地 / 诅咒源泉 计时（§新增） =====
		/** 在当前诅咒区域连续停留的秒数（每游戏秒 +1，离开区域重置）。 */
		public int zoneTicks = 0;
		/** 当前所在诅咒区域的标识（方块 key 或 biome key），用于判断是否换区域。 */
		public String zoneKey = null;
		/** 是否已经发出过"诅咒积聚"预警（避免重复刷屏）。 */
		public boolean zoneWarned = false;
		// ===== 诅咒系列怪物"涌现式"生成冷却（§新增） =====
		/** 距上次在诅咒区域生成怪物的剩余秒数（每游戏秒 -1）。 */
		public int mobSpawnCooldown = 0;
		/** 距上次唤醒万咒之主（Boss）的剩余秒数（每游戏秒 -1，严格限频避免骚扰）。 */
		public int bossCooldown = 0;
	}

	// ===================== 生命周期 =====================

	/** 玩家进入：首次则授予戒指 + 10 诅咒；否则重新应用已有诅咒。 */
	public static void onPlayerJoin(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		if (!data.ringGranted) {
			player.getInventory().add(new ItemStack(CurseRingItem.INSTANCE));
			List<CurseInstance> fresh = CurseGenerator.generate(10);
			data.curses.addAll(fresh);
			data.ringGranted = true;
			data.ringUsed = true;
			player.sendSystemMessage(Component.literal("§5[无限诅咒] 你戴上了诅咒之戒，10 道诅咒已悄然降临。"));
			reapply(player);
			sync(player, fresh.isEmpty() ? null : fresh.get(fresh.size() - 1).displayText());
		} else {
			reapply(player);
			sync(player, null);
		}
	}

	/** 死亡/重生保留诅咒数据（§11.4）。 */
	public static void copyData(ServerPlayer oldP, ServerPlayer newP) {
		PlayerCurseData src = PLAYERS.remove(oldP.getUUID());
		if (src != null) {
			PLAYERS.put(newP.getUUID(), src);
		}
	}

	/** 戒指右击：未使用过则再引动 10 道诅咒（仅一次）。 */
	public static void onRingUsed(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		if (!data.ringUsed) {
			List<CurseInstance> fresh = CurseGenerator.generate(10);
			data.curses.addAll(fresh);
			data.ringUsed = true;
			player.sendSystemMessage(Component.literal("§5[无限诅咒] 戒指骤然灼热，10 道新诅咒涌入体内。"));
			reapply(player);
			sync(player, fresh.isEmpty() ? null : fresh.get(fresh.size() - 1).displayText());
		} else {
			player.sendSystemMessage(Component.literal("§7[无限诅咒] 这枚戒指的力量已然耗尽。"));
			sync(player, null);
		}
	}

	// ===================== 千咒台（§七）—— GUI 显式动作 =====================

	/** 净化：1 咒火锭 + 2 逆咒尘，移除最重诅咒，慰藉 +100 咒点。 */
	public static void altarPurify(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		CurseInstance worst = worstActive(data);
		if (worst == null) {
			player.sendSystemMessage(Component.literal("§7[千咒台] 你身上没有可处理的诅咒。"));
			sync(player, null);
			return;
		}
		if (!hasItems(player, CursefireIngotItem.INSTANCE, 1) || !hasItems(player, ReverseDustItem.INSTANCE, 2)) {
			player.sendSystemMessage(Component.literal("§c[千咒台] 净化需要 1 个咒火锭与 2 个逆咒尘。"));
			sync(player, null);
			return;
		}
		consumeItems(player, CursefireIngotItem.INSTANCE, 1);
		consumeItems(player, ReverseDustItem.INSTANCE, 2);
		data.curses.remove(worst);
		data.points += 100;
		player.sendSystemMessage(Component.literal("§a[千咒台] 你净化了诅咒：「" + worst.displayText() + "」（获得 100 咒点慰藉）"));
		reapply(player);
		sync(player, null);
	}

	/** 改写：1 咒火锭 + 4 逆咒尘 + round(|score|*120) 咒点，翻转最重诅咒语义。 */
	public static void altarRewrite(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		CurseInstance worst = worstActive(data);
		if (worst == null) {
			player.sendSystemMessage(Component.literal("§7[千咒台] 你身上没有可处理的诅咒。"));
			sync(player, null);
			return;
		}
		int cost = (int) Math.round(Math.abs(worst.severityScore()) * 120 * InfiniteCurseConfig.get().altarCostMultiplier);
		if (data.points < cost) {
			player.sendSystemMessage(Component.literal("§c[千咒台] 改写需 " + cost + " 咒点，你仅有 " + data.points + "。"));
			sync(player, null);
			return;
		}
		if (!hasItems(player, CursefireIngotItem.INSTANCE, 1) || !hasItems(player, ReverseDustItem.INSTANCE, 4)) {
			player.sendSystemMessage(Component.literal("§c[千咒台] 改写需要 1 个咒火锭与 4 个逆咒尘。"));
			sync(player, null);
			return;
		}
		consumeItems(player, CursefireIngotItem.INSTANCE, 1);
		consumeItems(player, ReverseDustItem.INSTANCE, 4);
		data.points -= cost;
		worst.flipped = true;
		player.sendSystemMessage(Component.literal("§a[千咒台] 你改写了诅咒语法：「" + worst.displayText() + "」（耗费 " + cost + " 咒点）"));
		reapply(player);
		sync(player, null);
	}

	// ===================== 无限诅咒商店（§六）—— GUI 显式动作 =====================

	/** 签契返利：获得 1 道新诅咒，并立即返还其成本 ×0.3 的咒点。 */
	public static void shopSign(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		List<CurseInstance> fresh = CurseGenerator.generate(1);
		if (fresh.isEmpty()) {
			sync(player, null);
			return;
		}
		CurseInstance c = fresh.get(0);
		data.curses.add(c);
		int rebate = (int) Math.round(c.baseCost * 0.3);
		data.points += rebate;
		player.sendSystemMessage(Component.literal("§5[无限诅咒商店] 你签下契约，获得诅咒「" + c.displayText() + "」，返利 " + rebate + " 咒点。"));
		reapply(player);
		sync(player, c.displayText());
	}

	/** 增咒点：1 咒火锭 → +200 咒点。 */
	public static void shopAdd(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		if (!hasItems(player, CursefireIngotItem.INSTANCE, 1)) {
			player.sendSystemMessage(Component.literal("§c[无限诅咒商店] 增咒点需要 1 个咒火锭。"));
			sync(player, null);
			return;
		}
		consumeItems(player, CursefireIngotItem.INSTANCE, 1);
		data.points += 200;
		player.sendSystemMessage(Component.literal("§a[无限诅咒商店] 你以 1 咒火锭兑换了 200 咒点。"));
		sync(player, null);
	}

	// ===================== 临时诅咒（§新增：诅咒之地 / 诅咒源泉） =====================

	/** 追加一道临时诅咒：设置过期时刻并立即生效。durationTicks<=0 视为永久。 */
	public static void addTemporaryCurse(ServerPlayer player, CurseInstance c, long durationTicks, long gameTime) {
		PlayerCurseData data = get(player.getUUID());
		if (durationTicks > 0 && gameTime > 0) {
			c.expiresAt = gameTime + durationTicks;
		} else {
			c.expiresAt = 0;
		}
		data.curses.add(c);
		reapply(player);
		sync(player, null);
	}

	/** 每秒结算：移除所有到期的临时诅咒并刷新效果。 */
	public static void expireTemporary(ServerPlayer player, long gameTime) {
		PlayerCurseData data = get(player.getUUID());
		boolean removed = false;
		for (int i = data.curses.size() - 1; i >= 0; i--) {
			CurseInstance c = data.curses.get(i);
			if (c.isExpired(gameTime)) {
				data.curses.remove(i);
				removed = true;
			}
		}
		if (removed) {
			reapply(player);
			sync(player, null);
		}
	}

	// ===================== 机制应用（§四） =====================

	/** 重新应用该玩家全部"非条件"生效诅咒；条件类诅咒交由 updateConditional 按运行时条件启停。 */
	public static void reapply(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		VariablePool pool = VariablePool.get();

		// 1. 移除上一次应用的属性修正与 tick 计数
		for (Map.Entry<String, String> e : data.appliedAttr.entrySet()) {
			Attribute a = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(e.getValue()));
			if (a != null) {
				AttributeInstance ai = player.getAttribute(a);
				if (ai != null) {
					ai.removeModifier(uuidFromId(e.getKey()));
				}
			}
		}
		data.appliedAttr.clear();
		data.tickEffectCounts.clear();
		data.appliedConditional.clear();

		// 2. 应用当前"非条件"生效诅咒
		for (CurseInstance c : data.curses) {
			if (!c.isMechanical() || c.condition != null) {
				continue; // 条件类交给 updateConditional
			}
			applyUnconditional(player, c, pool, data);
		}

		// 3. 立即按当前条件启停条件类诅咒
		updateConditional(player);
	}

	/** 应用一条"非条件"诅咒的真实机制（属性/TICK）。药水类（POTION）无条件诅咒在此施加常驻药水。 */
	private static void applyUnconditional(ServerPlayer player, CurseInstance c, VariablePool pool, PlayerCurseData data) {
		AttributeDef attr = pool.getAttr(c.attributeKey);
		EffectDef eff = pool.getEff(c.effectKey);
		if (attr == null || eff == null) {
			return;
		}
		if ("ATTRIBUTE".equals(attr.category)) {
			Attribute a = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(attr.target));
			if (a == null) {
				return;
			}
			AttributeInstance ai = player.getAttribute(a);
			if (ai == null) {
				return;
			}
			double v = "RANDOM".equals(eff.op) ? c.randMult : eff.value;
			double amount = c.flipped ? (1.0 / v - 1.0) : (v - 1.0);
			ai.addTransientModifier(new AttributeModifier(
					uuidFromId(c.id), "curse:" + c.id, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
			data.appliedAttr.put(c.id, attr.target);
		} else if ("POTION".equals(attr.category)) {
			if (c.flipped) {
				return; // 翻正 = 不再施加负面药水
			}
			MobEffect me = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(attr.target));
			if (me == null) {
				return;
			}
			int amp = Math.max(0, (int) Math.round(Math.abs(eff.severity)) - 1);
			player.addEffect(new MobEffectInstance(me, 999999, amp));
		} else if ("TICK".equals(attr.category)) {
			data.tickEffectCounts.merge(attr.target, 1, Integer::sum);
		}
	}

	/** 每游戏秒结算一次 Tick 类诅咒（§四 TICK）：饥馑/窒息/腐坏。同时刷新条件类诅咒启停。 */
	public static void applyTickEffects(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		for (Map.Entry<String, Integer> e : data.tickEffectCounts.entrySet()) {
			if (e.getValue() <= 0) {
				continue;
			}
			String key = e.getKey();
			if ("hunger".equals(key)) {
				int fl = player.getFoodData().getFoodLevel();
				player.getFoodData().setFoodLevel(Math.max(0, fl - 1));
			} else if ("oxygen".equals(key)) {
				if (player.isUnderWater()) {
					player.setAirSupply(Math.max(0, player.getAirSupply() - 3));
				}
			} else if ("regen".equals(key)) {
				if (player.getHealth() > 1.0f) {
					player.setHealth(player.getHealth() - 0.15f);
				}
			}
		}
		updateConditional(player);
	}

	/** 条件类诅咒（§四）：按运行时条件动态启停 ATTRIBUTE / TICK 效果。 */
	public static void updateConditional(ServerPlayer player) {
		PlayerCurseData data = get(player.getUUID());
		VariablePool pool = VariablePool.get();

		// 1. 移除已不存在/已非条件的诅咒效果
		for (String id : new HashSet<>(data.appliedConditional)) {
			boolean stillConditional = false;
			for (CurseInstance c : data.curses) {
				if (c.id.equals(id) && c.condition != null && c.isMechanical()) {
					stillConditional = true;
					break;
				}
			}
			if (!stillConditional) {
				removeConditional(player, id, data);
				data.appliedConditional.remove(id);
			}
		}

		// 2. 对现存条件类诅咒做启停对账
		for (CurseInstance c : data.curses) {
			if (c.condition == null || !c.isMechanical()) {
				continue;
			}
			AttributeDef attr = pool.getAttr(c.attributeKey);
			if (attr == null) {
				continue;
			}
			boolean met = ConditionEvaluator.test(c.condition, player);
			boolean applied = data.appliedConditional.contains(c.id);
			if (met && !applied) {
				applyConditional(player, c, attr, pool, data);
				data.appliedConditional.add(c.id);
			} else if (!met && applied) {
				removeConditional(player, c.id, data);
				data.appliedConditional.remove(c.id);
			}
		}
	}

	private static void applyConditional(ServerPlayer player, CurseInstance c, AttributeDef attr, VariablePool pool, PlayerCurseData data) {
		EffectDef eff = pool.getEff(c.effectKey);
		if (eff == null) {
			return;
		}
		if ("ATTRIBUTE".equals(attr.category)) {
			Attribute a = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(attr.target));
			if (a == null) {
				return;
			}
			AttributeInstance ai = player.getAttribute(a);
			if (ai == null) {
				return;
			}
			double v = "RANDOM".equals(eff.op) ? c.randMult : eff.value;
			double amount = c.flipped ? (1.0 / v - 1.0) : (v - 1.0);
			ai.addTransientModifier(new AttributeModifier(
					uuidFromId(c.id), "curse:" + c.id, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
			data.appliedAttr.put(c.id, attr.target);
		} else if ("TICK".equals(attr.category)) {
			data.tickEffectCounts.merge(attr.target, 1, Integer::sum);
		}
		// POTION 条件类不生成，此处忽略
	}

	private static void removeConditional(ServerPlayer player, String id, PlayerCurseData data) {
		// 属性类：按 id 移除对应修正
		String target = data.appliedAttr.remove(id);
		if (target != null) {
			Attribute a = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(target));
			if (a != null) {
				AttributeInstance ai = player.getAttribute(a);
				if (ai != null) {
					ai.removeModifier(uuidFromId(id));
				}
			}
		}
		// tick 类：按 id 找到对应诅咒目标，引用计数减一
		for (CurseInstance c : data.curses) {
			if (c.id.equals(id) && c.condition != null) {
				VariablePool pool = VariablePool.get();
				AttributeDef attr = pool.getAttr(c.attributeKey);
				if (attr != null && "TICK".equals(attr.category)) {
					data.tickEffectCounts.merge(attr.target, -1, Integer::sum);
				}
				break;
			}
		}
	}

	// ===================== 自动索债（§十） =====================

	/** 跨日结算：挑选满足条件(auto&&!special&&!lethal&&reversible)且成本最低的诅咒索债。 */
	public static void collectDebt(ServerPlayer player, int day) {
		PlayerCurseData data = get(player.getUUID());
		data.lastDebtDay = day;
		if (day <= 0) {
			return;
		}
		if (!InfiniteCurseConfig.get().autoDebtEnabled) {
			return;
		}
		CurseInstance pick = null;
		int minCost = Integer.MAX_VALUE;
		for (CurseInstance c : data.curses) {
			if (!c.isMechanical()) {
				continue;
			}
			if (!(c.tags.auto && !c.tags.special && !c.tags.lethal && c.tags.reversible)) {
				continue;
			}
			if (c.baseCost < minCost) {
				minCost = c.baseCost;
				pick = c;
			}
		}
		if (pick == null) {
			return;
		}
		int cost = (int) Math.round(InfiniteCurseConfig.get().scaled(pick.baseCost));
		if (data.points >= cost) {
			data.points -= cost;
			data.curses.remove(pick);
			player.sendSystemMessage(Component.literal("§6[索债] 你偿还了债务，诅咒「" + pick.displayText() + "」被清偿（" + cost + " 咒点）。"));
			reapply(player);
			sync(player, null);
		} else {
			// 违约：诅咒加剧 + 受到反噬
			pick.severity *= 1.15;
			pick.baseCost = (int) (pick.baseCost * 1.15);
			player.setHealth(Math.max(0.5f, player.getHealth() - 1.0f));
			player.sendSystemMessage(Component.literal("§c[索债] 你无力偿还，诅咒「" + pick.displayText() + "」加剧，并遭到反噬！"));
			reapply(player);
			sync(player, null);
		}
	}

	// ===================== 同步包（§12） =====================

	public static void sync(ServerPlayer player, String toast) {
		PlayerCurseData data = get(player.getUUID());
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeVarInt(data.points);
		buf.writeVarInt(data.curses.size());
		for (CurseInstance c : data.curses) {
			buf.writeUtf(c.displayText());
		}
		buf.writeUtf(toast == null ? "" : toast);
		ServerPlayNetworking.send(player, ModNetworking.SYNC, buf);
	}

	// ===================== 存档 / 读档（§11.4） =====================

	public static void saveAll() {
		Path file = FabricLoader.getInstance().getGameDir().resolve("infinite_curse_save.json");
		try {
			Map<String, PlayerCurseData> snapshot = new HashMap<>();
			for (Map.Entry<UUID, PlayerCurseData> e : PLAYERS.entrySet()) {
				e.getValue().appliedAttr.clear();
				e.getValue().tickEffectCounts.clear();
				e.getValue().appliedConditional.clear();
				snapshot.put(e.getKey().toString(), e.getValue());
			}
			Files.writeString(file, GSON.toJson(snapshot, SAVE_TYPE));
		} catch (IOException ignored) {
		}
	}

	public static void loadAll() {
		Path file = FabricLoader.getInstance().getGameDir().resolve("infinite_curse_save.json");
		if (!Files.exists(file)) {
			return;
		}
		try {
			Map<String, PlayerCurseData> snapshot = GSON.fromJson(Files.readString(file), SAVE_TYPE);
			if (snapshot == null) {
				return;
			}
			PLAYERS.clear();
			for (Map.Entry<String, PlayerCurseData> e : snapshot.entrySet()) {
				try {
					UUID id = UUID.fromString(e.getKey());
					PlayerCurseData d = e.getValue();
					d.appliedAttr.clear();
					d.tickEffectCounts.clear();
					d.appliedConditional.clear();
					PLAYERS.put(id, d);
				} catch (IllegalArgumentException ignored) {
				}
			}
		} catch (IOException ignored) {
		}
	}

	// ===================== 内部工具 =====================

	private static CurseInstance worstActive(PlayerCurseData data) {
		CurseInstance worst = null;
		double worstScore = 0.0;
		for (CurseInstance c : data.curses) {
			if (c.flipped || !c.isMechanical()) {
				continue;
			}
			double s = c.severityScore();
			if (worst == null || s < worstScore) {
				worst = c;
				worstScore = s;
			}
		}
		return worst;
	}

	private static int countItem(Player player, Item item) {
		int c = 0;
		Inventory inv = player.getInventory();
		for (List<ItemStack> list : new List[]{inv.items, inv.armor, inv.offhand}) {
			for (ItemStack s : list) {
				if (s.getItem() == item) {
					c += s.getCount();
				}
			}
		}
		return c;
	}

	private static boolean hasItems(Player player, Item item, int n) {
		return countItem(player, item) >= n;
	}

	private static void consumeItems(Player player, Item item, int n) {
		int left = n;
		Inventory inv = player.getInventory();
		for (List<ItemStack> list : new List[]{inv.items, inv.armor, inv.offhand}) {
			for (ItemStack s : list) {
				if (left <= 0) {
					break;
				}
				if (s.getItem() == item) {
					int take = Math.min(left, s.getCount());
					s.shrink(take);
					left -= take;
				}
			}
		}
	}

	private static UUID uuidFromId(String id) {
		return UUID.nameUUIDFromBytes(("infinite_curse:" + id).getBytes(StandardCharsets.UTF_8));
	}
}
