// package robin.discordbot2.listener;

// import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
// import net.dv8tion.jda.api.hooks.ListenerAdapter;
// import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
// import org.springframework.stereotype.Component;

// @Component
// public class EntitySelectListener extends ListenerAdapter {

//     @Override
//     public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
//         if (event.getComponentId().equals("user_select")) {
//             // 处理用户选择
//             event.reply("您选择了用户: " + 
//                     event.getValues().stream()
//                             .map(mentionable -> ((UserSelectInteraction)mentionable).getAsMention())
//                             .reduce("", (a, b) -> a + ", " + b))
//                     .setEphemeral(true)
//                     .queue();
//         } else if (event.getComponentId().equals("channel_select")) {
//             // 处理频道选择
//             event.reply("您选择了频道: " + 
//                     event.getValues().stream()
//                             .map(channel -> channel.getAsChannel().getAsMention())
//                             .reduce("", (a, b) -> a + ", " + b))
//                     .setEphemeral(true)
//                     .queue();
//         }
//     }

//     // 创建用户选择菜单的示例方法
//     public EntitySelectMenu createUserSelect() {
//         return EntitySelectMenu.create("user_select", EntitySelectMenu.SelectTarget.USER)
//                 .setPlaceholder("选择用户")
//                 .setMinValues(1)
//                 .setMaxValues(3)
//                 .build();
//     }

//     // 创建频道选择菜单的示例方法
//     public EntitySelectMenu createChannelSelect() {
//         return EntitySelectMenu.create("channel_select", EntitySelectMenu.SelectTarget.CHANNEL)
//                 .setPlaceholder("选择频道")
//                 .setMinValues(1)
//                 .setMaxValues(3)
//                 .build();
//     }
// } 