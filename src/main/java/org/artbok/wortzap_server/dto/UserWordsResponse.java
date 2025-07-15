package org.artbok.wortzap_server.dto;
import lombok.Getter;
import org.artbok.wortzap_server.model.Word;

import java.util.List;


@Getter
public class UserWordsResponse {
    private final List<String> studiedLanguages;
    private final List<Word> words;

    public UserWordsResponse(List<String> studiedLanguages, List<Word> words) {
        this.studiedLanguages = studiedLanguages;
        this.words = words;
    }

}