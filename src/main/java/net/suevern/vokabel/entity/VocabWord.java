package net.suevern.vokabel.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "vocab_word")
public class VocabWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String translation;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VocabAlternative> alternatives = new ArrayList<>();

    @Column(nullable = false)
    private int correct = 0;

    @Column(nullable = false)
    private int attempts = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_list_id", nullable = false)
    @JsonIgnore
    private VocabList vocabList;

    public VocabWord() {}

    public VocabWord(String word, String translation) {
        this.word = word;
        this.translation = translation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public List<VocabAlternative> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<VocabAlternative> alternatives) {
        this.alternatives = alternatives;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public VocabList getVocabList() {
        return vocabList;
    }

    public void setVocabList(VocabList vocabList) {
        this.vocabList = vocabList;
    }
}
