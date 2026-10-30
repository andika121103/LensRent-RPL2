package com.lensrent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pengguna")
public class Pengguna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Berisi hash BCrypt, bukan password asli. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 13)
    private String noHp;

    @Column(length = 100)
    private String email;

    private String alamat;

    // columnDefinition agar tersimpan sebagai VARCHAR biasa, bukan tipe ENUM MySQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Role role;

    @Column(nullable = false)
    private boolean aktif = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void sebelumDisimpan() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
