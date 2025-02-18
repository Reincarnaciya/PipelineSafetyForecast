package gas.pipeline.safety.forecast.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@NoArgsConstructor
@AllArgsConstructor

@Entity
@EnableJpaRepositories
@Table(name = "employees")
@Data
public class Employee {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "email", nullable = false)
    String email;
}
