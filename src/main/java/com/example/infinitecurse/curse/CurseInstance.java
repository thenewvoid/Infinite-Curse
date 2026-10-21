package com.example.infinitecurse.curse;

import com.example.infinitecurse.curse.pool.TemplateDef;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * 诅咒实例（§4.1/§4.3/§11.4）：一句可读文本 + 背后真实机制。
 * 序列化存储 (templateId, 各变量键, flipped, randMult, condition, tags, severity, impact, baseCost)，读档确定性重算展示文本。
 */
public class CurseInstance {
	public String id;
	public String templateId;
	public String playerText;
	public String actionText = "";
	public String attributeKey;
	public String effectKey;
	public boolean flipped = false;
	public double randMult = 1.0;
	public String condition = null;
	public CurseTags tags = new CurseTags();
	public double severity = -1.0;
	public double impact = 0.6;
	public int baseCost = 240;
	public boolean core = false;
	/** 过期时刻（主世界游戏刻）。0 表示永久诅咒；>0 表示临时诅咒，到达该刻后自动移除（§新增：诅咒之地/诅咒源泉）。 */
	public long expiresAt = 0;

	/** 效果反转词映射（千咒台翻正后展示用）。 */
	private static final Map<String, String> INVERSE_WORD = new HashMap<>();

	static {
		INVERSE_WORD.put("翻倍", "减半");
		INVERSE_WORD.put("减半", "翻倍");
		INVERSE_WORD.put("提升50%", "衰减50%");
		INVERSE_WORD.put("衰减50%", "提升50%");
		INVERSE_WORD.put("归零", "满盈");
		INVERSE_WORD.put("满盈", "归零");
		INVERSE_WORD.put("随机化", "既定");
		INVERSE_WORD.put("既定", "随机化");
		INVERSE_WORD.put("反向", "顺向");
		INVERSE_WORD.put("顺向", "反向");
	}

	public CurseInstance() {
	}

	public CurseInstance(String id, String templateId, String playerText, String actionText,
						 String attributeKey, String effectKey, double randMult, String condition,
						 CurseTags tags, double severity, double impact, int baseCost, boolean core) {
		this.id = id;
		this.templateId = templateId;
		this.playerText = playerText;
		this.actionText = actionText == null ? "" : actionText;
		this.attributeKey = attributeKey;
		this.effectKey = effectKey;
		this.randMult = randMult;
		this.condition = condition;
		this.tags = tags;
		this.severity = severity;
		this.impact = impact;
		this.baseCost = baseCost;
		this.core = core;
	}

	/** 是否有真实机制（属性+效果均已绑定）。核心/叙事诅咒可能为纯文本。 */
	public boolean isMechanical() {
		return attributeKey != null && effectKey != null;
	}

	/** 展示文本：确定性重算（§11.4）。翻正后前缀【祝福】。 */
	public String displayText() {
		VariablePool pool = VariablePool.get();
		String attrDisplay = (attributeKey != null && pool.getAttr(attributeKey) != null)
				? pool.getAttr(attributeKey).display : (attributeKey == null ? "" : attributeKey);
		String effDisplay = (effectKey != null && pool.getEff(effectKey) != null)
				? pool.getEff(effectKey).display : (effectKey == null ? "" : effectKey);
		if (flipped && INVERSE_WORD.containsKey(effDisplay)) {
			effDisplay = INVERSE_WORD.get(effDisplay);
		}
		TemplateDef tpl = null;
		if (templateId != null) {
			for (TemplateDef t : pool.templates) {
				if (t.id.equals(templateId)) {
					tpl = t;
					break;
				}
			}
		}
		String text;
		if (tpl != null) {
			text = tpl.text;
			text = text.replace("{{玩家}}", playerText == null ? "" : playerText);
			text = text.replace("{{动作}}", actionText == null ? "" : actionText);
			text = text.replace("{{属性}}", attrDisplay);
			text = text.replace("{{效果}}", effDisplay);
		} else {
			text = (playerText == null ? "" : playerText) + "的" + attrDisplay + effDisplay + "。";
		}
		if (flipped) text = "【祝福】" + text;
		if (condition != null) {
			com.example.infinitecurse.curse.pool.ConditionDef cd = pool.getCondition(condition);
			if (cd != null) {
				text = "（" + cd.display + "）" + text;
			}
		}
		if (expiresAt > 0) text = text + "（临时）";
		if (core) text = "◆" + text;
		return text;
	}

	/** 是否为临时诅咒（带过期时刻）。 */
	public boolean isTemporary() {
		return expiresAt > 0;
	}

	/** 是否已过期（基于给定的当前游戏刻）。 */
	public boolean isExpired(long now) {
		return expiresAt > 0 && expiresAt <= now;
	}

	/** 净严重度（§4.3）：生成阶段恒负；翻正后取正。 */
	public double severityScore() {
		return flipped ? -severity : severity;
	}

	/** 句子级严重度（§4.3）：单效果诅咒即为其 severity（带符号）。 */
	public double sentenceScore() {
		return severityScore();
	}

	// ===== NBT 序列化（§11.4 确定性存档） =====
	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", id == null ? "" : id);
		tag.putString("tpl", templateId == null ? "" : templateId);
		tag.putString("player", playerText == null ? "" : playerText);
		tag.putString("action", actionText == null ? "" : actionText);
		tag.putString("attr", attributeKey == null ? "" : attributeKey);
		tag.putString("eff", effectKey == null ? "" : effectKey);
		tag.putBoolean("flipped", flipped);
		tag.putDouble("rand", randMult);
		tag.putString("cond", condition == null ? "" : condition);
		tag.putBoolean("auto", tags.auto);
		tag.putBoolean("special", tags.special);
		tag.putBoolean("lethal", tags.lethal);
		tag.putBoolean("rev", tags.reversible);
		tag.putDouble("sev", severity);
		tag.putDouble("imp", impact);
		tag.putInt("cost", baseCost);
		tag.putBoolean("core", core);
		tag.putLong("exp", expiresAt);
		return tag;
	}

	public static CurseInstance fromNbt(CompoundTag tag) {
		CurseInstance c = new CurseInstance();
		c.id = tag.getString("id");
		c.templateId = tag.getString("tpl");
		c.playerText = tag.getString("player");
		c.actionText = tag.getString("action");
		String a = tag.getString("attr");
		String e = tag.getString("eff");
		c.attributeKey = a.isEmpty() ? null : a;
		c.effectKey = e.isEmpty() ? null : e;
		c.flipped = tag.getBoolean("flipped");
		c.randMult = tag.contains("rand") ? tag.getDouble("rand") : 1.0;
		String cond = tag.getString("cond");
		c.condition = cond.isEmpty() ? null : cond;
		c.tags = new CurseTags(tag.getBoolean("auto"), tag.getBoolean("special"), tag.getBoolean("lethal"), tag.getBoolean("rev"));
		c.severity = tag.contains("sev") ? tag.getDouble("sev") : -1.0;
		c.impact = tag.contains("imp") ? tag.getDouble("imp") : 0.6;
		c.baseCost = tag.contains("cost") ? tag.getInt("cost") : 240;
		c.core = tag.getBoolean("core");
		c.expiresAt = tag.contains("exp") ? tag.getLong("exp") : 0;
		return c;
	}
}
