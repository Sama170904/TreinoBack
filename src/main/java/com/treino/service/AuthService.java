package com.treino.service;

import com.treino.dto.Create.LoginDTO;
import com.treino.dto.Response.AuthResponseDTO;
import com.treino.entity.Token;
import com.treino.entity.Usuario;
import com.treino.repository.TokenRepository;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        Usuario user = usuarioRepository.findByEmail(loginDTO.getEmail()).orElseThrow();
        
        // Incluir reclamos extra en el JWT payload para decodificación en Frontend
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("rol", user.getRol().name());
        extraClaims.put("roles", List.of("ROLE_" + user.getRol().name(), user.getRol().name()));

        String jwtToken = jwtService.generateToken(extraClaims, user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .build();
    }

    private void saveUserToken(Usuario user, String jwtToken) {
        Token token = Token.builder()
                .usuario(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Usuario user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUsuario(user.getUserId());
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    public void logout(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setExpired(true);
            t.setRevoked(true);
            tokenRepository.save(t);
        });
    }
}
