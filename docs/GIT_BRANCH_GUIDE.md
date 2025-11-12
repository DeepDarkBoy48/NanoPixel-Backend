# Git分支管理实战教学

本文档以DiscordBot项目多模块重构为例，详细讲解Git分支的实际使用场景和最佳实践。

## 📋 实战背景

**任务:** 将单模块Maven项目重构为多模块架构  
**风险:** 重构可能影响项目稳定性，需要在安全环境中进行  
**解决方案:** 使用Git分支进行隔离开发

## 🌳 分支策略概述

### 分支命名规范

```
main                           # 主分支（生产环境）
├── feature/新功能名称           # 功能开发分支
├── refactor/重构名称           # 重构分支（本次使用）
├── hotfix/紧急修复             # 热修复分支
└── develop                    # 开发分支（可选）
```

**本次使用的分支:**
```
main                           # 原始单模块代码
└── refactor/restructure-codebase  # 多模块重构分支
```

## 🚀 实战操作步骤

### 步骤1: 创建重构分支

```bash
# 确保在main分支上
git checkout main

# 获取最新代码
git pull origin main

# 创建并切换到重构分支
git checkout -b refactor/restructure-codebase
```

**分支命名解析:**
- `refactor/` - 前缀表示这是重构类型的分支
- `restructure-codebase` - 具体描述重构内容

### 步骤2: 在重构分支上开发

```bash
# 当前在 refactor/restructure-codebase 分支

# 1. 备份原始配置
cp pom.xml pom.xml.backup

# 2. 创建父模块配置
# 修改 pom.xml 为父模块配置...

# 3. 创建common模块
mkdir -p discordbot-common/src/main/java/robin/discordbot/utils
# 创建 discordbot-common/pom.xml...

# 4. 创建main模块
mkdir -p discordbot-main
cp -r src discordbot-main/
# 创建 discordbot-main/pom.xml...

# 5. 验证构建
mvn clean compile
```

### 步骤3: 提交重构进度

```bash
# 查看当前状态
git status

# 添加所有文件到暂存区
git add .

# 提交重构更改
git commit -m "$(cat <<'EOF'
refactor: 重构为多模块Maven项目

- 创建父模块 discordbot-parent 统一管理依赖版本
- 新增 discordbot-common 模块，包含通用工具类：
  * JwtUtil: JWT令牌工具
  * Md5Util: MD5加密工具  
  * ThreadLocalUtil: 线程本地存储工具
  * messageStore: LangChain4j消息存储实现
- 新增 discordbot-main 模块，包含主要业务逻辑
- 保持业务相关工具类在main模块中
- 配置模块间依赖关系，实现代码复用

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

### 步骤4: 处理敏感信息

```bash
# 发现.env文件被意外提交，需要清理
git reset --soft HEAD~1  # 撤销最后一次提交但保留更改

# 删除敏感文件
rm -rf discordbot-main/target discordbot-common/target
find . -name ".env" -delete

# 更新.gitignore
echo "discordbot-main/src/main/resources/.env" >> .gitignore

# 重新提交
git add .
git commit -m "refactor: 重构为多模块Maven项目 (安全版本)"
```

### 步骤5: 推送到远程仓库

```bash
# 首次推送分支到远程
git push -u origin refactor/restructure-codebase
```

**注意:** 如果推送被拒绝（如包含敏感信息），需要：
```bash
# 强制推送修复后的版本（谨慎使用）
git push --force-with-lease
```

### 步骤6: 清理冗余文件

```bash
# 发现仍有旧的src目录存在，需要清理
rm -rf src

# 提交清理更改
git add .
git commit -m "clean: 删除旧的单模块src目录"
git push
```

### 步骤7: 分支切换验证

```bash
# 切换回main分支查看原始状态
git checkout main

# 清理工作目录中的未跟踪文件
git clean -fd

# 切换回重构分支继续工作
git checkout refactor/restructure-codebase
```

## 🔄 分支操作详解

### 常用分支命令

#### 查看分支
```bash
git branch                    # 查看本地分支
git branch -r                # 查看远程分支
git branch -a                # 查看所有分支
```

#### 创建和切换分支
```bash
git branch <分支名>           # 创建分支
git checkout <分支名>         # 切换分支
git checkout -b <分支名>      # 创建并切换分支
```

#### 分支同步
```bash
git fetch origin             # 获取远程所有分支信息
git pull origin <分支名>      # 拉取远程分支更新
git push origin <分支名>      # 推送本地分支到远程
git push -u origin <分支名>   # 首次推送并设置上游分支
```

### 分支合并策略

#### 1. Merge合并（保留分支历史）
```bash
git checkout main
git merge refactor/restructure-codebase
```

#### 2. Rebase合并（线性历史）
```bash
git checkout refactor/restructure-codebase
git rebase main
git checkout main
git merge refactor/restructure-codebase
```

#### 3. Squash合并（压缩提交）
```bash
git checkout main
git merge --squash refactor/restructure-codebase
git commit -m "refactor: 重构为多模块Maven项目"
```

## ⚠️ 常见问题和解决方案

### 问题1: 分支推送被拒绝

**现象:**
```bash
git push -u origin refactor/restructure-codebase
# error: failed to push some refs
# remote: Repository rule violations found
# remote: Push cannot contain secrets
```

**原因:** 推送包含敏感信息（API密钥、token等）

**解决方案:**
```bash
# 1. 撤销包含敏感信息的提交
git reset --soft HEAD~1

# 2. 删除敏感文件或添加到.gitignore
echo "*.env" >> .gitignore
rm -f src/main/resources/.env

# 3. 重新提交
git add .
git commit -m "fix: 移除敏感信息"

# 4. 强制推送（如果已推送过）
git push --force-with-lease
```

### 问题2: 分支间文件混淆

**现象:**
```bash
git checkout main
# 但工作目录中仍能看到重构分支的文件
```

**原因:** 未跟踪的文件在分支切换时不会被清理

**解决方案:**
```bash
# 清理未跟踪的文件和目录
git clean -fd

# 预览会删除什么（推荐先执行）
git clean -n
```

### 问题3: 提交历史混乱

**现象:** 多次错误提交导致历史记录混乱

**解决方案:**
```bash
# 交互式rebase整理提交历史
git rebase -i HEAD~3

# 或者重置到某个干净的提交点
git reset --hard <clean-commit-hash>
```

## 🎯 最佳实践总结

### 分支命名规范
- **功能分支:** `feature/add-user-authentication`
- **修复分支:** `bugfix/fix-login-error`
- **重构分支:** `refactor/restructure-codebase`
- **热修复:** `hotfix/security-patch`

### 提交信息规范
```bash
# 格式: <类型>: <描述>
git commit -m "feat: 添加用户认证功能"
git commit -m "fix: 修复登录错误"
git commit -m "refactor: 重构项目架构"
git commit -m "docs: 更新API文档"
```

### 分支保护策略
1. **main分支保护** - 禁止直接推送，必须通过PR
2. **代码审查** - 重要变更需要团队审查
3. **自动化测试** - 分支合并前必须通过CI/CD

### 工作流程建议

```bash
# 日常开发流程
1. git checkout main
2. git pull origin main
3. git checkout -b feature/new-feature
4. # 开发代码...
5. git add .
6. git commit -m "feat: 实现新功能"
7. git push -u origin feature/new-feature
8. # 创建Pull Request
9. # 代码审查和合并
10. git checkout main
11. git pull origin main
12. git branch -d feature/new-feature
```

## 📊 本次重构的Git历史

```bash
# 查看重构分支的提交历史
git log --oneline refactor/restructure-codebase

# 输出示例:
96fd8f1 clean: 删除旧的单模块src目录
6050e79 fix: 确保.env文件被git忽略
b07e24d refactor: 重构为多模块Maven项目
bc4a15c remove .DS_Store file
```

## 🔮 后续操作建议

### 创建Pull Request
1. 访问GitHub仓库
2. 点击"Compare & pull request"
3. 填写PR描述:
   - 变更概述
   - 测试状态  
   - 审查要点

### 合并策略选择
- **重要架构变更:** 使用Merge保留完整历史
- **小功能添加:** 可考虑Squash合并
- **紧急修复:** 快速合并后删除分支

### 分支清理
```bash
# 合并后删除本地分支
git branch -d refactor/restructure-codebase

# 删除远程分支
git push origin --delete refactor/restructure-codebase
```

## 📝 总结

通过本次多模块重构实战，我们学会了：

1. **安全重构** - 使用分支隔离风险
2. **敏感信息处理** - 防止机密信息泄露
3. **分支切换** - 在不同版本间自由切换
4. **提交管理** - 保持干净的提交历史
5. **团队协作** - 通过PR进行代码审查

Git分支不仅是代码版本控制工具，更是项目风险管理和团队协作的重要手段。合理使用分支策略，可以让开发过程更加安全、有序、高效。