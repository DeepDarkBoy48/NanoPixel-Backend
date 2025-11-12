package robin.discordbot.listener;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;

@Component
public class ModalListener extends ListenerAdapter {
    
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("show_modal")) {
            // 创建文本输入组件
            TextInput name = TextInput.create("name", "名字", TextInputStyle.SHORT)
                    .setPlaceholder("请输入您的名字")
                    .setMinLength(1)
                    .setMaxLength(100)
                    .setRequired(true)
                    .build();

            TextInput description = TextInput.create("description", "描述", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("请输入描述")
                    .setMinLength(10)
                    .setMaxLength(1000)
                    .build();

            // 创建模态框
            Modal modal = Modal.create("example_modal", "示例模态框")
                    .addComponents(ActionRow.of(name), ActionRow.of(description))
                    .build();

            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("example_modal")) {
            String name = event.getValue("name").getAsString();
            String description = event.getValue("description").getAsString();
            
            event.reply("收到您的提交!\n名字: " + name + "\n描述: " + description)
                    .setEphemeral(true)
                    .queue();
        }
    }
}