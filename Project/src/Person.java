 public class Person {
    private String id;
    private  String nama;
    private String tanggalLahir;
    private String posisi;
    private String jabatan;

    public Person(){

    }
    public Person(String id, String nama, String tanggalLahir, String posisi, String jabatan){
        this.id = id;
        this.nama = nama;
        this.tanggalLahir = tanggalLahir;
        this.posisi = posisi;
        this.jabatan = jabatan;
    }

    void setId(String id){
        this.id = id;
    }
    void setNama(String nama){
        this.nama = nama;
    }
    void setTanggalLahir(String tanggalLahir){
        this.tanggalLahir = tanggalLahir;
    }
    void setPosisi(String posisi){
        this.posisi = posisi;
    }
    void setJabatan(String jabatan){
        this.jabatan = jabatan;
    }
    String getId(){
        return this.id;
    }
    String getNama(){
        return this.nama;
    }
    String getTanggalLahir(){
        return  this.tanggalLahir;
    }
    String getPosisi(){
        return this.posisi;
    }
    String getJabatan(){
        return this.jabatan;
    }
}

