/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/

package fi.tuni.microblock.edclexcel2ebsi;

import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** DataTable for accessing information about achievements . 
 * @author hylli
 *
 */
public class AchievementsTable extends DataTable {

    // column heading names
    public final static String TITLE_COLUMN = "Title";
    public final static String PROVEN_BY_COLUMN = "Proven by";
    
    public static final String INFLUENCED_BY_COLUMN = "Influenced by";
    public static final String SPECIFICATION_TITLE_COLUMN = "Specification Title";
    public static final String LEARNING_OUTCOMES_COLUMN = "Learning Outcomes";
    public static final String LEARNING_SETTING_COLUMN = "Learning Setting";
    public static final String LEARNING_OPPORTUNITY_TYPE_COLUMN = "Learning Opportunity Type";
    public static final String ECTS_CREDIT_POINTS_COLUMN = "ECTS Credit Points";
    
    private VocabularyMapping learningSettingMapping;
    private VocabularyMapping learningOpportunityTypeMapping;
    
    /** Create a AchievementsTable.
     * @param data excel workbook containing the activities sheet.
     * @param credentials CredentialData this will be a part of.
     */
    public AchievementsTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
        learningSettingMapping = new MapBasedVocabularyMapping( Map.of(
                "formal learning", "http://data.europa.eu/snb/learning-setting/6fd4685715"
                ));
        learningOpportunityTypeMapping = new MapBasedVocabularyMapping( Map.of(
                "Course", "http://data.europa.eu/snb/learning-opportunity/05053c1cbe"
                ));
    }

    @Override
    public String getSheetName() {
        return "Achievements";
    }

    @Override
    public int getHeaderRowNum() {
        return 7;
    }
    
    /** Get the title of the achievement on the current row.
     * @return achievement title.
     */
    public String getTitle() {
        return getCellValueStringForCurrentRow(TITLE_COLUMN);
    }
    
    /** Find the row number for the achievement with the given title.
     * @param title achievement title.
     * @return row number for achievement with the title.
     */
    public int getRowForAchievement(String title) {
        return getRowWithValues(Map.of( TITLE_COLUMN, title)).getRowNum();
    }
    
    /** Get name of assessment for the achievement on the current row. 
     * @return assessment name
     */
    public String getAssessment() {
        return getCellValueStringForCurrentRow(PROVEN_BY_COLUMN);
    }    
    
    /** Get names of learning activities for the achievement on the current row.
     * @return activity names
     */
    public List<String> getActivities() {
        return getCellMultiValueStringForCurrentRow(INFLUENCED_BY_COLUMN);
    }
    
    /** Get title of the learning specification on the current row.
     * @return learning specification title.
     */
    public String getSpecificationTitle() {
        return getCellValueStringForCurrentRow(SPECIFICATION_TITLE_COLUMN);
    }
    
    /** Get list of names of learning outcomes for the learning specification on the current row.
     * @return learning outcome names.
     */
    public List<String> getLearningOutcomes() {
        return getCellMultiValueStringForCurrentRow(LEARNING_OUTCOMES_COLUMN);
    }
    
    /** Get learning setting for the learning specification on the current row.
     * @return learning setting
     */
    public String getLearningSetting() {
        return learningSettingMapping.getUri( getCellValueStringForCurrentRow(LEARNING_SETTING_COLUMN));
    }
    
    /** Get learning opportunity type for learning specification on the current row.
     * @return learning opportunity type.
     */
    public String getLearningOpportunityType() {
        return learningOpportunityTypeMapping.getUri( getCellValueStringForCurrentRow(LEARNING_OPPORTUNITY_TYPE_COLUMN));
    }
    
    /** Get ects crdit points for the learning specification on the current row.
     * @return ects credit points.
     */
    public int getEctsCreditPoints() {
        return (int) getCellValueNumberForCurrentRow( ECTS_CREDIT_POINTS_COLUMN);
    }
}