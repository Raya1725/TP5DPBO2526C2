import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Jendela utama untuk mengelola data karyawan (tambah, ubah, hapus) lewat form dan tabel.
 * Satu tombol dipakai untuk tambah dan ubah, mode ditentukan oleh selectedIndex.
 */
public class PeopleMenu extends JFrame {
    private JButton deleteButton;
    private JButton cancelButton;
    private JButton updateButton;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JComboBox<String> posisiComboBox;
    private JComboBox<String> jabatanComboBox;
    private JTable personTable;
    // -1 berarti tidak ada baris yang dipilih (mode tambah data)
    private int selectedIndex = -1;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tanggalLahirField;
    private Database database;
    // STRICT agar tanggal yang tidak ada di kalender (2007-02-31) ditolak, bukan dibulatkan otomatis.
    // Pola "uuuu" dipakai karena "yyyy" (year-of-era) gagal diparse pada mode STRICT.
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    /**
     * Titik masuk program: membuat jendela lalu menampilkannya.
     *
     * @param args argumen command line (tidak dipakai)
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public static void main(String[] args){
        PeopleMenu menu = new PeopleMenu();
        menu.setSize(700, 600);
        // Menampilkan window di tengah layar
        menu.setLocationRelativeTo(null);
        // mainPanel dibuat oleh GUI Designer, sehingga harus dipasang secara manual
        menu.setContentPane(menu.mainPanel);
        menu.getContentPane().setBackground(Color.WHITE);
        menu.setVisible(true);
        // Menghentikan proses JVM saat window ditutup
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Membangun TableModel baru dari isi tabel employee_data di database.
     * Model dibuat ulang karena JTable perlu model baru agar tampilan
     * sinkron dengan database setelah tambah, ubah, atau hapus.
     *
     * @return DefaultTableModel berisi satu baris untuk setiap karyawan
     *
     * Kompleksitas: waktu O(n), memori O(n), dengan n = jumlah baris di database.
     */
    public final DefaultTableModel setTable(){
        Object[] cols = {"Id Karyawan", "Nama", "Tanggal Lahir", "posisi", "Jabatan"};
        // Parameter pertama null karena baris diisi lewat addRow di bawah
        DefaultTableModel temp = new DefaultTableModel(null, cols);
        try{
            ResultSet resultSet = database.selectQuery("SELECT * FROM employee_data");
            while(resultSet.next()){
                // Nama kolom harus sama persis dengan nama kolom di database
                Object[] row = new Object[5];
                row[0] = resultSet.getString("id");
                row[1] = resultSet.getString("nama");
                row[2] = resultSet.getString("tanggal_lahir");
                row[3] = resultSet.getString("posisi");
                row[4] = resultSet.getString("jabatan");
                temp.addRow(row);
            }
        } catch(SQLException e) {
            // Dilempar ulang agar kegagalan membaca database tidak diam-diam menghasilkan tabel kosong
            throw new RuntimeException(e);
        }
        return temp;
    }

    /**
     * Memeriksa apakah teks adalah tanggal lahir yang valid.
     *
     * @param tanggalLahir teks dengan format yyyy-MM-dd
     * @return true jika formatnya benar, tanggalnya ada di kalender, dan tidak di masa depan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public boolean isTanggalLahirValid(String tanggalLahir) {
        try {
            LocalDate tanggal = LocalDate.parse(tanggalLahir, FORMAT_TANGGAL);
            // Tanggal lahir di masa depan tidak masuk akal
            return !tanggal.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            // Format salah atau tanggal tidak ada: dianggap tidak valid, pesan ditampilkan oleh pemanggil
            return false;
        }
    }

    /**
     * Mengembalikan form ke kondisi awal (mode tambah data).
     * Dipanggil setelah operasi selesai atau saat pengguna menekan Cancel.
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public void clearForm(){
        // ID dikunci saat mode ubah, jadi harus dibuka lagi agar mode tambah bisa mengetik ID
        idField.setEditable(true);
        idField.setText("");
        namaField.setText("");
        tanggalLahirField.setText("");
        // Mengembalikan pilihan combo box ke item awal
        posisiComboBox.setSelectedItem("Choose");
        jabatanComboBox.setSelectedItem("Choose");

        // Teks tombol menyesuaikan mode: "Add" untuk tambah, "Update" untuk ubah
        updateButton.setText("Add");
        // Tombol hapus hanya bermakna jika ada baris yang dipilih
        deleteButton.setVisible(false);
        // Kembali ke mode tambah agar klik tombol berikutnya memanggil insertData
        selectedIndex = -1;
    }

    /**
     * Memeriksa apakah sebuah ID sudah ada di tabel employee_data.
     *
     * @param id ID yang akan diperiksa
     * @return 1 jika ID sudah ada, 0 jika belum
     *
     * Kompleksitas: waktu O(log n) jika kolom id berindeks (primary key), memori O(1).
     */
    public int datachecking(String id){
        String sqlQuery = "SELECT COUNT(*) FROM employee_data WHERE id = '" + id + "'";
        try{
            ResultSet resultSet = database.selectQuery(sqlQuery);
            // COUNT(*) selalu menghasilkan tepat satu baris
            resultSet.next();
            return resultSet.getInt(1) > 0 ? 1 : 0;
        } catch(SQLException e) {
            // Dilempar ulang agar kegagalan tidak diam-diam dianggap "ID belum ada"
            throw new IllegalStateException("Gagal memeriksa ID: " + id, e);
        }
    }

    /**
     * Memvalidasi form lalu menambahkan data karyawan ke database.
     * Validasi berurutan; pada kegagalan pertama form dibiarkan terbuka agar
     * pengguna cukup memperbaiki isian lalu menekan tombol lagi.
     *
     * Kompleksitas: waktu O(n) karena tabel dibangun ulang dari database
     * (pengecekan ID O(log n)), memori O(n).
     */
    public void insertData() {
        String id = idField.getText().trim();

        if(id.isEmpty()){
            JOptionPane.showMessageDialog(null, "id tidak boleh kosong");
        }
        // startsWith aman untuk ID kosong, tidak seperti charAt(0) yang melempar exception
        else if (!id.startsWith("P")) {
            JOptionPane.showMessageDialog(null, "id harus di awali dengan P...!!");
        }
        // Dicek sebelum INSERT agar pengguna mendapat pesan jelas, bukan error primary key dari database
        else if (datachecking(id) == 1) {
            JOptionPane.showMessageDialog(null, "id sudah ada, masukan id lain!!");
        }
        // Indeks 0 adalah placeholder "Choose", jadi bukan pilihan yang valid
        else if (posisiComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih posisi dengan benar");
        }
        else if (jabatanComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih jabatan dengan benar");
        }
        else {
            // trim agar isian berisi spasi saja terbaca kosong
            String nama = namaField.getText().trim();
            String tanggalLahir = tanggalLahirField.getText().trim();
            String posisi = posisiComboBox.getSelectedItem().toString();
            String jabatan = jabatanComboBox.getSelectedItem().toString();

            if(nama.isEmpty()){
                JOptionPane.showMessageDialog(null, "nama tidak boleh kosong");
            }
            else if(tanggalLahir.isEmpty()){
                JOptionPane.showMessageDialog(null, "tanggal lahir tidak boleh kosong");
            }

            else if(!isTanggalLahirValid(tanggalLahir)){
                JOptionPane.showMessageDialog(null, "tanggal lahir harus valid dengan format yyyy-MM-dd");
            }

            else{
                // Kolom ditulis eksplisit agar tidak bergantung pada urutan kolom di tabel
                String sqlQuery = "INSERT INTO employee_data (id, nama, tanggal_lahir, posisi, jabatan) VALUES('"
                        + id + "', '" + nama + "', '" + tanggalLahir + "', '" + posisi + "', '" + jabatan + "')";
                database.insertUpdateDeleteQuery(sqlQuery);

                // Model diganti agar baris baru muncul di tabel
                personTable.setModel(setTable());

                clearForm();
                JOptionPane.showMessageDialog(null, "Add Data succeed");
            }
        }
    }

    /**
     * Memvalidasi form lalu mengubah data karyawan pada baris terpilih (selectedIndex) di database.
     * Hanya dipanggil saat selectedIndex valid (bukan -1).
     *
     * Kompleksitas: waktu O(n) karena tabel dibangun ulang dari database, memori O(n).
     */
    public void updateData() {
        if (posisiComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih posisi dengan benar");
        }
        else if (jabatanComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih jabatan dengan benar");
        }
        else {
            // trim agar isian berisi spasi saja terbaca kosong
            String nama = namaField.getText().trim();
            String tanggalLahir = tanggalLahirField.getText().trim();
            if(nama.isEmpty()){
                JOptionPane.showMessageDialog(null, "nama tidak boleh kosong");
            }
            else if(tanggalLahir.isEmpty()){
                JOptionPane.showMessageDialog(null, "tanggal lahir tidak boleh kosong");
            }

            else if(!isTanggalLahirValid(tanggalLahir)){
                JOptionPane.showMessageDialog(null, "tanggal lahir harus valid dengan format yyyy-MM-dd");
            }

            else{
                // ID tidak bisa diubah di form, jadi ID baris terpilih dipakai sebagai kunci WHERE
                String id = personTable.getModel().getValueAt(selectedIndex, 0).toString();

                // WHERE wajib ada, tanpa itu seluruh baris di tabel ikut berubah
                String sqlQuery = "UPDATE employee_data set nama = '"+ nama +"', tanggal_lahir = '"+ tanggalLahir +"', " +
                        "posisi = '"+ posisiComboBox.getSelectedItem().toString() + "', jabatan = '"+ jabatanComboBox.getSelectedItem().toString() +"' WHERE id = '"+ id +"'";
                database.insertUpdateDeleteQuery(sqlQuery);


                // Model diganti agar perubahan terlihat di tabel
                personTable.setModel(setTable());

                clearForm();
                JOptionPane.showMessageDialog(null, "Update Data Succeed");

            }
        }
    }

    /**
     * Menghapus karyawan pada baris terpilih (selectedIndex) dari database, lalu menyegarkan tabel.
     * Konfirmasi pengguna ditangani oleh pemanggil, bukan di method ini.
     *
     * Kompleksitas: waktu O(n) karena tabel dibangun ulang dari database, memori O(n).
     */
    public void deleteData(){
        // ID diambil dari tabel karena data tidak lagi disimpan di list
        String id = personTable.getModel().getValueAt(selectedIndex, 0).toString();
        String sqlQuery = "DELETE FROM employee_data WHERE id = '"+ id +"' ";
        database.insertUpdateDeleteQuery(sqlQuery);
        personTable.setModel(setTable());
        // clearForm juga mereset selectedIndex agar tidak menunjuk data yang sudah terhapus
        clearForm();
        JOptionPane.showMessageDialog(null, "Delete Data Succeed");

    }

    /**
     * Menyiapkan koneksi database, tabel awal, isi combo box, tampilan awal, dan seluruh event listener.
     *
     * Kompleksitas: waktu O(n) karena tabel dibangun dari isi database, memori O(n).
     */
    public PeopleMenu(){
        database = new Database();
        // Tabel diisi di awal supaya data dari database langsung tampil
        personTable.setModel(setTable());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        // Item "Choose" berfungsi sebagai placeholder pilihan awal
        String[] posisiData = {"Choose", "Electrical Engineer", "Software Architect", "English Translator", "HRD", "CLown"};
        String[] jabatanData = {"Choose", "Leader", "Manager", "Super Visor", "Staff", "Part time"};
        posisiComboBox.setModel(new DefaultComboBoxModel<>(posisiData));
        jabatanComboBox.setModel(new DefaultComboBoxModel<>(jabatanData));
        updateButton.setText("Add");
        // Awalnya tidak ada baris terpilih, sehingga tombol hapus disembunyikan
        deleteButton.setVisible(false);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Satu tombol dipakai untuk dua mode, ditentukan oleh ada tidaknya baris terpilih
                if(selectedIndex == -1){
                    insertData();
                }
                else{
                    updateData();
                }
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Meminta konfirmasi karena penghapusan tidak dapat dibatalkan
                int choice = JOptionPane.showConfirmDialog(
                        deleteButton,
                        "are you sure?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if(choice == JOptionPane.YES_OPTION){
                    deleteData();
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Membatalkan pilihan baris dan mengosongkan form
                clearForm();
            }
        });
        personTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ID dikunci karena menjadi kunci WHERE pada update dan delete
                idField.setEditable(false);
                // Baris yang diklik menjadi target operasi update atau delete
                selectedIndex = personTable.getSelectedRow();
                // Kolom 0 adalah ID, kolom 1 sampai 4 adalah nama, tanggal lahir, posisi, jabatan
                String id = personTable.getModel().getValueAt(selectedIndex, 0).toString();
                String nama = personTable.getModel().getValueAt(selectedIndex, 1).toString();
                String tanggalLahir = personTable.getModel().getValueAt(selectedIndex, 2).toString();
                String posisi = personTable.getModel().getValueAt(selectedIndex, 3).toString();
                String jabatan = personTable.getModel().getValueAt(selectedIndex, 4).toString();

                // Menampilkan data baris terpilih di form agar dapat diedit
                idField.setText(id);
                namaField.setText(nama);
                tanggalLahirField.setText(tanggalLahir);
                posisiComboBox.setSelectedItem(posisi);
                jabatanComboBox.setSelectedItem(jabatan);

                // Beralih ke mode ubah
                updateButton.setText("Update");
                deleteButton.setVisible(true);
            }
        });
    }
}