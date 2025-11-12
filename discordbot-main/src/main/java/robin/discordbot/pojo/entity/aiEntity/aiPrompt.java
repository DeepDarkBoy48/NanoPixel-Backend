package robin.discordbot.pojo.entity.aiEntity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class aiPrompt {
    Integer id;
    String prompt;
    String creator;
    LocalDateTime creationTime;
    String modelName;
    String category;
    int isEnable;
    int thinkBudget;



    public aiPrompt() {
    }

    public aiPrompt(Integer id, String prompt, String creator, LocalDateTime createTime, String modelName, String category, int isEnable) {
        this.id = id;
        this.prompt = prompt;
        this.creator = creator;
        this.creationTime = createTime;
        this.modelName = modelName;
        this.category = category;
        this.isEnable = isEnable;
    }

    public aiPrompt( String prompt, String creator, LocalDateTime createTime , String modelName, String category, int isEnable) {
        this.prompt = prompt;
        this.creator = creator;
        this.creationTime = createTime;
        this.category = category;
        this.isEnable = isEnable;
        this.modelName = modelName;
    }

    public aiPrompt(  String creator, LocalDateTime createTime ,  String category, int isEnable) {
        this.creator = creator;
        this.creationTime = createTime;
        this.category = category;
        this.isEnable = isEnable;
    }

    public aiPrompt( String prompt, String creator, LocalDateTime createTime , String category, int isEnable) {
        this.prompt = prompt;
        this.creator = creator;
        this.creationTime = createTime;
        this.category = category;
        this.isEnable = isEnable;
    }

    public aiPrompt(Integer id, String creator, LocalDateTime createTime, String modelName) {
        this.id = id;
        this.creator = creator;
        this.creationTime = createTime;
        this.modelName = modelName;
    }
}

