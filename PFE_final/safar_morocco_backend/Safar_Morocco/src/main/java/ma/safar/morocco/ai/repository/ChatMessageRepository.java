package ma.safar.morocco.ai.repository;

import ma.safar.morocco.ai.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}