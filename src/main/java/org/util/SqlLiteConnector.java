package org.util;


import java.sql.*;


public class SqlLiteConnector  {

//    private final Logger log = LoggerFactory.getLogger(SqlLiteConnector.class);
    public Connection con = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error while loading JDBC-Driver");
            e.printStackTrace();
        }
    }

    public SqlLiteConnector() {
        try {
            getConnection();
//            Statement statement = con.createStatement();
//            for(GeneralTimePeriod table: GeneralTimePeriod.values()){
//               if(statement.execute(createTableStatement(table.toString()))){
//                   //log.debug("Created new table for {}",table);
//               }
//            }
        } catch (SQLException s){
            System.out.println("Failed to connect to sql-lite database: {} "+ s.getMessage());
        }
    }

    /**
     * Tries to establish a valid connection if none existis
     * @throws SQLException SQLException
     */
    private void getConnection() throws SQLException{
        if (con == null){
            String connectionString = String.format("jdbc:sqlite:%s","C:\\SqlLite\\ta4j.db");
            con = DriverManager.getConnection(connectionString);
            //log.debug("Connected to Database {}", connectionString);
        }
    }


}
