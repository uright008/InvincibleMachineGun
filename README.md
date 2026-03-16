# InvincibleMachineGun
A personal addon for the Meteor client that adds various entertainment modules. Most modules are my original ideas, implemented with AI assistance; some third‑party modules have been modified and are included.
用于Meteor客户端的个人插件，添加了各种娱乐模块。方便我自己使用，大多数模块是原创想法，运用大量AI代码实现（gemini，deepseek），基本没写任何代码哈哈；有很几个模块是别人的，已被修改并包括在内。
看到b站上别人有什么好玩的功能我就试着做一个，或者自己想用的功能就做一个，就这样，玩玩就做了4个月，有这些模块了。有些是大量搜集各种开源插件，或者反编译别人的jar来抄的。模块都是根据个人需求弄得，有什么可修改的尽管改，代码随你用。从2025年11月20几号，玩插件已经玩了4个月了学到了很多编程。我已经玩厌了所以把插件开源了。
这个插件我做了两个版本。1.21.4和1.21.11版本
1.21.4多几个模块，比如a*寻路百米刀、着色器（有bug。不会修），击杀特效之类的。因为我玩1.21.11版本了，所以把插件更新到1.21.11，这些模块没有移植到1.21.11，太麻烦，且没啥用。如果有人想移植过来可以试试
有些功能是没有完成的只是基本，很多空壳。很多功能不能绕过反作弊插件。只能原版服玩玩。如果有真正的开发者感兴趣可以对代码进行修改完善这个插件
很多没用的功能，做出来玩的，和没做完的构想。
很多模块都是中文的，方便我自己用。原版meteor不支持中文渲染，需要用meteor中文插件，或者别的办法显示中文。
基本是从meteor的那个模板插件开始开发的，有些模板的内容还没改，懒得改了。

以下功能列表是ai生成的，仅供参考，具体功能，有啥作用，以源码实际为主
# 1.21.11版模块完整介绍 67个模块
战斗类模块 (Combat)
MaceAura - 狼牙棒自动攻击光环
MaceBreakerPro - 专业狼牙棒破坏者
MaceDMGPlus - 狼牙棒伤害增强
CrossbowAura - 弩箭自动攻击光环
TpAura - 传送攻击光环
AttractAura - 吸引光环
TargetStrafe - 目标环绕攻击
SwordGap - 剑与金苹果自动切换
SpearExploit - 长矛利用技巧
AdvancedCriticals - 高级暴击系统
GrimCriticals - 致命暴击系统
AttackRangeIndicator - 攻击范围指示器
HitboxESP - 实体碰撞箱显示
ShieldESP - 盾牌ESP显示
移动类模块 (Movement)
ElytraFlyPlus - 鞘翅飞行增强版
ElytraFollower - 鞘翅跟随器
FlightAntiKick - 飞行防踢
VelocityAlien - 速度控制
KnockbackDirection - 击退方向控制
LegitNoFall - 合法无摔落伤害
AutoJump - 自动跳跃
AutoRespawn - 自动重生
PearlPhase - 末影珍珠穿墙
ODMGear - ODM装备移动
挖掘类模块 (Mining)
ScaffoldPlus - 脚手架增强版
xhPacketMinePlus - 数据包挖掘增强
AdaPacketMine - Ada数据包挖掘
TntBomber - TNT轰炸机
AntiAntiXray - 反反矿物透视
OreVeinESP - 矿脉ESP显示
MineESP - 矿洞ESP显示
FillESP - 填充物ESP显示
RTsearch - 实时搜索
自动操作类模块 (Automation)
AutoChestAura - 箱子自动开启光环
AutoChorus - 紫颂果自动使用
AutoDeoxidizer - 自动除锈器
AutoFirework - 自动烟花
AutoSmithing - 自动锻造
AutoTPAccept - 自动接受传送
AutoServer - 自动服务器连接
AutoNod - 自动点头
AutoMessage - 自动消息发送
AutoKouZi - 自动扣字
MacroAnchor - 宏锚点
TpAnchor - 传送锚点
FastCrossbow - 快速弩箭装填
视觉类模块 (Visual)
PortalESP - 传送门ESP显示
CustomItemESP - 自定义物品ESP
ModuleList - 模块列表显示
xhEntityList - 实体列表显示
ItemDespawnTimer - 物品消失计时器
CustomFov - 自定义视野
KillFX - 击杀特效
聊天类模块 (Chat)
ChatPrefixCustom - 自定义聊天前缀
ChatHighlight - 聊天高亮
ChatFilter - 聊天过滤器
ChatHider - 聊天隐藏器
HexChat - 十六进制聊天
InfiniteChat - 无限聊天
FeedbackBlocker - 反馈屏蔽器
实用工具类模块 (Utility)
AdvancedFakePlayer - 高级假人
adaManualCrystal - 手动水晶放置
adaAutoHotbar - 自动快捷栏
adaAttributeSwap - 属性交换
MassTpa - 批量传送请求
MusicPlayer - 音乐播放器
SchematicPro - 专业建筑方案
模块特点总结
这些模块具有以下共同特点：

基于Meteor客户端框架开发
支持中文界面和设置
包含详细的配置选项
具有实时渲染和事件处理功能
针对PVP和生存模式优化
每个模块都提供了特定的游戏功能增强，从基础的自动操作到高级的战斗辅助，涵盖了Minecraft游戏的各个方面。这些模块的设计目标是提高游戏效率和玩家体验，特别是在竞争性游戏环境中。（包括这个介绍列表也是ai生成）

# 1.21.4版 模块列表 (85个模块)
战斗类模块 (Combat)
近战战斗模块
SilentAura - 静默杀戮光环，无声攻击
AdvancedCriticals - 高级暴击系统 (Alien V4 MaceSpoof移植版)
GrimCriticals - GrimAC风格暴击系统
TotemSmash - 图腾破坏者，快速摧毁不死图腾
TeleportCriticals - 传送暴击，结合瞬移的暴击系统
TotemBypass - 图腾绕过，绕过不死图腾保护
TpAura - 如来神掌，从天而降的掌法
ArmorBreaker - 护甲破坏者，快速破坏敌人护甲
SimpleTpAura - 简单传送光环
MaceAura - 锤子光环，专门针对锤子的攻击系统
TargetStrafe - 目标环绕，围绕目标移动攻击
AnchorGod - 锚点之神，利用重生锚进行战斗
AutoCrystalPlus - 自动水晶增强版，智能放置和引爆水晶
MaceDMGPlus - 降龙十八掌，锤子伤害增强系统
xhGrimAura - 高度定制的GrimAC杀戮光环
MacePathAura - 寻路版降龙十八掌，智能绕路+重锤秒杀
MaceBreakerPro - 没敌打断，飞机大炮式盾牌打断
SmartTPAura - 智能传送光环，集成LB/Jigsaw灵魂的终极百米瞬移
TeleportNotebot - 如来神掌，深度集成A*与人体盒渲染
TestTpAura - 测试传送光环，LiquidBounce移植版
CrossbowAura - 万弩射江潮，机关枪式弩箭攻击
FastCrossbow - 快速弩箭，三模式的机关弩
AttractAura - 吸引光环，将敌人吸引到身边
SpearExploit - 长矛利用，特殊长矛攻击技巧
SwordGap - 剑与金苹果组合攻击
KnockbackDirection - 击退方向控制，强制击退方向
远程战斗模块
TntBomber - TNT轰炸机，自动放置和引爆TNT
AutoFirework - 自动烟花，利用烟花进行攻击
移动与传送类模块 (Movement & Teleport)
AutoJump - 自动跳跃，智能跳跃系统
FlightAntiKick - 飞行防踢，防止飞行时被踢出
ElytraFollower - 鞘翅跟随，自动跟随其他玩家飞行
VelocityAlien - 速度外星人，速度控制模块
PearlPhase - 珍珠相位，末影珍珠传送技巧
TpAnchor - 传送锚点，利用锚点进行传送
WoodenMan - 123木头人，被盯住时背身或瞬移
挖掘与资源类模块 (Mining & Resources)
AutoMiner - 自动矿工，智能挖掘系统
AutoObsidian - 自动黑曜石，快速获取黑曜石
AntiAntiXray - 反反矿透Pro Max，智能防误用矿透系统
MineESP - 挖掘提示，高亮显示其他玩家正在挖掘的方块
OreVeinESP - 矿脉扫描，高性能矿脉扫描与Baritone联动
xhPacketMinePlus - 发包挖掘增强版，Bypass Ground Optimized
AutoDeoxidizer - 自动除锈，铜块除锈系统
聊天与社交类模块 (Chat & Social)
AutoMessage - 定时发送消息，秒级延迟的消息发送
AutoKouZi - 自动扣字机，自动垃圾消息发送
ChatFilter - 聊天过滤器，过滤特定聊天内容
ChatHider - 聊天隐藏器，隐藏聊天消息
ChatHighlight - 聊天高亮，高亮显示重要聊天内容
ChatPrefixCustom - 改前缀术，强制修改并美化全端前缀
HexChat - 十六进制聊天，特殊格式聊天
InfiniteChat - 无限聊天，突破聊天限制
CommandFlooder - 命令洪水，大量发送命令
AutoTPAccept - 自动TP接受，自动接受传送请求
MassTpa - 批量传送，同时向多个玩家发送传送请求
视觉与渲染类模块 (Visual & Render)
CustomArmor - 自定义护甲，修改玩家护甲渲染
CustomFov - 自定义视野，调整游戏视野
CustomItemESP - 自定义物品ESP，高亮显示物品
CyberFujiOverlay - 赛博富士叠加层，赛博朋克风格界面
DuskOverlay - 黄昏叠加层，黄昏风格界面
SakuraOverlay - 樱花叠加层，樱花风格界面
SilentHillOverlay - 寂静岭叠加层，恐怖风格界面
MatrixOverlay - 矩阵叠加层，黑客帝国风格界面
HitboxESP - 碰撞箱ESP，显示实体碰撞箱
ShieldESP - 盾牌ESP，显示盾牌状态
PortalESP - 传送门ESP，高亮显示传送门
ItemDespawnTimer - 物品消失计时器，显示物品消失时间
AttackRangeIndicator - 攻击范围指示器，显示攻击范围
ModuleList - 模块列表增强版，屏幕显示模块列表
实用工具类模块 (Utility)
AutoRespawn - 自动重生，死亡后自动重生
AutoServer - 自动服务器，自动连接服务器
AutoChorus - 自动紫颂果，自动使用紫颂果传送
AutoSmithing - 自动锻造，自动使用锻造台
AutoChestAura - 自动箱子光环，自动打开箱子
FeedbackBlocker - 反馈阻止器，阻止游戏反馈
GTest - G测试，测试模块
KillFX - 击杀特效，击杀敌人时的特效
MusicPlayer - 音乐播放器，全自动点歌机
VanishDetector - 消失检测器，检测玩家消失
AdvancedFakePlayer - 高级假人，能受击的假人
AntiSignPluginGUI - 反告示牌插件GUI，绕过告示牌限制
CreativeLavaMountain - 创造模式熔岩山，快速建造熔岩山
RTsearch - 实时搜索，实时搜索功能
xhEntityList - 实体列表，显示实体信息
AutoNod - 自动点头，支持发包/可见模式切换
FlightAntiKick - 飞行防踢，防止飞行时被踢出
VelocityAlien - 速度外星人，速度控制模块
模块功能特点总结
技术特色
多版本兼容：大部分模块支持Minecraft 1.21.4及更早版本
智能算法：集成A*寻路、预测算法等高级功能
反检测机制：内置多种反作弊绕过技术
高度可定制：丰富的设置选项满足不同需求
性能优化
高效渲染：优化的ESP和叠加层渲染系统
智能缓存：减少不必要的计算和网络请求
多线程支持：部分模块支持多线程处理
用户体验
中文界面：大部分模块提供中文界面和描述
直观设置：分组设置界面，易于配置
实时反馈：提供实时状态显示和效果预览
这个模块集合涵盖了Minecraft游戏的各个方面，从基础的战斗功能到高级的自动化系统，为玩家提供了全面的游戏增强体验。（ai生成）
