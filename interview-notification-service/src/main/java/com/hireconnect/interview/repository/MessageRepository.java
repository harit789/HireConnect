package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);

    @Query("SELECT m FROM Message m WHERE (m.senderId = :id1 AND m.receiverId = :id2) OR (m.senderId = :id2 AND m.receiverId = :id1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("id1") Long id1, @Param("id2") Long id2);

    @Query("SELECT DISTINCT m.receiverId FROM Message m WHERE m.senderId = :senderId")
    List<Long> findRecipientIdsBySenderId(@Param("senderId") Long senderId);

    @Query("SELECT DISTINCT m.senderId FROM Message m WHERE m.receiverId = :receiverId")
    List<Long> findSenderIdsByReceiverId(@Param("receiverId") Long receiverId);
}
