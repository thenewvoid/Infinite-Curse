package com.example.infinitecurse.curse.pool;

import com.example.infinitecurse.curse.CurseTags;

/**
 * 变量池：效果定义（§4.2/§4.3/§十）。
 * op ∈ {MULTIPLY, SET, RANDOM, INVERT}；value 为真实运算值；severity 为设计分（带符号）；impact 为真实体验权重。
 */
public class EffectDef {
	/** 变量池键，如 half */
	public String key;
	/** 展示文字，如 减半 */
	public String display;
	/** MULTIPLY / SET / RANDOM / INVERT */
	public String op;
	/** 真实运算值（如 0.5 / 2.0 / 1.5 / 0.0） */
	public double value;
	/** 设计分（生成阶段恒负 → 必为诅咒） */
	public double severity = -1.0;
	/** 真实体验权重 0–1 */
	public double impact = 0.6;
	/** 标签（§十） */
	public CurseTags tags = new CurseTags();
	/** 该效果对应的诅咒基础成本（咒点） */
	public int baseCost = 240;

	public EffectDef() {
	}
}
