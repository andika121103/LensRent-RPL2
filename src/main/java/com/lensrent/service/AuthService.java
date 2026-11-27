package com.lensrent.service;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.repository.PenggunaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Logika login. Hanya admin yang bisa masuk; penyewa dicatat admin di toko tanpa akun.
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class AuthService {

    private final PenggunaRepository penggunaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(PenggunaRepository penggunaRepository) {
        this.penggunaRepository = penggunaRepository;
    }

    public Pengguna login(String username, String password) {
        if (kosong(username) || kosong(password)) {
            throw new IllegalArgumentException("Username dan password wajib diisi.");
        }

        Pengguna pengguna = penggunaRepository.findByUsername(username.trim()).orElse(null);
        // Pesan sengaja disamakan agar tidak membocorkan username mana yang terdaftar
        if (pengguna == null || !passwordEncoder.matches(password, pengguna.getPassword())) {
            throw new IllegalArgumentException("Username atau password salah.");
        }
        if (!pengguna.isAktif()) {
            throw new IllegalArgumentException("Akun ini sudah dinonaktifkan. Hubungi admin toko.");
        }
        if (pengguna.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Aplikasi ini hanya untuk admin toko.");
        }
        return pengguna;
    }

    private boolean kosong(String teks) {
        return teks == null || teks.isBlank();
    }
}
