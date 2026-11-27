package com.lensrent.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
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
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Semua warna, font, dan komponen yang sering dipakai. Ganti tema cukup di sini. */
public final class Tema {

    // Warna diambil dari logo LensRent: hitam + oranye-kuning
    public static final Color HITAM = Color.decode("#1A1A1A");
    /** Oranye untuk tombol dan teks: sedikit lebih gelap dari logo agar teks putih di atasnya tetap terbaca (kontras 4,8:1). */
    public static final Color ORANYE = Color.decode("#CC4400");
    /** Oranye logo, hanya untuk hiasan dan teks di atas latar hitam. */
    public static final Color ORANYE_TERANG = Color.decode("#FC5101");
    public static final Color KUNING = Color.decode("#FAA501");
    /** Latar baris tabel yang dipilih. */
    public static final Color ORANYE_MUDA = Color.decode("#FFE4D1");
    public static final Color KREM = Color.decode("#F4F1EA");
    public static final Color GARIS = Color.decode("#DDD6C8");
    public static final Color TEKS_SEKUNDER = Color.decode("#6B6358");
    public static final Color PUTIH = Color.WHITE;
    public static final Color ERROR_LATAR = Color.decode("#FBEAEA");
    public static final Color ERROR_TEKS = Color.decode("#8F1F1F");
    public static final Color HIJAU_LATAR = Color.decode("#E3F2E7");
    public static final Color HIJAU_TEKS = Color.decode("#1E6B37");
    public static final Color ABU_LATAR = Color.decode("#ECE8E0");
    /** Baris tabel genap, sedikit lebih gelap dari putih agar baris mudah diikuti mata. */
    public static final Color BARIS_SELANG = Color.decode("#FAF8F4");

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

    public static final String TAGLINE = "Rent the Gear. Capture the Moment.";

    /** Panel putih di sisi kiri halaman Login: logo + tagline. Putih karena logo berlatar transparan dengan tulisan hitam. */
    public static JPanel panelMerek() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PUTIH);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 56, 0, 56));

        JLabel logo = logo("logo.png", 460, HITAM);

        JLabel tagline = new JLabel(TAGLINE);
        tagline.setFont(font(Font.PLAIN, 20));
        tagline.setForeground(TEKS_SEKUNDER);

        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(tagline);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /** Kolom form putih. Komponen ditambahkan dari atas ke bawah. */
    public static JPanel kartuForm() {
        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(PUTIH);
        kartu.putClientProperty("FlatLaf.style", "arc: 16"); // sudut bulat, sama dengan kartu Dashboard
        kartu.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        return kartu;
    }

    public static JLabel judul(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(font(Font.BOLD, 28));
        label.setForeground(HITAM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Judul halaman dengan keterangan singkat abu-abu di bawahnya. */
    public static JPanel kepala(String teksJudul, String keterangan) {
        JPanel kepala = new JPanel();
        kepala.setLayout(new BoxLayout(kepala, BoxLayout.Y_AXIS));
        kepala.setOpaque(false);
        kepala.add(judul(teksJudul));
        JLabel label = new JLabel(keterangan);
        label.setFont(font(Font.PLAIN, 14));
        label.setForeground(TEKS_SEKUNDER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        kepala.add(Box.createVerticalStrut(2));
        kepala.add(label);
        return kepala;
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
        tombol.setBackground(ORANYE);
        tombol.setForeground(PUTIH);
        tombol.setFocusPainted(false);
        tombol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(LEBAR_FORM, 40));
        tombol.setPreferredSize(new Dimension(LEBAR_FORM, 40));
        return tombol;
    }

    /** Label pesan error/sukses. Tersembunyi sampai dipanggil tampilkanError. */
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
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd-MM-yyyy");
        editor.getFormat().setLenient(false); // 31-02-2026 ditolak, bukan diam-diam jadi 03-03-2026
        spinner.setEditor(editor);
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
        tabel.setRowHeight(38);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setIntercellSpacing(new Dimension(0, 1));
        tabel.getTableHeader().setFont(font(Font.BOLD, 13));
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tabel;
    }

    /** Kolom angka (Rupiah, jumlah) rata kanan agar digitnya sejajar. */
    public static void rataKanan(JTable tabel, int... kolom) {
        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(JLabel.RIGHT);
        for (int k : kolom) {
            tabel.getColumnModel().getColumn(k).setCellRenderer(kanan);
        }
    }

    /** Kolom status tampil sebagai label bulat berwarna: hijau, oranye, merah, atau abu-abu. */
    public static void kolomStatus(JTable tabel, int kolom) {
        tabel.getColumnModel().getColumn(kolom).setCellRenderer(new LabelStatus());
    }

    /** Warna {latar, teks} untuk teks status. */
    static Color[] warnaStatus(String status) {
        if (status.startsWith("Terlambat") || status.equals("Tidak Tersedia")) {
            return new Color[] {ERROR_LATAR, ERROR_TEKS};
        }
        if (status.equals("Tersedia") || status.equals("Dikembalikan")) {
            return new Color[] {HIJAU_LATAR, HIJAU_TEKS};
        }
        if (status.equals("Disewa")) {
            return new Color[] {ORANYE_MUDA, ORANYE};
        }
        return new Color[] {ABU_LATAR, TEKS_SEKUNDER};
    }

    private static final class LabelStatus extends DefaultTableCellRenderer {
        private Color latarLabel = ABU_LATAR;

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean dipilih, boolean fokus,
                                                       int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
            Color[] warna = warnaStatus(String.valueOf(nilai));
            latarLabel = warna[0];
            setForeground(warna[1]);
            setFont(font(Font.BOLD, 12));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics ukuran = g2.getFontMetrics(getFont());
            int tinggi = 24;
            int lebar = Math.min(getWidth() - 12, ukuran.stringWidth(getText()) + 20);
            int y = (getHeight() - tinggi) / 2;
            g2.setColor(latarLabel);
            g2.fillRoundRect(8, y, lebar, tinggi, tinggi, tinggi);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            g2.drawString(getText(), 18, y + (tinggi - ukuran.getHeight()) / 2 + ukuran.getAscent());
            g2.dispose();
        }
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

    /** Kotak foto alat berukuran tetap, isinya diatur lewat tampilkanFoto. */
    public static JLabel kotakFoto(int lebar, int tinggi) {
        JLabel label = new JLabel("", JLabel.CENTER);
        label.setPreferredSize(new Dimension(lebar, tinggi));
        label.setMinimumSize(new Dimension(lebar, tinggi));
        label.setMaximumSize(new Dimension(lebar, tinggi));
        label.setOpaque(true);
        label.setBackground(PUTIH);
        label.setForeground(TEKS_SEKUNDER);
        label.setBorder(BorderFactory.createLineBorder(GARIS));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Foto diperkecil agar muat di kotak tanpa mengubah perbandingan sisi. file null = belum ada foto. */
    public static void tampilkanFoto(JLabel kotak, Path file) {
        kotak.setIcon(null);
        kotak.setText("Belum ada foto");
        if (file == null) {
            return;
        }
        try {
            BufferedImage gambar = ImageIO.read(file.toFile());
            if (gambar == null) {
                throw new IOException("bukan gambar");
            }
            Dimension ukuran = kotak.getPreferredSize();
            double skala = Math.min((ukuran.width - 8.0) / gambar.getWidth(), (ukuran.height - 8.0) / gambar.getHeight());
            int lebar = Math.max(1, (int) (gambar.getWidth() * skala));
            int tinggi = Math.max(1, (int) (gambar.getHeight() * skala));
            kotak.setIcon(new ImageIcon(gambar.getScaledInstance(lebar, tinggi, Image.SCALE_SMOOTH)));
            kotak.setText(null);
        } catch (IOException e) {
            kotak.setText("Foto tidak bisa dibuka");
        }
    }

    /**
     * Logo dari /gambar/namaFile selebar lebar piksel. Jika file tidak ada, nama toko tampil sebagai teks berwarna warnaTeks.
     * logo.png untuk latar terang, logo-putih.png (tulisan putih) untuk latar gelap.
     */
    public static JLabel logo(String namaFile, int lebar, Color warnaTeks) {
        java.net.URL file = Tema.class.getResource("/gambar/" + namaFile);
        if (file == null) {
            JLabel teks = new JLabel("LensRent");
            teks.setFont(font(Font.BOLD, 32));
            teks.setForeground(warnaTeks);
            return teks;
        }
        ImageIcon gambar = new ImageIcon(file);
        int tinggi = gambar.getIconHeight() * lebar / gambar.getIconWidth();
        JLabel logo = new JLabel(new ImageIcon(gambar.getImage().getScaledInstance(lebar, tinggi, Image.SCALE_SMOOTH)));
        logo.getAccessibleContext().setAccessibleName("LensRent");
        return logo;
    }

    /** Dipanggil sekali sebelum jendela pertama dibuat: FlatLaf dengan warna aksen oranye logo. */
    public static void pasang() {
        FlatLaf.setGlobalExtraDefaults(java.util.Map.of("@accentColor", "#CC4400"));
        FlatLightLaf.setup();
        UIManager.put("defaultFont", font(Font.PLAIN, 14));
        // sudut membulat dan tabel yang lebih lega
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollPane.arc", 12);
        UIManager.put("Component.borderColor", GARIS);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.alternateRowColor", BARIS_SELANG);
        UIManager.put("Table.gridColor", Color.decode("#EEE9DF"));
        UIManager.put("Table.cellMargins", new java.awt.Insets(0, 6, 0, 6));
        UIManager.put("TableHeader.background", Color.decode("#F1ECE3"));
        UIManager.put("TableHeader.foreground", HITAM);
        UIManager.put("TableHeader.height", 40);
        UIManager.put("TableHeader.separatorColor", GARIS);
        UIManager.put("TableHeader.bottomSeparatorColor", GARIS);
        UIManager.put("Table.selectionBackground", ORANYE_MUDA);
        UIManager.put("Table.selectionForeground", HITAM);
        UIManager.put("Table.selectionInactiveBackground", ORANYE_MUDA);
        UIManager.put("Table.selectionInactiveForeground", HITAM);
    }
}
