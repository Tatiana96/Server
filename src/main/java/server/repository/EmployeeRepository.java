package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import server.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
