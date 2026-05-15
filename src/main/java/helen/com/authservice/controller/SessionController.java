package helen.com.authservice.controller;

import helen.com.authservice.dto.response.SessionResponse;
import helen.com.authservice.entity.User;
import helen.com.authservice.repository.UserRepository;
import helen.com.authservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @GetMapping
    public List<SessionResponse> sessions(Authentication authentication) {
        User user = userRepository.findByUsername(
                authentication.getName()).orElseThrow();
        return sessionService.getActiveSessions(user);
    }

    @DeleteMapping("/{sessionId}")
    public void revoke(@PathVariable String sessionId) {
        sessionService.revokeSession(sessionId);
    }

}
