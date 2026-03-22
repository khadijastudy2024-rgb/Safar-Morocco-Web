package ma.safar.morocco.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ma.safar.morocco.user.entity.Utilisateur user;

    @Column(name = "langue", length = 5)
    private String langue;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_derniere_activite")
    private LocalDateTime dateDerniereActivite;

    @Column(name = "active")
    private Boolean active;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        dateCreation = LocalDateTime.now();
        dateDerniereActivite = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (langue == null) {
            langue = "fr";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateDerniereActivite = LocalDateTime.now();
    }
}