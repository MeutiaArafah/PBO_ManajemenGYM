package src;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormInstruktur extends JFrame {

    Connection conn;
    PreparedStatement pst;
    ResultSet rs;

    private JTextField txtId, txtNama, txtUsia, txtKeahlian, txtTelp, txtCari;
    private JTable table;
    private DefaultTableModel model;

    public FormInstruktur() {
        setTitle("Form Data Instruktur Gym");
        setSize(760, 540);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ============================= LABEL =============================
        JLabel lblId = new JLabel("ID Instruktur (Auto)");
        JLabel lblNama = new JLabel("Nama");
        JLabel lblUsia = new JLabel("Usia");
        JLabel lblKeahlian = new JLabel("Keahlian");
        JLabel lblTelp = new JLabel("No. Telepon");

        lblId.setBounds(20, 20, 150, 25);
        lblNama.setBounds(20, 60, 150, 25);
        lblUsia.setBounds(20, 100, 150, 25);
        lblKeahlian.setBounds(20, 140, 150, 25);
        lblTelp.setBounds(20, 180, 150, 25);

        add(lblId); add(lblNama); add(lblUsia);
        add(lblKeahlian); add(lblTelp);

        // ============================= TEXT FIELD =============================
        txtId = new JTextField(); txtId.setEditable(false);
        txtNama = new JTextField();
        txtUsia = new JTextField();
        txtKeahlian = new JTextField();
        txtTelp = new JTextField();
        txtCari = new JTextField();

        txtId.setBounds(180, 20, 200, 25);
        txtNama.setBounds(180, 60, 200, 25);
        txtUsia.setBounds(180, 100, 200, 25);
        txtKeahlian.setBounds(180, 140, 200, 25);
        txtTelp.setBounds(180, 180, 200, 25);
        txtCari.setBounds(20, 220, 200, 25);

        add(txtId); add(txtNama); add(txtUsia);
        add(txtKeahlian); add(txtTelp); add(txtCari);

        // ============================= BUTTONS =============================
        JButton btnSimpan  = new JButton("Simpan");
        JButton btnUpdate  = new JButton("Update");
        JButton btnDelete  = new JButton("Delete");
        JButton btnReset   = new JButton("Reset");
        JButton btnCari    = new JButton("Cari");
        JButton btnRefresh = new JButton("Refresh");

        btnSimpan.setBounds(420, 20, 120, 30);
        btnUpdate.setBounds(420, 60, 120, 30);
        btnDelete.setBounds(420, 100, 120, 30);
        btnReset.setBounds(420, 140, 120, 30);
        btnCari.setBounds(230, 220, 80, 25);
        btnRefresh.setBounds(320, 220, 90, 25);

        add(btnSimpan); add(btnUpdate); add(btnDelete);
        add(btnReset); add(btnCari); add(btnRefresh);

        // ============================= TABLE =============================
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Usia", "Keahlian", "No Telp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // membuat tabel tidak bisa diedit langsung
            }
        };

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 260, 700, 220);
        add(sp);

        // ============================= KONEKSI & LOAD =============================
        koneksiDB();
        tampilData();
        autoID();

        // ============================= EVENT =============================
        btnSimpan.addActionListener(e -> simpanData());
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());
        btnReset.addActionListener(e -> resetForm());
        btnCari.addActionListener(e -> cariData());
        btnRefresh.addActionListener(e -> tampilData());

        table.getSelectionModel().addListSelectionListener(e -> tableKlik());
    }

    // ========================================================================
    // KONEKSI DATABASE
    // ========================================================================
    public void koneksiDB() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:5432/relasi_gym", "postgres", "12345");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.getMessage());
        }
    }

    // ========================================================================
    // AUTO GENERATE ID
    // ========================================================================
    public void autoID() {
        try {
            pst = conn.prepareStatement("SELECT MAX(id_instruktur) FROM instruktur_gym");
            rs = pst.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1) + 1;
                txtId.setText(String.valueOf(id));
            }
        } catch (Exception e) {
            txtId.setText("1");
        }
    }

    // ========================================================================
    // TAMPIL DATA
    // ========================================================================
    public void tampilData() {
        model.setRowCount(0);
        try {
            pst = conn.prepareStatement("SELECT * FROM instruktur_gym ORDER BY id_instruktur ASC");
            rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_instruktur"),
                        rs.getString("nama"),
                        rs.getInt("usia"),
                        rs.getString("keahlian"),
                        rs.getString("no_telp")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Tampil: " + e.getMessage());
        }
    }

    // ========================================================================
    // SIMPAN DATA
    // ========================================================================
    public void simpanData() {

        if (txtNama.getText().isEmpty() || txtUsia.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Usia wajib diisi!");
            return;
        }
        if (!txtUsia.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Usia harus angka!");
            return;
        }
        if (!txtTelp.getText().matches("\\d*")) {
            JOptionPane.showMessageDialog(this, "No Telepon harus angka!");
            return;
        }

        try {
            pst = conn.prepareStatement(
                    "INSERT INTO instruktur_gym (id_instruktur, nama, usia, keahlian, no_telp) VALUES (?, ?, ?, ?, ?)"
            );

            pst.setInt(1, Integer.parseInt(txtId.getText()));
            pst.setString(2, txtNama.getText());
            pst.setInt(3, Integer.parseInt(txtUsia.getText()));
            pst.setString(4, txtKeahlian.getText());
            pst.setString(5, txtTelp.getText());
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }

    // ========================================================================
    // UPDATE DATA
    // ========================================================================
    public void updateData() {

        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
            return;
        }

        if (!txtUsia.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Usia harus angka!");
            return;
        }

        try {
            pst = conn.prepareStatement(
                    "UPDATE instruktur_gym SET nama=?, usia=?, keahlian=?, no_telp=? WHERE id_instruktur=?"
            );

            pst.setString(1, txtNama.getText());
            pst.setInt(2, Integer.parseInt(txtUsia.getText()));
            pst.setString(3, txtKeahlian.getText());
            pst.setString(4, txtTelp.getText());
            pst.setInt(5, Integer.parseInt(txtId.getText()));

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Update: " + e.getMessage());
        }
    }

    // ========================================================================
    // DELETE DATA
    // ========================================================================
    public void deleteData() {

        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel!");
            return;
        }

        try {
            pst = conn.prepareStatement("DELETE FROM instruktur_gym WHERE id_instruktur=?");
            pst.setInt(1, Integer.parseInt(txtId.getText()));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Delete: " + e.getMessage());
        }
    }

    // ========================================================================
    // CARI DATA
    // ========================================================================
    public void cariData() {
        model.setRowCount(0);
        try {
            pst = conn.prepareStatement("SELECT * FROM instruktur_gym WHERE nama LIKE ?");
            pst.setString(1, "%" + txtCari.getText() + "%");

            rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_instruktur"),
                        rs.getString("nama"),
                        rs.getInt("usia"),
                        rs.getString("keahlian"),
                        rs.getString("no_telp")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Cari Data: " + e.getMessage());
        }
    }

    // ========================================================================
    // RESET
    // ========================================================================
    public void resetForm() {
        txtNama.setText("");
        txtUsia.setText("");
        txtKeahlian.setText("");
        txtTelp.setText("");
        txtCari.setText("");
        autoID();
    }

    // ========================================================================
    // PILIH TABEL
    // ========================================================================
    public void tableKlik() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtId.setText(model.getValueAt(row, 0).toString());
            txtNama.setText(model.getValueAt(row, 1).toString());
            txtUsia.setText(model.getValueAt(row, 2).toString());
            txtKeahlian.setText(model.getValueAt(row, 3).toString());
            txtTelp.setText(model.getValueAt(row, 4).toString());
        }
    }

    public static void main(String[] args) {
        new FormInstruktur().setVisible(true);
    }
}
