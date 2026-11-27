package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.service.AlatService;
import com.lensrent.service.TransaksiService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Halaman admin: daftar alat (cari, saring kategori, foto + keterangan) + tambah, ubah, hapus,
 * dan cek ketersediaan per tanggal untuk menjawab pertanyaan pelanggan.
 */
public class DataAlatPanel extends JPanel {

    private static final String SEMUA_KATEGORI = "Semua Kategori";

    private final AlatService alatService;
    private final TransaksiService transaksiService;
    private final JTextField fieldCari = new JTextField(18);
    private final JComboBox<Object> pilihKategori = new JComboBox<>();
    private final JTable tabel = Tema.tabel("Nama", "Kategori", "Merek", "Harga/Hari", "Stok", "Sisa Hari Ini", "Status");
    private final JLabel kotakFoto = Tema.kotakFoto(220, 165);
    private final JLabel labelKeterangan = new JLabel(" ");
    /** Urutan sama dengan baris tabel, untuk mengambil alat yang dipilih. */
    private List<Alat> daftarAlat = List.of();

    public DataAlatPanel(AlatService alatService, TransaksiService transaksiService) {
        super(new BorderLayout(16, 16)); // jarak 16 antara tabel dan foto
        this.alatService = alatService;
        this.transaksiService = transaksiService;
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

        JPanel judul = new JPanel(new BorderLayout());
        judul.setOpaque(false);
        judul.add(Tema.kepala("Data Alat", "Daftar alat, stok yang bisa disewa hari ini, dan fotonya"), BorderLayout.WEST);
        judul.add(tombolPanel, BorderLayout.EAST);

        pilihKategori.addItem(SEMUA_KATEGORI);
        for (Kategori kategori : Kategori.values()) {
            pilihKategori.addItem(kategori);
        }
        JButton tombolCari = new JButton("Cari");
        JLabel labelCari = new JLabel("Cari nama/merek:");
        labelCari.setLabelFor(fieldCari);
        JButton tombolCek = new JButton("Cek Ketersediaan per Tanggal");

        JPanel saringan = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        saringan.setOpaque(false);
        saringan.add(labelCari);
        saringan.add(fieldCari);
        saringan.add(pilihKategori);
        saringan.add(tombolCari);
        saringan.add(tombolCek);

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.setOpaque(false);
        atas.add(judul, BorderLayout.NORTH);
        atas.add(saringan, BorderLayout.CENTER);

        JPanel panelFoto = new JPanel(new BorderLayout(0, 8));
        panelFoto.setOpaque(false);
        panelFoto.add(new JLabel("Foto alat"), BorderLayout.NORTH);
        panelFoto.add(kotakFoto, BorderLayout.CENTER);
        labelKeterangan.setForeground(Tema.TEKS_SEKUNDER);
        panelFoto.add(labelKeterangan, BorderLayout.SOUTH);
        JPanel sisiKanan = new JPanel(new BorderLayout());
        sisiKanan.setOpaque(false);
        sisiKanan.add(panelFoto, BorderLayout.NORTH);

        add(atas, BorderLayout.NORTH);
        // lebar relatif kolom; nama alat paling panjang
        int[] lebar = {175, 80, 80, 110, 50, 95, 110};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        Tema.rataKanan(tabel, 3, 4, 5);
        Tema.kolomStatus(tabel, 6);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(sisiKanan, BorderLayout.EAST);

        tabel.getSelectionModel().addListSelectionListener(e -> {
            int baris = tabel.getSelectedRow();
            Alat alat = baris < 0 ? null : daftarAlat.get(baris);
            Tema.tampilkanFoto(kotakFoto, alat == null ? null : alatService.fileFoto(alat));
            // <html> agar keterangan panjang turun baris di bawah foto
            labelKeterangan.setText(alat == null || alat.getKeterangan() == null ? " "
                    : "<html><div style='width:200px'>" + alat.getKeterangan() + "</div></html>");
        });
        tombolCari.addActionListener(e -> muat());
        fieldCari.addActionListener(e -> muat()); // tekan Enter di kolom cari
        pilihKategori.addActionListener(e -> muat());
        tombolCek.addActionListener(e -> new CekKetersediaanDialog(
                SwingUtilities.getWindowAncestor(this), alatService, transaksiService).setVisible(true));
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
        Kategori kategori = pilihKategori.getSelectedItem() instanceof Kategori k ? k : null;
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        daftarAlat = alatService.cari(fieldCari.getText(), kategori);
        for (Alat alat : daftarAlat) {
            model.addRow(new Object[] {alat.getNama(), alat.getKategori(), alat.getMerek(),
                Tema.rupiah(alat.getHargaPerHari()), alat.getStok(),
                transaksiService.stokTersediaHariIni(alat), alat.getStatus()});
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
            try {
                alatService.hapus(alat.getId());
                muat();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Hapus Alat", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
