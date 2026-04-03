package org.artbok.wortzap_server.service;


import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.model.Word;
import org.artbok.wortzap_server.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordService {
    private final WordRepository wordRepository;

    public WordService(
            WordRepository wordRepository
    ) {
        this.wordRepository = wordRepository;
    }


    public List<Word> getWordsByOwnerAndLanguage(Long ownerId, String wordLanguage) {
        return wordRepository.findByOwnerIdAndWordLanguageOrderByIdAsc(ownerId, wordLanguage);
    }

    public List<Word> getWordsByOwner(Long ownerId) {
        return wordRepository.findByOwnerId(ownerId);
    }

    public List<Word> getByOwnerIdOrderByIdDesc(Long ownerId) {
        return wordRepository.findByOwnerIdOrderByIdAsc(ownerId);
    }
}

