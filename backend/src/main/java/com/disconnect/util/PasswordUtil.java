package com.disconnect.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12;

    private PasswordUtil() {
        // Classe utilitária: impede instanciação.
    }

    public static String gerarHash(String senhaEmTextoPuro) {
        if (senhaEmTextoPuro == null || senhaEmTextoPuro.isBlank()) {
            throw new IllegalArgumentException("Senha não pode estar vazia.");
        }

        return BCrypt.hashpw(senhaEmTextoPuro, BCrypt.gensalt(COST));
    }

    public static boolean verificarSenha(String senhaEmTextoPuro, String hashArmazenado) {
        if (senhaEmTextoPuro == null || hashArmazenado == null) {
            return false;
        }

        return BCrypt.checkpw(senhaEmTextoPuro, hashArmazenado);
    }
}
