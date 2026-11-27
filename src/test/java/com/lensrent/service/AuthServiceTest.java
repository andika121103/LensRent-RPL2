package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.repository.PenggunaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Unit test AuthService tanpa database: repository diganti mock (tiruan). */
class AuthServiceTest {

    private PenggunaRepository repository;
    private AuthService authService;

    @BeforeEach
    void siapkan() {
        repository = mock(PenggunaRepository.class);
        authService = new AuthService(repository);
    }

    private Pengguna akun(String username, String password, Role role, boolean aktif) {
        Pengguna p = new Pengguna();
        p.setUsername(username);
        p.setPassword(new BCryptPasswordEncoder().encode(password));
        p.setRole(role);
        p.setAktif(aktif);
        return p;
    }

    @Test
    void loginAdminBerhasil() {
        when(repository.findByUsername("admin1")).thenReturn(Optional.of(akun("admin1", "admin123", Role.ADMIN, true)));
        assertEquals(Role.ADMIN, authService.login("admin1", "admin123").getRole());
    }

    @Test
    void loginPasswordSalahDitolak() {
        when(repository.findByUsername("admin1")).thenReturn(Optional.of(akun("admin1", "admin123", Role.ADMIN, true)));
        var ex = assertThrows(IllegalArgumentException.class, () -> authService.login("admin1", "salah"));
        assertEquals("Username atau password salah.", ex.getMessage());
    }

    @Test
    void loginAkunNonaktifDitolak() {
        when(repository.findByUsername("budi")).thenReturn(Optional.of(akun("budi", "rahasia1", Role.USER, false)));
        assertThrows(IllegalArgumentException.class, () -> authService.login("budi", "rahasia1"));
    }

    @Test
    void loginFieldKosongDitolak() {
        assertThrows(IllegalArgumentException.class, () -> authService.login(" ", ""));
    }

    @Test
    void loginPenyewaDitolak() {
        when(repository.findByUsername("budi")).thenReturn(Optional.of(akun("budi", "rahasia1", Role.USER, true)));
        var ex = assertThrows(IllegalArgumentException.class, () -> authService.login("budi", "rahasia1"));
        assertEquals("Aplikasi ini hanya untuk admin toko.", ex.getMessage());
    }
}
