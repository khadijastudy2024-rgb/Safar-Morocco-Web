package ma.safar.morocco.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.safar.morocco.user.entity.Utilisateur;

@Entity
@Table(name = "user_memory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;

    @Column(name = "memory_key", nullable = false)
    private String memoryKey;

    @Column(name = "memory_value", nullable = false, columnDefinition = "TEXT")
    private String memoryValue;
}
