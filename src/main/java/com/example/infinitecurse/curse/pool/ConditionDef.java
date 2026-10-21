package com.example.infinitecurse.curse.pool;

/**
 * 变量池：条件定义（§四 条件类）。当玩家满足 condition.type 时，对应诅咒的效果才会生效。
 * 支持类型：raining / night / day / underwater / underground / low_health / nether。
 */
public class ConditionDef {
	/** 变量池键，如 rain */
	public String key;
	/** 展示文字，如 雨中 */
	public String display;
	/** 运行时判定类型 */
	public String type;

	public ConditionDef() {
	}
}
