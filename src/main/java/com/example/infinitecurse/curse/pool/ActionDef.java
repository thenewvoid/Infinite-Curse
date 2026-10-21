package com.example.infinitecurse.curse.pool;

import java.util.List;

/**
 * 变量池：动作定义（§4.1/§4.4 语义分组）。
 * compatible 为该动作兼容的属性键集合；为空或 null 表示与所有属性兼容。
 */
public class ActionDef {
	/** 变量池键，如 run */
	public String key;
	/** 展示文字，如 奔跑 */
	public String display;
	/** 语义分组：兼容的属性键 */
	public List<String> compatible;

	public ActionDef() {
	}
}
