package com.lensrent.service;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.model.StatusAlat;
import com.lensrent.repository.AlatRepository;
import com.lensrent.repository.DetailTransaksiRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika data alat: dipakai halaman Data Alat (admin).
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class AlatService {

    private static final String POLA_ANGKA = "\\d{1,9}";
    private static final long UKURAN_FOTO_MAKS = 5L * 1024 * 1024; // 5 MB

    /** Folder penyimpanan foto, di samping aplikasi. Diganti folder sementara oleh unit test. */
    Path folderFoto = Path.of("foto-alat");

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

    /**
     * id null = tambah alat baru, selain itu = ubah alat yang sudah ada.
     * fotoBaru null = foto tidak diganti; selain itu file disalin ke folder foto-alat.
     */
    @Transactional
    public Alat simpan(Long id, String nama, Kategori kategori, String merek, String hargaPerHari,
                       String stok, StatusAlat status, String keterangan, Path fotoBaru) {
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
        String ekstensi = fotoBaru == null ? null : ekstensiFoto(fotoBaru);

        Alat alat = id == null ? new Alat() : alatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alat tidak ditemukan, mungkin sudah dihapus."));
        alat.setNama(nama.trim());
        alat.setKategori(kategori);
        alat.setMerek(merek.trim());
        alat.setHargaPerHari(Long.parseLong(harga));
        alat.setStok(Integer.parseInt(stok.trim()));
        alat.setStatus(status);
        alat.setKeterangan(kosong(keterangan) ? null : keterangan.trim());
        if (fotoBaru != null) {
            // shortcut: foto lama tidak dihapus dari folder; tambah pembersihan jika folder membesar
            alat.setFoto(salinFoto(fotoBaru, ekstensi));
        }
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

    /** Lokasi file foto alat, atau null jika alat belum punya foto. */
    public Path fileFoto(Alat alat) {
        return alat.getFoto() == null ? null : folderFoto.resolve(alat.getFoto());
    }

    private String ekstensiFoto(Path file) {
        String nama = file.getFileName().toString().toLowerCase(Locale.ROOT);
        String ekstensi = nama.substring(nama.lastIndexOf('.') + 1);
        if (!nama.contains(".") || !List.of("jpg", "jpeg", "png").contains(ekstensi)) {
            throw new IllegalArgumentException("Foto harus file JPG atau PNG.");
        }
        try {
            if (!Files.isRegularFile(file) || Files.size(file) > UKURAN_FOTO_MAKS) {
                throw new IllegalArgumentException("File foto tidak ditemukan atau lebih dari 5 MB.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("File foto tidak bisa dibaca.");
        }
        return ekstensi;
    }

    private String salinFoto(Path sumber, String ekstensi) {
        String namaFile = "alat-" + UUID.randomUUID() + "." + ekstensi; // nama acak agar tidak menimpa foto lain
        try {
            Files.createDirectories(folderFoto);
            Files.copy(sumber, folderFoto.resolve(namaFile));
        } catch (IOException e) {
            throw new UncheckedIOException("Foto gagal disalin ke folder " + folderFoto.toAbsolutePath(), e);
        }
        return namaFile;
    }

    private boolean kosong(String teks) {
        return teks == null || teks.isBlank();
    }
}
