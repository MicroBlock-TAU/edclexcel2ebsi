/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/

package fi.tuni.microblock.edclexcel2ebsi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** DataTable for accessing information about assessments . 
 * @author hylli
 *
 */
public class AssessmentsTable extends DataTable {

    // column heading names
    public final static String TITLE_COLUMN = "Title";
    public final static String DESCRIPTION_COLUMN = "Description";
    public final static String SUB_ASSESSMENTS_COLUMN = "Sub-Assessments";
    
    /** Create a AssessmentsTable.
     * @param data excel workbook containing the assessments sheet.
     * @param credentials CredentialData this will be a part of.
     */
    public AssessmentsTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
    }

    @Override
    public String getSheetName() {
        return "Assessments";
    }

    @Override
    public int getHeaderRowNum() {
        return 7;
    }
    
    /** Get the title of the assessment on the current row.
     * @return assessment title.
     */
    public String getTitle() {
        return getCellValueStringForCurrentRow(TITLE_COLUMN);
    }
    
    /** Find the row number for the assessment with the given title.
     * @param title assessment title.
     * @return row number for assessment with the title.
     */
    public int getRowForAssessment(String title) {
        return getRowWithValues(Map.of( TITLE_COLUMN, title)).getRowNum();
    }
    
    /** Get the names of subassessments the assessment on the current row has. 
     * @return List of subassessment names. An empty list if there are no subassessments.
     */
    public List<String> getSubAssessments() {
        List<String> subAssessments = new ArrayList<>();
        String subAssessmentsStr = getCellValueStringForCurrentRow(SUB_ASSESSMENTS_COLUMN);
        if ( subAssessmentsStr.length() == 0 ) {
            return subAssessments;
        }
        
        for ( String subAssessment : subAssessmentsStr.split(";")) {
            subAssessments.add( subAssessment.strip());
        }
        return subAssessments;
    }
}