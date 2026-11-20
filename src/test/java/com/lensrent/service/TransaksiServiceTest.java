package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test TransaksiService tanpa database: repository diganti mock (tiruan). */
class TransaksiServiceTest {

    private static final LocalDate BESOK = LocalDate.now().plusDays(1);

    private TransaksiRepository transaksiRepository;
    private DetailTransaksiRepository detailRepository;
    private AlatRepository alatRepository;
    private PenggunaRepository penggunaRepository;
    private TransaksiService service;

    @BeforeEach
    void siapkan() {
        transaksiRepository = mock(TransaksiRepository.class);
        detailRepository = mock(DetailTransaksiRepository.class);
        alatRepository = mock(AlatRepository.class);
        penggunaRepository = mock(PenggunaRepository.class);
        when(transaksiRepository.save(any(Transaksi.class))).thenAnswer(i -> i.getArgument(0));
        when(penggunaRepository.save(any(Pengguna.class))).thenAnswer(i -> i.getArgument(0));
        service = new TransaksiService(transaksiRepository, detailRepository, alatRepository, penggunaRepository);
    }

    private Alat alat(long id, long harga, int stok, StatusAlat status) {
        Alat a = new Alat();
        a.setId(id);
        a.setNama("Alat " + id);
        a.setHargaPerHari(harga);
        a.setStok(stok);
        a.setStatus(status);
        when(alatRepository.findById(id)).thenReturn(Optional.of(a));
        return a;
    }

    /** Sewa yang sudah ada, dimulai n hari dari besok selama lama hari. */
    private DetailTransaksi sewa(int mulai, int lama, int jumlah) {
        Transaksi t = new Transaksi();
        t.setTanggalSewa(BESOK.plusDays(mulai));
        t.setTanggalKembali(BESOK.plusDays(mulai + lama));
        DetailTransaksi d = new DetailTransaksi();
        d.setTransaksi(t);
        d.setJumlah(jumlah);
        return d;
    }

    private void adaSewa(DetailTransaksi... daftar) {
        when(detailRepository.yangBertabrakan(any(), any(), any())).thenReturn(List.of(daftar));
    }

    private static final String NAMA = "Budi";
    private static final String HP = "081234567890";
    private static final String KTP = "1271010101900001";

    /** Sewa di toko oleh penyewa contoh (Budi). */
    private Transaksi simpan(LocalDate sewa, LocalDate kembali, Map<Long, Integer> item) {
        return service.simpan(NAMA, HP, KTP, sewa, kembali, item);
    }

    @Test
    void penyewaBaruDibuatTanpaAkun() {
        alat(1L, 100_000, 5, StatusAlat.TERSEDIA);
        Transaksi t = simpan(BESOK, BESOK.plusDays(1), Map.of(1L, 1));
        Pengguna p = t.getPenyewa();
        assertEquals(NAMA, p.getNama());
        assertEquals(HP, p.getNoHp());
        assertEquals(KTP, p.getNoKtp());
        assertEquals(Role.USER, p.getRole());
        assertNull(p.getUsername(), "penyewa di toko tidak punya akun login");
    }

    @Test
    void penyewaDenganNoHpSamaDipakaiUlang() {
        alat(1L, 100_000, 5, StatusAlat.TERSEDIA);
        Pengguna lama = new Pengguna();
        lama.setNama("Budi Santoso");
        when(penggunaRepository.findFirstByRoleAndNoHpOrderByIdAsc(Role.USER, HP)).thenReturn(Optional.of(lama));
        Transaksi t = service.simpan(" Budi ", " " + HP + " ", KTP, BESOK, BESOK.plusDays(1), Map.of(1L, 1));
        assertSame(lama, t.getPenyewa());
        assertEquals("Budi Santoso", lama.getNama(), "nama penyewa lama tidak ditimpa");
        assertEquals(KTP, lama.getNoKtp());
    }

    @Test
    void dataPenyewaTidakValidDitolak() {
        alat(1L, 100_000, 5, StatusAlat.TERSEDIA);
        Map<Long, Integer> item = Map.of(1L, 1);
        assertThrows(IllegalArgumentException.class, () -> service.simpan(" ", HP, KTP, BESOK, BESOK.plusDays(1), item));
        assertThrows(IllegalArgumentException.class, () -> service.simpan(NAMA, "08abc", KTP, BESOK, BESOK.plusDays(1), item));
        assertThrows(IllegalArgumentException.class, () -> service.simpan(NAMA, HP, "12345", BESOK, BESOK.plusDays(1), item));
        assertThrows(IllegalArgumentException.class, () -> service.simpan(NAMA, HP, null, BESOK, BESOK.plusDays(1), item));
        assertThrows(IllegalArgumentException.class, () -> service.simpan("x".repeat(101), HP, KTP, BESOK, BESOK.plusDays(1), item));
        verify(penggunaRepository, never()).save(any());
        verify(transaksiRepository, never()).save(any());
    }

    @Test
    void simpanMenghitungTotalDanMenyimpanHargaSaatDisewa() {
        alat(1L, 100_000, 5, StatusAlat.TERSEDIA);
        alat(2L, 50_000, 5, StatusAlat.TERSEDIA);
        Transaksi t = simpan(BESOK, BESOK.plusDays(3), Map.of(1L, 2, 2L, 1));
        assertEquals(750_000, t.getTotal()); // (100rb x 2 + 50rb x 1) x 3 hari
        assertEquals(3, t.getLamaHari());
        assertEquals(2, t.getDetail().size());
        assertTrue(t.getDetail().stream().anyMatch(d -> d.getHargaPerHari() == 100_000 && d.getJumlah() == 2));
    }

    @Test
    void inputTidakValidDitolak() {
        alat(1L, 100_000, 5, StatusAlat.TERSEDIA);
        Map<Long, Integer> item = Map.of(1L, 1);
        assertThrows(IllegalArgumentException.class, () -> simpan(null, BESOK, item));
        assertThrows(IllegalArgumentException.class, () -> simpan(BESOK, BESOK, item)); // kembali = sewa
        assertThrows(IllegalArgumentException.class, () -> simpan(BESOK.plusDays(1), BESOK, item)); // kembali < sewa
        assertThrows(IllegalArgumentException.class, () -> simpan(LocalDate.now().minusDays(1), BESOK, item));
        assertThrows(IllegalArgumentException.class, () -> simpan(BESOK, BESOK.plusDays(1), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> simpan(BESOK, BESOK.plusDays(1), Map.of(1L, 0)));
        assertThrows(IllegalArgumentException.class, () -> simpan(BESOK, BESOK.plusDays(1), Map.of(9L, 1)));
        verify(transaksiRepository, never()).save(any());
    }

    @Test
    void sewaMelebihiSisaDitolak() {
        alat(1L, 100_000, 3, StatusAlat.TERSEDIA);
        adaSewa(sewa(0, 2, 2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> simpan(BESOK, BESOK.plusDays(2), Map.of(1L, 2)));
        assertTrue(e.getMessage().contains("hanya 1 unit"));
        verify(transaksiRepository, never()).save(any());
    }

    @Test
    void totalYangMelebihiBatasDitolakBukanSalahHitung() {
        alat(1L, 999_999_999, 999_999_999, StatusAlat.TERSEDIA);
        assertThrows(IllegalArgumentException.class,
                () -> simpan(BESOK, BESOK.plusDays(20), Map.of(1L, 999_999_999)));
    }

    @Test
    void sisaUnitDihitungDariHariPalingRamai() {
        Alat a = alat(1L, 100_000, 2, StatusAlat.TERSEDIA);
        // dua sewa tidak bertabrakan satu sama lain: satu unit cukup melayani keduanya
        adaSewa(sewa(0, 1, 1), sewa(5, 1, 1));
        assertEquals(1, service.sisaUnit(a, BESOK, BESOK.plusDays(7)));
        // dua sewa pada hari yang sama: kedua unit terpakai
        adaSewa(sewa(0, 3, 1), sewa(2, 3, 1));
        assertEquals(0, service.sisaUnit(a, BESOK, BESOK.plusDays(7)));
    }

    @Test
    void stokTersediaHariIniTidakTerpengaruhSewaBesok() {
        Alat a = alat(1L, 100_000, 2, StatusAlat.TERSEDIA);
        adaSewa(sewa(0, 2, 1)); // mulai besok
        assertEquals(2, service.stokTersediaHariIni(a));
        adaSewa(sewa(-1, 2, 1)); // mulai hari ini
        assertEquals(1, service.stokTersediaHariIni(a));
        assertEquals(2, a.getStok(), "jumlah stok tidak berubah");
    }

    @Test
    void hariPengembalianMasihTerpakai() {
        Alat a = alat(1L, 100_000, 1, StatusAlat.TERSEDIA);
        adaSewa(sewa(0, 2, 1)); // kembali besok + 2
        assertEquals(0, service.sisaUnit(a, BESOK.plusDays(2), BESOK.plusDays(4)));
    }

    @Test
    void sisaUnitNolJikaAlatTidakTersediaAtauStokDiturunkan() {
        Alat rusak = alat(1L, 100_000, 5, StatusAlat.TIDAK_TERSEDIA);
        assertEquals(0, service.sisaUnit(rusak, BESOK, BESOK.plusDays(1)));
        Alat a = alat(2L, 100_000, 1, StatusAlat.TERSEDIA);
        adaSewa(sewa(0, 1, 3)); // stok diturunkan admin setelah 3 unit tersewa
        assertEquals(0, service.sisaUnit(a, BESOK, BESOK.plusDays(1)));
    }
}
