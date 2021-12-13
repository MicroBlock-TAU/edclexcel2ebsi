/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import org.junit.jupiter.api.Test;

import fi.tuni.microblock.edclexcel2ebsi.DiplomaDataProvider.ExcelStructureException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/** Tests for diploma data provider and the excel reader classes it uses.
 * @author Otto Hylli
 *
 */
class DataProviderTest {
    
    private DiplomaDataProvider provider = new DiplomaDataProvider(new CredentialData());
    
    /** Test that finds person with email and achievement.
     * 
     */
    @Test void getPersonalInfo() {
        try {
            var row = provider.data.getPersonalInfo("test3.doe3@test.dat", "Data and software business");
            provider.data.personsTable.setCurrentRow(row.getRowNum());
            var achievement = provider.data.personsTable.getCellValueForCurrentRow(PersonsTable.ACHIEVEMENT_COLUMN);
            assertEquals( achievement, "Data and software business");
            var email = provider.data.personsTable.getCellValueForCurrentRow(PersonsTable.EMAIL_COLUMN);
            assertEquals( email, "test3.doe3@test.dat");
        }
        
        catch ( DiplomaDataProvider.RequiredDataNotFoundException e) {
            fail("Should have found personal info.");
        }
    }
    
    /** Test that correct exception is thrown when person with email and achievement is not found.
     * 
     */
    @Test void personalInfoNotFound() {
        assertThrows(DiplomaDataProvider.RequiredDataNotFoundException.class, () -> provider.data.getPersonalInfo("test3.doe3@test.dat", "Data and software busines"), "Should not find information.");
    }
    
    /** Check that persons sheet has expected column headings.
     * 
     */
    @Test void findPersonsColumns() {
        var table = new PersonsTable(provider.data.credentialData, provider.data);
        var columns = List.of( PersonsTable.EMAIL_COLUMN, PersonsTable.ACHIEVEMENT_COLUMN, PersonsTable.FAMILY_NAME_COLUMN, PersonsTable.GIVEN_NAME_COLUMN  );
        for ( var name : columns ) {
            try {
                // check also that cache for headings works.
                for ( int i = 0; i < 2; i++ ) {
                    var columnNum = table.getColumnNumForHeader( name );
                    //System.out.println( name +": " +columnNum );
                }
            }
            
            catch ( ExcelStructureException e) {
                fail(e.getMessage());
            }
        }
    }
    
    /** Test that we can get expected values from a specific row.
     * 
     */
    @Test void getCellValueString() {
        var table = new PersonsTable(provider.data.credentialData, provider.data );
        final int rowNum = 12;
        table.setCurrentRow(rowNum);
        try {
            var email = table.getCellValueForCurrentRow( PersonsTable.EMAIL_COLUMN);
            assertEquals(email, "test1.doe1@test.dat");
            var achievement = table.getCellValueForCurrentRow( PersonsTable.ACHIEVEMENT_COLUMN);
            assertEquals( achievement, "Data and software business" );
        }
        
        catch ( DiplomaDataProvider.ExcelStructureException e) {
            fail(e.getMessage());
        }
    }
    
    /** Test that we can find row with certain values.
     * 
     */
    @Test void getRowWithValues() {
        var email = "test2.dan2@test.dat";
        var achievement = "Data and software business";
        var rowNum = provider.data.personsTable.getRowWithValues(Map.of( PersonsTable.EMAIL_COLUMN, email, PersonsTable.ACHIEVEMENT_COLUMN, achievement )).getRowNum();
        assertEquals( provider.data.personsTable.getCellValue(rowNum, PersonsTable.EMAIL_COLUMN ), email );
        assertEquals( provider.data.personsTable.getCellValue(rowNum, PersonsTable.ACHIEVEMENT_COLUMN ), achievement );
    }
    
    /** Check that we get correct exception if row with given values is not found.
     * 
     */
    @Test void rowWithValuesNotFound() {
        var email = "test2.dan2@test.dat";
        var achievement = "Data and software busines";
        assertThrows( DiplomaDataProvider.RequiredDataNotFoundException.class, () -> provider.data.personsTable.getRowWithValues(Map.of( PersonsTable.EMAIL_COLUMN, email, PersonsTable.ACHIEVEMENT_COLUMN, achievement )));
    }
    
    /** Test that table linking works.
     * 
     */
    @Test void tableLinkTest() {
        provider.data.credentialsTable.setCurrentRow(13);
        assertEquals(provider.data.credentialsTable.organisationLink.getLinkedRowForCurrentRow().getRowNum(), 12 );
    }
}