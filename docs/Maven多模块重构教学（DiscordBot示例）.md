# Maven 多模块重构教学（DiscordBot 示例）

面向零基础小白，手把手讲解：如何把一个单模块的 Spring Boot 项目重构为 Maven 多模块结构，并说明每一步 Maven POM 怎么改、源码和资源文件怎么移动。本指南同时对照当前仓库的实际重构结果（父模块 + common + main）。

---

## 1. 为什么做多模块

- 代码拆分清晰：公共工具、实体、常量等沉淀到 `common`，业务逻辑留在业务模块里。
- 复用与扩展：多个业务模块可依赖同一个 `common`，减少重复代码。
- 构建更快更灵活：只编译/运行需要的子模块；父模块统一依赖与插件版本。

---

## 2. 重构前后结构对比

重构前（单模块）：
```
project-root/
├── pom.xml
├── src/
│   ├── main/java/robin/discordbot/...
│   └── main/resources/...
└── target/
```

重构后（多模块）：
```
project-root/  （父模块：聚合 + 版本管理）
├── pom.xml
├── discordbot-common/   （公共模块：工具/通用类）
│   ├── pom.xml
│   └── src/main/java/robin/discordbot/utils/...
└── discordbot-main/     （主业务模块：Spring Boot 应用）
    ├── pom.xml
    ├── src/main/java/robin/discordbot/...
    ├── src/main/resources/...
    └── src/test/java/...
```

本仓库现状与上图一致：父模块为 `discordbot-parent`，子模块为 `discordbot-common` 与 `discordbot-main`。

---

## 3. 开始前先备份（强烈推荐）

```bash
# 备份原始 POM
cp pom.xml pom.xml.backup

# 用分支做重构，随时可回退
git checkout -b refactor/multi-module
```

---

## 4. 改造根 pom.xml（父聚合 POM）

目标：把根 `pom.xml` 改成“父模块（聚合 POM）”，只负责：
- 声明所有子模块（`<modules>`）
- 统一依赖版本（`<dependencyManagement>`）
- 统一插件版本（`<pluginManagement>`）

示例（简洁版，可直接用）：
```xml
<!-- project-root/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>robin</groupId>
  <artifactId>discordbot-parent</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>discordbot-parent</name>

  <!-- 子模块列表 -->
  <modules>
    <module>discordbot-common</module>
    <module>discordbot-main</module>
  </modules>

  <!-- 统一属性 -->
  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.3</spring-boot.version>
    <langchain4j.version>1.0.0-beta3</langchain4j.version>
    <lombok.version>1.18.34</lombok.version>
  </properties>

  <!-- 统一依赖版本（推荐引入 Spring Boot BOM） -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <!-- 内部模块版本（子模块引用时可不写 version） -->
      <dependency>
        <groupId>robin</groupId>
        <artifactId>discordbot-common</artifactId>
        <version>${project.version}</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <!-- 统一插件版本配置 -->
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
          <version>${spring-boot.version}</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.11.0</version>
          <configuration>
            <source>${java.version}</source>
            <target>${java.version}</target>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

进阶版：如果你有 MapStruct/Lombok 编译需求，可把注解处理器的配置放进 `<pluginManagement>` 的 `maven-compiler-plugin` 中（本仓库已采用）。

---

## 5. 新建公共模块：discordbot-common

目录：`discordbot-common/pom.xml`

作用：存放“可被多个业务复用且不依赖业务框架”的代码，例如工具类、通用 DTO、常量、枚举等。

示例 POM：
```xml
<!-- discordbot-common/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>robin</groupId>
    <artifactId>discordbot-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <!-- 建议加：<relativePath>../pom.xml</relativePath>，便于 IDE 单独导入子模块时找到父 POM -->
  </parent>

  <artifactId>discordbot-common</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <!-- 工具性质依赖，尽量精简 -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>cn.hutool</groupId>
      <artifactId>hutool-all</artifactId>
    </dependency>
    <dependency>
      <groupId>com.alibaba.fastjson2</groupId>
      <artifactId>fastjson2</artifactId>
    </dependency>
    <dependency>
      <groupId>com.auth0</groupId>
      <artifactId>java-jwt</artifactId>
    </dependency>
  </dependencies>
</project>
```

代码移动建议（示例）：
- `src/main/java/robin/discordbot/utils/JwtUtil.java`
- `src/main/java/robin/discordbot/utils/Md5Util.java`
- `src/main/java/robin/discordbot/utils/ThreadLocalUtil.java`
- `src/main/java/robin/discordbot/utils/messageStore.java`

> 提醒：尽量不要把 Web、数据库、JDA 等强业务依赖放进 common，否则会让公共模块“变重”。

---

## 6. 新建主业务模块：discordbot-main

目录：`discordbot-main/pom.xml`

作用：Spring Boot 主应用，包含 Controller、Service、Config、Listener、Mapper 等。

示例 POM：
```xml
<!-- discordbot-main/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>robin</groupId>
    <artifactId>discordbot-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>

  <artifactId>discordbot-main</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <!-- 依赖 common：版本走父 POM 的 dependencyManagement，无需写 <version> -->
    <dependency>
      <groupId>robin</groupId>
      <artifactId>discordbot-common</artifactId>
    </dependency>

    <!-- Spring Boot 基础与常用功能 -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>

    <!-- 数据库相关 -->
    <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId></dependency>
    <dependency><groupId>com.github.pagehelper</groupId><artifactId>pagehelper-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>

    <!-- AI / LLM 相关 -->
    <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j-open-ai-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j-google-ai-gemini</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j-community-dashscope</artifactId></dependency>
    <dependency><groupId>dev.langchain4j</groupId><artifactId>langchain4j-community-redis</artifactId></dependency>

    <!-- 其他依赖 -->
    <dependency><groupId>net.dv8tion</groupId><artifactId>JDA</artifactId></dependency>
    <dependency><groupId>io.github.cdimascio</groupId><artifactId>dotenv-java</artifactId></dependency>
    <dependency><groupId>com.squareup.okhttp3</groupId><artifactId>okhttp</artifactId></dependency>
    <dependency><groupId>software.amazon.awssdk</groupId><artifactId>s3</artifactId></dependency>

    <!-- 测试依赖 -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mybatis.spring.boot</groupId><artifactId>mybatis-spring-boot-starter-test</artifactId><version>3.0.3</version><scope>test</scope></dependency>

    <!-- Lombok（仅编译期） -->
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><scope>provided</scope><optional>true</optional></dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

源码与资源迁移：
- 将原根目录 `src` 下的“业务代码”整体搬到 `discordbot-main/src/`（保持原包名不变）
- 将 YAML、Mapper XML、静态资源等搬到 `discordbot-main/src/main/resources/`
- 启动类（`@SpringBootApplication`）放在 `discordbot-main/src/main/java/robin/discordbot/`

---

## 7. 构建与运行（命令）

在项目根目录（父模块）执行：
- 聚合构建（安装到本地仓库）：`mvn -DskipTests clean install`
- 运行主模块：`mvn -pl :discordbot-main -am spring-boot:run`
- 打包主模块：`mvn -pl :discordbot-main -am -DskipTests package`

Tip：第一次构建需联网下载依赖；若 IDE 报依赖解析失败，先在根执行一次 `mvn install`。

---

## 8. IDE（IntelliJ IDEA）设置

- 用“根 pom.xml”导入整个工程（不要只导入子模块）。
- 右侧 Maven 面板点“Reload All Maven Projects”刷新依赖。
- 若子模块找不到父 POM，在子模块 `<parent>` 内添加：
  - `<relativePath>../pom.xml</relativePath>`
- 若提示 `Unresolved dependency robin:discordbot-common`：
  - 确认 `父 POM` 的 `<modules>` 列出了 `discordbot-common`、`discordbot-main`
  - 先在根执行 `mvn install`，再刷新 Maven

---

## 9. 常见问题排查

- 版本冲突/依赖过多：尽量在父 POM 的 `dependencyManagement` 里统一管理版本；子模块引用时不写 `<version>`。
- 公共模块“变重”：common 只放工具/通用类，避免引入 Web、DB、JDA 等重量依赖。
- 资源加载不到（如 MyBatis XML）：确保 XML 位于 `discordbot-main/src/main/resources/**` 并按需配置扫描路径。
- Git 回退：
  - 丢弃未提交改动：`git reset --hard HEAD && git clean -fd`
  - 回到某次提交：`git reset --hard <commit>` 或 `git revert <commit>`（不改历史）

---

## 10. 本仓库实际重构摘要（供对照）

- 父模块：`discordbot-parent`（现有 `pom.xml` 已为聚合 POM，含 dependencyManagement 与 pluginManagement）
- 子模块：
  - `discordbot-common`：放置 `JwtUtil`、`Md5Util`、`ThreadLocalUtil`、`messageStore` 等工具类
  - `discordbot-main`：保留 Controller/Service/Config/Listener/Mapper、资源与测试
- 备份：在重构时保留了 `pom.xml.backup`

你可以按本文的“简洁版” POM 先跑通，再逐步把复杂的版本管理、注解处理器等配置迁移到父 POM 的 `dependencyManagement` 和 `pluginManagement` 中（与当前仓库一致）。

---

祝使用顺利！有需要我可以基于你当前代码，继续按上述步骤把公共代码梳理进 `discordbot-common`，并校验构建与运行流程。

