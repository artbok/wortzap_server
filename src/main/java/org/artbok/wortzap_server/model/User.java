package org.artbok.wortzap_server.model;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String email;

    @Column
    public String password;

    @Column
    public String nativeLanguage;

    @Column
    public String studiedLanguages;

    public User() {}
    public User(String email, String password, String nativeLanguage) {
        this.email = email;
        this.password = password;
        this.nativeLanguage = nativeLanguage;
        this.studiedLanguages = "All";
    }

    public List<String> getStudiedLanguages() {
        return Arrays.stream(this.studiedLanguages.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
