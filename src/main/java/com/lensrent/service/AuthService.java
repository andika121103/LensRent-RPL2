package com.lensrent.service;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.repository.PenggunaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika login dan pendaftaran akun.
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class AuthService {

    private static final String POLA_NO_HP = "\\d{10,13}";
    private static final String POLA_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

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
        return pengguna;
    }

    /** Pendaftaran khusus penyewa: role selalu USER. */
    @Transactional
    public Pengguna daftar(String nama, String noHp, String email, String alamat,
                           String username, String password, String ulangiPassword) {
        if (kosong(nama) || kosong(noHp) || kosong(email) || kosong(alamat)
                || kosong(username) || kosong(password) || kosong(ulangiPassword)) {
            throw new IllegalArgumentException("Semua kolom wajib diisi.");
        }
        if (!noHp.trim().matches(POLA_NO_HP)) {
            throw new IllegalArgumentException("No. HP hanya boleh angka, 10 sampai 13 digit.");
        }
        if (!email.trim().matches(POLA_EMAIL)) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }
        if (!password.equals(ulangiPassword)) {
            throw new IllegalArgumentException("Ulangi password tidak sama.");
        }
        if (penggunaRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username sudah dipakai, pilih username lain.");
        }

        Pengguna pengguna = new Pengguna();
        pengguna.setNama(nama.trim());
        pengguna.setNoHp(noHp.trim());
        pengguna.setEmail(email.trim());
        pengguna.setAlamat(alamat.trim());
        pengguna.setUsername(username.trim());
        pengguna.setPassword(passwordEncoder.encode(password));
        pengguna.setRole(Role.USER);
        return penggunaRepository.save(pengguna);
    }

    private boolean kosong(String teks) {
        return teks == null || teks.isBlank();
    }
}
