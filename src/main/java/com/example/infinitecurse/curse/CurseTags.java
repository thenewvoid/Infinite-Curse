package com.example.infinitecurse.curse;

/**
 * 标签体系（§十）。用于硬筛选（自动索债、特殊/致死/可逆转）与社区数据包扩展。
 * 自动索债筛选规则（原文逐字映射）：
 *   auto == true && special == false && lethal == false && reversible == true && baseCost == min(债务货架候选集)
 */
public class CurseTags {
	public boolean auto = false;
	public boolean special = false;
	public boolean lethal = false;
	public boolean reversible = true;

	public CurseTags() {
	}

	public CurseTags(boolean auto, boolean special, boolean lethal, boolean reversible) {
		this.auto = auto;
		this.special = special;
		this.lethal = lethal;
		this.reversible = reversible;
	}
}
