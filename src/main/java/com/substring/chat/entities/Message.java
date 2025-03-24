package com.substring.chat.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    private String sender;
    private String content;
    private LocalDateTime timeStamp;
    private String fileUrl; // New field for file URL
    private String fileType; // New field for file type (e.g., "image", "video", "audio", "document")

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timeStamp = LocalDateTime.now();
    }

    // New constructor to handle file messages
    public Message(String sender, String fileUrl, String fileType) {
        this.sender = sender;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.timeStamp = LocalDateTime.now();
    }
}