package com.hireconnect.interview.resource;

import com.hireconnect.interview.entity.Message;
import com.hireconnect.interview.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageResource {

    private final MessageRepository messageRepository;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        return ResponseEntity.ok(messageRepository.save(message));
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam Long user1, @RequestParam Long user2) {
        return ResponseEntity.ok(messageRepository.findConversation(user1, user2));
    }

    @GetMapping("/contacts/{userId}")
    public ResponseEntity<List<Long>> getContacts(@PathVariable Long userId) {
        List<Long> senders = new java.util.ArrayList<>(messageRepository.findSenderIdsByReceiverId(userId));
        List<Long> receivers = messageRepository.findRecipientIdsBySenderId(userId);
        senders.addAll(receivers);
        return ResponseEntity.ok(senders.stream().distinct().toList());
    }
}
