package com.lensrent.config;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.repository.PenggunaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Membuat 3 akun admin saat aplikasi jalan. Admin tidak bisa didaftarkan lewat aplikasi.
 * Dicek per username, jadi aman dijalankan berulang kali.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String PASSWORD_AWAL = "admin123";

    private final PenggunaRepository penggunaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(PenggunaRepository penggunaRepository) {
        this.penggunaRepository = penggunaRepository;
    }

    @Override
    public void run(String... args) {
        for (int i = 1; i <= 3; i++) {
            String username = "admin" + i;
            if (penggunaRepository.existsByUsername(username)) {
                continue;
            }
            Pengguna admin = new Pengguna();
            admin.setNama("Admin " + i);
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(PASSWORD_AWAL));
            admin.setRole(Role.ADMIN);
            penggunaRepository.save(admin);
        }
    }
}
