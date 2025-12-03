package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormJadwalKelas extends JFrame {

    Connection conn;
    PreparedStatement pst;
    ResultSet rs;

    JTable table;
    DefaultTableModel model;

    JTextField txtID, txtNama, txtJam;
    JComboBox<String> cbHari, cbInstruktur;

    JButton btnTambah, btnUpdate, btnHapus, btnClear;

    public static boolean validJam(String jam) {
        return jam.matches("\\d{2}:\\d{2}");
    }

    public FormJadwalKelas() {

        // Koneksi
        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/relasi_gym",
                    "postgres",
                    "12345"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }

        // Setting Frame
        setTitle("Form Jadwal Kelas Gym");
        setSize(900, 600);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ===========================================================
        // PANEL INPUT (FORM)
        // ===========================================================
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1 - ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ID Kelas:"), gbc);

        gbc.gridx = 1;
        txtID = new JTextField(20);
        txtID.setEditable(false);
        panel.add(txtID, gbc);

        // Row 2 - Nama Kelas
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nama Kelas:"), gbc);

        gbc.gridx = 1;
        txtNama = new JTextField(20);
        panel.add(txtNama, gbc);

        // Row 3 - Hari
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Hari:"), gbc);

        gbc.gridx = 1;
        cbHari = new JComboBox<>(new String[]{
            "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        });
        cbHari.setPreferredSize(new Dimension(200, 25));
        panel.add(cbHari, gbc);

        // Row 4 - Jam
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Jam (HH:mm):"), gbc);

        gbc.gridx = 1;
        txtJam = new JTextField(20);
        panel.add(txtJam, gbc);

        // Row 5 - Instruktur
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Instruktur:"), gbc);

        gbc.gridx = 1;
        cbInstruktur = new JComboBox<>();
        cbInstruktur.setPreferredSize(new Dimension(200, 25));
        panel.add(cbInstruktur, gbc);

        add(panel, BorderLayout.NORTH);

        // ===========================================================
        // TABEL DATA
        // ===========================================================
        model = new DefaultTableModel(
                new String[]{"ID", "Nama Kelas", "Hari", "Jam", "Instruktur"}, 0
        );

        table = new JTable(model);
        table.setRowHeight(22);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        add(scroll, BorderLayout.CENTER);

        // ===========================================================
        // PANEL TOMBOL CRUD
        // ===========================================================
        JPanel panelBtn = new JPanel();
        panelBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnTambah = new JButton("Tambah");
        btnUpdate = new JButton("Update");
        btnHapus = new JButton("Hapus");
        btnClear = new JButton("Clear");

        btnTambah.setPreferredSize(new Dimension(120, 30));
        btnUpdate.setPreferredSize(new Dimension(120, 30));
        btnHapus.setPreferredSize(new Dimension(120, 30));
        btnClear.setPreferredSize(new Dimension(120, 30));

        panelBtn.add(btnTambah);
        panelBtn.add(btnUpdate);
        panelBtn.add(btnHapus);
        panelBtn.add(btnClear);

        add(panelBtn, BorderLayout.SOUTH);

        // Load data
        loadInstruktur();
        tampilData();
        generateID();

        // Event CRUD
        btnTambah.addActionListener(e -> simpanData());
        btnUpdate.addActionListener(e -> updateData());
        btnHapus.addActionListener(e -> hapusData());
        btnClear.addActionListener(e -> resetForm());

        table.getSelectionModel().addListSelectionListener(e -> isiForm());
    }

    // ========================= DATABASE FUNCTIONS =============================
    public int getSelectedInstrukturID() {
        return Integer.parseInt(cbInstruktur.getSelectedItem().toString().split(" - ")[0]);
    }

    public void loadInstruktur() {
        try {
            pst = conn.prepareStatement(
                    "SELECT id_instruktur, nama FROM instruktur_gym ORDER BY nama"
            );
            rs = pst.executeQuery();

            while (rs.next()) {
                cbInstruktur.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load instruktur: " + e.getMessage());
        }
    }

    public void generateID() {
        try {
            pst = conn.prepareStatement("SELECT MAX(id_kelas) FROM jadwal_kelas");
            rs = pst.executeQuery();

            if (rs.next() && rs.getString(1) != null) {
                txtID.setText(String.valueOf(rs.getInt(1) + 1));
            } else {
                txtID.setText("1");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal generate ID!");
        }
    }

    public void simpanData() {

        // validasi nama kelas 
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kelas wajib diisi!");
            return;
        }

        //validasi penulisan jam
        if (!validJam(txtJam.getText())) {
            JOptionPane.showMessageDialog(this, "Format jam salah! Contoh: 15:30");
            return;
        }

        // validasi jam yang valid
        String[] parts = txtJam.getText().split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);

        if (hh < 0 || hh > 23 || mm < 0 || mm > 59) {
            JOptionPane.showMessageDialog(this, "Jam tidak valid!");
            return;
        }
        
        try {
            pst = conn.prepareStatement(
                    "INSERT INTO jadwal_kelas (nama_kelas, hari, jam_kelas, id_instruktur) "
                    + "VALUES (?, ?, ?, ?) RETURNING id_kelas"
            );

            pst.setString(1, txtNama.getText());
            pst.setString(2, cbHari.getSelectedItem().toString());
            pst.setString(3, txtJam.getText());
            pst.setInt(4, getSelectedInstrukturID());

            rs = pst.executeQuery();
            if (rs.next()) {
                txtID.setText(rs.getString(1));
            }

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan: " + e.getMessage());
        }
    }

    public void updateData() {
        try {
            pst = conn.prepareStatement(
                    "UPDATE jadwal_kelas SET nama_kelas=?, hari=?, jam_kelas=?, id_instruktur=? WHERE id_kelas=?"
            );

            pst.setString(1, txtNama.getText());
            pst.setString(2, cbHari.getSelectedItem().toString());
            pst.setString(3, txtJam.getText());
            pst.setInt(4, getSelectedInstrukturID());
            pst.setInt(5, Integer.parseInt(txtID.getText()));

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update: " + e.getMessage());
        }
    }

    public void hapusData() {
        try {
            pst = conn.prepareStatement("DELETE FROM jadwal_kelas WHERE id_kelas=?");
            pst.setInt(1, Integer.parseInt(txtID.getText()));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus: " + e.getMessage());
        }
    }

    public void tampilData() {
        model.setRowCount(0);

        try {
            pst = conn.prepareStatement(
                    "SELECT jk.id_kelas, jk.nama_kelas, jk.hari, jk.jam_kelas, ig.nama AS instruktur "
                    + "FROM jadwal_kelas jk LEFT JOIN instruktur_gym ig "
                    + "ON jk.id_instruktur = ig.id_instruktur ORDER BY jk.id_kelas ASC"
            );

            rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_kelas"),
                    rs.getString("nama_kelas"),
                    rs.getString("hari"),
                    rs.getString("jam_kelas"),
                    rs.getString("instruktur")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal tampil: " + e.getMessage());
        }
    }

    public void isiForm() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtID.setText(model.getValueAt(row, 0).toString());
            txtNama.setText(model.getValueAt(row, 1).toString());
            cbHari.setSelectedItem(model.getValueAt(row, 2).toString());
            txtJam.setText(model.getValueAt(row, 3).toString());
        }
    }

    public void resetForm() {
        txtNama.setText("");
        txtJam.setText("");
        cbInstruktur.setSelectedIndex(0);
        generateID();
    }

    public static void main(String[] args) {
        new FormJadwalKelas().setVisible(true);
    }
}
