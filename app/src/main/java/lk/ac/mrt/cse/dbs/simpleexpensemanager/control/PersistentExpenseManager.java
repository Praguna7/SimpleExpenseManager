package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.Context;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.exception.ExpenseManagerException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.persistentAccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.persistentTransactionDAO;
public class PersistentExpenseManager extends ExpenseManager{
    Context context;
    public PersistentExpenseManager(Context context) throws ExpenseManagerException {
        this.context = context;
        setup();}

    @Override
    public void setup() {
        TransactionDAO persistentTransactionDAO = new persistentTransactionDAO(context);
        setTransactionsDAO(persistentTransactionDAO);

        AccountDAO persistentAccountDAO = new persistentAccountDAO(context);
        setAccountsDAO(persistentAccountDAO);

    }
}
