package ma.safar.morocco.ai.repository;

import ma.safar.morocco.ai.entity.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
    List<UserMemory> findByUserId(Long userId);
}
