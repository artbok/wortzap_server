package org.artbok.translator.model;
import jakarta.persistence.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String email;

    @Column
    public String tempCode;

    @Column

    public OffsetDateTime requestDate;

    @Column
    public String password;

    public User() {}
    public User(String email) {
        this.email = email;
    }
}
