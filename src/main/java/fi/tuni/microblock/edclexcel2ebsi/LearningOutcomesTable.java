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

/** DataTable for accessing information about learning outcomes. 
 * @author hylli
 *
 */
public class LearningOutcomesTable extends DataTable {

    // column heading names
    public final static String TITLE_COLUMN = "Title";
    public final static String DESCRIPTION_COLUMN = "Description";
    public final static String[] ESCO_SKILL_COLUMNS = {"Related ESCO Skill 1 URL", "Related ESCO Skill 2 URL", "Related ESCO Skill 3 URL" };
    
    /** Create a LearningOutcomesTable.
     * @param data excel workbook containing the sheet.
     * @param credentials CredentialData this will be a part of.
     */
    public LearningOutcomesTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
    }

    @Override
    public String getSheetName() {
        return "Learning Outcomes";
    }

    @Override
    public int getHeaderRowNum() {
        return 7;
    }
    
    /** Get the title of the learning outcome on the current row.
     * @return learning outcome title.
     */
    public String getTitle() {
        return getCellValueStringForCurrentRow(TITLE_COLUMN);
    }
    
    /** Get the description of the learning outcome on the current row.
     * @return learning outcome description.
     */
    public String getDescription() {
        return getCellValueStringForCurrentRow(DESCRIPTION_COLUMN);
    }
    
    /** Find the row number for the learning outcome with the given title.
     * @param title learning outcome title.
     * @return row number for learning outcome with the title.
     */
    public int getRowForLearningOutcome(String title) {
        return getRowWithValues(Map.of( TITLE_COLUMN, title)).getRowNum();
    }
    
    /** Get the esco skills of the learning outcome on the current row.
     * @return list of esco skill urls.
     */
    public List<String> getEscoSkills() {
        List<String> skills = new ArrayList<>();
        for ( String skillColumn : ESCO_SKILL_COLUMNS) {
            String skill = getCellValueStringForCurrentRow(skillColumn);
            if ( skill != null && skill.length() > 0 ) {
                skills.add(skill);
            }
        }
        
        return skills;
    }
}