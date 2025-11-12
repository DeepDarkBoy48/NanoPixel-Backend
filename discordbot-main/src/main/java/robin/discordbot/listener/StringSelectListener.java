package robin.discordbot.listener;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

@Component
public class StringSelectListener extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("menu:main")) {
            String selected = event.getValues().get(0);
            
            switch (selected) {
                case "option1":
                    event.reply("您选择了选项1").setEphemeral(true).queue();
                    break;
                case "option2":
                    event.reply("您选择了选项2").setEphemeral(true).queue();
                    break;
                case "option3":
                    event.reply("您选择了选项3").setEphemeral(true).queue();
                    break;
            }
        }
    }

    // 创建下拉菜单的示例方法
    public StringSelectMenu createSelectMenu() {
        return StringSelectMenu.create("menu:main")
                .setPlaceholder("请选择一个选项") // 占位符文本
                .addOption("选项1", "option1", "这是第一个选项的描述")
                .addOption("选项2", "option2", "这是第二个选项的描述")
                .addOption("选项3", "option3", "这是第三个选项的描述")
                .setMinValues(1)
                .setMaxValues(1)
                .build();
    }
} 