package com.lensrent.model;

import jakarta.persistence.*;

/** Satu jenis alat dalam sebuah transaksi. */
@Entity
@Table(name = "detail_transaksi")
public class DetailTransaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaksi_id")
    private Transaksi transaksi;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alat_id")
    private Alat alat;

    @Column(nullable = false)
    private int jumlah;

    /** Harga saat disewa, agar transaksi lama tidak berubah jika harga alat diubah. */
    @Column(nullable = false)
    private long hargaPerHari;

    public Long getId() { return id; }

    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }

    public Alat getAlat() { return alat; }
    public void setAlat(Alat alat) { this.alat = alat; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }

    public long getHargaPerHari() { return hargaPerHari; }
    public void setHargaPerHari(long hargaPerHari) { this.hargaPerHari = hargaPerHari; }
}
