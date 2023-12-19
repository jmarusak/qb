import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TestJDBC 
{
    public static void main(String[] args) {
        try {
            
            String driver = new String();
            String url = new String();  
            String user = new String();
            String password = new String();
            
            user = "jozef.marusak@telus.com";            
            password = "";
 
 
            /*
            driver = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@142.63.132.71:41521:PDWM";
            */

            /*
            driver = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = CAMPPR-scan.corp.ads)(PORT = 41521)) (CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = CAMPPRsva.WORLD)))";
            */

            /*
            driver = "org.apache.hive.jdbc.HiveDriver";
            url = "jdbc:hive2://datalake-gw.oss.ads:8443/default;ssl=true;sslTrustStore=C:/keys/truststore.jks;trustStorePassword=****;transportMode=http;httpPath=gateway/default/hive";
            */
            
            /*
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            url = "jdbc:sqlserver://WP40991.corp.ads\\SQL08PR02:1433;databaseName=VISION";
            */
            
            /*
            driver = "com.ibm.db2.jcc.DB2Driver";
            url = "jdbc:db2://EDWUSR.TSL.TELUS.COM:9680/DSS";
            */
            
            /*
            driver = "com.sas.net.sharenet.ShareNetDriver";
            url = "jdbc:sharenet://sp20634:5013?librefs=bmudata '/sasdata/SASSMB/BMUData';mdm '/sasdata/SASCSP/MDM/sasdata'";
            */
            
            /*
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://NGSANDBOX7:3306/?useSSL=false";
            */
            
            driver = "com.simba.googlebigquery.jdbc.Driver";
            url = "jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=np-deep-ingestion;OAuthType=1;";
            
            
                
            System.out.println("\nConnecting...to "+ url + "\n");

            long stopwatch_start = System.nanoTime();
            
            DriverManager.registerDriver ((Driver)Class.forName(driver).newInstance());
            Connection connection = DriverManager.getConnection(url, user, password);
            
            long stopwatch_stop = System.nanoTime();        
            long stopwatch_diff = (stopwatch_stop - stopwatch_start)/1000000;

            System.out.println("\nConnected Successfully ("+ Long.toString(stopwatch_diff) + " ms).");        
            
            connection.close();
            System.out.println("\nDisconnecting...");   

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}