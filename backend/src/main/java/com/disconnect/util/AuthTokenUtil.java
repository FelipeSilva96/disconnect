package com.disconnect.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AuthTokenUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TOKEN_TTL_SECONDS = 60L * 60L * 24L * 7L;
    private static final String SECRET = AppConfig.get("app.auth.secret", "disconnect-dev-secret");

    private AuthTokenUtil() {
    }

    public static String gerarToken(Integer usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido para autenticação.");
        }

        long expiraEm = Instant.now().plusSeconds(TOKEN_TTL_SECONDS).getEpochSecond();
        String payload = usuarioId + ":" + expiraEm;
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        return payloadBase64 + "." + assinar(payloadBase64);
    }

    public static Integer validarCabecalhoAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Token de autenticação ausente.");
        }

        return validarToken(authorizationHeader.substring("Bearer ".length()).trim());
    }

    private static Integer validarToken(String token) {
        String[] partes = token.split("\\.", -1);
        if (partes.length != 2 || partes[0].isBlank() || partes[1].isBlank()) {
            throw new SecurityException("Token de autenticação inválido.");
        }

        String assinaturaEsperada = assinar(partes[0]);
        if (!MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                partes[1].getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Token de autenticação inválido.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
        String[] dados = payload.split(":", -1);
        if (dados.length != 2) {
            throw new SecurityException("Token de autenticação inválido.");
        }

        long expiraEm;
        int usuarioId;
        try {
            usuarioId = Integer.parseInt(dados[0]);
            expiraEm = Long.parseLong(dados[1]);
        } catch (NumberFormatException e) {
            throw new SecurityException("Token de autenticação inválido.");
        }

        if (Instant.now().getEpochSecond() > expiraEm) {
            throw new SecurityException("Token de autenticação expirado.");
        }

        return usuarioId;
    }

    private static String assinar(String valor) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token de autenticação.", e);
        }
    }
}