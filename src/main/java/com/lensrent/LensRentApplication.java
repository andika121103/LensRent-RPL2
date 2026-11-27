package com.lensrent;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import com.lensrent.ui.LoginFrame;
import com.lensrent.ui.Tema;
import javax.swing.SwingUtilities;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class LensRentApplication {

    public static void main(String[] args) {
        // headless(false) harus diset di sini; Spring menentukan mode headless sebelum membaca application.properties
        ConfigurableApplicationContext context = new SpringApplicationBuilder(LensRentApplication.class)
                .headless(false)
                .run(args);

        AuthService authService = context.getBean(AuthService.class);
        AlatService alatService = context.getBean(AlatService.class);
        TransaksiService transaksiService = context.getBean(TransaksiService.class);
        PembayaranService pembayaranService = context.getBean(PembayaranService.class);

        // Semua kode Swing dijalankan di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            Tema.pasang();
            new LoginFrame(authService, alatService, transaksiService, pembayaranService).setVisible(true);
        });
    }
}
