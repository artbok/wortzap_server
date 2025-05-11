package org.artbok.translator.repository;

import org.artbok.translator.model.TempCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;


@Repository
public interface TempCodeRepository extends JpaRepository<TempCode, Long> {

    Optional<TempCode> findByEmailAndCodeAndRequestDateAfter(
            String email,
            String code,
            OffsetDateTime afterDate
    );

}
