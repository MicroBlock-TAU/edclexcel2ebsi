/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PersonsTable extends DataTable {
    
    public static final String SHEET_NAME = "Persons";
    
    protected final static String EMAIL_COLUMN = "E-Mail Address";
    protected final static String ACHIEVEMENT_COLUMN = "Learning Achievements";
    protected final static String FAMILY_NAME_COLUMN = "Family Name";
    protected final static String GIVEN_NAME_COLUMN = "Given Name";
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
}