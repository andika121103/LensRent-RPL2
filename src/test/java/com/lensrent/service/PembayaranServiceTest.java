package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Pembayaran;
import com.lensrent.model.StatusBayar;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test PembayaranService tanpa database: repository diganti mock (tiruan). */
class PembayaranServiceTest {

    private static final LocalDate HARI_INI = LocalDate.now();

    private PembayaranRepository pembayaranRepository;
    private TransaksiRepository transaksiRepository;
    private Transaksi transaksi;
    private PembayaranService service;

    @BeforeEach
    void siapkan() {
        pembayaranRepository = mock(PembayaranRepository.class);
        transaksiRepository = mock(TransaksiRepository.class);
        transaksi = new Transaksi();
        transaksi.setTotal(300_000);
        when(transaksiRepository.findById(1L)).thenReturn(Optional.of(transaksi));
        when(pembayaranRepository.save(any(Pembayaran.class))).thenAnswer(i -> i.getArgument(0));
        service = new PembayaranService(pembayaranRepository, transaksiRepository, null); // tanpa database
    }

    private void sudahDibayar(long jumlah) {
        when(pembayaranRepository.totalDibayar(1L)).thenReturn(jumlah);
    }

    private String pesanError(LocalDate tanggal, MetodeBayar metode) {
        return assertThrows(IllegalArgumentException.class,
                () -> service.catat(1L, tanggal, metode)).getMessage();
    }

    @Test
    void statusDihitungDariJumlahDibayar() {
        assertEquals(StatusBayar.BELUM_BAYAR, PembayaranService.status(300_000, 0));
        assertEquals(StatusBayar.LUNAS, PembayaranService.status(300_000, 300_000));
    }

    @Test
    void bayarDiTokoLangsungLunasSebesarTotal() {
        sudahDibayar(0);
        Pembayaran p = service.catat(1L, HARI_INI, MetodeBayar.TUNAI);
        assertEquals(300_000, p.getJumlah());
        assertEquals(MetodeBayar.TUNAI, p.getMetode());
    }

    @Test
    void transaksiLunasTidakBisaDibayarLagi() {
        sudahDibayar(300_000);
        assertEquals("Transaksi ini sudah lunas.", pesanError(HARI_INI, MetodeBayar.TUNAI));
        verify(pembayaranRepository, never()).save(any());
    }

    @Test
    void tanggalDanMetodeDiperiksa() {
        sudahDibayar(0);
        assertEquals("Tanggal bayar tidak boleh setelah hari ini.", pesanError(HARI_INI.plusDays(1), MetodeBayar.TUNAI));
        assertEquals("Pilih metode bayar.", pesanError(HARI_INI, null));
    }

    @Test
    void transaksiTidakDipilihDitolak() {
        assertThrows(IllegalArgumentException.class, () -> service.catat(null, HARI_INI, MetodeBayar.TUNAI));
    }

    @Test
    void laporanDenganTanggalAwalSetelahAkhirDitolak() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.laporanTransaksi(HARI_INI, HARI_INI.minusDays(1)));
        assertTrue(e.getMessage().contains("Tanggal awal"));
    }

    @Test
    void notaTanpaTransaksiDitolak() {
        assertThrows(IllegalArgumentException.class, () -> service.nota(null));
    }
}
