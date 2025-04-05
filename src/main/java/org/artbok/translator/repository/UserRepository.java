package org.artbok.translator.repository;

import org.artbok.translator.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
//    Optional<User> findByCode(String code);
    User findByCode(String code);
    List<User> findLikeByCode(String code);
}
