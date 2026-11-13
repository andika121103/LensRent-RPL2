package com.lensrent.model;

/** Tidak disimpan di database: dihitung dari total transaksi dan jumlah yang sudah dibayar. */
public enum StatusBayar {
    BELUM_BAYAR("Belum Bayar"),
    LUNAS("Lunas");

    private final String label;

    StatusBayar(String label) {
        this.label = label;
    }

    /** Teks yang tampil di tabel. */
    @Override
    public String toString() {
        return label;
    }
}
