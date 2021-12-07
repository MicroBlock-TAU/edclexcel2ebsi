/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Represents information about organisations that issue credentials.
 * @author Otto Hylli
 *
 */
public class OrganisationsTable extends DataTable {
    
    public static final String SHEET_NAME = "Organisations";
    public static final int HEADER_ROW_NUM = 7;
    
    protected static final String LEGAL_NAME_COLUMN = "Legal Name";
    protected static final String COMMON_NAME_COLUMN = "Common Name";
    protected static final String LEGAL_IDENTIFIER_COLUMN = "Legal Identifier";
    protected static final String HOMEPAGE_COLUMN = "homepage";
    protected static final String LOCATION_COLUMN = "Location"; 
     
    /** Create organisations table for the given workbook.
     * @param data excel workbook
     * @param provider provider used to access other tables.
     */
    public OrganisationsTable( XSSFWorkbook data, DiplomaDataProvider provider ) {
        super(data, provider);
    }
    
    @Override
    public String getSheetName() {
        return SHEET_NAME;
    }
    
    @Override
    public int getHeaderRowNum() {
        return HEADER_ROW_NUM;
    }
    
    /** Get common name of organisation on current row.
     * @return common name
     */
    public String getCommonName() {
        return getCellValueForCurrentRow(COMMON_NAME_COLUMN);
    }
    
    /** Get legal identifier of organisation on the current row.
     * @return legal indentifier
     */
    public String getLegalIdentifier() {
        return getCellValueForCurrentRow( LEGAL_IDENTIFIER_COLUMN);
    }
    
    /** Get the home page of organisation on the current row.
     * @return home page
     */
    public String getHomepage() {
        return getCellValueForCurrentRow(HOMEPAGE_COLUMN);
    }
    
    /** Get the location of the organisation on the current row. 
     * @return location
     */
    public String getLocation() {
        return getCellValueForCurrentRow(LOCATION_COLUMN);
    }
}