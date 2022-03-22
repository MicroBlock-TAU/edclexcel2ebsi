/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fi.tuni.microblock.edclexcel2ebsi.DiplomaDataProvider.ExcelStructureException;
import fi.tuni.microblock.edclexcel2ebsi.DiplomaDataProvider.RequiredDataNotFoundException;

/** Represents the excel workbook that contains credential data.
 * 
 * Provides access to it sheets like persons and organisations via DataTables.
 * @author Otto Hylli
 */
public class CredentialData {

    // the excel data
    protected XSSFWorkbook credentialData;
    // the sheet and DataTable instances for the different sheets of the excel.
    protected XSSFSheet persons;
    protected PersonsTable personsTable;
    protected CredentialsTable credentialsTable;
    protected OrganisationsTable organisationsTable;
    protected XSSFSheet credentials;
    protected ActivitiesTable activitiesTable;
    protected AssessmentsTable assessmentsTable;
    protected AchievementsTable achievementsTable;
    protected LearningOutcomesTable outcomesTable;
    
    /** Create from the default credentials.xlsm file.
     * 
     */
    public CredentialData() {
        try {
            var fileName = "credentials.xlsm";
            OPCPackage pkg = OPCPackage.open(new File(fileName));
            credentialData = new XSSFWorkbook(pkg);
            /*for ( var i : credentialData.getAllNames()) {
                System.out.println(i.getNameName() +" " +i.getRefersToFormula());
            }*/
            persons = credentialData.getSheet("Persons");
            personsTable = new PersonsTable( credentialData, this );
            credentials = credentialData.getSheet("Europass Credentials");
            organisationsTable = new OrganisationsTable( credentialData, this );
            credentialsTable = new CredentialsTable( credentialData, this );
            activitiesTable = new ActivitiesTable( credentialData, this );
            assessmentsTable = new AssessmentsTable( credentialData, this );
            achievementsTable = new AchievementsTable ( credentialData, this );
            outcomesTable = new LearningOutcomesTable ( credentialData, this );
            
            /*for ( var sheet : credentialData ) {
                System.out.println( sheet.getSheetName() );
            }*/
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /** From the credentials sheet get row that has the given title and is related to the persons row with given email.
     * @param expectedEmail student email
     * @param expectedTitle title of a credential
     * @return row of the credential sheet which matches the parameters. 
     * @throws ExcelStructureException There is something wrong with the excel.
     * @throws RequiredDataNotFoundException No row with the given email and achievement found.
     */
    public XSSFRow getCredential(String expectedEmail, String expectedTitle ) throws DiplomaDataProvider.ExcelStructureException, DiplomaDataProvider.RequiredDataNotFoundException {
        var values = Map.of( CredentialsTable.TITLE_COLUMN, expectedTitle );
        var credentials =  credentialsTable.getRowsWithValues( values );
        for ( var credential : credentials ) {
            var person = getPerson(credential);
            var email = personsTable.getCellValueString(person.getRowNum(), PersonsTable.EMAIL_COLUMN );
            if ( email.equals(expectedEmail)) {
                return credential;
            }
        }
        
        throw new DiplomaDataProvider.RequiredDataNotFoundException( "Credential with title " +expectedTitle + " for student with email " +expectedEmail +" not found.");
    }
    
    /** Get the persons sheet row that corresponds to the given credentials row.
     * @param credential Row of the credentials sheet
     * @return Corresponding row of the credentials sheet.
     */
    public XSSFRow getPerson(XSSFRow credential) {
        return personsTable.getSheet().getRow(credential.getRowNum());
    }
    
    public List<String> listCredentialsForStudent( String email ) {
        var credentials = new ArrayList<String>();
        for ( int row = personsTable.getHeaderRowNum() +1; row <= personsTable.getLastRowNum(); row++ ) {
            if ( email.equals( personsTable.getCellValueString(row, PersonsTable.EMAIL_COLUMN))) {
                var title = credentialsTable.getCellValueString(row, CredentialsTable.TITLE_COLUMN);
                credentials.add( title );
            }
        }
        
        return credentials;
    }
    
    /** Check if there is personal data for student with given email.
     * @param email student email
     * @return true if data is found, false if not.
     */
    public boolean studentExists( String email ) {
        try {
            personsTable.getRowForPerson(email);
            return true;
        }
        
        catch ( DiplomaDataProvider.RequiredDataNotFoundException e ) {
            return false;
        }
    }
}