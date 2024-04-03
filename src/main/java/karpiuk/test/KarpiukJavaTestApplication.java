package karpiuk.test;

import karpiuk.test.model.Role;
import karpiuk.test.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class KarpiukJavaTestApplication {
    private final RoleRepository roleRepository;

    public static void main(String[] args) {
        SpringApplication.run(KarpiukJavaTestApplication.class, args);
    }

    @Bean
   public CommandLineRunner commandLineRunner() {
        return args -> {
            roleRepository.save(new Role(Role.RoleName.ROLE_USER));
        };
    }
}
