package com.lensrent.service;

import com.lensrent.model.Alat;
import com.lensrent.model.DetailTransaksi;
import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Pembayaran;
import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.model.StatusAlat;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.AlatRepository;
import com.lensrent.repository.DetailTransaksiRepository;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.PenggunaRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    private static final String POLA_NO_HP = "\\d{10,13}"; // sama dengan AuthService
    private static final String POLA_NO_KTP = "\\d{16}";

    private final TransaksiRepository transaksiRepository;
    private final DetailTransaksiRepository detailRepository;
    private final AlatRepository alatRepository;
    private final PenggunaRepository penggunaRepository;
    private final PembayaranRepository pembayaranRepository;

    public TransaksiService(TransaksiRepository transaksiRepository, DetailTransaksiRepository detailRepository,
                            AlatRepository alatRepository, PenggunaRepository penggunaRepository,
                            PembayaranRepository pembayaranRepository) {
        this.transaksiRepository = transaksiRepository;
        this.detailRepository = detailRepository;
        this.alatRepository = alatRepository;
        this.penggunaRepository = penggunaRepository;
        this.pembayaranRepository = pembayaranRepository;
    }

    public List<Transaksi> semua() {
        return transaksiRepository.semuaLengkap();
    }

    /**
     * Unit yang masih bisa disewa sepanjang rentang tanggal. Dihitung dari hari paling ramai:
     * dua sewa yang bertabrakan dengan rentang tetapi berbeda hari hanya memakai satu unit yang sama.
     * Hari pengembalian dihitung terpakai karena jam kembalinya tidak dicatat. Alat yang dikembalikan lebih awal
     * bebas sejak hari berikutnya; alat terlambat yang belum kembali dihitung terpakai sampai hari ini.
     */
    public int sisaUnit(Alat alat, LocalDate sewa, LocalDate kembali) {
        periksaTanggal(sewa, kembali);
        return hitungSisa(alat, sewa, kembali);
    }

    /** Kolom Sisa Hari Ini di Data Alat: hanya melihat hari ini, bukan sewa yang baru mulai besok. */
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
                                && !hari.isAfter(d.getTransaksi().getAkhirPakai(LocalDate.now())))
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
    /**
     * Sewa di toko. Data penyewa diisi admin; penyewa dengan No HP yang sama dipakai ulang (No KTP diperbarui),
     * jika belum ada dibuat sebagai penyewa tanpa akun. jumlahPerAlat: id alat -> jumlah unit yang disewa.
     * Penyewa langsung membayar lunas saat itu (alat baru diserahkan setelah dibayar), jadi pembayaran
     * dicatat dalam transaksi database yang sama: tidak ada sewa yang tersimpan tanpa pembayaran.
     */
    @Transactional
    public Transaksi simpan(String nama, String noHp, String noKtp, LocalDate sewa, LocalDate kembali,
                            Map<Long, Integer> jumlahPerAlat, MetodeBayar metode) {
        if (nama == null || nama.isBlank() || noHp == null || noHp.isBlank() || noKtp == null || noKtp.isBlank()) {
            throw new IllegalArgumentException("Nama, No. HP, dan No. KTP penyewa wajib diisi.");
        }
        if (nama.trim().length() > 100) {
            throw new IllegalArgumentException("Nama penyewa maksimal 100 karakter.");
        }
        if (!noHp.trim().matches(POLA_NO_HP)) {
            throw new IllegalArgumentException("No. HP hanya boleh angka, 10 sampai 13 digit.");
        }
        if (!noKtp.trim().matches(POLA_NO_KTP)) {
            throw new IllegalArgumentException("No. KTP harus 16 digit angka.");
        }
        periksaTanggal(sewa, kembali);
        if (jumlahPerAlat == null || jumlahPerAlat.isEmpty()) {
            throw new IllegalArgumentException("Tambahkan minimal satu alat.");
        }
        if (metode == null) {
            throw new IllegalArgumentException("Pilih metode bayar.");
        }
        Pengguna penyewa = penggunaRepository.findFirstByRoleAndNoHpOrderByIdAsc(Role.USER, noHp.trim())
                .map(lama -> {
                    // satu No HP = satu penyewa; nama berbeda biasanya salah ketik No HP
                    if (!lama.getNama().equalsIgnoreCase(nama.trim())) {
                        throw new IllegalArgumentException("No. HP " + noHp.trim() + " sudah terdaftar atas nama "
                                + lama.getNama() + ". Pakai nama itu, atau periksa kembali No. HP.");
                    }
                    return lama;
                })
                .orElseGet(() -> {
                    Pengguna baru = new Pengguna();
                    baru.setNama(nama.trim());
                    baru.setNoHp(noHp.trim());
                    baru.setRole(Role.USER);
                    return baru;
                });
        penyewa.setNoKtp(noKtp.trim());
        penyewa = penggunaRepository.save(penyewa);

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
        transaksi = transaksiRepository.save(transaksi);

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTransaksi(transaksi);
        pembayaran.setJumlah(total);
        pembayaran.setTanggal(LocalDate.now());
        pembayaran.setMetode(metode);
        pembayaranRepository.save(pembayaran);
        return transaksi;
    }

    /** Harga sewa semua alat di transaksi untuk satu hari (harga saat disewa x jumlah). */
    public static long hargaPerHari(Transaksi transaksi) {
        long total = 0;
        for (DetailTransaksi d : transaksi.getDetail()) {
            total = Math.addExact(total, Math.multiplyExact(d.getHargaPerHari(), (long) d.getJumlah()));
        }
        return total;
    }

    /** Denda telat = hari terlambat x harga sewa per hari seluruh alat di transaksi. */
    public static long dendaTelat(Transaksi transaksi, LocalDate dikembalikan) {
        long hari = Math.max(0, ChronoUnit.DAYS.between(transaksi.getTanggalKembali(), dikembalikan));
        return Math.multiplyExact(hari, hargaPerHari(transaksi));
    }

    /** Status untuk tabel: Dikembalikan, Terlambat n hari, Disewa, atau Belum Diambil (tanggal sewa belum tiba). */
    public static String status(Transaksi transaksi, LocalDate hariIni) {
        if (transaksi.getTanggalDikembalikan() != null) {
            return "Dikembalikan";
        }
        long telat = transaksi.getHariTerlambat(hariIni);
        if (telat > 0) {
            return "Terlambat " + telat + " hari";
        }
        return hariIni.isBefore(transaksi.getTanggalSewa()) ? "Belum Diambil" : "Disewa";
    }

    /**
     * Mencatat alat dikembalikan. Denda telat dihitung otomatis, denda kerusakan diisi admin (0 jika tidak ada).
     * Denda dibayar saat itu juga, jadi langsung tersimpan sebagai pendapatan transaksi ini.
     */
    @Transactional
    public Transaksi kembalikan(Long transaksiId, LocalDate dikembalikan, long dendaKerusakan, String keterangan) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
        }
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));
        if (transaksi.getTanggalDikembalikan() != null) {
            throw new IllegalArgumentException("Alat transaksi No " + transaksiId + " sudah dikembalikan.");
        }
        if (dikembalikan == null) {
            throw new IllegalArgumentException("Tanggal dikembalikan wajib diisi.");
        }
        if (dikembalikan.isBefore(transaksi.getTanggalSewa())) {
            throw new IllegalArgumentException("Tanggal dikembalikan tidak boleh sebelum tanggal sewa.");
        }
        if (dikembalikan.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal dikembalikan tidak boleh setelah hari ini.");
        }
        if (dendaKerusakan < 0) {
            throw new IllegalArgumentException("Denda kerusakan tidak boleh negatif.");
        }
        String teks = keterangan == null ? "" : keterangan.trim();
        if (dendaKerusakan > 0 && teks.isEmpty()) {
            throw new IllegalArgumentException("Isi keterangan kerusakan/kehilangan.");
        }
        if (teks.length() > 255) {
            throw new IllegalArgumentException("Keterangan maksimal 255 karakter.");
        }
        long telat;
        try {
            telat = dendaTelat(transaksi, dikembalikan);
            Math.addExact(telat, dendaKerusakan); // total denda harus muat di long
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Denda terlalu besar, periksa nominal denda kerusakan.");
        }
        transaksi.setDendaTelat(telat);
        transaksi.setTanggalDikembalikan(dikembalikan);
        transaksi.setDendaKerusakan(dendaKerusakan);
        transaksi.setKeteranganDenda(teks.isEmpty() ? null : teks);
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
