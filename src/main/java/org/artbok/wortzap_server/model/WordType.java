package org.artbok.wortzap_server.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wordTypes")
@Getter
@Setter
@Builder
public class WordType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String word;

    @Column
    public String wordLanguage;

    @Column
    public String article;

    @Column
    public String plural;

    @Column
    public String translation;

    @Column
    public String translationLanguage;

    @Column
    public String creator;

}
