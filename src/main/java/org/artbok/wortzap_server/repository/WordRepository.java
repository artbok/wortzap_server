package org.artbok.wortzap_server.repository;

import org.artbok.wortzap_server.model.Word;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends CrudRepository<Word, Long> {
    List<Word> findByOwnerIdAndWordLanguageOrderByIdAsc(Long ownerId, String wordLanguage);
    List<Word> findByOwnerIdOrderByIdAsc(Long ownerId);
    List<Word> findByOwnerId(Long ownerId);
}
