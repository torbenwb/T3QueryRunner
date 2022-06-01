/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryrunner;

import com.mysql.cj.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * QueryRunner takes a list of Queries that are initialized in it's constructor
 * and provides functions that will call the various functions in the QueryJDBC class 
 * which will enable MYSQL queries to be executed. It also has functions to provide the
 * returned data from the Queries. Currently the eventHandlers in QueryFrame call these
 * functions in order to run the Queries.
 */
public class QueryRunner {
    final String queriesPath = "Queries.txt";
    
    public QueryRunner() throws FileNotFoundException {
        this.m_jdbcData = new QueryJDBC();
        m_updateAmount = 0;
        m_queryArray = new ArrayList<>();
        queryNames = new ArrayList<>();
        m_error="";

        // Load Queries loads queries from Queries.txt source file.
        LoadQueries();
        this.m_projectTeamApplication="VeryGoodCars.Com";
    }

    // Loads query data from source file.
    public void LoadQueries() throws FileNotFoundException {
        File f = new File(queriesPath);
        if (f.exists()){
            Scanner readFile = new Scanner(f);
            StringBuilder concat = new StringBuilder();
            String name;
            ArrayList<String> paramNames = new ArrayList<>();
            boolean action = false;

            while (readFile.hasNextLine()){
                String nextLine = readFile.nextLine();


                if (!nextLine.isEmpty()){
                    // Lines starting with [ are query names.
                    if (nextLine.charAt(0) == '[') {
                        name = nextLine.replace("[","");
                        name = name.replace("]","");
                        queryNames.add(name);
                        if (name.toLowerCase().contains("insert")) action = true;
                        else if (name.toLowerCase().contains("update")) action = true;
                        else action = false;
                    }
                    else{
                        if (nextLine.contains("[")){
                            int paramStartIndex = 0, paramEndIndex = 0;

                            for (int i = 0; i < nextLine.length(); i++){
                                if (nextLine.charAt(i) == '[') paramStartIndex = i;
                                if (nextLine.charAt(i) == ']') {
                                    paramEndIndex = i;
                                    String paramName = nextLine.substring(paramStartIndex, paramEndIndex + 1);
                                    paramNames.add(paramName);

                                    String a = nextLine.substring(0, paramStartIndex);
                                    String b = nextLine.substring(paramEndIndex, nextLine.length());
                                    nextLine = a + " ? " + b;
                                    nextLine = nextLine.replaceAll("]","");
                                    //System.out.println(paramName);
                                    //System.out.println(nextLine);
                                    concat.append(nextLine);
                                    break;
                                }
                            }
                        }
                        else{
                            concat.append(nextLine);
                            // End of query
                            if (nextLine.charAt(nextLine.length() - 1) == ';'){

                                if (paramNames.size() > 0){
                                    String[] params = new String[paramNames.size()];
                                    boolean[] likeParams = new boolean[paramNames.size()];

                                    for (int i = 0; i < paramNames.size(); i++){
                                        params[i] = paramNames.get(i);
                                        likeParams[i] = false;
                                        //System.out.println(params[i]);
                                    }
                                    paramNames = new ArrayList<>();
                                    m_queryArray.add(new QueryData(concat.toString(),
                                            params, likeParams,
                                            action,true));
                                    paramNames = new ArrayList<String>();
                                }
                                else{

                                    m_queryArray.add(new QueryData(concat.toString(),
                                            null, null,
                                            action,false));
                                }

                                concat = new StringBuilder();
                            }
                            else{
                                concat.append(" \n");
                            }
                        }

                    }
                }
            }
        }
    }

    public int GetTotalQueries()
    {
        return m_queryArray.size();
    }

    public String GetQueryName(int index){
        return queryNames.get(index);
    }

    public int GetQueryIndex(String name){
        for (int i = 0; i < queryNames.size(); i++){

            if (queryNames.get(i).equals(name)) return i;
        }
        return -1;
    }
    
    public int GetParameterAmtForQuery(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.GetParmAmount();
    }
              
    public String  GetParamText(int queryChoice, int parmnum )
    {
       QueryData e=m_queryArray.get(queryChoice);        
       return e.GetParamText(parmnum); 
    }   

    public String GetQueryText(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.GetQueryString();        
    }
    
    /**
     * Function will return how many rows were updated as a result
     * of the update query
     * @return Returns how many rows were updated
     */
    
    public int GetUpdateAmount()
    {
        return m_updateAmount;
    }
    
    /**
     * Function will return ALL of the Column Headers from the query
     * @return Returns array of column headers
     */
    public String [] GetQueryHeaders()
    {
        return m_jdbcData.GetHeaders();
    }
    
    /**
     * After the query has been run, all of the data has been captured into
     * a multi-dimensional string array which contains all the row's. For each
     * row it also has all the column data. It is in string format
     * @return multi-dimensional array of String data based on the resultset 
     * from the query
     */
    public String[][] GetQueryData()
    {
        return m_jdbcData.GetData();
    }

    public String GetProjectTeamApplication()
    {
        return m_projectTeamApplication;        
    }
    public boolean  isActionQuery (int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.IsQueryAction();
    }
    
    public boolean isParameterQuery(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.IsQueryParm();
    }
    
     
    public boolean ExecuteQuery(int queryChoice, String [] parms)
    {
        boolean bOK = true;
        QueryData e=m_queryArray.get(queryChoice);        
        bOK = m_jdbcData.ExecuteQuery(e.GetQueryString(), parms, e.GetAllLikeParams());
        return bOK;
    }
    
     public boolean ExecuteUpdate(int queryChoice, String [] parms)
    {
        boolean bOK = true;
        QueryData e=m_queryArray.get(queryChoice);        
        bOK = m_jdbcData.ExecuteUpdate(e.GetQueryString(), parms);
        m_updateAmount = m_jdbcData.GetUpdateCount();
        return bOK;
    }   
    
      
    public boolean Connect(String szHost, String szUser, String szPass, String szDatabase)
    {
        boolean bConnect = m_jdbcData.ConnectToDatabase(szHost, szUser, szPass, szDatabase);
        if (bConnect == false)
            m_error = m_jdbcData.GetError();        
        return bConnect;
    }
    
    public boolean Disconnect()
    {
        // Disconnect the JDBCData Object
        boolean bConnect = m_jdbcData.CloseDatabase();
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return true;
    }
    
    public String GetError()
    {
        return m_error;
    }
 
    private QueryJDBC m_jdbcData;
    private String m_error;    
    private String m_projectTeamApplication;
    private ArrayList<QueryData> m_queryArray;
    ArrayList<String> queryNames;
    private int m_updateAmount;
            
    /**
    Console Functionality
     */

    private static void consoleTryConnect(QueryRunner qeryRunner, Scanner input){
        boolean success = false;
        while(!success) {
            String host, user, password, database;
            System.out.print("Please enter host: ");
            host = input.nextLine();
            System.out.print("Please enter user: ");
            user = input.nextLine();
            System.out.print("Please enter password: ");
            password = input.nextLine();
            System.out.print("Please enter database name: ");
            database = input.nextLine();

            if (qeryRunner.Connect(host,user,password,database)){
                System.out.println("Connection Successful");

                success = true;
            }
            else{
                System.out.println("We did not connect");

                success = false;
            }
        }
    }

    private static boolean consoleGetNextAction(QueryRunner queryRunner, Scanner input){
        System.out.println("Please choose a query.");
        System.out.println("0 Quit");
        for (int i = 0; i < queryRunner.queryNames.size(); i++){
            System.out.printf("%d %s\n",i + 1, queryRunner.queryNames.get(i));
        }
        int choice = Integer.parseInt(input.nextLine()) - 1;
        if (choice < 0 || choice >= queryRunner.queryNames.size()) return false;
        QueryData queryData = queryRunner.m_queryArray.get(choice);
        ArrayList<String> paramInput = new ArrayList<>();

        if (queryData.IsQueryAction()){
            System.out.println(queryRunner.queryNames.get(choice));
            queryRunner.ExecuteUpdate(choice, consoleGetParams(input,queryData));
            consoleDisplayUpdate(queryRunner);
        }
        else{
            System.out.println(queryRunner.queryNames.get(choice));
            queryRunner.ExecuteQuery(choice, consoleGetParams(input, queryData));
            consoleDisplayResults(queryRunner);
        }

        return true;
    }

    private static String[] consoleGetParams(Scanner input,
                                         QueryData queryData)
    {
        ArrayList<String> paramInput = new ArrayList<>();

        for (int i = 0; i < queryData.GetParmAmount(); i++){
            System.out.print(queryData.GetParamText(i) + ": ");
            String val = input.nextLine();
            paramInput.add(val);
        }

        String[] arr = new String[paramInput.size()];
        for (int i = 0; i < paramInput.size(); i++){
            arr[i] = paramInput.get(i);
        }
        return arr;
    }

    private static void consoleDisplayResults(QueryRunner queryRunner){
        for (int i = 0; i < queryRunner.GetQueryHeaders().length; i++){
            String s = queryRunner.GetQueryHeaders()[i];
            s = StringUtils.padString(s, 20);
            s = s.substring(0,20);
            System.out.printf("| %s |",s);

        }
        System.out.print("\n");
        for (int r = 0; r < queryRunner.GetQueryData().length; r++){
            for (int c = 0; c < queryRunner.GetQueryData()[r].length; c++){
                try {
                    String s = StringUtils.padString(queryRunner.GetQueryData()[r][c], 20);
                    s = s.substring(0,20);
                    System.out.printf("| %s |", s); }
                catch (NullPointerException n){
                    System.out.print("|");
                    for (int i = 0; i < 22; i++) System.out.print(" ");
                    System.out.print("|");
                }
            }
            System.out.print("\n");
        }
    }

    private static void consoleDisplayUpdate(QueryRunner queryRunner){
        int rowsUpdated = queryRunner.GetUpdateAmount();
        System.out.printf("%d rows affected.\n", rowsUpdated);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {

        final QueryRunner queryrunner = new QueryRunner();
        
        if (args.length == 0)
        {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new AppFrame(queryrunner).setVisible(true);
                }            
            });
        }
        else
        {
            if (args[0].equals ("-console"))
            {
                System.out.println("Welcome to the VeryGoodCars.com Console Application. Please Enjoy.\n");
                Scanner input = new Scanner(System.in);
                boolean run = true;

                consoleTryConnect(queryrunner,input);
                while (run){
                    run = consoleGetNextAction(queryrunner,input);
                }
                System.out.println("Thank you for using the VeryGoodCars.com Console Application. Goodbye.");
                queryrunner.Disconnect(); // Throws exceptions from JDBC

            }
        }
    }    
}
