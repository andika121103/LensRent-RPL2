package com.lensrent.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

/** Semua warna, font, dan komponen yang sering dipakai. Ganti tema cukup di sini. */
public final class Tema {

    // Tema "Kamar Gelap"
    public static final Color HITAM = Color.decode("#141211");
    public static final Color MERAH = Color.decode("#B8241C");
    public static final Color KREM = Color.decode("#F4F1EA");
    public static final Color GARIS = Color.decode("#DDD6C8");
    public static final Color TEKS_SEKUNDER = Color.decode("#6B6358");
    public static final Color PUTIH = Color.WHITE;
    public static final Color ERROR_LATAR = Color.decode("#FBEAEA");
    public static final Color ERROR_TEKS = Color.decode("#8F1F1F");
    public static final Color SUKSES_LATAR = Color.decode("#E3EDE4");
    public static final Color SUKSES_TEKS = Color.decode("#24533A");

    public static final int LEBAR_JENDELA = 1280;
    public static final int TINGGI_JENDELA = 800;
    private static final int LEBAR_FORM = 380;

    private static final String NAMA_FONT = pilihFont();
    private static final NumberFormat FORMAT_RUPIAH = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Tema() {}

    /** Inter jika terpasang, kalau tidak Segoe UI, terakhir SansSerif. */
    private static String pilihFont() {
        List<String> tersedia = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (tersedia.contains("Inter")) {
            return "Inter";
        }
        if (tersedia.contains("Segoe UI")) {
            return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int gaya, int ukuran) {
        return new Font(NAMA_FONT, gaya, ukuran);
    }

    /** Panel gelap di sisi kiri halaman Login dan Daftar. */
    public static JPanel panelMerek() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(HITAM);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 72, 0, 72));

        JLabel logo = new JLabel("LensRent");
        logo.setFont(font(Font.BOLD, 48));
        logo.setForeground(PUTIH);

        JLabel garisMerah = new JLabel();
        garisMerah.setOpaque(true);
        garisMerah.setBackground(MERAH);
        garisMerah.setPreferredSize(new Dimension(64, 4));
        garisMerah.setMaximumSize(new Dimension(64, 4));

        JLabel tagline = new JLabel("Cek alat dari rumah, sewa di toko.");
        tagline.setFont(font(Font.PLAIN, 20));
        tagline.setForeground(GARIS);

        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(16));
        panel.add(garisMerah);
        panel.add(Box.createVerticalStrut(16));
        panel.add(tagline);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /** Kolom form putih. Komponen ditambahkan dari atas ke bawah. */
    public static JPanel kartuForm() {
        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(PUTIH);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GARIS),
                BorderFactory.createEmptyBorder(24, 36, 24, 36)));
        return kartu;
    }

    public static JLabel judul(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(font(Font.BOLD, 28));
        label.setForeground(HITAM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Menambahkan label + field (kolom teks atau dropdown) ke kartu form, dengan jarak di bawahnya. */
    public static void tambahField(JPanel kartu, String teksLabel, JComponent field) {
        JLabel label = new JLabel(teksLabel);
        label.setFont(font(Font.BOLD, 13));
        label.setForeground(TEKS_SEKUNDER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLabelFor(field);

        field.setFont(font(Font.PLAIN, 14));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(LEBAR_FORM, 32));
        field.setPreferredSize(new Dimension(LEBAR_FORM, 32));

        kartu.add(label);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(field);
        kartu.add(Box.createVerticalStrut(8));
    }

    public static JButton tombolUtama(String teks) {
        JButton tombol = new JButton(teks);
        tombol.setFont(font(Font.BOLD, 14));
        tombol.setBackground(MERAH);
        tombol.setForeground(PUTIH);
        tombol.setFocusPainted(false);
        tombol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(LEBAR_FORM, 40));
        tombol.setPreferredSize(new Dimension(LEBAR_FORM, 40));
        return tombol;
    }

    /** Tombol yang tampil seperti tautan teks (tetap bisa dipilih dengan Tab + Enter). */
    public static JButton tombolLink(String teks) {
        JButton tombol = new JButton(teks);
        tombol.setFont(font(Font.PLAIN, 13));
        tombol.setForeground(MERAH);
        tombol.setBorderPainted(false);
        tombol.setContentAreaFilled(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        tombol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tombol;
    }

    /** Label pesan error/sukses. Tersembunyi sampai dipanggil tampilkanError/tampilkanSukses. */
    public static JLabel labelPesan() {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setFont(font(Font.PLAIN, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(LEBAR_FORM, 60));
        label.setVisible(false);
        return label;
    }

    public static void tampilkanError(JLabel label, String pesan) {
        tampilkanPesan(label, pesan, ERROR_LATAR, ERROR_TEKS);
    }

    public static void tampilkanSukses(JLabel label, String pesan) {
        tampilkanPesan(label, pesan, SUKSES_LATAR, SUKSES_TEKS);
    }

    private static void tampilkanPesan(JLabel label, String pesan, Color latar, Color teks) {
        // <html> agar pesan panjang otomatis turun baris
        label.setText("<html>" + pesan + "</html>");
        label.setBackground(latar);
        label.setForeground(teks);
        label.setVisible(true);
    }

    /** Contoh: 150000 menjadi "Rp 150.000". */
    public static String rupiah(long nilai) {
        return "Rp " + FORMAT_RUPIAH.format(nilai);
    }

    /** Contoh: 2026-11-06 menjadi "06-11-2026". */
    public static String teksTanggal(LocalDate tanggal) {
        return FORMAT_TANGGAL.format(tanggal);
    }

    /** Kolom tanggal tanpa library tambahan; isi awal hari ini + tambahanHari. */
    public static JSpinner pilihTanggal(int tambahanHari) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd-MM-yyyy"));
        spinner.setValue(Date.from(LocalDate.now().plusDays(tambahanHari)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return spinner;
    }

    /** Membaca kolom tanggal, termasuk teks yang baru diketik. */
    public static LocalDate nilaiTanggal(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Format tanggal harus dd-MM-yyyy, contoh 25-12-2026.");
        }
        return tanggalTerakhir(spinner);
    }

    /** Nilai valid terakhir, tanpa membaca teks yang masih diketik. */
    public static LocalDate tanggalTerakhir(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Dropdown berisi objek: teks yang tampil ditentukan oleh fungsi teks. */
    public static <T> void teksDropdown(JComboBox<T> dropdown, Function<T, String> teks) {
        DefaultListCellRenderer dasar = new DefaultListCellRenderer();
        dropdown.setRenderer((daftar, nilai, indeks, dipilih, fokus) -> dasar.getListCellRendererComponent(
                daftar, nilai == null ? "" : teks.apply(nilai), indeks, dipilih, fokus));
    }

    /** Tabel hanya-baca: sel tidak bisa diketik langsung, satu baris dipilih sekaligus. */
    public static JTable tabel(String... kolom) {
        DefaultTableModel model = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int indeksKolom) {
                return false;
            }
        };
        JTable tabel = new JTable(model);
        tabel.setFont(font(Font.PLAIN, 14));
        tabel.setRowHeight(32);
        tabel.getTableHeader().setFont(font(Font.BOLD, 13));
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tabel;
    }

    /** Mengganti isi area tengah frame saat menu sidebar dipilih. */
    public static void gantiIsi(JPanel wadah, JComponent isi) {
        wadah.removeAll();
        wadah.add(isi, BorderLayout.CENTER);
        wadah.revalidate();
        wadah.repaint();
    }

    /** Membungkus kartu form agar berada di tengah area berlatar krem. */
    public static JPanel tengahkan(JComponent isi) {
        JPanel latar = new JPanel(new GridBagLayout());
        latar.setBackground(KREM);
        latar.add(isi);
        return latar;
    }
}
