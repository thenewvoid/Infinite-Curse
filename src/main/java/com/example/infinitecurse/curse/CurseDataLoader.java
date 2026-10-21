package com.example.infinitecurse.curse;

import com.example.infinitecurse.curse.pool.ActionDef;
import com.example.infinitecurse.curse.pool.AttributeDef;
import com.example.infinitecurse.curse.pool.ConditionDef;
import com.example.infinitecurse.curse.pool.EffectDef;
import com.example.infinitecurse.curse.pool.TemplateDef;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;

/**
 * 数据驱动加载器（§4.2/§11.5）：从数据包读取 templates.json 与 variables.json，可被社区数据包覆盖/扩展。
 * 取资源栈末位（最高优先级）作为生效数据。
 */
public class CurseDataLoader implements SimpleSynchronousResourceReloadListener {
	public static final ResourceLocation FABRIC_ID = new ResourceLocation("infinite_curse", "curses_loader");

	@Override
	public ResourceLocation getFabricId() {
		return FABRIC_ID;
	}

	public static void register() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CurseDataLoader());
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		VariablePool pool = VariablePool.get();
		pool.reset();

		loadOne(manager, new ResourceLocation("infinite_curse", "curses/templates.json"), pool::loadTemplates);
		loadOne(manager, new ResourceLocation("infinite_curse", "curses/variables.json"), pool::loadVariables);

		if (!pool.isLoaded() || pool.templates.isEmpty() || pool.attributes.isEmpty() || pool.effects.isEmpty()) {
			loadBuiltinFallback(pool);
		}
		if (pool.conditions.isEmpty()) {
			loadBuiltinConditions(pool);
		}
	}

	private void loadOne(ResourceManager manager, ResourceLocation id, Loader loader) {
		List<Resource> stack = manager.getResourceStack(id);
		if (stack.isEmpty()) return;
		Resource resource = stack.get(stack.size() - 1);
		try (Reader reader = new BufferedReader(resource.openAsReader())) {
			loader.load(reader);
		} catch (Exception e) {
			// 读取失败：忽略该文件，依赖兜底
		}
	}

	private interface Loader {
		void load(Reader reader);
	}

	/** 数据包缺失时的极小内建兜底，保证引擎不会 NPE。 */
	private void loadBuiltinFallback(VariablePool pool) {
		if (pool.players.isEmpty()) pool.players.add("你");
		if (pool.templates.isEmpty()) {
			TemplateDef t = new TemplateDef();
			t.id = "fallback";
			t.text = "{{玩家}}的{{属性}}{{效果}}。";
			pool.templates.add(t);
		}
		if (pool.attributes.isEmpty()) {
			AttributeDef a = new AttributeDef();
			a.key = "move_speed";
			a.display = "移速";
			a.category = "ATTRIBUTE";
			a.target = "minecraft:generic.movement_speed";
			pool.attributes.add(a);
		}
		if (pool.effects.isEmpty()) {
			EffectDef e = new EffectDef();
			e.key = "half";
			e.display = "减半";
			e.op = "MULTIPLY";
			e.value = 0.5;
			e.severity = -1.0;
			e.impact = 0.6;
			e.baseCost = 240;
			pool.effects.add(e);
		}
	}

	/** 条件类型的内建兜底。 */
	private void loadBuiltinConditions(VariablePool pool) {
		String[][] defs = {
				{"rain", "雨中", "raining"},
				{"night", "夜幕下", "night"},
				{"day", "白昼", "day"},
				{"water", "水下", "underwater"},
				{"deep", "幽邃地底", "underground"},
				{"hurt", "伤重时", "low_health"},
				{"nether", "地狱中", "nether"}
		};
		for (String[] d : defs) {
			ConditionDef c = new ConditionDef();
			c.key = d[0];
			c.display = d[1];
			c.type = d[2];
			pool.conditions.add(c);
		}
	}
}
