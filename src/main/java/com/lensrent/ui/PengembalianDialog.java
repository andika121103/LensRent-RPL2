package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.TransaksiService;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Form catat pengembalian satu transaksi: tanggal dikembalikan, denda telat (otomatis), denda kerusakan. */
public class PengembalianDialog extends JDialog {

    private final TransaksiService transaksiService;
    private final Transaksi transaksi;
    private final JSpinner fieldTanggal = Tema.pilihTanggal(0);
    private final JTextField fieldKerusakan = new JTextField("0");
    private final JTextField fieldKeterangan = new JTextField();
    private final JLabel labelDenda = new JLabel();
    private final JLabel labelPesan = Tema.labelPesan();

    public PengembalianDialog(Window pemilik, TransaksiService transaksiService, Transaksi transaksi) {
        super(pemilik, "Catat Pengembalian", ModalityType.APPLICATION_MODAL);
        this.transaksiService = transaksiService;
        this.transaksi = transaksi;

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Pengembalian No " + transaksi.getId()));
        kartu.add(Box.createVerticalStrut(8));
        String alat = transaksi.getDetail().stream()
                .map(d -> d.getAlat().getNama() + " x" + d.getJumlah())
                .collect(Collectors.joining(", "));
        JLabel info = new JLabel("<html><div style='width:300px'>" + transaksi.getPenyewa().getNama()
                + "<br>Jadwal kembali: " + Tema.teksTanggal(transaksi.getTanggalKembali())
                + "<br>" + alat + "</div></html>");
        info.setForeground(Tema.TEKS_SEKUNDER);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(info);
        kartu.add(Box.createVerticalStrut(16));

        Tema.tambahField(kartu, "Tanggal Dikembalikan", fieldTanggal);
        Tema.tambahField(kartu, "Denda Kerusakan/Kehilangan (Rp, 0 jika tidak ada)", fieldKerusakan);
        Tema.tambahField(kartu, "Keterangan Kerusakan", fieldKeterangan);

        labelDenda.setFont(Tema.font(Font.BOLD, 14));
        labelDenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(labelDenda);
        kartu.add(Box.createVerticalStrut(12));

        JButton tombolSimpan = Tema.tombolUtama("Simpan Pengembalian");
        kartu.add(tombolSimpan);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        tombolSimpan.addActionListener(e -> simpan());
        fieldTanggal.addChangeListener(e -> perbaruiDenda());
        fieldKerusakan.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { perbaruiDenda(); }
            public void removeUpdate(DocumentEvent e) { perbaruiDenda(); }
            public void changedUpdate(DocumentEvent e) { perbaruiDenda(); }
        });
        perbaruiDenda();

        setContentPane(kartu);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }

    /** "50.000" atau "50000" menjadi 50000; kosong = 0. */
    static long bacaRupiah(String teks) {
        String angka = teks == null ? "" : teks.trim().replace(".", "");
        if (angka.isEmpty()) {
            return 0;
        }
        if (!angka.matches("\\d{1,15}")) {
            throw new IllegalArgumentException("Denda kerusakan hanya boleh angka, contoh 50000.");
        }
        return Long.parseLong(angka);
    }

    /** Hanya pratinjau; denda yang tersimpan dihitung ulang oleh TransaksiService. */
    private void perbaruiDenda() {
        LocalDate tanggal = Tema.tanggalTerakhir(fieldTanggal);
        long hari = Math.max(0, ChronoUnit.DAYS.between(transaksi.getTanggalKembali(), tanggal));
        try {
            long telat = TransaksiService.dendaTelat(transaksi, tanggal);
            String teks = "Denda telat (" + hari + " hari): " + Tema.rupiah(telat);
            try {
                teks += "<br>Total denda: " + Tema.rupiah(Math.addExact(telat, bacaRupiah(fieldKerusakan.getText())));
            } catch (IllegalArgumentException e) {
                teks += "<br>Total denda: -";
            }
            labelDenda.setText("<html>" + teks + "</html>");
        } catch (ArithmeticException e) {
            labelDenda.setText("Denda: terlalu besar");
        }
    }

    private void simpan() {
        try {
            transaksiService.kembalikan(transaksi.getId(), Tema.nilaiTanggal(fieldTanggal),
                    bacaRupiah(fieldKerusakan.getText()), fieldKeterangan.getText());
            dispose();
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            pack(); // dialog membesar agar pesan error terlihat
        }
    }
}
