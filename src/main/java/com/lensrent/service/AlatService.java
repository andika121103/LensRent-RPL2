package com.lensrent.service;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.model.StatusAlat;
import com.lensrent.repository.AlatRepository;
import com.lensrent.repository.DetailTransaksiRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika data alat: dipakai halaman Data Alat (admin) dan Katalog (penyewa).
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class AlatService {

    private static final String POLA_ANGKA = "\\d{1,9}";

    private final AlatRepository alatRepository;
    private final DetailTransaksiRepository detailTransaksiRepository;

    public AlatService(AlatRepository alatRepository, DetailTransaksiRepository detailTransaksiRepository) {
        this.alatRepository = alatRepository;
        this.detailTransaksiRepository = detailTransaksiRepository;
    }

    public List<Alat> semua() {
        return alatRepository.findAllByOrderByNamaAsc();
    }

    /** Cari di nama atau merek (huruf besar/kecil sama saja). kategori null = semua kategori. */
    public List<Alat> cari(String kata, Kategori kategori) {
        String dicari = kata == null ? "" : kata.trim().toLowerCase();
        // shortcut: disaring di Java, bukan query DB; cukup untuk ratusan alat, pindah ke query jika data ribuan
        return semua().stream()
                .filter(a -> kategori == null || a.getKategori() == kategori)
                .filter(a -> a.getNama().toLowerCase().contains(dicari)
                        || a.getMerek().toLowerCase().contains(dicari))
                .toList();
    }

    /** id null = tambah alat baru, selain itu = ubah alat yang sudah ada. */
    @Transactional
    public Alat simpan(Long id, String nama, Kategori kategori, String merek, String hargaPerHari,
                       String stok, StatusAlat status, String keterangan) {
        if (kosong(nama) || kategori == null || kosong(merek) || kosong(hargaPerHari)
                || kosong(stok) || status == null) {
            throw new IllegalArgumentException("Nama, kategori, merek, harga per hari, stok, dan status wajib diisi.");
        }
        if (nama.trim().length() > 100 || merek.trim().length() > 50
                || (keterangan != null && keterangan.trim().length() > 255)) {
            throw new IllegalArgumentException("Nama maksimal 100 karakter, merek 50, keterangan 255.");
        }
        // Titik pemisah ribuan boleh diketik: "150.000" dibaca 150000
        String harga = hargaPerHari.trim().replace(".", "");
        if (!harga.matches(POLA_ANGKA) || Long.parseLong(harga) == 0) {
            throw new IllegalArgumentException("Harga per hari harus angka lebih dari 0, contoh 150000.");
        }
        if (!stok.trim().matches(POLA_ANGKA)) {
            throw new IllegalArgumentException("Stok harus angka 0 atau lebih.");
        }

        Alat alat = id == null ? new Alat() : alatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alat tidak ditemukan, mungkin sudah dihapus."));
        alat.setNama(nama.trim());
        alat.setKategori(kategori);
        alat.setMerek(merek.trim());
        alat.setHargaPerHari(Long.parseLong(harga));
        alat.setStok(Integer.parseInt(stok.trim()));
        alat.setStatus(status);
        alat.setKeterangan(kosong(keterangan) ? null : keterangan.trim());
        return alatRepository.save(alat);
    }

    /** Alat yang pernah disewa tidak boleh dihapus agar riwayat transaksi tetap utuh. */
    @Transactional
    public void hapus(Long id) {
        if (detailTransaksiRepository.existsByAlatId(id)) {
            throw new IllegalArgumentException("Alat ini pernah disewa sehingga tidak bisa dihapus. "
                    + "Ubah statusnya menjadi Tidak Tersedia.");
        }
        alatRepository.deleteById(id);
    }

    private boolean kosong(String teks) {
        return teks == null || teks.isBlank();
    }
}
