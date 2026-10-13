/**
 * Menyimpan data satu karyawan: id, nama, tanggal lahir, posisi, dan jabatan.
 * Dipakai sebagai wadah data sederhana tanpa logika validasi.
 */
public class Person {
    private String id;
    private String nama;
    // Disimpan sebagai teks berformat yyyy-MM-dd, sama dengan format kolom DATE di database
    private String tanggalLahir;
    private String posisi;
    private String jabatan;

    /**
     * Membuat Person kosong, nilainya diisi lewat setter.
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public Person(){

    }

    /**
     * Membuat Person dengan data lengkap.
     *
     * @param id ID karyawan
     * @param nama nama karyawan
     * @param tanggalLahir tanggal lahir berformat yyyy-MM-dd
     * @param posisi posisi pekerjaan
     * @param jabatan jabatan karyawan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    public Person(String id, String nama, String tanggalLahir, String posisi, String jabatan){
        this.id = id;
        this.nama = nama;
        this.tanggalLahir = tanggalLahir;
        this.posisi = posisi;
        this.jabatan = jabatan;
    }

    /**
     * Mengubah ID karyawan.
     *
     * @param id ID baru
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    void setId(String id){
        this.id = id;
    }

    /**
     * Mengubah nama karyawan.
     *
     * @param nama nama baru
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    void setNama(String nama){
        this.nama = nama;
    }

    /**
     * Mengubah tanggal lahir karyawan.
     *
     * @param tanggalLahir tanggal lahir baru berformat yyyy-MM-dd
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    void setTanggalLahir(String tanggalLahir){
        this.tanggalLahir = tanggalLahir;
    }

    /**
     * Mengubah posisi karyawan.
     *
     * @param posisi posisi baru
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    void setPosisi(String posisi){
        this.posisi = posisi;
    }

    /**
     * Mengubah jabatan karyawan.
     *
     * @param jabatan jabatan baru
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    void setJabatan(String jabatan){
        this.jabatan = jabatan;
    }

    /**
     * @return ID karyawan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    String getId(){
        return this.id;
    }

    /**
     * @return nama karyawan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    String getNama(){
        return this.nama;
    }

    /**
     * @return tanggal lahir berformat yyyy-MM-dd
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    String getTanggalLahir(){
        return this.tanggalLahir;
    }

    /**
     * @return posisi karyawan
     * Kompleksitas: waktu O(1), memori O(1).
     */
    String getPosisi(){
        return this.posisi;
    }

    /**
     * @return jabatan karyawan
     *
     * Kompleksitas: waktu O(1), memori O(1).
     */
    String getJabatan(){
        return this.jabatan;
    }
}