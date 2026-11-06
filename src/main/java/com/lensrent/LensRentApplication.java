package com.lensrent;

import com.formdev.flatlaf.FlatLightLaf;
import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.TransaksiService;
import com.lensrent.ui.LoginFrame;
import com.lensrent.ui.Tema;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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

        // Semua kode Swing dijalankan di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            UIManager.put("defaultFont", Tema.font(java.awt.Font.PLAIN, 14));
            new LoginFrame(authService, alatService, transaksiService).setVisible(true);
        });
    }
}
