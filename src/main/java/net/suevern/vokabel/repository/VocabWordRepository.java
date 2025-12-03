package net.suevern.vokabel.repository;

import net.suevern.vokabel.entity.VocabWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabWordRepository extends JpaRepository<VocabWord, Long> {
}
