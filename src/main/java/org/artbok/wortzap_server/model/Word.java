package org.artbok.wortzap_server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public Long owner;

    @Column
    public String word;

    @Column
    public String translation;
}
