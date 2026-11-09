# 源码恢复与验证记录

## 输入

- 文件名：`SA ULTRA Client.jar`
- 文件大小：3,002,173 字节
- SHA-256：`e1da7113898b348b48ce56790c8427a62a22edf7ab4d712e8656f7e63c2fbe19`
- JAR 声明的名称：SA ULTRA Client
- JAR 声明的作者：kbdps
- mod ID / 版本：`seaastral` / `0.1.2`
- Minecraft / Java：`26.1.2` / `>=25`
- 原构建信息：Loom `1.16.3`、Gradle `9.4.1`、Fabric Loader `0.19.2`、official namespace。

## 恢复方式

使用 Vineflower 1.12.0 在 JDK 25 下将字节码反编译成 Java，按原始包名组织进 `src/main/java`。将全部原始资源保存至 `src/main/resources`。未使用原始 JAR 作为编译依赖，也未将原始 class 文件混入输出。

恢复结果包含 20 个 Java 源文件，编译生成 22 个 class，包含 HUD 内部类及 `TpMace.ScoredPos`。全部模块、命令、Mixin 和 HUD 均有实际实现。

重新建立 Gradle 工程、Wrapper、源码包任务、`runClient` 和 GitHub Actions。Loader 与 Loom 使用原 JAR 的版本；Meteor 固定为兼容的 `26.1.2-20260810.135545-42` 构建。由于本地原 Gradle 发行包下载缓慢，Wrapper 使用已验证兼容的 Gradle 9.7.1，并校验官方 SHA-256。

`fabric.mod.json` 中的版本改为由 `gradle.properties` 的 `mod_version` 生成；打包后的值仍为 `0.1.2`。原代码功能、模块设置、名称、聊天文本及 Mixin 配置予以保留。未宣称修复原 addon 本身的游戏逻辑。

## 本地验证

2026-11-09，Windows、JDK 25.0.2：

| 检查 | 结果 |
| --- | --- |
| `gradlew.bat build` | 通过，源码实际编译成功 |
| `gradlew.bat clean build` | 通过，生成 addon 与 sources JAR |
| 原始与输出 class 路径清单 | 一致，均为 22 个 |
| 5 个 PNG 的 SHA-256 对比 | 全部一致 |
| `gradlew.bat runClient` | 客户端进程启动，Fabric/Meteor/addon 初始化完成，正常退出，Gradle 返回成功 |

启动日志中的关键事件：

```text
Loading Minecraft 26.1.2 with Fabric Loader 0.19.2
- meteor-client 26.1.2-42
- seaastral 0.1.2
Reloading ResourceManager: vanilla, fabric-api-base, fabric-resource-loader-v1, fabricloader, meteor-client, seaastral
Initializing SA ULTRA Client
Sound engine started
Stopping!
BUILD SUCCESSFUL
```

开发启动保留了 Meteor 对未安装 Baritone 的可选 Mixin 警告，以及开发账户无法认证 Realms 的提示；这些未阻止初始化。Gradle 提示 Loom 使用了计划在 Gradle 10 移除的 API，当前固定的 Gradle 9.7.1 构建成功。

Windows 界面检查服务不可用，因此本次未做截图或按钮交互验证。未进行多人服务器、战斗模块、命令或 HUD 的逐项游戏行为测试。上述结果证明源码构建和开发启动流程可用，不代表对原 addon 所有行为的完整回归测试。

## 原始信息的限制

JAR 没有许可证文件和源码附件。恢复无法获得已被编译器丢弃的注释、原始排版或所有原始变量名。反编译得到的源码不保证与原始 class 字节完全相同。本仓库保留输入 JAR 的作者信息，不另行授予其代码及图片的许可。
