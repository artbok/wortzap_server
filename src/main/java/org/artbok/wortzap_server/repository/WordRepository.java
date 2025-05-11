package org.artbok.wortzap_server.repository;

import org.artbok.wortzap_server.model.Word;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends CrudRepository<Word, Long> {
    //    Optional<User> findByCode(String email);
}
