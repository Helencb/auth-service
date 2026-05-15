package helen.com.authservice.Util;

import helen.com.authservice.enums.DeviceType;
import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtils {

    public static String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static String extractIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    public static String generateFingerprint(HttpServletRequest request) {
        String userAgent = extractUserAgent(request);
        String ip = extractIp(request);

        return Integer.toHexString((userAgent + ip).hashCode());
    }

    public static DeviceType resolveDeviceType(String userAgent) {
        if (userAgent == null) {
            return DeviceType.DESKTOP;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile")) {
            return DeviceType.MOBILE;
        }

        if (ua.contains("tablet")) {
            return DeviceType.TABLET;
        }

        return DeviceType.DESKTOP;
    }
}
