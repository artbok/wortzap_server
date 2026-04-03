package org.artbok.wortzap_server.dto;
import lombok.Getter;
import org.artbok.wortzap_server.model.Word;

import java.util.List;


@Getter
public class WordsForLearningResponse {
    private final List<Word> words;

    public WordsForLearningResponse(List<Word> words) {
        this.words = words;
    }

}