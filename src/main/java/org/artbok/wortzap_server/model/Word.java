package org.artbok.wortzap_server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public Long ownerId;

    @Column
    public String wordLanguage;

    @Column
    public String wordArticle;

    @Column
    public String word;

    @Column
    public String wordPlural;

    @Column
    public String translationLanguage;

    @Column
    public String translationArticle;

    @Column
    public String translation;

    @Column
    public String translationPlural;

    public Word() {}
    public Word(Long ownerId, String wordLanguage, String wordArticle, String word, String wordPlural, String translationLanguage, String translationArticle, String translation, String translationPlural) {
        this.ownerId = ownerId;
        this.wordLanguage = wordLanguage;
        this.wordArticle = wordArticle;
        this.word = word;
        this.wordPlural = wordPlural;
        this.translationLanguage = translationLanguage;
        this.translationArticle = translationArticle;
        this.translation = translation;
        this.translationPlural = translationPlural;
    }
}
