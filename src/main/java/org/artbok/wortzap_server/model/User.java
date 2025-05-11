package org.artbok.wortzap_server.model;
import jakarta.persistence.*;


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
    public Boolean verified = false;

    @Column
    public String nativeLanguage;

    public User() {}
    public User(String email, String password, String nativeLanguage) {
        this.email = email;
        this.password = password;
        this.nativeLanguage = nativeLanguage;
    }
}
