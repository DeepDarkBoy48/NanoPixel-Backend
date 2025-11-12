package robin.discordbot.listener;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MemberListener extends ListenerAdapter {
    private String globalName = "";

    // 成员加入事件
    /**
     * Event fires when a new member joins a guild
     * Warning: Will not work without "Guild Members" gateway intent!
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // WILL NOT WORK WITHOUT GATEWAY INTENT!
        String avatar = event.getUser().getEffectiveAvatarUrl();
        System.out.println(avatar);
    }

    /**
     * Event fires when a user updates their online status
     * Requires "Guild Presences" gateway intent AND cache enabled!
     */
    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        int onlineMembers = 0;
        for (Member member : event.getGuild().getMembers()) {
            if (member.getOnlineStatus() == OnlineStatus.ONLINE) {
                onlineMembers++;
            }
        }
        // 使用cache
        User user = event.getJDA().getUserById("1121767044715651122");
        Member member = event.getGuild().getMemberById("1121767044715651122");

        // 不需要cache，使用rest action
        event.getJDA().retrieveUserById("1121767044715651122").queue(new Consumer<User>() {
            @Override
            public void accept(User user) {
                globalName = user.getGlobalName();
            }
        });

        String message = event.getUser().getAsTag() + "updated their online status! There are " + onlineMembers
                + " members online now!";
        // TextChannel textChannel = event.getGuild().getTextChannelsByName("常规",
        // true).stream().findFirst().orElse(null);
        event.getGuild().getTextChannelsByName("常规", true).stream().findFirst().orElse(null).sendMessage(message)
                .queue();
    }
}
