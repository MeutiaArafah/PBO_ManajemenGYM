package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormJadwalKelas extends JFrame {

    // Koneksi database
    Connection conn;
    PreparedStatement pst;
    ResultSet rs;

    // Komponen tabel
    JTable table;
    DefaultTableModel model;

    // Komponen input
    JTextField txtID, txtNama, txtJam;
    JComboBox<String> cbHari, cbInstruktur;

    // Tombol
    JButton btnTambah, btnUpdate, btnHapus, btnClear;

    // Validasi format jam HH:mm
    public static boolean validJam(String jam) {
        return jam.matches("\\d{2}:\\d{2}");
    }

    public FormJadwalKelas() {

        // Membuka koneksi ke PostgreSQL
        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/relasi_gym",
                    "postgres",
                    "12345"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }

        // Setting utama JFrame
        setTitle("Form Jadwal Kelas Gym");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel Input (Form)
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Input ID kelas (otomatis dan tidak bisa diedit)
        panel.add(new JLabel("ID Kelas:"));
        txtID = new JTextField();
        txtID.setEditable(false);
        panel.add(txtID);

        // Input nama kelas
        panel.add(new JLabel("Nama Kelas:"));
        txtNama = new JTextField();
        panel.add(txtNama);

        // Dropdown pilihan hari
        panel.add(new JLabel("Hari:"));
        cbHari = new JComboBox<>(new String[]{
                "Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu"});
        panel.add(cbHari);

        // Input jam
        panel.add(new JLabel("Jam (HH:mm):"));
        txtJam = new JTextField();
        panel.add(txtJam);

        // Dropdown daftar instruktur diambil dari database
        panel.add(new JLabel("Instruktur:"));
        cbInstruktur = new JComboBox<>();
        panel.add(cbInstruktur);

        add(panel, BorderLayout.NORTH);

        // Tabel untuk menampilkan data jadwal kelas
        model = new DefaultTableModel(
                new String[]{"ID", "Nama Kelas", "Hari", "Jam", "Instruktur"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel tombol aksi CRUD
        JPanel panelBtn = new JPanel();
        btnTambah = new JButton("Tambah");
        btnUpdate = new JButton("Update");
        btnHapus = new JButton("Hapus");
        btnClear = new JButton("Clear");

        panelBtn.add(btnTambah);
        panelBtn.add(btnUpdate);
        panelBtn.add(btnHapus);
        panelBtn.add(btnClear);

        add(panelBtn, BorderLayout.SOUTH);

        // Load data awal
        loadInstruktur();
        tampilData();
        generateID();

        // Event listener tombol CRUD
        btnTambah.addActionListener(e -> simpanData());
        btnUpdate.addActionListener(e -> updateData());
        btnHapus.addActionListener(e -> hapusData());
        btnClear.addActionListener(e -> resetForm());

        // Listener saat memilih baris di tabel
        table.getSelectionModel().addListSelectionListener(e -> isiForm());
    }

    // Mengambil ID instruktur dari pilihan combo (format: "id - nama")
    public int getSelectedInstrukturID() {
        return Integer.parseInt(cbInstruktur.getSelectedItem().toString().split(" - ")[0]);
    }

    // Mengambil daftar instruktur dari database
    public void loadInstruktur() {
        try {
            pst = conn.prepareStatement(
                    "SELECT id_instruktur, nama FROM instruktur_gym ORDER BY nama");
            rs = pst.executeQuery();
            while (rs.next()) {
                cbInstruktur.addItem(rs.getString("id_instruktur")
                        + " - " + rs.getString("nama"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load instruktur: " + e.getMessage());
        }
    }

    // Menghasilkan ID kelas otomatis berdasarkan MAX(id)
    public void generateID() {
        try {
            pst = conn.prepareStatement("SELECT MAX(id_kelas) FROM jadwal_kelas");
            rs = pst.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                txtID.setText(String.valueOf(Integer.parseInt(rs.getString(1)) + 1));
            } else {
                txtID.setText("1");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal generate ID!");
        }
    }

    // Menyimpan data ke database
    public void simpanData() {

        // Validasi format jam
        if (!validJam(txtJam.getText())) {
            JOptionPane.showMessageDialog(this, "Format jam harus HH:mm");
            return;
        }

        try {
            pst = conn.prepareStatement(
                    "INSERT INTO jadwal_kelas (nama_kelas, hari, jam_kelas, id_instruktur) " +
                            "VALUES (?, ?, ?, ?) RETURNING id_kelas"
            );

            pst.setString(1, txtNama.getText());
            pst.setString(2, cbHari.getSelectedItem().toString());
            pst.setString(3, txtJam.getText());
            pst.setInt(4, getSelectedInstrukturID());

            rs = pst.executeQuery();
            if (rs.next()) txtID.setText(rs.getString("id_kelas"));

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan: " + e.getMessage());
        }
    }

    // Mengupdate data jadwal kelas
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

            JOptionPane.showMessageDialog(this, "Data diupdate!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update: " + e.getMessage());
        }
    }

    // Menghapus data berdasarkan ID
    public void hapusData() {
        try {
            pst = conn.prepareStatement("DELETE FROM jadwal_kelas WHERE id_kelas=?");
            pst.setInt(1, Integer.parseInt(txtID.getText()));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data dihapus!");
            tampilData();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus: " + e.getMessage());
        }
    }

    // Menampilkan seluruh data tabel dari database
    public void tampilData() {
        model.setRowCount(0);

        try {
            pst = conn.prepareStatement(
                    "SELECT jk.id_kelas, jk.nama_kelas, jk.hari, jk.jam_kelas, ig.nama AS instruktur " +
                            "FROM jadwal_kelas jk " +
                            "LEFT JOIN instruktur_gym ig ON jk.id_instruktur = ig.id_instruktur " +
                            "ORDER BY jk.id_kelas ASC");

            rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("id_kelas"),
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

    // Mengisi form dari baris tabel yang dipilih
    public void isiForm() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtID.setText(model.getValueAt(row, 0).toString());
            txtNama.setText(model.getValueAt(row, 1).toString());
            cbHari.setSelectedItem(model.getValueAt(row, 2).toString());
            txtJam.setText(model.getValueAt(row, 3).toString());
        }
    }

    // Reset form input
    public void resetForm() {
        txtNama.setText("");
        txtJam.setText("");
        cbInstruktur.setSelectedIndex(0);
        generateID();
    }

    // Menjalankan aplikasi
    public static void main(String[] args) {
        new FormJadwalKelas().setVisible(true);
    }
}
