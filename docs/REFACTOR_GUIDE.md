# Maven多模块重构指南

本文档详细介绍如何将单模块Maven项目重构为多模块架构，以DiscordBot项目为例。

## 📋 重构概述

### 重构前后对比

**重构前（单模块）:**
```
discordbot/
├── pom.xml                    # 单一pom文件
├── src/
│   ├── main/java/robin/discordbot/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── utils/             # 所有工具类混在一起
│   │   └── ...
│   └── resources/
└── target/
```

**重构后（多模块）:**
```
discordbot-parent/
├── pom.xml                    # 父模块pom
├── discordbot-common/         # 通用工具模块
│   ├── pom.xml
│   └── src/main/java/robin/discordbot/utils/
│       ├── JwtUtil.java       # 纯工具类
│       ├── Md5Util.java
│       ├── ThreadLocalUtil.java
│       └── messageStore.java
└── discordbot-main/           # 主业务模块
    ├── pom.xml
    └── src/main/java/robin/discordbot/
        ├── controller/
        ├── service/
        ├── utils/             # 业务相关工具类
        └── ...
```

## 🚀 详细重构步骤

### 步骤1: 备份原始配置

```bash
# 备份原始pom.xml
cp pom.xml pom.xml.backup

# 创建新分支进行重构（推荐）
git checkout -b refactor/multi-module
```

### 步骤2: 创建父模块pom.xml

将原始的 `pom.xml` 改造为父模块配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- 父模块配置 -->
    <groupId>robin</groupId>
    <artifactId>discordbot-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>  <!-- 注意：改为pom -->
    <name>discordbot-parent</name>
    <description>Discord Bot Parent Module</description>

    <!-- 子模块声明 -->
    <modules>
        <module>discordbot-common</module>
        <module>discordbot-main</module>
    </modules>

    <!-- 属性配置 -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>
        <spring-boot.version>3.3.3</spring-boot.version>
        <langchain4j.version>1.0.0-beta3</langchain4j.version>
        <lombok.version>1.18.34</lombok.version>
    </properties>

    <!-- 依赖管理 - 统一版本管理 -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- 内部模块依赖 -->
            <dependency>
                <groupId>robin</groupId>
                <artifactId>discordbot-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            
            <!-- 其他第三方依赖版本管理 -->
            <!-- ... -->
        </dependencies>
    </dependencyManagement>

    <!-- 公共插件配置 -->
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
                        <source>17</source>
                        <target>17</target>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### 步骤3: 创建common模块

#### 3.1 创建目录结构
```bash
mkdir -p discordbot-common/src/main/java/robin/discordbot/utils
```

#### 3.2 创建common模块的pom.xml
`discordbot-common/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- 继承父模块 -->
    <parent>
        <groupId>robin</groupId>
        <artifactId>discordbot-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>discordbot-common</artifactId>
    <packaging>jar</packaging>
    <name>discordbot-common</name>
    <description>Discord Bot Common Utilities Module</description>

    <dependencies>
        <!-- 只包含纯工具类需要的基础依赖 -->
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
        
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 3.3 移动纯工具类
分析原项目中的工具类，将**不依赖业务逻辑**的纯工具类移动到common模块：

```bash
# 移动纯工具类
cp src/main/java/robin/discordbot/utils/JwtUtil.java discordbot-common/src/main/java/robin/discordbot/utils/
cp src/main/java/robin/discordbot/utils/Md5Util.java discordbot-common/src/main/java/robin/discordbot/utils/
cp src/main/java/robin/discordbot/utils/ThreadLocalUtil.java discordbot-common/src/main/java/robin/discordbot/utils/
cp src/main/java/robin/discordbot/utils/messageStore.java discordbot-common/src/main/java/robin/discordbot/utils/
```

**判断标准：**
- ✅ 移动到common：`JwtUtil`、`Md5Util`、`ThreadLocalUtil` - 纯工具类
- ❌ 保留在main：`GeminiFactory`、`aiHistoryUtil` - 依赖业务类和配置

### 步骤4: 创建main业务模块

#### 4.1 创建目录结构
```bash
mkdir -p discordbot-main
```

#### 4.2 移动主要业务代码
```bash
# 复制整个src目录到main模块
cp -r src discordbot-main/

# 删除main模块中已移动到common的工具类
rm discordbot-main/src/main/java/robin/discordbot/utils/JwtUtil.java
rm discordbot-main/src/main/java/robin/discordbot/utils/Md5Util.java
rm discordbot-main/src/main/java/robin/discordbot/utils/ThreadLocalUtil.java
rm discordbot-main/src/main/java/robin/discordbot/utils/messageStore.java
```

#### 4.3 创建main模块的pom.xml
`discordbot-main/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- 继承父模块 -->
    <parent>
        <groupId>robin</groupId>
        <artifactId>discordbot-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>discordbot-main</artifactId>
    <packaging>jar</packaging>
    <name>discordbot-main</name>
    <description>Discord Bot Main Business Module</description>

    <dependencies>
        <!-- 引用common模块 -->
        <dependency>
            <groupId>robin</groupId>
            <artifactId>discordbot-common</artifactId>
        </dependency>
        
        <!-- Spring Boot相关依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- 数据库相关 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        
        <!-- AI相关 -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Discord JDA -->
        <dependency>
            <groupId>net.dv8tion</groupId>
            <artifactId>JDA</artifactId>
        </dependency>
        
        <!-- 其他业务相关依赖 -->
        <!-- ... -->
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot打包插件 -->
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

### 步骤5: 处理import依赖

在main模块中，需要更新对工具类的引用。由于Maven会自动处理模块间的依赖，通常不需要修改import语句，除非有包名变更。

### 步骤6: 清理和优化

#### 6.1 删除原始src目录
```bash
# 确认一切正常后删除原始src目录
rm -rf src
```

#### 6.2 更新.gitignore
```gitignore
# Maven build directories
target/
*/target/

# Environment files with sensitive data
.env.local
.env.production
**/.env.local
**/.env.production
**/src/main/resources/.env

# IDE files
.DS_Store
```

### 步骤7: 验证构建

```bash
# 清理并编译所有模块
mvn clean compile

# 安装到本地仓库
mvn clean install

# 运行主模块
cd discordbot-main
mvn spring-boot:run
```

## 🎯 重构的核心要点

### Maven配置关键点

1. **父模块pom.xml要点:**
   - `<packaging>pom</packaging>` - 必须设置为pom
   - `<modules>` - 声明所有子模块
   - `<dependencyManagement>` - 统一管理依赖版本
   - `<pluginManagement>` - 统一管理插件配置

2. **子模块pom.xml要点:**
   - `<parent>` - 继承父模块
   - 只需要声明`<artifactId>`，groupId和version继承自父模块
   - 在`<dependencies>`中引用其他模块时不需要指定版本

3. **依赖关系:**
   - common模块：只包含基础依赖，不依赖其他业务模块
   - main模块：依赖common模块，包含所有业务相关依赖

### 模块划分原则

1. **common模块应该包含:**
   - 纯工具类（不依赖Spring上下文）
   - 通用常量和枚举
   - 基础数据结构
   - 可复用的算法

2. **main模块应该包含:**
   - 业务逻辑代码
   - Spring配置类
   - 依赖外部服务的工具类
   - 特定领域的工具类

### 常见问题和解决方案

#### 问题1: 编译失败，找不到类
**原因:** 工具类移动后，某些业务类无法找到引用  
**解决:** 检查import语句，确认类确实在common模块中

#### 问题2: 循环依赖
**原因:** common模块引用了main模块的类  
**解决:** 重新分析依赖关系，将业务相关的工具类保留在main模块

#### 问题3: 依赖冲突
**原因:** 父模块和子模块版本不一致  
**解决:** 使用`<dependencyManagement>`统一管理版本

## 📊 重构效果

### 优势
- ✅ **代码复用性提升** - common模块可被其他项目引用
- ✅ **依赖管理优化** - 父模块统一版本管理
- ✅ **构建性能提升** - 模块可以并行构建
- ✅ **代码组织清晰** - 业务逻辑与工具类分离
- ✅ **团队协作友好** - 不同团队可负责不同模块

### 注意事项
- ⚠️ **增加复杂性** - 需要维护多个pom文件
- ⚠️ **构建顺序** - 需要先构建common再构建main
- ⚠️ **IDE支持** - 确保IDE正确识别多模块结构

## 🔧 后续优化建议

1. **添加更多模块:** 如`discordbot-api`、`discordbot-data`等
2. **集成测试:** 在父模块添加集成测试配置
3. **文档生成:** 配置javadoc插件为每个模块生成文档
4. **CI/CD配置:** 更新构建脚本支持多模块构建

## 📝 总结

多模块重构是一个系统性工程，核心在于：
1. 合理的模块划分
2. 正确的Maven配置
3. 清晰的依赖关系
4. 充分的测试验证

通过这次重构，项目结构更加清晰，代码复用性得到提升，为后续开发和维护奠定了良好基础。