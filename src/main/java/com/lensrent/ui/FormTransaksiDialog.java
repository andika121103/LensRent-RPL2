package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.Pengguna;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Form catat transaksi sewa: satu penyewa, rentang tanggal, satu atau lebih alat. */
public class FormTransaksiDialog extends JDialog {

    private final TransaksiService transaksiService;
    private final JComboBox<Pengguna> pilihPenyewa = new JComboBox<>();
    private final JSpinner fieldSewa = Tema.pilihTanggal(0);
    private final JSpinner fieldKembali = Tema.pilihTanggal(1);
    private final JComboBox<Alat> pilihAlat = new JComboBox<>();
    private final JSpinner fieldJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTable tabelItem = Tema.tabel("Alat", "Jumlah");
    private final JLabel labelTotal = new JLabel();
    private final JLabel labelPesan = Tema.labelPesan();
    /** Alat yang akan disewa -> jumlah unit; urutan sama dengan baris tabel. */
    private final Map<Alat, Integer> daftarItem = new LinkedHashMap<>();

    public FormTransaksiDialog(Window pemilik, TransaksiService transaksiService, AlatService alatService) {
        super(pemilik, "Catat Transaksi", ModalityType.APPLICATION_MODAL);
        this.transaksiService = transaksiService;

        transaksiService.penyewaAktif().forEach(pilihPenyewa::addItem);
        Tema.teksDropdown(pilihPenyewa, p -> p.getNama() + " (" + p.getUsername() + ")");
        alatService.semua().stream().filter(Alat::isBisaDisewa).forEach(pilihAlat::addItem);
        Tema.teksDropdown(pilihAlat, a -> a.getNama() + " - " + Tema.rupiah(a.getHargaPerHari()) + "/hari");

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Catat Transaksi"));
        kartu.add(Box.createVerticalStrut(16));

        Tema.tambahField(kartu, "Penyewa", pilihPenyewa);
        Tema.tambahField(kartu, "Tanggal Sewa", fieldSewa);
        Tema.tambahField(kartu, "Tanggal Kembali", fieldKembali);
        Tema.tambahField(kartu, "Alat", pilihAlat);
        Tema.tambahField(kartu, "Jumlah Unit", fieldJumlah);

        JButton tombolTambah = new JButton("Tambah ke Daftar");
        JButton tombolHapus = new JButton("Hapus dari Daftar");
        JPanel tombolItem = new JPanel();
        tombolItem.setOpaque(false);
        tombolItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombolItem.add(tombolTambah);
        tombolItem.add(tombolHapus);
        kartu.add(tombolItem);

        JScrollPane gulirItem = new JScrollPane(tabelItem);
        gulirItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        gulirItem.setPreferredSize(new Dimension(380, 110));
        gulirItem.setMaximumSize(new Dimension(380, 110));
        kartu.add(gulirItem);
        kartu.add(Box.createVerticalStrut(12));

        labelTotal.setFont(Tema.font(Font.BOLD, 16));
        labelTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(labelTotal);
        kartu.add(Box.createVerticalStrut(12));

        JButton tombolSimpan = Tema.tombolUtama("Simpan");
        kartu.add(tombolSimpan);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

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
            Pengguna penyewa = (Pengguna) pilihPenyewa.getSelectedItem();
            Map<Long, Integer> jumlahPerAlat = new LinkedHashMap<>(); // urutan sesuai yang diisi admin
            daftarItem.forEach((alat, jumlah) -> jumlahPerAlat.put(alat.getId(), jumlah));
            transaksiService.simpan(penyewa == null ? null : penyewa.getId(),
                    Tema.nilaiTanggal(fieldSewa), Tema.nilaiTanggal(fieldKembali), jumlahPerAlat);
            dispose();
        } catch (IllegalArgumentException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    private void tampilkanError(String pesan) {
        Tema.tampilkanError(labelPesan, pesan);
        pack(); // dialog membesar agar pesan error terlihat
    }
}
