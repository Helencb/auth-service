package helen.com.authservice.service;

import helen.com.authservice.dto.response.SessionResponse;
import helen.com.authservice.entity.Device;
import helen.com.authservice.entity.User;
import helen.com.authservice.entity.UserSession;
import helen.com.authservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public void createSession(
            User user,
            Device device,
            String accessToken,
            String refreshToken) {

        UserSession session = UserSession.builder()
                .user(user)
                .device(device)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(device.getIpAddress())
                .userAgent(device.getBrowser())
                .active(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        sessionRepository.save(session);
    }

    public List<SessionResponse> getActiveSessions(User user) {
        return sessionRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(session -> SessionResponse.builder()
                        .sessionId(session.getId())
                        .deviceName(session.getDevice().getDeviceName())
                        .browser(session.getDevice().getBrowser())
                        .operatingSystem(session.getDevice().getOperatingSystem())
                        .ipAddress(session.getIpAddress())
                        .active(session.getActive())
                        .createdAt(session.getCreatedAt())
                        .build())
                .toList();
    }

    public void revokeSession(String sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setActive(false);
        sessionRepository.save(session);
    }

    public void revokeRefreshToken(String refreshToken) {
        UserSession session = sessionRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setActive(false);
        sessionRepository.save(session);
    }
}
