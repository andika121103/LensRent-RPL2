package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.service.AlatService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman admin: daftar alat + tambah, ubah, hapus. */
public class DataAlatPanel extends JPanel {

    private final AlatService alatService;
    private final JTable tabel = Tema.tabel("Nama", "Kategori", "Merek", "Harga/Hari", "Stok", "Status");
    /** Urutan sama dengan baris tabel, untuk mengambil alat yang dipilih. */
    private List<Alat> daftarAlat = List.of();

    public DataAlatPanel(AlatService alatService) {
        super(new BorderLayout(0, 16));
        this.alatService = alatService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JButton tombolTambah = Tema.tombolUtama("Tambah Alat");
        tombolTambah.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form
        JButton tombolUbah = new JButton("Ubah");
        JButton tombolHapus = new JButton("Hapus");

        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombolPanel.setOpaque(false);
        tombolPanel.add(tombolUbah);
        tombolPanel.add(tombolHapus);
        tombolPanel.add(tombolTambah);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.judul("Data Alat"), BorderLayout.WEST);
        atas.add(tombolPanel, BorderLayout.EAST);

        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        tombolTambah.addActionListener(e -> bukaForm(null));
        tombolUbah.addActionListener(e -> {
            Alat alat = alatTerpilih();
            if (alat != null) {
                bukaForm(alat);
            }
        });
        tombolHapus.addActionListener(e -> hapus());

        muat();
    }

    private void muat() {
        daftarAlat = alatService.semua();
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        for (Alat alat : daftarAlat) {
            model.addRow(new Object[] {alat.getNama(), alat.getKategori(), alat.getMerek(),
                Tema.rupiah(alat.getHargaPerHari()), alat.getStok(), alat.getStatus()});
        }
    }

    private Alat alatTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih alat di tabel terlebih dahulu.",
                    "Data Alat", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return daftarAlat.get(baris);
    }

    private void bukaForm(Alat alat) {
        // Dialog modal: baris berikutnya baru jalan setelah dialog ditutup
        new FormAlatDialog(SwingUtilities.getWindowAncestor(this), alatService, alat).setVisible(true);
        muat();
    }

    private void hapus() {
        Alat alat = alatTerpilih();
        if (alat == null) {
            return;
        }
        int jawaban = JOptionPane.showConfirmDialog(this,
                "Hapus \"" + alat.getNama() + "\" dari data alat?", "Hapus Alat",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (jawaban == JOptionPane.YES_OPTION) {
            alatService.hapus(alat.getId());
            muat();
        }
    }
}
