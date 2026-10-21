package com.example.infinitecurse.curse;

import com.example.infinitecurse.curse.pool.ActionDef;
import com.example.infinitecurse.curse.pool.AttributeDef;
import com.example.infinitecurse.curse.pool.EffectDef;
import com.example.infinitecurse.curse.pool.TemplateDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 生成式诅咒引擎（§四）：从变量池采样 模板/玩家词/动作/属性/效果，组装成可序列化的诅咒实例。
 * 设计分 severity 在生成阶段恒为负（必为诅咒）；改写(flip)后由 CurseInstance 取正。
 */
public final class CurseGenerator {

	private CurseGenerator() {
	}

	public static List<CurseInstance> generate(int count) {
		VariablePool pool = VariablePool.get();
		List<CurseInstance> out = new ArrayList<>();
		Random r = new Random();
		for (int i = 0; i < count; i++) {
			TemplateDef tpl = pool.randomTemplate(r);
			String playerWord = pool.randomPlayer(r);
			ActionDef action = pool.randomAction(r);
			AttributeDef attr = pool.randomAttribute(r, action == null ? null : action.key);
			EffectDef eff = pool.randomEffect(r, null);
			if (attr == null || eff == null) {
				continue;
			}
			CurseInstance c = new CurseInstance();
			c.id = UUID.randomUUID().toString();
			c.templateId = tpl == null ? null : tpl.id;
			c.playerText = playerWord;
			c.actionText = action == null ? "" : action.display;
			c.attributeKey = attr.key;
			c.effectKey = eff.key;
			c.randMult = "RANDOM".equals(eff.op) ? (0.7 + r.nextDouble() * 0.6) : 1.0;
			// 条件类诅咒（§四）：仅 ATTRIBUTE / TICK 类可附加"满足条件才生效"
			if ((attr.category.equals("ATTRIBUTE") || attr.category.equals("TICK"))
					&& !pool.conditions.isEmpty() && r.nextDouble() < 0.28) {
				c.condition = pool.conditions.get(r.nextInt(pool.conditions.size())).key;
			} else {
				c.condition = null;
			}
			c.tags = new CurseTags(eff.tags.auto, eff.tags.special, eff.tags.lethal, eff.tags.reversible);
			c.severity = eff.severity;
			c.impact = eff.impact;
			c.baseCost = eff.baseCost;
			c.core = false;
			c.flipped = false;
			out.add(c);
		}
		return out;
	}

	/**
	 * 生成"固定"诅咒（§新增 诅咒之地）：类型由传入的 attribute/effect 决定，确定性、可复现，不附加条件。
	 * 用于玩家在诅咒方块区域停留过久时被种下的临时诅咒（根据踩的方块不同而不同）。
	 * @param seed   用于挑选模板与生成唯一 id 的种子（如方块 key）
	 * @param attrKey 属性键（必须存在于 variables.json）
	 * @param effKey  效果键（必须存在于 variables.json）
	 */
	public static CurseInstance generateFixed(String seed, String attrKey, String effKey) {
		VariablePool pool = VariablePool.get();
		AttributeDef attr = pool.getAttr(attrKey);
		EffectDef eff = pool.getEff(effKey);
		CurseInstance c = new CurseInstance();
		c.id = UUID.nameUUIDFromBytes(("fixed:" + seed + ":" + System.nanoTime()).getBytes()).toString();
		if (!pool.templates.isEmpty()) {
			int idx = Math.floorMod(seed.hashCode(), pool.templates.size());
			c.templateId = pool.templates.get(idx).id;
		}
		c.playerText = "你";
		c.actionText = "";
		c.attributeKey = attr == null ? null : attrKey;
		c.effectKey = eff == null ? null : effKey;
		c.randMult = 1.0;
		c.condition = null;
		if (eff != null) {
			c.tags = new CurseTags(eff.tags.auto, eff.tags.special, eff.tags.lethal, eff.tags.reversible);
			c.severity = eff.severity;
			c.impact = eff.impact;
			c.baseCost = eff.baseCost;
		} else {
			c.tags = new CurseTags();
			c.severity = -1.0;
			c.impact = 0.6;
			c.baseCost = 240;
		}
		c.flipped = false;
		c.core = false;
		c.expiresAt = 0; // 由调用方按需设置
		return c;
	}
}
