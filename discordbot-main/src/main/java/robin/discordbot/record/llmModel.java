package robin.discordbot.record;

public enum llmModel {
    GEMINI_2_5_PRO("gemini-2.5-pro-exp-03-25"),
    GEMINI_FLASH_THINKING("gemini-2.0-flash-thinking-exp-01-21"),
    GEMINI_FLASH_LITE("gemini-2.0-flash-lite-preview-02-05"),
    GEMINI_FLASH("gemini-2.0-flash"),
    GEMINI_PRO("gemini-2.0-pro-exp-02-05"),
    GEMINI_1206("gemini-exp-1206"),
    GEMINI_1_5("gemini-1.5-flash"),
    CHATGPT_4o_MINI("gpt-4o-mini");

    public String modle;
    llmModel(String s) {
        this.modle = s;
    }
    public String getModle() {
        return modle;
    }
}
