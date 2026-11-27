package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.model.StatusAlat;
import com.lensrent.service.AlatService;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Form tambah/ubah alat. alat == null berarti tambah alat baru. */
public class FormAlatDialog extends JDialog {

    private final JTextField fieldNama = new JTextField();
    private final JComboBox<Kategori> pilihKategori = new JComboBox<>(Kategori.values());
    private final JTextField fieldMerek = new JTextField();
    private final JTextField fieldHarga = new JTextField();
    private final JTextField fieldStok = new JTextField();
    private final JComboBox<StatusAlat> pilihStatus = new JComboBox<>(StatusAlat.values());
    private final JTextField fieldKeterangan = new JTextField();
    private final JLabel labelPesan = Tema.labelPesan();
    private final JLabel kotakFoto = Tema.kotakFoto(200, 150);
    private Path fotoDipilih; // null = foto tidak diganti

    public FormAlatDialog(Window pemilik, AlatService alatService, Alat alat) {
        super(pemilik, alat == null ? "Tambah Alat" : "Ubah Alat", ModalityType.APPLICATION_MODAL);

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul(alat == null ? "Tambah Alat" : "Ubah Alat"));
        kartu.add(Box.createVerticalStrut(16));

        Tema.tambahField(kartu, "Nama Alat", fieldNama);
        Tema.tambahField(kartu, "Kategori", pilihKategori);
        Tema.tambahField(kartu, "Merek", fieldMerek);
        Tema.tambahField(kartu, "Harga per Hari (Rp)", fieldHarga);
        Tema.tambahField(kartu, "Stok (jumlah unit)", fieldStok);
        Tema.tambahField(kartu, "Status", pilihStatus);
        Tema.tambahField(kartu, "Keterangan (boleh kosong)", fieldKeterangan);

        JLabel labelFoto = new JLabel("Foto (boleh kosong)");
        labelFoto.setFont(Tema.font(Font.BOLD, 13));
        labelFoto.setForeground(Tema.TEKS_SEKUNDER);
        labelFoto.setAlignmentX(LEFT_ALIGNMENT);
        JButton tombolFoto = new JButton("Pilih Foto...");
        tombolFoto.setToolTipText("JPG atau PNG, maksimal 5 MB");
        tombolFoto.setAlignmentX(LEFT_ALIGNMENT);
        labelFoto.setLabelFor(tombolFoto);
        // Foto di kolom kanan agar form tidak terlalu tinggi untuk layar laptop
        JPanel kolomFoto = new JPanel();
        kolomFoto.setLayout(new BoxLayout(kolomFoto, BoxLayout.Y_AXIS));
        kolomFoto.setBackground(Tema.PUTIH);
        kolomFoto.setBorder(BorderFactory.createEmptyBorder(70, 0, 24, 36));
        kolomFoto.add(labelFoto);
        kolomFoto.add(Box.createVerticalStrut(4));
        kolomFoto.add(kotakFoto);
        kolomFoto.add(Box.createVerticalStrut(8));
        kolomFoto.add(tombolFoto);
        Tema.tampilkanFoto(kotakFoto, alat == null ? null : alatService.fileFoto(alat));
        tombolFoto.addActionListener(e -> pilihFoto());

        JButton tombolSimpan = Tema.tombolUtama("Simpan");
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(tombolSimpan);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        if (alat != null) {
            fieldNama.setText(alat.getNama());
            pilihKategori.setSelectedItem(alat.getKategori());
            fieldMerek.setText(alat.getMerek());
            fieldHarga.setText(String.valueOf(alat.getHargaPerHari()));
            fieldStok.setText(String.valueOf(alat.getStok()));
            pilihStatus.setSelectedItem(alat.getStatus());
            fieldKeterangan.setText(alat.getKeterangan());
        }

        Long id = alat == null ? null : alat.getId();
        tombolSimpan.addActionListener(e -> {
            try {
                alatService.simpan(id, fieldNama.getText(), (Kategori) pilihKategori.getSelectedItem(),
                        fieldMerek.getText(), fieldHarga.getText(), fieldStok.getText(),
                        (StatusAlat) pilihStatus.getSelectedItem(), fieldKeterangan.getText(), fotoDipilih);
                dispose();
            } catch (IllegalArgumentException ex) {
                Tema.tampilkanError(labelPesan, ex.getMessage());
                pack(); // dialog membesar agar pesan error terlihat
            }
        });

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(Tema.PUTIH);
        isi.setBorder(BorderFactory.createLineBorder(Tema.GARIS));
        kartu.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
        isi.add(kartu, BorderLayout.CENTER);
        isi.add(kolomFoto, BorderLayout.EAST);
        setContentPane(isi);
        getRootPane().setDefaultButton(tombolSimpan); // tekan Enter = Simpan
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }

    private void pilihFoto() {
        JFileChooser pemilih = new JFileChooser();
        pemilih.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
        if (pemilih.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fotoDipilih = pemilih.getSelectedFile().toPath();
            Tema.tampilkanFoto(kotakFoto, fotoDipilih); // pratinjau; baru disalin saat Simpan
        }
    }
}
