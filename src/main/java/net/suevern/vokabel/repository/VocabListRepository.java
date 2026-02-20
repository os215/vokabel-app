package net.suevern.vokabel.repository;

import net.suevern.vokabel.entity.VocabList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VocabListRepository extends JpaRepository<VocabList, Long> {
    List<VocabList> findAllByOrderByUpdatedAtDesc();
}
