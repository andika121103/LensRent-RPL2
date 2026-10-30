package com.lensrent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Satu baris = satu jenis alat; stok = jumlah unit jenis itu. */
@Entity
@Table(name = "alat")
public class Alat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nama;

    // columnDefinition agar tersimpan sebagai VARCHAR biasa, bukan tipe ENUM MySQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Kategori kategori;

    @Column(nullable = false, length = 50)
    private String merek;

    /** Dalam Rupiah, tanpa desimal. */
    @Column(nullable = false)
    private long hargaPerHari;

    @Column(nullable = false)
    private int stok;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private StatusAlat status;

    private String keterangan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void sebelumDisimpan() {
        createdAt = LocalDateTime.now();
    }

    /** Tampil "Tersedia" di katalog hanya jika statusnya tersedia dan stok masih ada. */
    public boolean isBisaDisewa() {
        return status == StatusAlat.TERSEDIA && stok > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public Kategori getKategori() { return kategori; }
    public void setKategori(Kategori kategori) { this.kategori = kategori; }

    public String getMerek() { return merek; }
    public void setMerek(String merek) { this.merek = merek; }

    public long getHargaPerHari() { return hargaPerHari; }
    public void setHargaPerHari(long hargaPerHari) { this.hargaPerHari = hargaPerHari; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public StatusAlat getStatus() { return status; }
    public void setStatus(StatusAlat status) { this.status = status; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
