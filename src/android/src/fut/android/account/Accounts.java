package fut.android.account;

//TODO kontrolovat carky v CSV a pripadne uvozovat do uvozovek

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;

/**
 * @author Petr Nohejl
 * @version 1.0
 * @since 1.0 - 22.11.2010
 */
public class Accounts {
	
	public static final String CSV_SEPARATOR = ",";
	public static final String CSV_HEADER = "Account Name,Account Type";
	
	private Account[] accountsList;
	
	
	/**
     * Object with informations about accounts.
     * 
     * @param accountManager
     *            account manager 
     */
	public Accounts(AccountManager accountManager)
	{
		refreshData(accountManager);
	}
	
	
	/**
     * Create CSV file with accounts.<br>
     * Account list including name and type.
     *
     * @param directoryPath
     *            directory path of CSV file
     * @param fileName
     *            name of CSV file
     * @return true, if CSV file was created successfully
     */
	public boolean createCsvFile(String directoryPath, String fileName)
    {
        // textovy vystup do souboru
        File fileParent = new File(directoryPath);
        if(!fileParent.exists()) fileParent.mkdirs();
    	File file = new File(fileParent, fileName);
    	BufferedWriter writer;	
		
    	try 
		{
    		// vytvoreni bufferu
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(CSV_HEADER);
			writer.newLine();
			
			// prochazeni uctu
			Account account = null;
			for(int i=0; i < this.accountsList.length; i++)
			{
				// ziskani jednoho uctu
				account = accountsList[i];
				
				// zapis cisla
	        	writer.write(account.name);
	        	writer.write(CSV_SEPARATOR);
	        	
	        	// zapis jmena
	        	writer.write(account.type);
	        	
	        	// zalomeni radku
	            writer.newLine();	            
			}

			// uzavreni souboru
            writer.flush();
            writer.close();
            
            return true;
		}
    	catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
    }
	
	
	
	/**
     * Create CSV string with accounts.<br>
     * Account list including name and type.
     *
     * @return string with content
     */
	public String createCsvString()
    {
        // textovy vystup do stringu
		StringBuffer output = new StringBuffer("");
		String newline = System.getProperty("line.separator");
		
		// vytvoreni bufferu
		output = output.append(CSV_HEADER);
		output = output.append(newline);
		
		// prochazeni uctu
		Account account = null;
		for(int i=0; i < this.accountsList.length; i++)
		{
			// ziskani jednoho uctu
			account = accountsList[i];
			
			// zapis cisla
			output = output.append(account.name);
			output = output.append(CSV_SEPARATOR);
        	
        	// zapis jmena
			output = output.append(account.type);
        	
        	// zalomeni radku
			output = output.append(newline);	            
		}
        
        return output.toString();
    }
	
	
	public void refreshData(AccountManager accountManager)
	{
		this.accountsList = accountManager.getAccounts(); 
	}
	
	
	public Account[] getAccounts() {
		return this.accountsList;
	}

	public void setAccounts(Account[] accounts) {
		this.accountsList = accounts;
	}

}
