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

    public PersonsTable( XSSFWorkbook data, DiplomaDataProvider provider ) {
        super(data, provider);
    }
    
    @Override
    public String getSheetName() {
        return SHEET_NAME;
    }
    
    @Override
    public int getHeaderRowNum() {
        return PERSONS_HEADER_ROW_NUM;
    }
}