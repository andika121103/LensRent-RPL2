package com.lensrent.service;

import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Pembayaran;
import com.lensrent.model.StatusBayar;
import com.lensrent.model.StatusPembayaran;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika konfirmasi pembayaran. Status bayar tidak disimpan, tetapi dihitung dari jumlah yang sudah dibayar.
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class PembayaranService {

    private final PembayaranRepository pembayaranRepository;
    private final TransaksiRepository transaksiRepository;
    private final TransaksiService transaksiService;

    public PembayaranService(PembayaranRepository pembayaranRepository, TransaksiRepository transaksiRepository,
                             TransaksiService transaksiService) {
        this.pembayaranRepository = pembayaranRepository;
        this.transaksiRepository = transaksiRepository;
        this.transaksiService = transaksiService;
    }

    /** Semua transaksi, untuk halaman admin. */
    public List<Transaksi> semuaTransaksi() {
        return transaksiRepository.semuaLengkap();
    }

    // shortcut: memuat semua transaksi lalu disaring di memori; cukup untuk satu toko, tambah query per penyewa jika data ribuan
    /** Transaksi milik satu penyewa, untuk halaman penyewa. */
    public List<Transaksi> transaksiPenyewa(Long penyewaId) {
        return transaksiRepository.semuaLengkap().stream()
                .filter(t -> t.getPenyewa().getId().equals(penyewaId))
                .toList();
    }

    public long dibayar(Transaksi transaksi) {
        return pembayaranRepository.totalDibayar(transaksi.getId());
    }

    /** Pembayaran online yang belum dicek admin, jika ada. */
    public Optional<Pembayaran> menunggu(Transaksi transaksi) {
        return pembayaranRepository.findFirstByTransaksiIdAndStatus(transaksi.getId(), StatusPembayaran.MENUNGGU);
    }

    /** Teks kolom Status: "Menunggu Konfirmasi" selama ada transfer yang belum dicek, selain itu Belum Bayar/Lunas. */
    public String keterangan(Transaksi transaksi) {
        if (menunggu(transaksi).isPresent()) {
            return StatusPembayaran.MENUNGGU.toString();
        }
        return status(transaksi.getTotal(), dibayar(transaksi)).toString();
    }

    /** Tanpa DP: Lunas jika yang dibayar sudah mencapai total, selain itu Belum Bayar. */
    public static StatusBayar status(long total, long dibayar) {
        return dibayar >= total ? StatusBayar.LUNAS : StatusBayar.BELUM_BAYAR;
    }

    // shortcut: cek sisa lalu simpan tanpa mengunci baris; aman untuk satu admin di satu PC, tambah kunci PESSIMISTIC_WRITE jika dipakai bersamaan
    /** Penyewa membayar di toko (tunai atau transfer): langsung lunas sebesar sisa tagihan, tanpa DP. */
    @Transactional
    public Pembayaran catat(Long transaksiId, LocalDate tanggal, MetodeBayar metode) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
        }
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));

        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal bayar wajib diisi.");
        }
        if (tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal bayar tidak boleh setelah hari ini.");
        }
        if (metode == null) {
            throw new IllegalArgumentException("Pilih metode bayar.");
        }

        if (pembayaranRepository.findFirstByTransaksiIdAndStatus(transaksiId, StatusPembayaran.MENUNGGU).isPresent()) {
            throw new IllegalArgumentException("Masih ada transfer online yang menunggu konfirmasi. Konfirmasi atau tolak dulu.");
        }
        long sisa = transaksi.getTotal() - pembayaranRepository.totalDibayar(transaksiId);
        if (sisa <= 0) {
            throw new IllegalArgumentException("Transaksi ini sudah lunas.");
        }

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTransaksi(transaksi);
        pembayaran.setJumlah(sisa);
        pembayaran.setTanggal(tanggal);
        pembayaran.setMetode(metode);
        return pembayaranRepository.save(pembayaran);
    }

    /**
     * Penyewa memesan dari aplikasi lalu menekan "Saya Sudah Transfer": transaksi dibuat lewat TransaksiService
     * (validasi tanggal dan sisa unit sama seperti di toko) dan transfer sebesar total dicatat menunggu konfirmasi.
     */
    @Transactional
    public Transaksi pesanOnline(Long penyewaId, LocalDate sewa, LocalDate kembali, Map<Long, Integer> jumlahPerAlat) {
        Transaksi transaksi = transaksiService.simpan(penyewaId, sewa, kembali, jumlahPerAlat);
        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTransaksi(transaksi);
        pembayaran.setJumlah(transaksi.getTotal());
        pembayaran.setTanggal(LocalDate.now());
        pembayaran.setMetode(MetodeBayar.TRANSFER);
        pembayaran.setStatus(StatusPembayaran.MENUNGGU);
        pembayaranRepository.save(pembayaran);
        return transaksi;
    }

    /** Admin sudah melihat uang masuk di rekening toko. */
    @Transactional
    public void konfirmasi(Long transaksiId) {
        Pembayaran pembayaran = pembayaranMenunggu(transaksiId);
        long sisa = pembayaran.getTransaksi().getTotal() - pembayaranRepository.totalDibayar(transaksiId);
        if (pembayaran.getJumlah() > sisa) {
            throw new IllegalArgumentException("Jumlah transfer melebihi sisa tagihan.");
        }
        pembayaran.setStatus(StatusPembayaran.DITERIMA);
        pembayaranRepository.save(pembayaran);
    }

    /**
     * Uang tidak masuk: transfer dihapus. Jika transaksi itu belum punya pembayaran lain (pesanan online),
     * transaksinya ikut dihapus agar alatnya tersedia lagi. Mengembalikan true jika pesanan dibatalkan.
     */
    @Transactional
    public boolean tolak(Long transaksiId) {
        Pembayaran pembayaran = pembayaranMenunggu(transaksiId);
        pembayaranRepository.delete(pembayaran);
        if (pembayaranRepository.existsByTransaksiId(transaksiId)) {
            return false;
        }
        transaksiRepository.delete(pembayaran.getTransaksi()); // detail ikut terhapus (cascade)
        return true;
    }

    private Pembayaran pembayaranMenunggu(Long transaksiId) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
        }
        return pembayaranRepository.findFirstByTransaksiIdAndStatus(transaksiId, StatusPembayaran.MENUNGGU)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi ini tidak punya transfer yang menunggu konfirmasi."));
    }
}
