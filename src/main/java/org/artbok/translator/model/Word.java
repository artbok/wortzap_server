package org.artbok.translator.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "words")
@Getter
@Setter
@Builder
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String owner;

    @Column
    public String word;

    @Column
    public String translation;

}
