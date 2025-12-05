package net.suevern.vokabel.api;

import net.suevern.vokabel.entity.VocabList;
import net.suevern.vokabel.entity.VocabWord;
import net.suevern.vokabel.repository.VocabListRepository;
import net.suevern.vokabel.repository.VocabWordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vocab")
public class VocabApiController {

    private final VocabListRepository vocabListRepository;
    private final VocabWordRepository vocabWordRepository;

    public VocabApiController(VocabListRepository vocabListRepository, VocabWordRepository vocabWordRepository) {
        this.vocabListRepository = vocabListRepository;
        this.vocabWordRepository = vocabWordRepository;
    }

    private String getUserEmail(OAuth2AuthenticationToken token) {
        if (token == null || token.getPrincipal() == null) {
            return "anonymous@localhost";
        }
        String email = token.getPrincipal().getAttribute("email");
        return email != null ? email : "anonymous@localhost";
    }

    @GetMapping("/lists")
    public List<VocabList> getLists(OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabListRepository.findByOwnerEmailOrderByUpdatedAtDesc(email);
    }

    @PostMapping("/lists")
    public VocabList createList(@RequestBody Map<String, String> payload, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        String name = payload.getOrDefault("name", "Neue Liste");
        VocabList list = new VocabList(name, email);
        return vocabListRepository.save(list);
    }

    @GetMapping("/lists/{id}")
    public ResponseEntity<VocabList> getList(@PathVariable Long id, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabListRepository.findById(id)
                .filter(list -> list.getOwnerEmail().equals(email))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/lists/{id}")
    public ResponseEntity<VocabList> updateList(@PathVariable Long id, @RequestBody Map<String, String> payload, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabListRepository.findById(id)
                .filter(list -> list.getOwnerEmail().equals(email))
                .map(list -> {
                    if (payload.containsKey("name")) {
                        list.setName(payload.get("name"));
                        list.setUpdatedAt(LocalDateTime.now());
                    }
                    return ResponseEntity.ok(vocabListRepository.save(list));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/lists/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabListRepository.findById(id)
                .filter(list -> list.getOwnerEmail().equals(email))
                .map(list -> {
                    vocabListRepository.delete(list);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lists/{listId}/words")
    public ResponseEntity<VocabWord> addWord(@PathVariable Long listId, @RequestBody Map<String, String> payload, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabListRepository.findById(listId)
                .filter(list -> list.getOwnerEmail().equals(email))
                .map(list -> {
                    VocabWord word = new VocabWord(payload.get("word"), payload.get("translation"));
                    word.setVocabList(list);
                    list.getWords().add(word);
                    list.setUpdatedAt(LocalDateTime.now());
                    vocabListRepository.save(list);
                    return ResponseEntity.ok(word);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/words/{wordId}")
    public ResponseEntity<VocabWord> updateWord(@PathVariable Long wordId, @RequestBody Map<String, Object> payload, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabWordRepository.findById(wordId)
                .filter(word -> word.getVocabList().getOwnerEmail().equals(email))
                .map(word -> {
                    if (payload.containsKey("word")) word.setWord((String) payload.get("word"));
                    if (payload.containsKey("translation")) word.setTranslation((String) payload.get("translation"));
                    if (payload.containsKey("correct")) word.setCorrect((Integer) payload.get("correct"));
                    if (payload.containsKey("attempts")) word.setAttempts((Integer) payload.get("attempts"));
                    // update alternatives if provided as comma-separated string
                    if (payload.containsKey("alternatives")) {
                        String altStr = (String) payload.get("alternatives");
                        word.getAlternatives().clear();
                        if (altStr != null && !altStr.trim().isEmpty()) {
                            String[] parts = altStr.split(",");
                            for (String p : parts) {
                                String t = p.trim();
                                if (!t.isEmpty()) {
                                    var alt = new net.suevern.vokabel.entity.VocabAlternative(t);
                                    alt.setWord(word);
                                    word.getAlternatives().add(alt);
                                }
                            }
                        }
                    }
                    word.getVocabList().setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(vocabWordRepository.save(word));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/words/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long wordId, OAuth2AuthenticationToken token) {
        String email = getUserEmail(token);
        return vocabWordRepository.findById(wordId)
                .filter(word -> word.getVocabList().getOwnerEmail().equals(email))
                .map(word -> {
                    word.getVocabList().setUpdatedAt(LocalDateTime.now());
                    vocabWordRepository.delete(word);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
