import java.sql.DriverManager;
import java.sql.Statement;
import java.sql .*;

public class Database {
    private Connection connection;
    private Statement statement;

    public Database(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_people_menu", "people_user", "password_anda");
            statement = connection.createStatement();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    public ResultSet selectQuery(String sql){
        try{
            statement.executeQuery(sql);
            return statement.getResultSet();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    public int insertUpdateDeleteQuery(String sql){
        try{
            return statement.executeUpdate(sql);
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    public Statement getStatement(){
        return statement;
    }
}
