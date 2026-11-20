package com.lensrent.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pembayaran di toko (tunai atau transfer), langsung lunas sebesar sisa tagihan. */
@Entity
@Table(name = "pembayaran")
public class Pembayaran {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaksi_id")
    private Transaksi transaksi;

    /** Dalam Rupiah, tanpa desimal. */
    @Column(nullable = false)
    private long jumlah;

    @Column(nullable = false)
    private LocalDate tanggal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodeBayar metode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void sebelumDisimpan() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }

    public long getJumlah() { return jumlah; }
    public void setJumlah(long jumlah) { this.jumlah = jumlah; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public MetodeBayar getMetode() { return metode; }
    public void setMetode(MetodeBayar metode) { this.metode = metode; }


    public LocalDateTime getCreatedAt() { return createdAt; }
}
