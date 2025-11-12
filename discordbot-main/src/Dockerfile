# 使用 OpenJDK 17 Alpine 作为基础镜像
FROM openjdk:17.0-jdk-alpine

# 创建工作目录 /discord
WORKDIR /discord

# 将宿主机上的文件复制到容器的 /discord 目录
COPY /home/discordbot/discordbot.jar /discord/

# 暴露应用程序可能使用的端口（根据 Discord Bot 的具体需求）
# 假设你用的是端口 8080
EXPOSE 8080

# 启动 discordbot.jar
CMD ["java", "-jar", "discordbot.jar"]

