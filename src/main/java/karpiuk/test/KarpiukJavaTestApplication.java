package karpiuk.test;

import java.util.Set;
import karpiuk.test.model.Role;
import karpiuk.test.model.User;
import karpiuk.test.repository.RoleRepository;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class KarpiukJavaTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KarpiukJavaTestApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(RoleRepository roleRepository,
                                               UserRepository userRepository,
                                               PasswordEncoder encoder) {
        User user = new User(true);
        user.setPassword(encoder.encode("password"));
        user.setEmail("alexkarpiuk99@gmail.com");
        user.setFirstName("Bob");
        user.setLastName("Bobson");
        return args -> {
            Role save = roleRepository.save(new Role(Role.RoleName.ROLE_USER));
            user.setRoles(Set.of(save));

            userRepository.save(user);
        };
    }
}
