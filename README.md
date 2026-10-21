# 无限诅咒 (Infinite Curse)

基于 Fabric 1.20.1 的 Minecraft 模组，落实《计划书-完整版》的**核心可玩垂直切片**。
命名已统一（§16）：mod id = `infinite_curse`，包名 = `com.example.infinitecurse`（原模板误拼 `infinite-cruse` 已修正）。

## 已实现（核心循环）

- **生成式诅咒引擎（§四）**：`CurseGenerator` 从数据包变量池采样 模板/玩家词/动作/属性/效果，
  组装成可读句子 + 真实机制。severity 在生成阶段恒为负（必为诅咒），改写(flip)后取正。
- **五类真实机制**：属性(AttributeModifier) / 药水(MobEffect) / Tick(饥馑·窒息·腐坏) /
  **条件(Conditional，§四 条件类)**。运算支持 MULTIPLY / SET / RANDOM，翻转语义由 `flipped` 反向倍率实现。
- **条件类诅咒运行时判定（§四 条件类）**：约 28% 的 ATTRIBUTE/TICK 诅咒会附条件（雨中 / 夜幕 / 白昼 / 水下 /
  幽邃地底 / 伤重 / 地狱中），`ConditionEvaluator` 每游戏秒按玩家环境动态启停其效果；
  翻正(`flipped`)后仍以反向倍率生效。条件类型在数据池 `condition` 列表中定义，可被数据包扩展。
- **数据驱动（§4.2/§11.5）**：`data/infinite_curse/curses/{variables,templates}.json`，可被社区数据包覆盖。
- **咒点经济 + 自动索债（§五/§十）**：玩家级咒点；每日（游戏日）自动挑选 `auto && !special && !lethal && reversible`
  且成本最低的诅咒索债——偿清则移除，违约则加剧并受到反噬。
- **诅咒之戒 + 开局 10 诅咒（§八）**：首次进入自动授予戒指并触发 10 道诅咒；戒指右击可再引动一次。
- **千咒台（§七）**：右击打开**图形界面**，按钮执行 净化（1 咒火锭 + 2 逆咒尘，移除最严重诅咒 +100 慰藉）/
  改写（1 咒火锭 + 4 逆咒尘 + round(|score|×120) 咒点，翻转最严重诅咒语义）。
- **无限诅咒商店（§六）**：右击打开**图形界面**，按钮执行 签契返利（获得新诅咒 + 成本×0.3 返利）/
  增咒点（1 咒火锭 → +200）。界面显示玩家咒点、诅咒列表与操作反馈。
- **图形界面（§七/§六）**：`CurseScreen` 客户端界面，按钮通过 C2S 包请求服务端执行；右击方块由客户端
  `UseBlockCallback` 拦截打开，无 ScreenHandler 依赖，兼容单人/专用服务器。
- **HUD + Toast（§12）**：右上角常驻咒点徽标与诅咒列表；新诅咒以弹句提示。通过 S2C 同步包驱动。
- **配置（§11.5）**：`config/infinite_curse.json`（难度系数 / 自动索债开关 / 千咒台成本系数 / 每日上限）。
- **中文 + 英文本地化**，方块/物品复用原版贴图（无需自定义资源）。

## 运行

```bash
./gradlew build          # 产出 build/libs/*.jar
```
将 jar 放入 `.minecraft/mods` 即可。首次进入世界会自动授予戒指与 10 道诅咒。

## 第二阶段（计划中，尚未实现）

- **Boss / 遗迹 / 结局（§九）**：窃取神格、成为千诅万咒之神的终局流程（含专属 BOSS AI、遗迹生成、结局叙事/成就）。
  该部分体量大、需要美术资源，且无法在本地完成真实运行测试，留作后续阶段。
- **更丰富的结局叙事与成就系统。**
- **条件类诅咒的更丰富类型与组合**（如「仅在雨天且伤重时」多条件叠加）。

> 说明：本模组已通过 `./gradlew build` 编译与 remap 验证，但**尚未启动真实 Minecraft 客户端做运行测试**。
> 逻辑（条件启停、界面交互、索债）均为编译期验证，建议首次实机时重点验证：① 右击方块能否打开界面；
> ② 条件诅咒在雨中/夜晚等环境切换时效果是否正确启停；③ 自动索债跨日结算。

## 目录结构

```
src/main/java/com/example/infinitecurse/
  InfiniteCurse.java            主入口 / 注册 / 事件 / C2S 接收
  config/                       配置
  curse/                        诅咒引擎 · 管理器 · 索债 · 数据加载 · 变量池 · 条件判定
  item/                         戒指 · 逆咒尘 · 咒火锭
  block/                        千咒台 · 商店
  network/                      S2C 同步 / C2S 动作通道
src/client/java/.../client/     客户端 HUD · Toast · 同步接收 · 图形界面 · 入口
src/main/resources/             fabric.mod.json · 数据包 · 本地化 · 模型
```
