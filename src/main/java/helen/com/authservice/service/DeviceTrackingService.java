package helen.com.authservice.service;

import helen.com.authservice.Util.DeviceUtils;
import helen.com.authservice.entity.Device;
import helen.com.authservice.entity.User;
import helen.com.authservice.repository.DeviceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceTrackingService {

    private final DeviceRepository deviceRepository;

    public Device trackDevice(User user, HttpServletRequest request) {

        String fingerprint =
                DeviceUtils.generateFingerprint(request);

        return deviceRepository.findByFingerprint(fingerprint)
                .orElseGet(() -> {
                    String userAgent = DeviceUtils.extractUserAgent(request);

                    Device device = Device.builder()
                            .fingerprint(fingerprint)
                            .deviceName(userAgent)
                            .browser(userAgent)
                            .operatingSystem("Unknown")
                            .deviceType(DeviceUtils.resolveDeviceType(userAgent))
                            .ipAddress(DeviceUtils.extractIp(request))
                            .lastLoginAt(LocalDateTime.now())
                            .user(user)
                            .build();

                    return deviceRepository.save(device);
                });
    }
}
