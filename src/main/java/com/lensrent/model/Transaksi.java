package com.lensrent.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Satu kali sewa oleh satu penyewa; alat yang disewa ada di DetailTransaksi. */
@Entity
@Table(name = "transaksi")
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "penyewa_id")
    private Pengguna penyewa;

    @Column(nullable = false)
    private LocalDate tanggalSewa;

    @Column(nullable = false)
    private LocalDate tanggalKembali;

    /** Dalam Rupiah, tanpa desimal. */
    @Column(nullable = false)
    private long total;

    @OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL)
    private List<DetailTransaksi> detail = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void sebelumDisimpan() {
        createdAt = LocalDateTime.now();
    }

    public long getLamaHari() {
        return ChronoUnit.DAYS.between(tanggalSewa, tanggalKembali);
    }

    public Long getId() { return id; }

    public Pengguna getPenyewa() { return penyewa; }
    public void setPenyewa(Pengguna penyewa) { this.penyewa = penyewa; }

    public LocalDate getTanggalSewa() { return tanggalSewa; }
    public void setTanggalSewa(LocalDate tanggalSewa) { this.tanggalSewa = tanggalSewa; }

    public LocalDate getTanggalKembali() { return tanggalKembali; }
    public void setTanggalKembali(LocalDate tanggalKembali) { this.tanggalKembali = tanggalKembali; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public List<DetailTransaksi> getDetail() { return detail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
