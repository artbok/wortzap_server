package org.artbok.translator.repository;

import org.artbok.translator.model.User;
import org.artbok.translator.model.Word;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends CrudRepository<Word, Long> {
    //    Optional<User> findByCode(String code);
}
