package com.substring.chat.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.substring.chat.entities.Message;
import com.substring.chat.entities.Room;
import com.substring.chat.playload.MessageRequest;
import com.substring.chat.repositories.RoomRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin("http://localhost:5173")
@RestController
public class ChatController {

    private final RoomRepository roomRepository;
    private final Cloudinary cloudinary;

    public ChatController(RoomRepository roomRepository, Cloudinary cloudinary) {
        this.roomRepository = roomRepository;
        this.cloudinary = cloudinary;
    }

    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Message sendMessage(
            @DestinationVariable String roomId,
            MessageRequest request
    ) {
        Room room = roomRepository.findByRoomId(request.getRoomId());
        if (room == null) {
            throw new RuntimeException("Room not found!");
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(LocalDateTime.now());
        message.setFileUrl(request.getFileUrl());
        message.setFileType(request.getFileType());

        room.getMessages().add(message);
        roomRepository.save(room);

        return message;
    }

    @PostMapping("/api/v1/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Debug: Log the file details
            System.out.println("Uploading file: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());

            // Upload the file to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "two-three-app",
                    "resource_type", "auto"
            ));

            // Get the file URL from Cloudinary
            String fileUrl = (String) uploadResult.get("secure_url");

            // Determine the file type
            String contentType = file.getContentType();
            String fileType;
            if (contentType != null) {
                System.out.println("Detected content type: " + contentType);
                if (contentType.startsWith("image") && !contentType.equals("image/gif")) {
                    fileType = "image";
                } else if (contentType.startsWith("video")) {
                    fileType = "video";
                } else if (contentType.startsWith("audio")) {
                    fileType = "audio";
                } else if (contentType.equals("image/gif")) {
                    fileType = "gif";
                } else {
                    fileType = "document";
                }
            } else {
                System.out.println("Content type is null, defaulting to document");
                fileType = "document";
            }

            // Return the file URL and type
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("fileType", fileType);
            System.out.println("File uploaded successfully to Cloudinary: " + fileUrl + ", Type: " + fileType);
            return response;

        } catch (IOException e) {
            System.err.println("File upload failed: " + e.getMessage());
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }
}