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

    /** null = alat belum dikembalikan. */
    private LocalDate tanggalDikembalikan;

    /** Dalam Rupiah; null sampai alat dikembalikan. Dibayar tunai saat pengembalian. */
    private Long dendaTelat;

    private Long dendaKerusakan;

    private String keteranganDenda;

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

    /**
     * Hari terakhir alat dipakai: hari dikembalikan, atau jika belum kembali jadwal kembali
     * (hari ini jika sudah terlambat, karena alat masih di penyewa).
     */
    public LocalDate getAkhirPakai(LocalDate hariIni) {
        if (tanggalDikembalikan != null) {
            return tanggalDikembalikan;
        }
        return hariIni.isAfter(tanggalKembali) ? hariIni : tanggalKembali;
    }

    /** Hari terlambat sampai dikembalikan (atau sampai hariIni jika belum kembali); 0 jika tepat waktu. */
    public long getHariTerlambat(LocalDate hariIni) {
        LocalDate akhir = tanggalDikembalikan != null ? tanggalDikembalikan : hariIni;
        return Math.max(0, ChronoUnit.DAYS.between(tanggalKembali, akhir));
    }

    /** Denda telat + kerusakan; 0 jika belum dikembalikan. */
    public long getTotalDenda() {
        return (dendaTelat == null ? 0 : dendaTelat) + (dendaKerusakan == null ? 0 : dendaKerusakan);
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

    public LocalDate getTanggalDikembalikan() { return tanggalDikembalikan; }
    public void setTanggalDikembalikan(LocalDate tanggalDikembalikan) { this.tanggalDikembalikan = tanggalDikembalikan; }

    public Long getDendaTelat() { return dendaTelat; }
    public void setDendaTelat(Long dendaTelat) { this.dendaTelat = dendaTelat; }

    public Long getDendaKerusakan() { return dendaKerusakan; }
    public void setDendaKerusakan(Long dendaKerusakan) { this.dendaKerusakan = dendaKerusakan; }

    public String getKeteranganDenda() { return keteranganDenda; }
    public void setKeteranganDenda(String keteranganDenda) { this.keteranganDenda = keteranganDenda; }

    public List<DetailTransaksi> getDetail() { return detail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
