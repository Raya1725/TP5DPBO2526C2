import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pembungkus koneksi ke database MariaDB db_people_menu.
 * Satu koneksi dan satu Statement dipakai bersama oleh seluruh query.
 */
public class Database {
    private Connection connection;
    private Statement statement;

    /**
     * Membuka koneksi ke database dan menyiapkan Statement.
     * Dilempar ulang sebagai RuntimeException agar kegagalan koneksi tidak diam-diam diabaikan.
     *
     * Kompleksitas: waktu O(1) di sisi Java (selain waktu jaringan), memori O(1).
     */
    public Database(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_people_menu", "people_user", "password_anda");
            statement = connection.createStatement();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Menjalankan query SELECT.
     *
     * @param sql query SELECT lengkap
     * @return hasil query; harus dibaca habis sebelum query berikutnya karena Statement dipakai bersama
     *
     * Kompleksitas: waktu mengikuti query, memori O(1) di sisi Java selain ResultSet.
     */
    public ResultSet selectQuery(String sql){
        try{
            statement.executeQuery(sql);
            return statement.getResultSet();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Menjalankan query INSERT, UPDATE, atau DELETE.
     * executeUpdate dipakai untuk ketiganya, sehingga tidak perlu dibedakan per jenis query.
     *
     * @param sql query INSERT, UPDATE, atau DELETE lengkap
     * @return jumlah baris yang terpengaruh
     *
     * Kompleksitas: waktu mengikuti query, memori O(1).
     */
    public int insertUpdateDeleteQuery(String sql){
        try{
            return statement.executeUpdate(sql);
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Mengembalikan Statement yang dipakai bersama.
     *
     * @return Statement aktif milik koneksi ini
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public Statement getStatement(){
        return statement;
    }
}