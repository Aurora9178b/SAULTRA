# SA ULTRA Client

从 `SA ULTRA Client.jar`（addon 版本 **0.1.2**）恢复的 Meteor Client addon 源码工程，目标为 **Minecraft 26.1.2 / Fabric / Java 25**。

原 JAR 的作者标记为 **kbdps**，mod ID 为 `seaastral`。本仓库保存反编译得到的 Java 代码、原始图片资源，以及重新建立的 Gradle 构建和开发环境。原作者的注释、原始局部变量名和原始构建脚本无法由字节码完整还原；本仓库不是原作者发布的原始源码。

## 环境

| 组件 | 版本 |
| --- | --- |
| JDK | 25 |
| Minecraft | 26.1.2 |
| Fabric Loader | 0.19.2 |
| Fabric Loom | 1.16.3 |
| Gradle Wrapper | 9.7.1 |
| Meteor Client | 26.1.2，固定构建 `20260810.135545-42` |

需要可访问 Minecraft、Fabric、Meteor 和 Maven 依赖仓库的网络。仓库包含 Gradle Wrapper，无须另外安装 Gradle。Minecraft 26.1.2 使用官方可读名称，因此此项目不使用旧版本的 Yarn 映射或 `modImplementation`。

## 编译

Windows PowerShell（先把 `JAVA_HOME` 改为自己的 JDK 25 安装目录）：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean build
```

Linux / macOS：

```bash
export JAVA_HOME=/path/to/jdk-25
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean build
```

输出：

- `build/libs/sa-ultra-client-0.1.2.jar`：可加载的 addon。
- `build/libs/sa-ultra-client-0.1.2-sources.jar`：源码包。

用于普通游戏实例时，安装相应的 Fabric Loader 和 **26.1.2** 版 Meteor Client，再将 addon JAR 放进实例的 `mods` 目录。addon 不内置 Meteor Client。

## 启动开发客户端

```powershell
.\gradlew.bat runClient
```

```bash
./gradlew runClient
```

`runClient` 自动加载当前工程和 Meteor 依赖，使用独立的 `run/` 目录，不需要把已编译的 addon 再复制进 `run/mods`。这是开发环境；联机使用正版账户的认证需求由 Minecraft / Meteor 自身处理。

也可在 IntelliJ IDEA 中把仓库作为 Gradle 工程导入，选择 JDK 25，运行 Gradle 的 `runClient` 任务。

## 内容

- 10 个模块：`TpMace`、`Circle`、`TpGo`、`CircleMove`、`AntiMiss`、`MonsterKill`、`BizarreTp`、`AutoEZ`、`AutoMama`、`SpearAura`。
- 3 个命令实现：`TpCommand`、`TpKill`、`TpPop`。
- 模块 HUD、5 个 Mixin、主入口和 5 个原始 PNG。
- `src/main/resources/fabric.mod.json` / `seaastral.mixins.json`：Fabric 入口与 Mixin 配置。
- `.github/workflows/build.yml`：JDK 25 编译检查和构建产物上传。

## 恢复记录与归属

输入文件 SHA-256：

```text
e1da7113898b348b48ce56790c8427a62a22edf7ab4d712e8656f7e63c2fbe19
```

反编译工具为 Vineflower 1.12.0。保留原 addon 的包名、功能和资源；构建脚本、Wrapper 和工作流为恢复项目重新建立。详细验证记录见 [docs/RECOVERY.md](docs/RECOVERY.md)。

输入 JAR 未包含许可证文件，本仓库不为原代码或图片另行指定开源许可证；相关权利归各自权利人。Gradle Wrapper 文件来自 Gradle 官方发行源码，保留其原有许可声明。
