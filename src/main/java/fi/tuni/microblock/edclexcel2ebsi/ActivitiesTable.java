/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/

package fi.tuni.microblock.edclexcel2ebsi;

import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** DataTable for accessing information about learning activities. 
 * @author hylli
 *
 */
public class ActivitiesTable extends DataTable {

    // column heading names
    public final static String TITLE_COLUMN = "Title";
    public final static String DESCRIPTION_COLUMN = "Description";
    public static final String SPECIFICATION_TITLE_COLUMN  = "Specification Title";
    public static final String SPECIFICATION_TYPE_COLUMN  = "Learning Activity Type";
    public static final String SPECIFICATION_DESCRIPTIONCOLUMN  = "Specification Description";
    public static final String MODE_OF_LEARNING_COLUMN = "Mode of Learning";
    
    /** Create a ActivitiesTable.
     * @param data excel workbook containing the activities sheet.
     * @param credentials CredentialData this will be a part of.
     */
    public ActivitiesTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
    }

    @Override
    public String getSheetName() {
        return "Activities";
    }

    @Override
    public int getHeaderRowNum() {
        return 7;
    }
    
    /** Get the title of the learning activity on the current row.
     * @return learning activity title.
     */
    public String getTitle() {
        return getCellValueStringForCurrentRow(TITLE_COLUMN);
    }
    
    /** Title for the activity specification on the current row.
     * @return activity specification title
     */
    public String getSpecificationTitle() {
        return getCellValueStringForCurrentRow(SPECIFICATION_TITLE_COLUMN);
    }
    
    /** description of learning activity specification for the activity specification on the current row.
     * @return learning activity specification description
     */
    public String getSpecificationDescription() {
        return getCellValueStringForCurrentRow(SPECIFICATION_DESCRIPTIONCOLUMN);
    }
    
    /** Learning activity type for the activity specification on the current row.
     * @return learning activity type
     */
    public String getActivityType() {
        return getCellValueStringForCurrentRow(SPECIFICATION_TYPE_COLUMN);
    }
    
    /** Mode of learning for the activity specification on the current row.
     * @return mode of learning
     */
    public String getModeOfLearning() {
        return getCellValueStringForCurrentRow(MODE_OF_LEARNING_COLUMN);
    }
    
    /** Get the description of the learning activity on the current row.
     * @return learning activity description.
     */
    public String getDescription() {
        return getCellValueStringForCurrentRow(DESCRIPTION_COLUMN);
    }
    
    /** Find the row number for the learning activity with the given title.
     * @param title learning activity title.
     * @return row number for learning activity with the title.
     */
    public int getRowForActivity(String title) {
        return getRowWithValues(Map.of( TITLE_COLUMN, title)).getRowNum();
    }
}