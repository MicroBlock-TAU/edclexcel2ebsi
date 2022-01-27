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

/** Tests for the excel reader classes.
 * @author Otto Hylli
 *
 */
class CredentialDataTest {
    
    private CredentialData data = new CredentialData();
    
    /** Test that finds person with email and achievement.
     * 
     */
    @Test void getPersonalInfo() {
        try {
            var expectedTitle = "Data and Software Business module";
            var row = data.getCredential("jane2.doe2@test.edu", expectedTitle );
            data.credentialsTable.setCurrentRow(row.getRowNum());
            var title = data.credentialsTable.getCellValueStringForCurrentRow(CredentialsTable.TITLE_COLUMN);
            assertEquals( expectedTitle, title);
        }
        
        catch ( DiplomaDataProvider.RequiredDataNotFoundException e) {
            fail("Should have found personal info.");
        }
    }
    
    /** Test that correct exception is thrown when person with email and achievement is not found.
     * 
     */
    @Test void personalInfoNotFound() {
        assertThrows(DiplomaDataProvider.RequiredDataNotFoundException.class, () -> data.getCredential("test3.doe3@test.dat", "Data and Software Business module"), "Should not find information.");
    }
    
    /** Check that persons sheet has expected column headings.
     * 
     */
    @Test void findPersonsColumns() {
        var table = new PersonsTable(data.credentialData, data);
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
        var table = new PersonsTable(data.credentialData, data );
        final int rowNum = 12;
        table.setCurrentRow(rowNum);
        try {
            var email = table.getCellValueStringForCurrentRow( PersonsTable.EMAIL_COLUMN);
            assertEquals( "Jane1.doe1@test.edu", email);
            var achievement = table.getCellValueStringForCurrentRow( PersonsTable.ACHIEVEMENT_COLUMN);
            assertEquals( "Data and Software Business", achievement );
        }
        
        catch ( DiplomaDataProvider.ExcelStructureException e) {
            fail(e.getMessage());
        }
    }
    
    /** Test that we can find row with certain values.
     * 
     */
    @Test void getRowWithValues() {
        var email = "jane2.doe2@test.edu";
        var achievement = "Data and Software Business";
        var rowNum = data.personsTable.getRowWithValues(Map.of( PersonsTable.EMAIL_COLUMN, email, PersonsTable.ACHIEVEMENT_COLUMN, achievement )).getRowNum();
        assertEquals( data.personsTable.getCellValueString(rowNum, PersonsTable.EMAIL_COLUMN ), email );
        assertEquals( data.personsTable.getCellValueString(rowNum, PersonsTable.ACHIEVEMENT_COLUMN ), achievement );
    }
    
    /** Check that we get correct exception if row with given values is not found.
     * 
     */
    @Test void rowWithValuesNotFound() {
        var email = "test2.dan2@test.dat";
        var achievement = "Data and software busines";
        assertThrows( DiplomaDataProvider.RequiredDataNotFoundException.class, () -> data.personsTable.getRowWithValues(Map.of( PersonsTable.EMAIL_COLUMN, email, PersonsTable.ACHIEVEMENT_COLUMN, achievement )));
    }
    
    /** Test that table linking works.
     * 
     */
    @Test void tableLinkTest() {
        data.credentialsTable.setCurrentRow(13);
        assertEquals(data.credentialsTable.organisationLink.getLinkedRowForCurrentRow().getRowNum(), 12 );
    }
    
    /** Test that we can get learning activities for a person.
     * 
     */
    @Test void getLearningActivities() {
        data.personsTable.setCurrentRow(13);
        var activities = data.personsTable.getLearningActivities();
        var expected = List.of("Individual exercise", "Course exercise", "Zoom lectures");
        assertEquals(expected, activities);
    }
    
    /** Test that we can get assesments and their grades for a person.
     * 
     */
    @Test void getGrades() {
        data.personsTable.setCurrentRow(13);
        var grades = data.personsTable.getAssesments();
        Map<String, Double> expected = Map.of( "Individual assignment1", 3.0, "Individual assignment2", 5.0, "Project assignment", 4.0, "Overall grade", 4.0 );
        assertEquals( expected, grades );
    }
    
    /** Test we can get learning activity by title.
     * 
     */
    @Test void getLearningActivity() {
        var title = "Course exercise";
        var row = data.activitiesTable.getRowForActivity(title);
        data.activitiesTable.setCurrentRow(row);
        assertEquals( title, data.activitiesTable.getTitle());
        assertNotNull( data.activitiesTable.getDescription());
    }
    
    /** Test we can find assessment by title and that it has no subassessments.
     * 
     */
    @Test void getAssessmentWithNoSubAssessments() {
        var title = "Individual assignment1";
        var row = data.assessmentsTable.getRowForAssessment(title);
        data.assessmentsTable.setCurrentRow(row);
        assertEquals( title, data.assessmentsTable.getTitle());
        assertTrue( data.assessmentsTable.getSubAssessments().isEmpty());
    }
    
    /** Test we can get assessment with given title and that it has the expected subassessments.
     * 
     */
    @Test void getAssessmentWithSubAssessments() {
        var title = "Overall grade";
        var row = data.assessmentsTable.getRowForAssessment(title);
        data.assessmentsTable.setCurrentRow(row);
        assertEquals( title, data.assessmentsTable.getTitle());
        List<String> expected = List.of("Individual assignment1", "Individual assignment2", "Project assignment");
        assertEquals( expected, data.assessmentsTable.getSubAssessments());
    }
    
    /** Test we get achievement by title and its assesment and activities.
     * 
     */
    @Test void getAchievement() {
        String title = "Data and Software Business";
        int row = data.achievementsTable.getRowForAchievement(title);
        data.achievementsTable.setCurrentRow(row);
        assertEquals( title, data.achievementsTable.getTitle());
        assertEquals( "Overall grade", data.achievementsTable.getAssessment());
        List<String> expectedActivities = List.of("Individual exercise", "course exercise", "zoom lectures" );
        assertEquals( expectedActivities, data.achievementsTable.getActivities());
    }
}