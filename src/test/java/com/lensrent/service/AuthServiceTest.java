package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        when(repository.save(any(Pengguna.class))).thenAnswer(i -> i.getArgument(0));
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
    void daftarBerhasilDenganRoleUserDanPasswordTerhash() {
        Pengguna p = authService.daftar("Budi", "081234567890", "budi@mail.com", "Medan", "budi", "rahasia1", "rahasia1");
        assertEquals(Role.USER, p.getRole());
        assertNotEquals("rahasia1", p.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("rahasia1", p.getPassword()));
    }

    @Test
    void daftarUsernameSudahAdaDitolak() {
        when(repository.existsByUsername("admin1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> authService.daftar("Budi", "081234567890", "budi@mail.com", "Medan", "admin1", "rahasia1", "rahasia1"));
        verify(repository, never()).save(any());
    }

    @Test
    void daftarInputTidakValidDitolak() {
        // No. HP huruf, No. HP terlalu pendek, email salah, password < 6, ulangi password beda
        assertThrows(IllegalArgumentException.class, () -> authService.daftar("Budi", "08abc4567890", "budi@mail.com", "Medan", "budi", "rahasia1", "rahasia1"));
        assertThrows(IllegalArgumentException.class, () -> authService.daftar("Budi", "08123", "budi@mail.com", "Medan", "budi", "rahasia1", "rahasia1"));
        assertThrows(IllegalArgumentException.class, () -> authService.daftar("Budi", "081234567890", "budi@mail", "Medan", "budi", "rahasia1", "rahasia1"));
        assertThrows(IllegalArgumentException.class, () -> authService.daftar("Budi", "081234567890", "budi@mail.com", "Medan", "budi", "12345", "12345"));
        assertThrows(IllegalArgumentException.class, () -> authService.daftar("Budi", "081234567890", "budi@mail.com", "Medan", "budi", "rahasia1", "rahasia2"));
        verify(repository, never()).save(any());
    }
}
