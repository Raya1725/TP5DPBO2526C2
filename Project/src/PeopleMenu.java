import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Jendela utama untuk mengelola data Person (tambah, ubah, hapus) lewat form dan tabel.
 * Satu tombol dipakai untuk tambah dan ubah, mode ditentukan oleh selectedIndex.
 */
public class PeopleMenu extends JFrame {
    private JButton deleteButton;
    private JButton cancelButton;
    private JButton updateButton;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private ArrayList<Person> listPerson;
    private JComboBox<String> posisiComboBox;
    private JComboBox<String> jabatanComboBox;
    private JTable personTable;
    // -1 berarti tidak ada baris yang dipilih (mode tambah data)
    private int selectedIndex = -1;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tanggalLahirField;
    private Database database;
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
     * Mengisi listPerson dengan data awal.
     * Dipanggil sekali di constructor agar tabel tidak kosong saat pertama dibuka.
     *
     * Kompleksitas: waktu O(1), memori O(1) (jumlah data awal tetap).
     */
    public void populateList(){
        listPerson.add(new Person("P000", "Renaldy", "2007-02-18", "Software Architect", "Leader"));
        listPerson.add(new Person("P001", "Rizky", "2007-09-13", "Electrical Engineer", "SuperVisor"));
        listPerson.add(new Person("P002", "Noe", "2007-03-17", "English Translator", "Staff"));
        listPerson.add(new Person("P003", "Salman", "2006-11-05", "Software Architect", "Manager"));
        listPerson.add(new Person("P004", "Hilmy", "2007-07-22", "HRD", "Super Visor"));
        listPerson.add(new Person("P005", "Kaisar", "2007-01-30", "Electrical Engineer", "Staff"));
        listPerson.add(new Person("P006", "Dewi", "2006-04-09", "English Translator", "Part time"));
        listPerson.add(new Person("P007", "Bayu", "2007-12-14", "CLown", "Staff"));
        listPerson.add(new Person("P008", "Aulia", "2006-08-27", "HRD", "Leader"));
        listPerson.add(new Person("P009", "Fajar", "2007-06-03", "Software Architect", "Super Visor"));
        listPerson.add(new Person("P010", "Nadia", "2006-10-19", "English Translator", "Manager"));
        listPerson.add(new Person("P011", "Rafi", "2007-02-11", "CLown", "Part time"));
    }

    /**
     * Membangun TableModel baru dari isi listPerson.
     * Model dibuat ulang karena JTable perlu model baru agar tampilan
     * sinkron dengan data setelah tambah, ubah, atau hapus.
     *
     * @return DefaultTableModel berisi satu baris untuk setiap Person
     *
     * Kompleksitas: waktu O(n), memori O(n), dengan n = jumlah Person.
     */
    public final DefaultTableModel setTable(){
        Object[] cols = {"Id Karyawan", "Nama", "Tanggal Lahir", "posisi", "Jabatan"};
        // Parameter pertama null karena baris diisi lewat addRow di bawah
        DefaultTableModel temp = new DefaultTableModel(null, cols);
        try{
            ResultSet resultSet = database.selectQuery("SELECT * FROM employee_data");
            int i = 0;
            while(resultSet.next()){
                Object[] row = new Object[5];
                row[0] = resultSet.getString("id");
                row[1] = resultSet.getString("nama");
                row[2] = resultSet.getString("tanggal_lahir");
                row[3] = resultSet.getString("posisi");
                row[4] = resultSet.getString("jabatan");
                temp.addRow(row);
                i++;
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
        return temp;
    }

    // STRICT agar tanggal yang tidak ada di kalender (31-02-2007) ditolak, bukan dibulatkan otomatis
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Memeriksa apakah teks adalah tanggal lahir yang valid.
     *
     * @param tanggalLahir teks dengan format dd-MM-yyyy
     * @return true jika formatnya benar, tanggalnya ada di kalender, dan tidak di masa depan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public boolean isTanggalLahirValid(String tanggalLahir) {
        try {
            LocalDate tanggal = LocalDate.parse(tanggalLahir, FORMAT_TANGGAL);
            return !tanggal.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            // Format salah atau tanggal tidak ada: dianggap tidak valid, pesan ditampilkan oleh pemanggil
            return false;
        }
    }

    /**
     * Mengembalikan form ke kondisi awal (mode tambah data).
     * Dipanggil setelah operasi selesai atau saat pengguna menekan Cancel.
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public void clearForm(){
        idField.setEditable(true);
        idField.setText("");
        namaField.setText("");
        tanggalLahirField.setText("");
        // Mengembalikan pilihan combo box ke item awal
        posisiComboBox.setSelectedItem("Choose");
        jabatanComboBox.setSelectedItem("Choose");

        // Teks tombol menyesuaikan mode: "add" untuk tambah, "Update" untuk ubah
        updateButton.setText("add");
        // Tombol hapus hanya bermakna jika ada baris yang dipilih
        deleteButton.setVisible(false);
        // Kembali ke mode tambah agar klik tombol berikutnya memanggil insertData
        selectedIndex = -1;
    }

    /**
     * Memeriksa apakah sebuah ID sudah dipakai Person lain di daftar.
     * @param id ID yang akan diperiksa
     * @return 1 jika ID sudah ada, 0 jika belum
     *
     * Kompleksitas: waktu O(n) (pencarian linear), memori O(1).
     */
    public int datachecking(String id){
        int i = 0;
        Person cek;
        Boolean ada = false;
        // Pencarian berhenti saat ID ditemukan (return 1) atau seluruh daftar sudah diperiksa
        while(i != listPerson.size() && ada != true){
            // Data diambil per indeks agar setiap iterasi memeriksa Person berikutnya
            cek = listPerson.get(i);
            if(cek.getId().equals(id)){
                ada = true;
                return 1;
            }
            i++;
        }
        return 0;
    }

    /**
     * Memvalidasi form lalu menambahkan data Person ke daftar.
     * Validasi berurutan; pada kegagalan pertama form dibiarkan terbuka agar
     * pengguna cukup memperbaiki isian lalu menekan tombol lagi.
     *
     * Kompleksitas: waktu O(n) karena datachecking menelusuri daftar dan
     * tabel dibangun ulang, memori O(n).
     */
    public void insertData() {
        String id = idField.getText().trim();

        if(id.isEmpty()){
            JOptionPane.showMessageDialog(null, "id tidak boleh kosong");
            idField.requestFocus();
            idField.selectAll();
        }
        // startsWith aman untuk ID kosong, tidak seperti charAt(0) yang melempar exception
        else if (!id.startsWith("P")) {
            JOptionPane.showMessageDialog(null, "id harus di awali dengan P...!!");
            idField.requestFocus();
            // Teks diblok agar pengguna bisa langsung mengetik ulang tanpa menghapus manual
            idField.selectAll();
        }
        else if (datachecking(id) == 1) {
            JOptionPane.showMessageDialog(null, "id sudah ada, masukan id lain!!");
            idField.requestFocus();
            idField.selectAll();
        }
        // Indeks 0 adalah placeholder "Choose", jadi bukan pilihan yang valid
        else if (posisiComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih posisi dengan benar");
            posisiComboBox.requestFocus();
        }
        else if (jabatanComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih jabatan dengan benar");
            jabatanComboBox.requestFocus();
        }
        else {
            String nama = namaField.getText();
            String tanggalLahir = tanggalLahirField.getText();
            String posisi = posisiComboBox.getSelectedItem().toString();
            String jabatan = jabatanComboBox.getSelectedItem().toString();

            if(nama.isEmpty()){
                JOptionPane.showMessageDialog(null, "nama tidak boleh kosong");
                namaField.requestFocus();
                namaField.selectAll();
            }
            else if(tanggalLahir.isEmpty()){
                JOptionPane.showMessageDialog(null, "tanggal lahir tidak boleh kosong");
                tanggalLahirField.requestFocus();
                tanggalLahirField.selectAll();
            }

            else if(!isTanggalLahirValid(tanggalLahir)){
                JOptionPane.showMessageDialog(null, "tanggal lahir harus valid dengan format yyyy-MM-dd");
                tanggalLahirField.requestFocus();
                tanggalLahirField.selectAll();
            }

            else{
                String sqlQuery = "INSERT INTO employee_data (id, nama, tanggal_lahir, posisi, jabatan) VALUES('"
                        + id + "', '" + nama + "', '" + tanggalLahir + "', '" + posisi + "', '" + jabatan + "')";
                    database.insertUpdateDeleteQuery(sqlQuery);

                listPerson.add(new Person(id, nama, tanggalLahir, posisi, jabatan));

                // Model diganti agar baris baru muncul di tabel
                personTable.setModel(setTable());

                clearForm();
                JOptionPane.showMessageDialog(null, "Add Data succeed");
            }
        }
    }

    /**
     * Memvalidasi form lalu mengubah Person pada baris terpilih (selectedIndex).
     * Hanya dipanggil saat selectedIndex valid (bukan -1).
     *
     * Kompleksitas: waktu O(n) karena datachecking menelusuri daftar dan
     * tabel dibangun ulang, memori O(n).
     */
    public void updateData() {
        Person dipilih = listPerson.get(selectedIndex);

        if (posisiComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih posisi dengan benar");
        }
        else if (jabatanComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "pilih jabatan dengan benar");
        }
        else {
            // Objek Person yang sudah ada diubah langsung, bukan diganti objek baru
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
                String id = dipilih.getId();
                dipilih.setNama(nama);
                dipilih.setTanggalLahir(tanggalLahir);
                dipilih.setPosisi(posisiComboBox.getSelectedItem().toString());
                dipilih.setJabatan(jabatanComboBox.getSelectedItem().toString());

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
     * Menghapus Person pada baris terpilih (selectedIndex), lalu menyegarkan tabel.
     * Konfirmasi pengguna ditangani oleh pemanggil, bukan di method ini.
     *
     * Kompleksitas: waktu O(n) (penggeseran elemen ArrayList dan pembuatan ulang
     * tabel), memori O(n).
     */
    public void deleteData(){
        Person orangHapus = listPerson.get(selectedIndex);
        String sqlQuery = "DELETE FROM employee_data WHERE id = '"+ orangHapus.getId() +"' ";
        database.insertUpdateDeleteQuery(sqlQuery);
        listPerson.remove(selectedIndex);
        personTable.setModel(setTable());
        // clearForm juga mereset selectedIndex agar tidak menunjuk data yang sudah terhapus
        clearForm();
        System.out.println("Delete Succeed");
        JOptionPane.showMessageDialog(null, "Delete Data Succeed");

    }

    /**
     * Menyiapkan data awal, isi combo box, tampilan awal, dan seluruh event listener.
     *
     * Kompleksitas: waktu O(n) karena tabel dibangun dari data awal, memori O(n).
     */
    public PeopleMenu(){
        listPerson = new ArrayList<>();
        database = new Database();
        populateList();
        // Tabel diisi di awal supaya data awal langsung tampil
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
                idField.setEditable(false);
                // Baris yang diklik menjadi target operasi update atau delete
                selectedIndex = personTable.getSelectedRow();
                // Kolom 0 (No) dilewati karena hanya nomor urut tampilan
                String nama = personTable.getModel().getValueAt(selectedIndex, 2).toString();
                String tanggalLahir = personTable.getModel().getValueAt(selectedIndex, 3).toString();
                String posisi = personTable.getModel().getValueAt(selectedIndex, 4).toString();
                String jabatan = personTable.getModel().getValueAt(selectedIndex, 5).toString();

                // Menampilkan data baris terpilih di form agar dapat diedit
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