package com.lensrent.service;

import com.lensrent.model.Alat;
import com.lensrent.model.DetailTransaksi;
import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.model.StatusAlat;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.AlatRepository;
import com.lensrent.repository.DetailTransaksiRepository;
import com.lensrent.repository.PenggunaRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika transaksi sewa dan cek ketersediaan.
 * Stok alat tidak dikurangi saat disewa: sisa unit dihitung per tanggal dari transaksi yang bertabrakan.
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final DetailTransaksiRepository detailRepository;
    private final AlatRepository alatRepository;
    private final PenggunaRepository penggunaRepository;

    public TransaksiService(TransaksiRepository transaksiRepository, DetailTransaksiRepository detailRepository,
                            AlatRepository alatRepository, PenggunaRepository penggunaRepository) {
        this.transaksiRepository = transaksiRepository;
        this.detailRepository = detailRepository;
        this.alatRepository = alatRepository;
        this.penggunaRepository = penggunaRepository;
    }

    public List<Transaksi> semua() {
        return transaksiRepository.semuaLengkap();
    }

    public List<Pengguna> penyewaAktif() {
        return penggunaRepository.findByRoleAndAktifTrueOrderByNamaAsc(Role.USER);
    }

    /**
     * Unit yang masih bisa disewa sepanjang rentang tanggal. Dihitung dari hari paling ramai:
     * dua sewa yang bertabrakan dengan rentang tetapi berbeda hari hanya memakai satu unit yang sama.
     * Hari pengembalian dihitung terpakai karena jam kembalinya tidak dicatat.
     */
    public int sisaUnit(Alat alat, LocalDate sewa, LocalDate kembali) {
        periksaTanggal(sewa, kembali);
        return hitungSisa(alat, sewa, kembali);
    }

    /** Stok Tersedia di Data Alat dan Katalog: hanya melihat hari ini, bukan sewa yang baru mulai besok. */
    public int stokTersediaHariIni(Alat alat) {
        return hitungSisa(alat, LocalDate.now(), LocalDate.now());
    }

    private int hitungSisa(Alat alat, LocalDate sewa, LocalDate kembali) {
        if (alat.getStatus() != StatusAlat.TERSEDIA) {
            return 0;
        }
        List<DetailTransaksi> bentrok = detailRepository.yangBertabrakan(alat.getId(), sewa, kembali);
        int terpakai = sewa.datesUntil(kembali.plusDays(1))
                .mapToInt(hari -> bentrok.stream()
                        .filter(d -> !hari.isBefore(d.getTransaksi().getTanggalSewa())
                                && !hari.isAfter(d.getTransaksi().getTanggalKembali()))
                        .mapToInt(DetailTransaksi::getJumlah)
                        .sum())
                .max().orElse(0);
        return Math.max(0, alat.getStok() - terpakai); // stok bisa diturunkan admin di bawah jumlah yang tersewa
    }

    /** Harga per hari x jumlah x lama hari. Melempar ArithmeticException jika melebihi batas long. */
    public static long subtotal(long hargaPerHari, int jumlah, long lamaHari) {
        return Math.multiplyExact(Math.multiplyExact(hargaPerHari, (long) jumlah), lamaHari);
    }

    // shortcut: cek sisa lalu simpan tanpa mengunci baris; aman untuk satu admin di satu PC, tambah kunci PESSIMISTIC_WRITE jika dipakai bersamaan
    /** jumlahPerAlat: id alat -> jumlah unit yang disewa. */
    @Transactional
    public Transaksi simpan(Long penyewaId, LocalDate sewa, LocalDate kembali, Map<Long, Integer> jumlahPerAlat) {
        if (penyewaId == null) {
            throw new IllegalArgumentException("Pilih penyewa terlebih dahulu.");
        }
        periksaTanggal(sewa, kembali);
        if (jumlahPerAlat == null || jumlahPerAlat.isEmpty()) {
            throw new IllegalArgumentException("Tambahkan minimal satu alat.");
        }
        Pengguna penyewa = penggunaRepository.findById(penyewaId)
                .orElseThrow(() -> new IllegalArgumentException("Penyewa tidak ditemukan."));

        Transaksi transaksi = new Transaksi();
        transaksi.setPenyewa(penyewa);
        transaksi.setTanggalSewa(sewa);
        transaksi.setTanggalKembali(kembali);

        long total = 0;
        try {
            for (Map.Entry<Long, Integer> item : jumlahPerAlat.entrySet()) {
                Alat alat = alatRepository.findById(item.getKey())
                        .orElseThrow(() -> new IllegalArgumentException("Alat tidak ditemukan, mungkin sudah dihapus."));
                int jumlah = item.getValue();
                if (jumlah < 1) {
                    throw new IllegalArgumentException("Jumlah alat minimal 1.");
                }
                int sisa = sisaUnit(alat, sewa, kembali);
                if (jumlah > sisa) {
                    throw new IllegalArgumentException("Sisa " + alat.getNama() + " pada tanggal itu hanya " + sisa + " unit.");
                }
                DetailTransaksi detail = new DetailTransaksi();
                detail.setTransaksi(transaksi);
                detail.setAlat(alat);
                detail.setJumlah(jumlah);
                detail.setHargaPerHari(alat.getHargaPerHari());
                transaksi.getDetail().add(detail);
                total = Math.addExact(total, subtotal(alat.getHargaPerHari(), jumlah, transaksi.getLamaHari()));
            }
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Total terlalu besar, periksa harga dan jumlah alat.");
        }
        transaksi.setTotal(total);
        return transaksiRepository.save(transaksi);
    }

    private void periksaTanggal(LocalDate sewa, LocalDate kembali) {
        if (sewa == null || kembali == null) {
            throw new IllegalArgumentException("Tanggal sewa dan tanggal kembali wajib diisi.");
        }
        if (sewa.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal sewa tidak boleh sebelum hari ini.");
        }
        if (!kembali.isAfter(sewa)) {
            throw new IllegalArgumentException("Tanggal kembali harus setelah tanggal sewa.");
        }
    }
}
