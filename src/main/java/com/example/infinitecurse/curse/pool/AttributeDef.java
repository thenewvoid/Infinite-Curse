package com.example.infinitecurse.curse.pool;

/**
 * 变量池：属性定义（§4.1/§4.2/§4.4）。
 * category ∈ {ATTRIBUTE, POTION, TICK}；target 为真实机制目标（属性注册键 / 药水键 / tick 键）。
 */
public class AttributeDef {
	/** 变量池键，如 move_speed */
	public String key;
	/** 展示文字，如 移速 */
	public String display;
	/** ATTRIBUTE / POTION / TICK */
	public String category;
	/** 真实运算目标：属性注册键(minecraft:generic.x) / 药水键(mining|vision) / tick 键(hunger|oxygen|regen) */
	public String target;

	public AttributeDef() {
	}
}
