package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.service.PembayaranService;
import com.lensrent.session.Sesi;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Rincian sewa + rekening toko. Pesanan baru tersimpan saat penyewa menekan "Saya Sudah Transfer". */
public class BayarOnlineDialog extends JDialog {

    // Ganti dengan rekening toko yang asli
    private static final String REKENING = "BCA 1234567890 a.n. LensRent";

    private final PembayaranService pembayaranService;
    private final LocalDate sewa;
    private final LocalDate kembali;
    private final Map<Long, Integer> jumlahPerAlat;
    private final JLabel labelPesan = Tema.labelPesan();
    private Long nomorPesanan;

    public BayarOnlineDialog(Window pemilik, PembayaranService pembayaranService, LocalDate sewa, LocalDate kembali,
                             Map<Alat, Integer> daftarItem, Map<Long, Integer> jumlahPerAlat, long total) {
        super(pemilik, "Pembayaran Online", ModalityType.APPLICATION_MODAL);
        this.pembayaranService = pembayaranService;
        this.sewa = sewa;
        this.kembali = kembali;
        this.jumlahPerAlat = jumlahPerAlat;
        long hari = ChronoUnit.DAYS.between(sewa, kembali);

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Pembayaran Online"));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(info("Ambil di toko " + Tema.teksTanggal(sewa) + ", kembali " + Tema.teksTanggal(kembali), Font.PLAIN));
        daftarItem.forEach((alat, jumlah) -> kartu.add(info(alat.getNama() + " x" + jumlah + " · " + hari + " hari", Font.PLAIN)));
        kartu.add(Box.createVerticalStrut(8));
        kartu.add(info("Total transfer: " + (total < 0 ? "-" : Tema.rupiah(total)), Font.BOLD));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(info("Transfer ke", Font.BOLD));
        kartu.add(info(REKENING, Font.PLAIN));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(info("<html>Setelah transfer sesuai total, klik tombol di bawah.<br>"
                + "Status menjadi \"Menunggu Konfirmasi\" sampai admin mengecek rekening.</html>", Font.PLAIN));
        kartu.add(Box.createVerticalStrut(12));

        JButton tombolSudah = Tema.tombolUtama("Saya Sudah Transfer");
        kartu.add(tombolSudah);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);
        tombolSudah.addActionListener(e -> sudahTransfer());

        setContentPane(kartu);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }

    /** Nomor transaksi yang tersimpan; null jika dialog ditutup tanpa menekan "Saya Sudah Transfer". */
    public Long getNomorPesanan() {
        return nomorPesanan;
    }

    private static JLabel info(String teks, int gaya) {
        JLabel label = new JLabel(teks);
        label.setFont(Tema.font(gaya, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void sudahTransfer() {
        try {
            nomorPesanan = pembayaranService.pesanOnline(Sesi.getPenggunaAktif().getId(), sewa, kembali, jumlahPerAlat).getId();
            dispose();
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            pack(); // dialog membesar agar pesan error terlihat
        }
    }
}
