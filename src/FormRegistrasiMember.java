package src;

import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.swing.*;

public class FormRegistrasiMember {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Form Registrasi Member Gym");
        frame.setSize(420, 380);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Label nama
        JLabel lblNama = new JLabel("Nama Member:");
        lblNama.setBounds(20, 20, 120, 25);
        frame.add(lblNama);

        // input nama
        JTextField txtNama = new JTextField();
        txtNama.setBounds(150, 20, 230, 25);
        frame.add(txtNama);

        // Label Usia
        JLabel lblUsia = new JLabel("Usia:");
        lblUsia.setBounds(20, 60, 120, 25);
        frame.add(lblUsia);

        // input usia
        JTextField txtUsia = new JTextField();
        txtUsia.setBounds(150, 60, 230, 25);
        frame.add(txtUsia);

        // label alamat
        JLabel lblAlamat = new JLabel("Alamat:");
        lblAlamat.setBounds(20, 100, 120, 25);
        frame.add(lblAlamat);

        // input alamat
        JTextField txtAlamat = new JTextField();
        txtAlamat.setBounds(150, 100, 230, 25);
        frame.add(txtAlamat);

        // label no_telp
        JLabel lblTelp = new JLabel("No. Telp:");
        lblTelp.setBounds(20, 140, 120, 25);
        frame.add(lblTelp);

        // input no_telp
        JTextField txtTelp = new JTextField();
        txtTelp.setBounds(150, 140, 230, 25); // width, x, y, height
        frame.add(txtTelp);

        // tombol simpan
        JButton btnSimpan = new JButton("Daftar Member");
        btnSimpan.setBounds(20, 180, 170, 30);
        frame.add(btnSimpan);

        // tombol reset
        JButton btnReset = new JButton("Reset");
        btnReset.setBounds(210, 180, 170, 30);
        frame.add(btnReset);

        // textarea untuk hasil
        JTextArea areaHasil = new JTextArea();
        areaHasil.setBounds(20, 240, 360, 120);
        areaHasil.setEditable(false);
        frame.add(areaHasil);

        // event simpan ke mysql
        btnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nama = txtNama.getText().trim();
                String usiaStr = txtUsia.getText().trim();
                String alamat = txtAlamat.getText().trim();
                String no_telp = txtTelp.getText().trim();

                // validasi input kosong
                if (nama.isEmpty() || usiaStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Nama dan usia wajib diisi!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // validasi usia harus angka
                int usia;
                try {
                    usia = Integer.parseInt(usiaStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "USia harus berupa angka!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // koneksi ke MySQL
                    Connection conn = DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/relasi_gym",
                            "postgres", // username default
                            "12345" // isi dengan password postgre kamu
                    );

                    // query insert
                    String sql = "INSERT INTO member_gym (nama, usia, alamat, no_telp)"
                            + "VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nama);
                    stmt.setInt(2, usia);
                    stmt.setString(3, alamat);
                    stmt.setString(4, no_telp);
                    stmt.executeUpdate();
                    conn.close();

                    // tampilkan hasil
                    areaHasil.setText("REGISTRASI BERHASIL:\n");
                    areaHasil.append("Nama     : " + nama + "\n");
                    areaHasil.append("Usia     : " + usia + "\n");
                    areaHasil.append("Alamat   : " + alamat + "\n");
                    areaHasil.append("No. Telp : " + no_telp + "\n");
                    JOptionPane.showMessageDialog(frame,
                            "Member gym berhasil didaftarkan!",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Gagal menyimpan ke database!\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // event reset form
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtNama.setText("");
                txtUsia.setText("");
                txtAlamat.setText("");
                txtTelp.setText("");
                areaHasil.setText("");
            }
        });
        frame.setVisible(true);
    }
}
