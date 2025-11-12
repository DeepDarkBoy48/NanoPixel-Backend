package robin.discordbot;

import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import robin.discordbot.commond.CommandManager;
import robin.discordbot.listener.ChannelListener;
import robin.discordbot.listener.ForumListener;
import robin.discordbot.listener.PingListener;
import robin.discordbot.listener.ReactionListener;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Slf4j
@Service
public class DiscordBotService {

    private ShardManager shardManager;

    @PostConstruct
    public void init() {
        try {
            Dotenv dotenv = Dotenv.load();
            String discordToken = dotenv.get("discordToken");


             //配置代理
//            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
//                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897)));

            // 初始化 ShardManager
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(discordToken)
                    .setActivity(Activity.playing("Genshin"))
                    .setStatus(OnlineStatus.ONLINE)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableCache(CacheFlag.ONLINE_STATUS,CacheFlag.FORUM_TAGS,CacheFlag.ROLE_TAGS)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES,GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .addEventListeners(new ForumListener(), new CommandManager(),new ReactionListener(),new ChannelListener(),new PingListener());
//                    .setHttpClientBuilder(httpClientBuilder); // 设置 HttpClient 配置

            // 启动 ShardManager
            this.shardManager = builder.build();
            log.info("Discord Bot initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Discord Bot", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (shardManager != null) {
            shardManager.shutdown();
            log.info("Discord Bot shutdown successfully.");
        }
    }
}
