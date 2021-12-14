/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** This class is used in getting data from a excel sheet.
 * 
 * For each different sheet in the credentials excel there is a subclass of this class.
 * @author Otto Hylli
 *
 */
public abstract class DataTable {
    
    // sheet this datatable corresponds to.
    private XSSFSheet sheet;
    // the row containing the column headers.
    private XSSFRow headerRow;
    // credential data used to access other data tables.
    private CredentialData credentialData;
    // mapping of colun header names to colun number.
    private Map<String, Integer> headerColumns = new HashMap<>();
    // current row that is being processed.
    private int currentRow = 0;
    
    /** Create data table for a sheet.
     * 
     * Subclass methods are used to get the name of the sheet in the workbook and number of the row which contains the column headers.
     * @param data The excel workbook that has the sheet. 
     * @param credentialData credential data this is a part of.  
     */
    public DataTable( XSSFWorkbook data, CredentialData credentialData ) {
        sheet = data.getSheet(getSheetName());
        this.headerRow = sheet.getRow(getHeaderRowNum());
        this.credentialData = credentialData;
    }
    
    /** Name of the sheet in the workbook.
     * 
     * Subclass implements this so that constructor is able to get the correct sheet.
     * @return name of the sheet
     */
    public abstract String getSheetName(); 
    
    /** Number of the row which contains the column headers.
     * @return row number
     */
    public abstract int getHeaderRowNum();
    
    /** The number of the row that is currently being processed.
     * @return number of current row.
     */
    public int getCurrentRow() {
        return currentRow;
    }

    /** Set the row you want information rom.
     * @param currentRow number of a row in the sheet.
     */
    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
    
    /** Get number of last row that has data.
     * @return row number
     */
    public int getLastRowNum() {
        return sheet.getLastRowNum();
    }

    /** Get the number of the column that has the specified heading.
     * @param name name of a colun heading.
     * @return Number of the colun.
     * @throws DiplomaDataProvider.ExcelStructureException No colun with the given name found from the headers row.
     */
    protected int getColumnNumForHeader( String name ) throws DiplomaDataProvider.ExcelStructureException {
        // check cache first and if not found search the header row.
        Integer columnNum = headerColumns.get(name);
        if ( columnNum == null ) {
            for ( var cell : headerRow ) {
                if ( cell.toString().equals(name)) {
                    headerColumns.put(name, cell.getColumnIndex());
                    return cell.getColumnIndex();
                }
            }
            
            throw new DiplomaDataProvider.ExcelStructureException("sheet " +sheet.getSheetName() +" does not have column " +name +" on row " +headerRow.getRowNum() );
        }
        
        return columnNum;
    }
    
    /**
     * @param row
     * @param columnHeading
     * @return
     * @throws DiplomaDataProvider.ExcelStructureException
     */
    /** For the given row get the value for the colun with the given heading name.
     * @param row number of row
     * @param columnHeading name of column
     * @return Value of the cell.
     * @throws DiplomaDataProvider.ExcelStructureException There is no colun with the given heading.
     */
    public String getCellValue( int row, String columnHeading ) throws DiplomaDataProvider.ExcelStructureException {
        int column = getColumnNumForHeader(columnHeading);
        var cell = sheet.getRow(row).getCell(column);
        if ( cell.getCellType() == CellType.FORMULA ) {
            if (cell.getCachedFormulaResultType() == CellType.STRING) {
                return cell.getStringCellValue();
            }
            
            else {
                throw new DiplomaDataProvider.ExcelStructureException("Cell value " +cell +" at " +cell.getAddress() +" on sheet " +sheet.getSheetName() +" is not a string it is of type " +cell.getCachedFormulaResultType() );
            }
        }
        
        else if ( cell.getCellType() == CellType.STRING || cell.getCellType() == CellType.BLANK ) {
            return cell.getStringCellValue();
        }
        
        else {
            throw new DiplomaDataProvider.ExcelStructureException("Cell value " +cell +" at " +cell.getAddress() +" on sheet " +sheet.getSheetName() +" is not a string it is of type " +cell.getCellType() );
        }
    }
    
    /** For the current row get cell value for the given heading.
     * @param columnHeading Colun heading name.
     * @return cell value
     * @throws DiplomaDataProvider.ExcelStructureException no colun with given heading name
     */
    public String getCellValueForCurrentRow( String columnHeading ) throws DiplomaDataProvider.ExcelStructureException {
        return getCellValue(currentRow, columnHeading);
    }
    
    /** Find a row that has the given values for the given coluns.
     * @param values Values for colun headings. Key is a colun heading and value is a value for that colun.
     * @return The row that has the given values.
     */
    public XSSFRow getRowWithValues( Map<String, String> values ) throws DiplomaDataProvider.RequiredDataNotFoundException {
        XSSFRow row = null;
        rowLoop: for ( int i = headerRow.getRowNum() +1; i <= sheet.getLastRowNum(); i++ ) {
            row = sheet.getRow(i);
            for ( var valueEntry : values.entrySet()) {
                var value = getCellValue(i, valueEntry.getKey() );
                if ( !value.equals(valueEntry.getValue())) {
                    continue rowLoop;
                }
            }
            
            return row;
        }
        
        throw new DiplomaDataProvider.RequiredDataNotFoundException( sheet.getSheetName() +" cannot find row ro values " +values );
    }
    
    /** Reprsents a relationship between two DataTables based on a shared value on a row.
     * 
     * For example credentials and organisations are linked to each other by the name of the organisation which is under issuer in the credentials table and legal name in the organisations table.
     * @author Otto Hylli
     * @param sourceTable DataTable that is the source of the linking.
     * @param sourceHeading Column heading of the source table under which the linking value is located.
     * @param targetTable DataTable that is the target / destination  of the linking.
     * @param targetHeading Column heading of the target table under which the linking value is located.
     */
    public static record TableLink( DataTable sourceTable, String sourceHeading, DataTable targetTable, String targetHeading ) {
        
        
        /** From the target table get the row that is linked to the given row in the source table.
         * @param rowNum number of a row in the source table.
         * @return Linked row from target table.
         */
        public XSSFRow getLinkedRow( int rowNum ) {
            var sourceValue = sourceTable.getCellValue(rowNum, sourceHeading);
            return targetTable.getRowWithValues( Map.of( targetHeading, sourceValue ));
        }
        
        /** From the target table get the row that is linked to the current row in the source table.  
         * @return The linked row from target table.
         */
        public XSSFRow getLinkedRowForCurrentRow() {
            return getLinkedRow( sourceTable.getCurrentRow());
        }
    }
}