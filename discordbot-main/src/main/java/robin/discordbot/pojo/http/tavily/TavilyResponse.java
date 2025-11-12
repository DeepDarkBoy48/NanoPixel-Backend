package robin.discordbot.pojo.http.tavily;

import lombok.Data;

import java.util.List;

@Data
public class TavilyResponse {
    private String query;
    // JSON中的 "follow_up_questions" 会被映射到这里
    private List<String> followUpQuestions;
    private String answer;
    private List<Image> images;
    private List<Result> results;
    // JSON中的 "response_time" 会被映射到这里
    private double responseTime;
    // JSON中的 "request_id" 会被映射到这里
    private String requestId;


    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "query='" + query + '\'' +
                ", answer='" + answer + '\'' +
                ", imagesCount=" + (images != null ? images.size() : 0) +
                ", resultsCount=" + (results != null ? results.size() : 0) +
                ", responseTime=" + responseTime +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}