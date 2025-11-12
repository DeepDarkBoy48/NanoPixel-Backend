package robin.discordbot.pojo.entity;

import java.time.LocalDateTime;

public class ChatMessage {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String messageContent;
    private String roomId;
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long senderId, String senderNickname, String messageContent, String roomId) {
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.messageContent = messageContent;
        this.roomId = roomId;
    }


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
