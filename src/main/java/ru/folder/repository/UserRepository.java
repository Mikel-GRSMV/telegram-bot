package ru.folder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.folder.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
}
