/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PersonsTable extends DataTable {
    
    public static final String SHEET_NAME = "Persons";
    
    protected final static String EMAIL_COLUMN = "E-Mail Address";
    protected final static String ACHIEVEMENT_COLUMN = "Learning Achievements";
    protected final static String FAMILY_NAME_COLUMN = "Family Name";
    protected final static String GIVEN_NAME_COLUMN = "Given Name";
    public static final String LEARNING_ACTIVITIES_COLUMN = "Learning Activities";
    public static final String GRADE_COLUMN = "Grade";
    public final static String IDENTIFIER_1_SCHEME_NAME_COLUMN = "Other Identifier 1 Scheme Name";
    public final static String IDENTIFIER_1_COLUMN = "Other Identifier 1";
    public static final String DATE_OF_BIRTH_COLUMN = "Date of Birth";  
    protected final static int PERSONS_HEADER_ROW_NUM = 7;

    /** Create persons table for the given work book.
     * @param data The excel work book
     * @param credentials for accessing other tables.
     */
    public PersonsTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
    }
    
    @Override
    public String getSheetName() {
        return SHEET_NAME;
    }
    
    @Override
    public int getHeaderRowNum() {
        return PERSONS_HEADER_ROW_NUM;
    }
    
    /** Get family name for current row.
     * @return family name for current row.
     */
    public String getFamilyName() {
        return getCellValueStringForCurrentRow( PersonsTable.FAMILY_NAME_COLUMN);
    }
    
    /** Get given name for person on current row.
     * @return given name on current row.
     */
    public String getGivenName() {
        return getCellValueStringForCurrentRow( PersonsTable.GIVEN_NAME_COLUMN);
    }
    
    /** Get the achievement for the current row.
     * @return the achievemnet for current row.
     */
    public String getAchievement() {
        return getCellValueStringForCurrentRow( PersonsTable.ACHIEVEMENT_COLUMN);
    }
    
    /** Get names of learning activities for the current row.
     * @return List of learning activity names.
     */
    public List<String> getLearningActivities() {
        return getCellMultiValueStringForCurrentRow( LEARNING_ACTIVITIES_COLUMN);
    }
    
    /** Get assesments and their grades for the current row.
     * @return Key is name of assesment and value is grade for it.
     */
    public Map<String, Double> getAssesments() {
        Map<String, Double> grades = new HashMap<String, Double>();
        var gradeColumn = getColumnNumForHeader(GRADE_COLUMN);
        var assesmentNameRow = getHeaderRowNum() +1;
        XSSFCell headingCell = null;
        final String NAME_PREFIX = "Assessment -";
        do {
            String gradeName = getSheet().getRow(assesmentNameRow).getCell(gradeColumn).getStringCellValue();
            if ( gradeName.length() > NAME_PREFIX.length() ) {
                gradeName = gradeName.substring(NAME_PREFIX.length()).strip();
                if ( gradeName.length() > 0 ) {
                    var grade = getSheet().getRow(getCurrentRow()).getCell(gradeColumn).getNumericCellValue();
                    grades.put(gradeName, grade);
                }
            }
            
            gradeColumn += 1;
            headingCell = getSheet().getRow(getHeaderRowNum()).getCell(gradeColumn);
        } while( headingCell != null && headingCell.getStringCellValue().equals(GRADE_COLUMN));
        
        return grades;
    }
    
    /** Get other identifier 1 scheme name for person on the current row.
     * @return other identifier 1 scheme name
     */
    public String getOtherIdentifier1SchemeName() {
        return getCellValueStringForCurrentRow( PersonsTable.IDENTIFIER_1_SCHEME_NAME_COLUMN);
    }
    
    /** Get other identifier 1 for person on the current row.
     * @return other identifier 1
     */
    public String getOtherIdentifier1() {
        return getCellValueStringForCurrentRow( PersonsTable.IDENTIFIER_1_COLUMN);
    }
    
    public Date getDateOfBirth()  {
        return getCellValueDateForCurrentRow( PersonsTable.DATE_OF_BIRTH_COLUMN);
    }
    
    /** Get number of row for person with the given email.
     * @param email email address of a person.
     * @return row number for the person.
     */
    public int getRowForPerson( String email ) throws DiplomaDataProvider.RequiredDataNotFoundException {
        return getRowWithValues(Map.of( EMAIL_COLUMN, email)).getRowNum();
    }
}