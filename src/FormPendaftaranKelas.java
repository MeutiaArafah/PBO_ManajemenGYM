package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Color; // <-- PERBAIKAN 1: Menambahkan import untuk Color
import java.awt.Component; // <-- PERBAIKAN 2: Menambahkan import yang dibutuhkan oleh Component

public class FormPendaftaranKelas extends JFrame {

    // --- Inner Class untuk ComboBox Model ---
    class Member {
        int id;
        String nama;
        public Member(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }
        @Override
        public String toString() {
            return nama;
        }
    }
    
    class Kelas {
        int id;
        String nama;
        public Kelas(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }
        @Override
        public String toString() {
            return nama;
        }
    }
    
    // --- Deklarasi Komponen GUI & JDBC ---
    private JTextField txtIdPendaftaran, txtTanggalDaftar;
    private JTextArea txtCatatan;
    private JComboBox<Member> cbMember;
    private JComboBox<Kelas> cbKelas;
    private JButton btnSimpan, btnHapus, btnReset;
    private JTable pendaftaranTable;
    private DefaultTableModel tableModel;
    
    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;
    
    // --- Konstanta Koneksi PostgreSQL ---
    private final String URL = "jdbc:postgresql://localhost:5432/relasi_gym";
    private final String USER = "postgres"; 
    private final String PASSWORD = "12345"; 

    public FormPendaftaranKelas() {
        // --- 1. Pengaturan Frame ---
        setTitle("Form 4 - Pendaftaran Kelas Gym");
        setSize(900, 600);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); 
        koneksiDB(); // Hubungkan ke DB saat form dibuat

        // --- 2. Inisialisasi Komponen Input ---
        
        // ID Pendaftaran (Non-Editable)
        JLabel lblId = new JLabel("ID Pendaftaran:");
        lblId.setBounds(20, 20, 120, 25); add(lblId);
        txtIdPendaftaran = new JTextField();
        txtIdPendaftaran.setBounds(150, 20, 100, 25);
        txtIdPendaftaran.setEditable(false); 
        add(txtIdPendaftaran);

        // Member (JComboBox)
        JLabel lblMember = new JLabel("Member:");
        lblMember.setBounds(20, 50, 120, 25); add(lblMember);
        cbMember = new JComboBox<>();
        cbMember.setBounds(150, 50, 250, 25); add(cbMember);

        // Kelas Gym (JComboBox)
        JLabel lblKelas = new JLabel("Kelas Gym:");
        lblKelas.setBounds(20, 80, 120, 25); add(lblKelas);
        cbKelas = new JComboBox<>();
        cbKelas.setBounds(150, 80, 250, 25); add(cbKelas);
        
        // Tanggal Daftar (JTextField yyyy-MM-dd)
        JLabel lblTglDaftar = new JLabel("Tanggal Daftar (YYYY-MM-DD):");
        lblTglDaftar.setBounds(20, 110, 180, 25); add(lblTglDaftar);
        txtTanggalDaftar = new JTextField();
        txtTanggalDaftar.setBounds(200, 110, 150, 25);
        
        // Set tanggal hari ini sebagai default
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtTanggalDaftar.setText(sdf.format(new Date()));
        add(txtTanggalDaftar);
        
        // Catatan (JTextArea)
        JLabel lblCatatan = new JLabel("Catatan:");
        lblCatatan.setBounds(20, 140, 120, 25); add(lblCatatan);
        txtCatatan = new JTextArea();
        txtCatatan.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // <-- Perbaikan Error Color
        JScrollPane scrollCatatan = new JScrollPane(txtCatatan);
        scrollCatatan.setBounds(150, 140, 250, 60); add(scrollCatatan);
        
        // --- 3. Tombol Aksi ---
        btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(20, 220, 120, 30); add(btnSimpan);

        btnHapus = new JButton("Hapus");
        btnHapus.setBounds(150, 220, 120, 30); add(btnHapus);

        btnReset = new JButton("Reset");
        btnReset.setBounds(280, 220, 120, 30); add(btnReset);

        // --- 4. Tabel (JTable) ---
        String[] columnNames = {"ID Daftar", "Member", "Kelas", "Tanggal Daftar", "Catatan", "ID Member", "ID Kelas"};
        tableModel = new DefaultTableModel(columnNames, 0);
        pendaftaranTable = new JTable(tableModel);
        
        // Sembunyikan Kolom FK
        for (int i = 5; i <= 6; i++) {
            pendaftaranTable.getColumnModel().getColumn(i).setMinWidth(0);
            pendaftaranTable.getColumnModel().getColumn(i).setMaxWidth(0);
            pendaftaranTable.getColumnModel().getColumn(i).setPreferredWidth(0);
        }

        JScrollPane scrollPane = new JScrollPane(pendaftaranTable);
        scrollPane.setBounds(20, 270, 850, 280); 
        add(scrollPane);
        
        // --- 5. Load Data & Event Listener ---
        loadMemberKelasComboBox(); 
        loadDataPendaftaran(); 
        generateID();
        
        // Event JTable: Mengisi form saat baris diklik
        pendaftaranTable.getSelectionModel().addListSelectionListener(e -> tableKlik());
        
        // Event untuk tombol-tombol
        btnSimpan.addActionListener(e -> simpanData());
        btnHapus.addActionListener(e -> hapusData());
        btnReset.addActionListener(e -> resetForm());

        setVisible(true);
    }

    // ========================================================================
    // KONEKSI POSTGRESQL
    // ========================================================================
    public void koneksiDB() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // LOAD COMBOBOX DATA
    // ========================================================================
    private void loadMemberKelasComboBox() {
        cbMember.removeAllItems();
        cbKelas.removeAllItems();
        
        // Load Member
        try {
            pst = conn.prepareStatement("SELECT id_member, nama FROM member_gym ORDER BY nama");
            rs = pst.executeQuery();
            while (rs.next()) {
                cbMember.addItem(new Member(rs.getInt("id_member"), rs.getString("nama")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load Member: " + e.getMessage());
        }

        // Load Kelas
        try {
            pst = conn.prepareStatement("SELECT id_kelas, nama_kelas FROM jadwal_kelas ORDER BY nama_kelas");
            rs = pst.executeQuery();
            while (rs.next()) {
                cbKelas.addItem(new Kelas(rs.getInt("id_kelas"), rs.getString("nama_kelas")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load Kelas: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // GENERATE ID PENDAFTARAN
    // ========================================================================
    public void generateID() {
        try {
            pst = conn.prepareStatement("SELECT MAX(id_pendaftaran) FROM pendaftaran_kelas");
            rs = pst.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                txtIdPendaftaran.setText(String.valueOf(rs.getInt(1) + 1));
            } else {
                txtIdPendaftaran.setText("1");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal generate ID!");
        }
    }

    // ========================================================================
    // SIMPAN DATA
    // ========================================================================
    public void simpanData() {
        if (cbMember.getSelectedItem() == null || cbKelas.getSelectedItem() == null || txtTanggalDaftar.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
            return;
        }

        try {
            int idMember = ((Member) cbMember.getSelectedItem()).id;
            int idKelas = ((Kelas) cbKelas.getSelectedItem()).id;
            
            String sql = "INSERT INTO pendaftaran_kelas (id_member, id_kelas, tanggal_daftar, catatan) VALUES (?, ?, ?, ?) RETURNING id_pendaftaran";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, idMember);
            pst.setInt(2, idKelas);
            pst.setDate(3, java.sql.Date.valueOf(txtTanggalDaftar.getText()));
            pst.setString(4, txtCatatan.getText());

            rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Pendaftaran berhasil disimpan!");
            }

            loadDataPendaftaran();
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }

    // ========================================================================
    // TAMPIL DATA (READ)
    // ========================================================================
    public void loadDataPendaftaran() {
        tableModel.setRowCount(0);

        try {
            String sql = "SELECT p.id_pendaftaran, m.nama AS nama_member, jk.nama_kelas, p.tanggal_daftar, p.catatan, p.id_member, p.id_kelas " +
                         "FROM pendaftaran_kelas p " +
                         "JOIN member_gym m ON p.id_member = m.id_member " +
                         "JOIN jadwal_kelas jk ON p.id_kelas = jk.id_kelas " +
                         "ORDER BY p.id_pendaftaran ASC";
            
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_pendaftaran"),
                    rs.getString("nama_member"),
                    rs.getString("nama_kelas"),
                    rs.getString("tanggal_daftar"),
                    rs.getString("catatan"),
                    rs.getInt("id_member"), // FK tersembunyi
                    rs.getInt("id_kelas")   // FK tersembunyi
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Tampil Data Pendaftaran: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // HAPUS DATA
    // ========================================================================
    public void hapusData() {
        if (txtIdPendaftaran.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data pendaftaran yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus pendaftaran ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pst = conn.prepareStatement("DELETE FROM pendaftaran_kelas WHERE id_pendaftaran=?");
                pst.setInt(1, Integer.parseInt(txtIdPendaftaran.getText()));
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                loadDataPendaftaran();
                resetForm();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // KLIK TABEL
    // ========================================================================
    public void tableKlik() {
        int row = pendaftaranTable.getSelectedRow();
        if (row != -1) {
            txtIdPendaftaran.setText(tableModel.getValueAt(row, 0).toString());
            txtTanggalDaftar.setText(tableModel.getValueAt(row, 3).toString());
            txtCatatan.setText(tableModel.getValueAt(row, 4).toString());
            
            // Set ComboBox Member dan Kelas berdasarkan ID
            int idMemberFK = (int) tableModel.getValueAt(row, 5);
            int idKelasFK = (int) tableModel.getValueAt(row, 6);
            
            setSelectedComboBox(cbMember, idMemberFK);
            setSelectedComboBox(cbKelas, idKelasFK);
        }
    }
    
    // Helper function untuk memilih item ComboBox berdasarkan ID
    private void setSelectedComboBox(JComboBox cb, int idFK) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            Object item = cb.getItemAt(i);
            if (item instanceof Member) {
                if (((Member) item).id == idFK) {
                    cb.setSelectedIndex(i);
                    return;
                }
            } else if (item instanceof Kelas) {
                 if (((Kelas) item).id == idFK) {
                    cb.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    // ========================================================================
    // RESET FORM
    // ========================================================================
    public void resetForm() {
        txtTanggalDaftar.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtCatatan.setText("");
        pendaftaranTable.clearSelection();
        if (cbMember.getItemCount() > 0) cbMember.setSelectedIndex(0);
        if (cbKelas.getItemCount() > 0) cbKelas.setSelectedIndex(0);
        generateID();
    }

    public static void main(String[] args) {
        new FormPendaftaranKelas().setVisible(true);
    }
}