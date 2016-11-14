package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import server.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

   // List<User> findByLogin(String login);
}