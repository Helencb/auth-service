package helen.com.authservice.controller;

import helen.com.authservice.dto.request.VerifyMFARequest;
import helen.com.authservice.dto.response.MFASetupResponse;
import helen.com.authservice.service.MFAService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mfa")
@RequiredArgsConstructor
public class MFAController {
    private final MFAService mfaService;

    @PostMapping("/setup")
    public MFASetupResponse setup(Authentication authentication) {
        return mfaService.setupMFA(authentication.getName());
    }

    @PostMapping("/enable")
    public void enable(Authentication authentication, @RequestBody VerifyMFARequest request) {
        mfaService.enableMFA(
                authentication.getName(),
                request.getCode()
        );
    }

    @PostMapping("/verify")
    public boolean verify(@RequestBody VerifyMFARequest request) {
        return mfaService.verifyCode(
                request.getUsername(),
                request.getCode()
        );
    }
}
