package src;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {

        setTitle("Aplikasi Manajemen Gym - Main Menu");
        setSize(1000, 800);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int btnWidth = 400;
        int btnHeight = 45;

        int centerX = (1000 - btnWidth) / 2;

        JLabel lblTitle = new JLabel("MENU UTAMA APLIKASI GYM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setBounds((1000 - 500) / 2, 30, 500, 40);
        add(lblTitle);

        JLabel lblSubTitle = new JLabel("Kelompok 4", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Arial", Font.PLAIN, 18));
        lblSubTitle.setBounds((1000 - 500) / 2, 75, 500, 30);
        add(lblSubTitle);

        //member
        JButton btnMember = new JButton("Registrasi Member");
        btnMember.setBounds(centerX, 140, btnWidth, btnHeight);
        add(btnMember);

        //member action
        btnMember.addActionListener(e -> new FormRegistrasiMember().setVisible(true));
        
        // instruktur
        JButton btnInstruktur = new JButton("Kelola Instruktur");
        btnInstruktur.setBounds(centerX, 200, btnWidth, btnHeight);
        add(btnInstruktur);

        // instruktur action
        btnInstruktur.addActionListener(e -> new FormInstruktur().setVisible(true));

        // jadwal kelas
        JButton btnJadwal = new JButton("Kelola Jadwal Kelas");
        btnJadwal.setBounds(centerX, 260, btnWidth, btnHeight);
        add(btnJadwal);

        // jadwal kelas action
        btnJadwal.addActionListener(e -> new FormJadwalKelas().setVisible(true));

        // pendaftaran kelas
        JButton btnPendaftaran = new JButton("Kelola Pendaftaran Kelas");
        btnPendaftaran.setBounds(centerX, 320, btnWidth, btnHeight);
        add(btnPendaftaran);
        
        // pendaftaran kelas action
        btnPendaftaran.addActionListener(e -> new FormPendaftaranKelas().setVisible(true));

        // exit button
        JButton btnExit = new JButton("Keluar");
        btnExit.setBounds(centerX, 380, btnWidth, btnHeight);
        add(btnExit);
        
        // exit action
        btnExit.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        new MainMenu().setVisible(true);
    }
}
