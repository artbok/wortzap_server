package org.artbok.translator.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "temp_codes")
public class TempCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String email;

    @Column
    public String code;

    @Column
    public OffsetDateTime requestDate;

    public TempCode() {}
    public TempCode(String email, String code, OffsetDateTime requestDate) {
        this.email = email;
        this.code = code;
        this.requestDate = requestDate;
    }

}
