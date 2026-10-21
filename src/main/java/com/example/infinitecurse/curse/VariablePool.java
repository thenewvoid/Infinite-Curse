package com.example.infinitecurse.curse;

import com.example.infinitecurse.curse.pool.ActionDef;
import com.example.infinitecurse.curse.pool.AttributeDef;
import com.example.infinitecurse.curse.pool.ConditionDef;
import com.example.infinitecurse.curse.pool.EffectDef;
import com.example.infinitecurse.curse.pool.TemplateDef;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

/**
 * 变量池（§4.2）：从数据包加载模板/变量，并提供采样与查找。
 * 是生成式引擎的数据源；社区可通过数据包覆盖/扩展。
 */
public final class VariablePool {
	private static final VariablePool INSTANCE = new VariablePool();
	private static final Gson GSON = new GsonBuilder().create();

	public final List<String> players = new ArrayList<>();
	public final List<ActionDef> actions = new ArrayList<>();
	public final List<AttributeDef> attributes = new ArrayList<>();
	public final List<EffectDef> effects = new ArrayList<>();
	public final List<TemplateDef> templates = new ArrayList<>();
	public final List<ConditionDef> conditions = new ArrayList<>();

	private final Map<String, AttributeDef> attrMap = new HashMap<>();
	private final Map<String, EffectDef> effMap = new HashMap<>();
	private final Map<String, ActionDef> actionMap = new HashMap<>();
	private final Map<String, ConditionDef> condMap = new HashMap<>();

	private boolean loaded = false;

	public static VariablePool get() {
		return INSTANCE;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void reset() {
		players.clear();
		actions.clear();
		attributes.clear();
		effects.clear();
		templates.clear();
		conditions.clear();
		attrMap.clear();
		effMap.clear();
		actionMap.clear();
		condMap.clear();
	}

	public void loadVariables(Reader reader) {
		VariablesData data = GSON.fromJson(reader, VariablesData.class);
		if (data == null) return;
		if (data.player != null) players.addAll(data.player);
		if (data.action != null) actions.addAll(data.action);
		if (data.attribute != null) attributes.addAll(data.attribute);
		if (data.effect != null) effects.addAll(data.effect);
		if (data.condition != null) conditions.addAll(data.condition);
		rebuildIndexes();
	}

	public void loadTemplates(Reader reader) {
		TemplateDef[] arr = GSON.fromJson(reader, TemplateDef[].class);
		if (arr != null) {
			for (TemplateDef t : arr) templates.add(t);
		}
		loaded = true;
	}

	private void rebuildIndexes() {
		attrMap.clear();
		effMap.clear();
		actionMap.clear();
		condMap.clear();
		for (AttributeDef a : attributes) attrMap.put(a.key, a);
		for (EffectDef e : effects) effMap.put(e.key, e);
		for (ActionDef a : actions) actionMap.put(a.key, a);
		for (ConditionDef c : conditions) condMap.put(c.key, c);
	}

	public AttributeDef getAttr(String key) {
		return attrMap.get(key);
	}

	public EffectDef getEff(String key) {
		return effMap.get(key);
	}

	public ActionDef getAction(String key) {
		return actionMap.get(key);
	}

	public ConditionDef getCondition(String key) {
		return condMap.get(key);
	}

	public TemplateDef randomTemplate(Random r) {
		if (templates.isEmpty()) return null;
		return templates.get(r.nextInt(templates.size()));
	}

	public String randomPlayer(Random r) {
		if (players.isEmpty()) return "你";
		return players.get(r.nextInt(players.size()));
	}

	public ActionDef randomAction(Random r) {
		if (actions.isEmpty()) return null;
		return actions.get(r.nextInt(actions.size()));
	}

	/** 按语义分组（§4.4）挑选与动作兼容的属性；actionKey 为 null 时随机取全部。 */
	public AttributeDef randomAttribute(Random r, String actionKey) {
		List<AttributeDef> pool = attributes;
		if (actionKey != null) {
			ActionDef ad = actionMap.get(actionKey);
			if (ad != null && ad.compatible != null && !ad.compatible.isEmpty()) {
				List<AttributeDef> filtered = new ArrayList<>();
				for (String k : ad.compatible) {
					AttributeDef a = attrMap.get(k);
					if (a != null) filtered.add(a);
				}
				if (!filtered.isEmpty()) pool = filtered;
			}
		}
		if (pool.isEmpty()) return null;
		return pool.get(r.nextInt(pool.size()));
	}

	/** 从效果池采样，可传入筛选谓词（如自动索债的标签硬筛选 §十）。 */
	public EffectDef randomEffect(Random r, Predicate<EffectDef> filter) {
		List<EffectDef> pool = new ArrayList<>();
		for (EffectDef e : effects) {
			if (filter == null || filter.test(e)) pool.add(e);
		}
		if (pool.isEmpty()) return null;
		return pool.get(r.nextInt(pool.size()));
	}

	/** 在候选集中挑选基础成本最低的 effect（自动索债用，§十）。 */
	public EffectDef lowestCostEffect(List<EffectDef> candidates) {
		EffectDef best = null;
		for (EffectDef e : candidates) {
			if (best == null || e.baseCost < best.baseCost) best = e;
		}
		return best;
	}

	private static class VariablesData {
		List<String> player;
		List<ActionDef> action;
		List<AttributeDef> attribute;
		List<EffectDef> effect;
		List<ConditionDef> condition;
	}
}
