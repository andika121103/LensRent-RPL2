package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.model.StatusAlat;
import com.lensrent.service.AlatService;
import java.awt.Window;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
                        (StatusAlat) pilihStatus.getSelectedItem(), fieldKeterangan.getText());
                dispose();
            } catch (IllegalArgumentException ex) {
                Tema.tampilkanError(labelPesan, ex.getMessage());
                pack(); // dialog membesar agar pesan error terlihat
            }
        });

        setContentPane(kartu);
        getRootPane().setDefaultButton(tombolSimpan); // tekan Enter = Simpan
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);
    }
}
