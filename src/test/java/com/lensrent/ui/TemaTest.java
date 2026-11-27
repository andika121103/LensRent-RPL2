package com.lensrent.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import javax.swing.JSpinner;
import org.junit.jupiter.api.Test;

class TemaTest {

    @Test
    void tanggalMustahilDitolakBukanDigeser() {
        JSpinner spinner = Tema.pilihTanggal(0);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setText("31-02-2026");
        assertThrows(IllegalArgumentException.class, () -> Tema.nilaiTanggal(spinner));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setText("28-02-2026");
        assertEquals(LocalDate.of(2026, 2, 28), Tema.nilaiTanggal(spinner));
    }

    @Test
    void bacaRupiahMenerimaTitikRibuan() {
        assertEquals(50_000, PengembalianDialog.bacaRupiah(" 50.000 "));
        assertEquals(0, PengembalianDialog.bacaRupiah(""));
        assertThrows(IllegalArgumentException.class, () -> PengembalianDialog.bacaRupiah("-5"));
        assertThrows(IllegalArgumentException.class, () -> PengembalianDialog.bacaRupiah("lima"));
    }
}
