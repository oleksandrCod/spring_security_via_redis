package karpiuk.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User endpoint management.",
        description = "Provide endpoints for receiving data from db for Admin role users.")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    @Operation(summary = "Get all user endpoint.",
            description = "Provide flow for retrieving all registered users from DB. "
                    + "Requires Admin role for current user, receive request with page specification. "
                    + "If request is valid return users information with status FOUND. "
                    + "If request is not valid return error message with status NOT_FOUND.")
    public ResponseEntity<List<LoggedInUserResponse>> getAllUsers(Pageable pageable) {
        log.info("Received get all users request");
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping-pong endpoint.",
            description = "Provide an attempt to retrieve the message if the application is running correctly. "
                    + "Endpoint not secured.")
    public ResponseEntity<?> ping() {
        log.info("Received ping request!");
        return ResponseEntity.ok("Pong!");
    }

    @GetMapping("/secure-ping")
    @Operation(summary = "Ping-pong endpoint.",
            description = "Provide an attempt to retrieve the message if the application is running correctly. "
                    + "Endpoint secured need authentication.")
    public ResponseEntity<?> securePing() {
        log.info("Received secured ping request!");
        return ResponseEntity.ok("Pong!");
    }
}
