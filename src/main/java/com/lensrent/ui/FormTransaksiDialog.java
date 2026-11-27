package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Transaksi;
import com.lensrent.service.AlatService;
import com.lensrent.service.TransaksiService;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Form catat sewa di toko: data penyewa (nama/HP/KTP), rentang tanggal, alat, dan metode bayar (langsung lunas). */
public class FormTransaksiDialog extends JDialog {

    private final TransaksiService transaksiService;
    private final JTextField fieldNama = new JTextField();
    private final JTextField fieldNoHp = new JTextField();
    private final JTextField fieldNoKtp = new JTextField();
    private final JSpinner fieldSewa = Tema.pilihTanggal(0);
    private final JSpinner fieldKembali = Tema.pilihTanggal(1);
    private final JComboBox<Alat> pilihAlat = new JComboBox<>();
    private final JSpinner fieldJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTable tabelItem = Tema.tabel("Alat", "Jumlah");
    private final JComboBox<MetodeBayar> pilihMetode = new JComboBox<>(MetodeBayar.values());
    private final JLabel labelTotal = new JLabel();
    private final JLabel labelPesan = Tema.labelPesan();
    /** Alat yang akan disewa -> jumlah unit; urutan sama dengan baris tabel. */
    private final Map<Alat, Integer> daftarItem = new LinkedHashMap<>();
    private Transaksi tersimpan; // null = dialog ditutup tanpa menyimpan

    public FormTransaksiDialog(Window pemilik, TransaksiService transaksiService, AlatService alatService) {
        super(pemilik, "Catat Transaksi", ModalityType.APPLICATION_MODAL);
        this.transaksiService = transaksiService;

        alatService.semua().stream().filter(Alat::isBisaDisewa).forEach(pilihAlat::addItem);
        Tema.teksDropdown(pilihAlat, a -> a.getNama() + " - " + Tema.rupiah(a.getHargaPerHari()) + "/hari");

        // Dua kolom agar dialog muat di layar laptop: kiri data penyewa & tanggal, kanan alat & pembayaran
        JPanel kiri = kolom();
        kiri.add(Tema.judul("Catat Transaksi"));
        kiri.add(Box.createVerticalStrut(16));
        Tema.tambahField(kiri, "Nama Penyewa", fieldNama);
        Tema.tambahField(kiri, "No. HP", fieldNoHp);
        Tema.tambahField(kiri, "No. KTP", fieldNoKtp);
        Tema.tambahField(kiri, "Tanggal Sewa", fieldSewa);
        Tema.tambahField(kiri, "Tanggal Kembali", fieldKembali);

        JPanel kanan = kolom();
        Tema.tambahField(kanan, "Alat", pilihAlat);
        Tema.tambahField(kanan, "Jumlah Unit", fieldJumlah);

        JButton tombolTambah = new JButton("Tambah ke Daftar");
        JButton tombolHapus = new JButton("Hapus dari Daftar");
        JPanel tombolItem = new JPanel();
        tombolItem.setOpaque(false);
        tombolItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombolItem.add(tombolTambah);
        tombolItem.add(tombolHapus);
        kanan.add(tombolItem);

        JScrollPane gulirItem = new JScrollPane(tabelItem);
        gulirItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        gulirItem.setPreferredSize(new Dimension(380, 110));
        gulirItem.setMaximumSize(new Dimension(380, 110));
        kanan.add(gulirItem);
        kanan.add(Box.createVerticalStrut(12));

        labelTotal.setFont(Tema.font(Font.BOLD, 16));
        labelTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        kanan.add(labelTotal);
        kanan.add(Box.createVerticalStrut(12));
        Tema.tambahField(kanan, "Metode Bayar (dibayar lunas sekarang)", pilihMetode);

        JButton tombolSimpan = Tema.tombolUtama("Simpan & Bayar Lunas");
        kanan.add(tombolSimpan);
        kanan.add(Box.createVerticalStrut(12));
        kanan.add(labelPesan);

        JPanel kartu = Tema.kartuForm();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.X_AXIS));
        kartu.add(kiri);
        kartu.add(Box.createHorizontalStrut(32));
        kartu.add(kanan);

        tombolTambah.addActionListener(e -> tambahItem());
        tombolHapus.addActionListener(e -> hapusItem());
        tombolSimpan.addActionListener(e -> simpan());
        fieldSewa.addChangeListener(e -> perbaruiTotal());
        fieldKembali.addChangeListener(e -> perbaruiTotal());
        perbaruiTotal();

        setContentPane(kartu);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }

    private static JPanel kolom() {
        JPanel kolom = new JPanel();
        kolom.setLayout(new BoxLayout(kolom, BoxLayout.Y_AXIS));
        kolom.setOpaque(false);
        kolom.setAlignmentY(Component.TOP_ALIGNMENT);
        return kolom;
    }

    private void tambahItem() {
        Alat alat = (Alat) pilihAlat.getSelectedItem();
        if (alat == null) {
            tampilkanError("Tidak ada alat yang bisa disewa.");
            return;
        }
        daftarItem.merge(alat, (Integer) fieldJumlah.getValue(), Integer::sum);
        muatItem();
    }

    private void hapusItem() {
        int baris = tabelItem.getSelectedRow();
        if (baris < 0) {
            tampilkanError("Pilih alat di daftar terlebih dahulu.");
            return;
        }
        daftarItem.remove(daftarItem.keySet().stream().skip(baris).findFirst().orElseThrow());
        muatItem();
    }

    private void muatItem() {
        DefaultTableModel model = (DefaultTableModel) tabelItem.getModel();
        model.setRowCount(0);
        daftarItem.forEach((alat, jumlah) -> model.addRow(new Object[] {alat.getNama(), jumlah}));
        perbaruiTotal();
    }

    /** Hanya pratinjau; total yang tersimpan dihitung ulang oleh TransaksiService. */
    private void perbaruiTotal() {
        long hari = ChronoUnit.DAYS.between(Tema.tanggalTerakhir(fieldSewa), Tema.tanggalTerakhir(fieldKembali));
        if (hari <= 0) {
            labelTotal.setText("Total: -");
            return;
        }
        try {
            long total = 0;
            for (Map.Entry<Alat, Integer> item : daftarItem.entrySet()) {
                total = Math.addExact(total, TransaksiService.subtotal(item.getKey().getHargaPerHari(), item.getValue(), hari));
            }
            labelTotal.setText("Total (" + hari + " hari): " + Tema.rupiah(total));
        } catch (ArithmeticException e) {
            labelTotal.setText("Total: terlalu besar");
        }
    }

    private void simpan() {
        try {
            Map<Long, Integer> jumlahPerAlat = new LinkedHashMap<>(); // urutan sesuai yang diisi admin
            daftarItem.forEach((alat, jumlah) -> jumlahPerAlat.put(alat.getId(), jumlah));
            tersimpan = transaksiService.simpan(fieldNama.getText(), fieldNoHp.getText(), fieldNoKtp.getText(),
                    Tema.nilaiTanggal(fieldSewa), Tema.nilaiTanggal(fieldKembali), jumlahPerAlat,
                    (MetodeBayar) pilihMetode.getSelectedItem());
            dispose();
        } catch (IllegalArgumentException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    /** Transaksi yang baru disimpan (sudah lunas), atau null jika dibatalkan. */
    public Transaksi getTersimpan() {
        return tersimpan;
    }

    private void tampilkanError(String pesan) {
        // lebar tetap: pesan panjang turun baris, bukan melebarkan kolom kanan
        Tema.tampilkanError(labelPesan, "<div style='width:290px'>" + pesan + "</div>");
        pack(); // dialog membesar agar pesan error terlihat
    }
}
