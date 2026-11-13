package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Pembayaran;
import com.lensrent.model.StatusBayar;
import com.lensrent.model.StatusPembayaran;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test PembayaranService tanpa database: repository diganti mock (tiruan). */
class PembayaranServiceTest {

    private static final LocalDate HARI_INI = LocalDate.now();

    private PembayaranRepository pembayaranRepository;
    private TransaksiRepository transaksiRepository;
    private TransaksiService transaksiService;
    private Transaksi transaksi;
    private PembayaranService service;

    @BeforeEach
    void siapkan() {
        pembayaranRepository = mock(PembayaranRepository.class);
        transaksiRepository = mock(TransaksiRepository.class);
        transaksiService = mock(TransaksiService.class);
        transaksi = new Transaksi();
        transaksi.setTotal(300_000);
        when(transaksiRepository.findById(1L)).thenReturn(Optional.of(transaksi));
        when(pembayaranRepository.save(any(Pembayaran.class))).thenAnswer(i -> i.getArgument(0));
        when(pembayaranRepository.findFirstByTransaksiIdAndStatus(any(), any())).thenReturn(Optional.empty());
        service = new PembayaranService(pembayaranRepository, transaksiRepository, transaksiService);
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
        assertEquals(StatusPembayaran.DITERIMA, p.getStatus());
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

    /** Transfer online sebesar total yang menunggu konfirmasi untuk transaksi 1. */
    private Pembayaran adaTransferMenunggu() {
        Pembayaran p = new Pembayaran();
        p.setTransaksi(transaksi);
        p.setJumlah(300_000);
        p.setStatus(StatusPembayaran.MENUNGGU);
        when(pembayaranRepository.findFirstByTransaksiIdAndStatus(1L, StatusPembayaran.MENUNGGU)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void pesanOnlineMembuatTransaksiDanTransferMenunggu() {
        Map<Long, Integer> item = Map.of(7L, 1);
        LocalDate besok = HARI_INI.plusDays(1);
        when(transaksiService.simpan(2L, HARI_INI, besok, item)).thenReturn(transaksi);

        assertSame(transaksi, service.pesanOnline(2L, HARI_INI, besok, item));
        verify(pembayaranRepository).save(argThat(p -> p.getStatus() == StatusPembayaran.MENUNGGU
                && p.getJumlah() == 300_000 && p.getMetode() == MetodeBayar.TRANSFER && p.getTransaksi() == transaksi));
    }

    @Test
    void pesanOnlineGagalTidakMenyimpanPembayaran() {
        when(transaksiService.simpan(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Sisa Alat 7 pada tanggal itu hanya 0 unit."));
        assertThrows(IllegalArgumentException.class, () -> service.pesanOnline(2L, HARI_INI, HARI_INI.plusDays(1), Map.of(7L, 1)));
        verify(pembayaranRepository, never()).save(any());
    }

    @Test
    void konfirmasiMengubahStatusJadiDiterima() {
        Pembayaran p = adaTransferMenunggu();
        sudahDibayar(0);
        service.konfirmasi(1L);
        assertEquals(StatusPembayaran.DITERIMA, p.getStatus());
        verify(pembayaranRepository).save(p);
    }

    @Test
    void konfirmasiTanpaTransferMenungguDitolak() {
        assertThrows(IllegalArgumentException.class, () -> service.konfirmasi(1L));
    }

    @Test
    void tolakPesananOnlineMenghapusTransaksi() {
        Pembayaran p = adaTransferMenunggu();
        when(pembayaranRepository.existsByTransaksiId(1L)).thenReturn(false);
        assertTrue(service.tolak(1L));
        verify(pembayaranRepository).delete(p);
        verify(transaksiRepository).delete(transaksi);
    }

    @Test
    void tolakTransferTransaksiYangSudahAdaPembayaranLainTidakMenghapusTransaksi() {
        adaTransferMenunggu();
        when(pembayaranRepository.existsByTransaksiId(1L)).thenReturn(true);
        assertFalse(service.tolak(1L));
        verify(transaksiRepository, never()).delete(any());
    }

    @Test
    void catatDiTokoDitolakSelamaAdaTransferMenunggu() {
        adaTransferMenunggu();
        sudahDibayar(0);
        assertTrue(pesanError(HARI_INI, MetodeBayar.TUNAI).contains("menunggu konfirmasi"));
    }
}
