package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.exception.InsufficientAmountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class persistentTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {

    //database properties
    private static final String DBName = "200084L.db";
    private static final int VERSION = 2;

    //name of transaction table
    private static final String transactionTable = "transactions";

    //columns of transaction table
    private static final String DATE_COL = "date";
    private static final String ACCOUNT_NO_COL = "accountNo";
    private static final String EXPENSE_TYPE_COL = "expenseType";
    private static final String AMOUNT_COL = "amount";

    //date format
    private static final String dateFormat="dd-MM-yyyy";
    private static final String TRANSACTION_ID = "transactionID";


    public persistentTransactionDAO(Context context){
        super(context,DBName,null,VERSION);
        onCreate(this.getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_transaction= "CREATE TABLE IF NOT EXISTS " + transactionTable + " (" +
                DATE_COL + " TEXT," +
                ACCOUNT_NO_COL + " TEXT," +
                EXPENSE_TYPE_COL + " TEXT," +
                AMOUNT_COL + " REAL, "+
                TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)";
        sqLiteDatabase.execSQL(CREATE_TABLE_transaction);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropTableTransaction = "DROP TABLE "+transactionTable;
        sqLiteDatabase.execSQL(dropTableTransaction);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) throws InsufficientAmountException {
        SQLiteDatabase db = this.getWritableDatabase();

        String  currentBalQuery = "SELECT balance FROM "+persistentAccountDAO.accTable+ " WHERE " + persistentAccountDAO.ACCOUNT_NUM_COL + " = " +"'"+accountNo+"'";
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(currentBalQuery,null);
        cursor.moveToFirst();
        double currentBal = cursor.getDouble(0);
        if(expenseType==ExpenseType.EXPENSE && currentBal<amount){
            throw new InsufficientAmountException();
            //When account balance is not sufficient to do the transaction
        }

        //convert date object to a String
        @SuppressLint("SimpleDateFormat") DateFormat df =new SimpleDateFormat(dateFormat);

        String dt =df.format(date);
        String query = "INSERT INTO "+transactionTable+" (" +
                DATE_COL+","+
                ACCOUNT_NO_COL+","+
                EXPENSE_TYPE_COL+","+
                AMOUNT_COL+
                ") VALUES(" +
                "'"+dt +"',"+
                "'"+accountNo +"',"+
                (expenseType == ExpenseType.EXPENSE ? "\"1\"": "\"0\"") +","+
                amount +
                ")";
        db.execSQL(query);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    //get transactions in DESC order by time
    public List<Transaction> getAllTransactionLogs() {
        SQLiteDatabase db =this.getReadableDatabase();
        String query = "SELECT * FROM "+transactionTable + " ORDER BY "+TRANSACTION_ID+" DESC;";
        Cursor cursor = db.rawQuery(query,null);
        List<Transaction> transactionsList=new ArrayList<>();
        while (cursor.moveToNext()) {
            Transaction tempTransaction = null;
            try {
                tempTransaction = new Transaction(
                        new SimpleDateFormat(dateFormat).parse(cursor.getString(0)),
                        cursor.getString(1),
                        (Objects.equals(cursor.getString(2), "1") ? ExpenseType.EXPENSE: ExpenseType.INCOME),
                        cursor.getDouble(3)
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            transactionsList.add(tempTransaction);
        }
        cursor.close();
        db.close();
        return transactionsList;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    //get "limit" number of transactions in DESC order by time
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        SQLiteDatabase db =this.getReadableDatabase();
        String query = "SELECT * FROM "+transactionTable + " ORDER BY "+TRANSACTION_ID+" DESC LIMIT " + limit + ";";
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query,null);
        List<Transaction> transactionsList=new ArrayList<>();

        while (cursor.moveToNext()) {
            Transaction tempTransaction = null;
                try {
                    tempTransaction = new Transaction(
                            new SimpleDateFormat(dateFormat).parse(cursor.getString(0)),
                            cursor.getString(1),
                            (Objects.equals(cursor.getString(2), "1") ? ExpenseType.EXPENSE: ExpenseType.INCOME),
                            cursor.getDouble(3)
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                transactionsList.add(tempTransaction);
        }

        cursor.close();
        return transactionsList;
    }
}
