package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class persistentAccountDAO extends SQLiteOpenHelper implements AccountDAO {
    //database properties
    private static final String DBName = "200084L.db";
    private static final int VERSION = 2;

    //name of account table
    public static final String accTable="accounts";

    //Required column names of
    public static final String BAL_COL = "balance";
    public static final String ACCOUNT_NUM_COL = "accountNum";
    private static final String BANK_NAME_COL = "bankName";
    private static final String ACC_HOLDER_NAME_COL = "accHolderName";

    public persistentAccountDAO(Context context){
        super(context, DBName, null, VERSION);
        onCreate(this.getReadableDatabase());
    }

    @Override
    public List<String> getAccountNumbersList() {
        SQLiteDatabase db =this.getReadableDatabase();
        String query = "SELECT "+ACCOUNT_NUM_COL+" FROM "+accTable;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        List<String> accountNumbers = new ArrayList<>();

        while (cursor.moveToNext()) {
            accountNumbers.add(cursor.getString(0));
        }
        return accountNumbers;
    }

    @Override
    public List<Account> getAccountsList() {
        SQLiteDatabase db =this.getReadableDatabase();
        String query = "SELECT * FROM "+accTable;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        List<Account> accounts = new ArrayList<>();

        if(cursor.moveToFirst()){
            do{
                Account account = new Account(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3)
                );
                accounts.add(account);
            }
            while (cursor.moveToFirst());
        }
        return accounts;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Account account;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+accTable+" WHERE "+ACCOUNT_NUM_COL+" = "+accountNo;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            account = new Account(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3));
        }
        else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

        return account;
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db =this.getWritableDatabase();

        String query = "INSERT INTO accounts(balance,accountNum,bankName,accHolderName) VALUES(" +
                "'"+account.getBalance()+ "'," +
                "'"+account.getAccountNo()+"'," +
                "'"+account.getBankName()+"'," +
                "'"+account.getAccountHolderName()+"'" +
                ")";
        db.execSQL(query);

    }

    @Override
    public void removeAccount(String accountNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteAcc = "DELETE FROM "+accTable+ " WHERE " + ACCOUNT_NUM_COL + "=" +"accountNo";
        db.execSQL(deleteAcc);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        String updateBalQuery ="";
        String  currentBalQuery = "SELECT balance FROM "+accTable+ " WHERE " + ACCOUNT_NUM_COL + " = " +"'"+accountNo+"'";
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(currentBalQuery,null);
        double currentBal;

        if(cursor.moveToFirst()){
            currentBal = cursor.getDouble(0);
        }
        else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

        double newBal;
        switch (expenseType) {
            case EXPENSE:
                newBal= currentBal - amount;
                updateBalQuery ="UPDATE "+accTable+ " SET  " + BAL_COL + "=" + newBal;
                break;
            case INCOME:
                newBal= currentBal + amount;
                updateBalQuery ="UPDATE "+accTable+ " SET  " + BAL_COL + "=" + newBal;
                break;
        }
        db.execSQL(updateBalQuery);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_accounts="CREATE TABLE IF NOT EXISTS accounts(" +
                ACCOUNT_NUM_COL + " TEXT PRIMARY KEY," +
                BANK_NAME_COL+" TEXT," +
                ACC_HOLDER_NAME_COL+" TEXT," +
                BAL_COL + " REAL)";
        sqLiteDatabase.execSQL(CREATE_TABLE_accounts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropTableAccount = "DROP TABLE accounts";
        sqLiteDatabase.execSQL(dropTableAccount);
        onCreate(sqLiteDatabase);
    }
}

