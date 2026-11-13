package com.lensrent.ui;

import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Transaksi;
import com.lensrent.service.PembayaranService;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/** Form bayar di toko untuk satu transaksi: langsung lunas, admin hanya memilih tanggal dan metode. */
public class FormPembayaranDialog extends JDialog {

    private final PembayaranService pembayaranService;
    private final Transaksi transaksi;
    private final JSpinner fieldTanggal = Tema.pilihTanggal(0);
    private final JComboBox<MetodeBayar> pilihMetode = new JComboBox<>(MetodeBayar.values());
    private final JLabel labelPesan = Tema.labelPesan();

    public FormPembayaranDialog(Window pemilik, PembayaranService pembayaranService, Transaksi transaksi) {
        super(pemilik, "Catat Pembayaran", ModalityType.APPLICATION_MODAL);
        this.pembayaranService = pembayaranService;
        this.transaksi = transaksi;

        long dibayar = pembayaranService.dibayar(transaksi);
        long sisa = Math.max(0, transaksi.getTotal() - dibayar);

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Catat Pembayaran"));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(info("Transaksi No " + transaksi.getId() + " - " + transaksi.getPenyewa().getNama()));
        JLabel labelBayar = info(sisa > 0 ? "Bayar lunas: " + Tema.rupiah(sisa) : "Transaksi ini sudah lunas.");
        labelBayar.setFont(Tema.font(Font.BOLD, 16));
        kartu.add(labelBayar);
        kartu.add(Box.createVerticalStrut(12));

        Tema.tambahField(kartu, "Tanggal Bayar", fieldTanggal);
        Tema.tambahField(kartu, "Metode", pilihMetode);

        JButton tombolSimpan = Tema.tombolUtama("Simpan");
        tombolSimpan.setEnabled(sisa > 0); // transaksi lunas tidak bisa dibayar lagi
        kartu.add(tombolSimpan);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        tombolSimpan.addActionListener(e -> simpan());

        setContentPane(kartu);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }

    private static JLabel info(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(Tema.font(Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void simpan() {
        try {
            pembayaranService.catat(transaksi.getId(), Tema.nilaiTanggal(fieldTanggal), (MetodeBayar) pilihMetode.getSelectedItem());
            dispose();
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            pack(); // dialog membesar agar pesan error terlihat
        }
    }
}
