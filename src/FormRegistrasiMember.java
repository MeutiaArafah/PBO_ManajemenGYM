package src;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormRegistrasiMember extends JFrame {

    public FormRegistrasiMember() {

        setTitle("Form Registrasi Member Gym");
        setSize(520, 660);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Label nama
        JLabel lblNama = new JLabel("Nama Member:");
        lblNama.setBounds(20, 20, 120, 25);
        add(lblNama);

        // input nama
        JTextField txtNama = new JTextField();
        txtNama.setBounds(150, 20, 330, 25);
        add(txtNama);

        // Label Usia
        JLabel lblUsia = new JLabel("Usia:");
        lblUsia.setBounds(20, 60, 120, 25);
        add(lblUsia);

        JTextField txtUsia = new JTextField();
        txtUsia.setBounds(150, 60, 330, 25);
        add(txtUsia);

        // label alamat
        JLabel lblAlamat = new JLabel("Alamat:");
        lblAlamat.setBounds(20, 100, 120, 25);
        add(lblAlamat);

        JTextField txtAlamat = new JTextField();
        txtAlamat.setBounds(150, 100, 330, 25);
        add(txtAlamat);

        // label no_telp
        JLabel lblTelp = new JLabel("No. Telp:");
        lblTelp.setBounds(20, 140, 120, 25);
        add(lblTelp);

        JTextField txtTelp = new JTextField();
        txtTelp.setBounds(150, 140, 330, 25);
        add(txtTelp);

        // tombol SIMPAN
        JButton btnSimpan = new JButton("Daftar Member");
        btnSimpan.setBounds(20, 180, 180, 30);
        add(btnSimpan);

        // tombol RESET
        JButton btnReset = new JButton("Reset");
        btnReset.setBounds(210, 180, 120, 30);
        add(btnReset);

        // tombol DELETE
        JButton btnDelete = new JButton("Delete Member");
        btnDelete.setBounds(340, 180, 150, 30);
        add(btnDelete);

        // textarea hasil
        JTextArea areaHasil = new JTextArea();
        JScrollPane scrollArea = new JScrollPane(areaHasil);
        scrollArea.setBounds(20, 230, 460, 120);
        areaHasil.setEditable(false);
        add(scrollArea);

        // TABEL
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Nama", "Usia", "Alamat", "Telp"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBounds(20, 370, 460, 220);
        add(scrollTable);

        JLabel lblTotal = new JLabel("Total Data: 0");
        lblTotal.setBounds(20, 600, 200, 25);
        add(lblTotal);

        // KONEKSI DATABASE
        final Connection[] connHolder = new Connection[1];

        try {
            connHolder[0] = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/relasi_gym",
                    "postgres",
                    "12345"
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Koneksi gagal: " + ex.getMessage());
        }

        Connection conn = connHolder[0];

        // FUNGSI TAMPIL DATA
        Runnable tampilData = () -> {
            model.setRowCount(0);
            try {
                PreparedStatement pst
                        = conn.prepareStatement("SELECT * FROM member_gym ORDER BY id_member ASC");

                ResultSet rs = pst.executeQuery();

                int count = 0;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id_member"),
                        rs.getString("nama"),
                        rs.getInt("usia"),
                        rs.getString("alamat"),
                        rs.getString("no_telp")
                    });
                    count++;
                }
                lblTotal.setText("Total Data: " + count);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal tampil data: " + e.getMessage());
            }
        };

        // EVENT SIMPAN
        btnSimpan.addActionListener(e -> {
            String nama = txtNama.getText().trim();
            String usiaStr = txtUsia.getText().trim();
            String alamat = txtAlamat.getText().trim();
            String no_telp = txtTelp.getText().trim();

            // validasi nama
            if (nama.isEmpty() || usiaStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama dan usia wajib diisi!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO member_gym (nama, usia, alamat, no_telp) VALUES (?, ?, ?, ?)"
                );

                //validasi usia 
                int usia;
                try {
                    usia = Integer.parseInt(usiaStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Usia harus berupa angka!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // validasi usia tidak boleh 0 atau negatif
                if (usia <= 0) {
                    JOptionPane.showMessageDialog(this, "Usia harus lebih dari 0!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // validasi no telp
                if (!no_telp.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "No. Telp harus angka!");
                    return;
                }
                if (no_telp.length() < 10) {
                    JOptionPane.showMessageDialog(this, "No. Telp minimal 10 digit!");
                    return;
                }

                // validasi no telp yang double
                PreparedStatement cekTelp = conn.prepareStatement(
                        "SELECT COUNT(*) FROM member_gym WHERE no_telp=?"
                );
                cekTelp.setString(1, no_telp);
                ResultSet rsTelp = cekTelp.executeQuery();
                rsTelp.next();

                if (rsTelp.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "No. Telp sudah terdaftar!");
                    return;
                }

                pst.setString(1, nama);
                pst.setInt(2, usia);
                pst.setString(3, alamat);
                pst.setString(4, no_telp);
                pst.executeUpdate();

                // tampilkan hasil
                areaHasil.setText("REGISTRASI BERHASIL:\n");
                areaHasil.append("Nama: " + nama + "\n");
                areaHasil.append("Usia: " + usia + "\n");
                areaHasil.append("Alamat: " + alamat + "\n");
                areaHasil.append("Telp: " + no_telp + "\n");

                JOptionPane.showMessageDialog(this, "Member berhasil didaftarkan!");

                tampilData.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal simpan: " + ex.getMessage());
            }
        });

        // EVENT RESET
        btnReset.addActionListener(e -> {
            txtNama.setText("");
            txtUsia.setText("");
            txtAlamat.setText("");
            txtTelp.setText("");
            areaHasil.setText("");
        });

        // EVENT DELETE
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data dulu dari tabel!");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Yakin ingin hapus member ini?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                PreparedStatement pst = conn.prepareStatement(
                        "DELETE FROM member_gym WHERE id_member=?"
                );
                pst.setInt(1, id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Member berhasil dihapus!");
                tampilData.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal hapus: " + ex.getMessage());
            }
        });

        // EVENT KLIK TABEL
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();

                txtNama.setText(model.getValueAt(row, 1).toString());
                txtUsia.setText(model.getValueAt(row, 2).toString());
                txtAlamat.setText(model.getValueAt(row, 3).toString());
                txtTelp.setText(model.getValueAt(row, 4).toString());
            }
        });

        // Load data SETELAH frame tampil agar tidak lama membuka form
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        SwingUtilities.invokeLater(tampilData);

    }
}
