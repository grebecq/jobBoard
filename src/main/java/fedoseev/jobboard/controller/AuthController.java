package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.LoginRequest;
import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.dto.response.LoginResponse;
import fedoseev.jobboard.dto.response.RegisterResponse;
import fedoseev.jobboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и вход")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest request){
        return authService.register(request);
    }

    @Operation(summary = "Текущий пользователь")
    @GetMapping("/me")
    public RegisterResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    @Operation(summary = "Вход, получить JWT")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
