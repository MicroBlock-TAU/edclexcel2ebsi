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
    
    /** Create from the default credentials.xlsm file.
     * 
     */
    public CredentialData() {
        try {
            OPCPackage pkg = OPCPackage.open(new File("credentials.xlsm"));
            credentialData = new XSSFWorkbook(pkg);
            /*for ( var i : credentialData.getAllNames()) {
                System.out.println(i.getNameName() +" " +i.getRefersToFormula());
            }*/
            persons = credentialData.getSheet("Persons");
            personsTable = new PersonsTable( credentialData, this );
            credentials = credentialData.getSheet("Europass Credentials");
            organisationsTable = new OrganisationsTable( credentialData, this );
            credentialsTable = new CredentialsTable( credentialData, this );
            
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
    
    /** From the persons sheet get row that has the given email and achievement.
     * @param expectedEmail student email
     * @param expectedAchievement name of an achievement
     * @return Row having the student with the email and achievement.
     * @throws ExcelStructureException There is something wrong with the excel.
     * @throws RequiredDataNotFoundException No row with the given email and achievement found.
     */
    public XSSFRow getPersonalInfo(String expectedEmail, String expectedAchievement ) throws DiplomaDataProvider.ExcelStructureException, DiplomaDataProvider.RequiredDataNotFoundException {
        var values = Map.of( PersonsTable.EMAIL_COLUMN, expectedEmail, PersonsTable.ACHIEVEMENT_COLUMN, expectedAchievement);
        return personsTable.getRowWithValues( values );
    }
    
    /** Get the credentials sheet row that corresponds to the given persons row.
     * @param person Row of the persons sheet
     * @return Corresponding row of the credentials sheet.
     */
    public XSSFRow getCredential(XSSFRow person) {
        return credentials.getRow(person.getRowNum());
    }
    
    public List<String> listCredentialsForStudent( String email ) {
        var credentials = new ArrayList<String>();
        for ( int row = personsTable.getHeaderRowNum() +1; row <= personsTable.getLastRowNum(); row++ ) {
            if ( email.equals( personsTable.getCellValue(row, PersonsTable.EMAIL_COLUMN))) {
                credentials.add( personsTable.getCellValue(row, PersonsTable.ACHIEVEMENT_COLUMN ));
            }
        }
        
        return credentials;
    }
}