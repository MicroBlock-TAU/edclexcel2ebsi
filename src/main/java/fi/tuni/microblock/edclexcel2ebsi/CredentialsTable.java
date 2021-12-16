/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Represents the excel sheet which contains data about issued credentials.
 * @author Otto Hylli
 *
 */
public class CredentialsTable extends DataTable {
    
    public static final String SHEET_NAME = "Europass Credentials";
    public static final int HEADER_ROW_NUM = 7;
    
    // column containing name of organisation that issued the credential
    public static final String ISSUER_COLUMN = "Issuer";
    // column with credential title
    public static final String TITLE_COLUMN = "Title"; 
    // relation link to organisations table
    protected DataTable.TableLink organisationLink;
    
    /** Create credentials table for the given workbook.
     * @param data the excel wokrbook
     * @param credentialData for accessing other tables.
     */
    public CredentialsTable( XSSFWorkbook data, CredentialData credentials ) {
        super(data, credentials);
        organisationLink = new DataTable.TableLink( this, CredentialsTable.ISSUER_COLUMN, credentials.organisationsTable, OrganisationsTable.LEGAL_NAME_COLUMN);
    }
    
    @Override
    public String getSheetName() {
        return SHEET_NAME;
    }
    
    @Override
    public int getHeaderRowNum() {
        return HEADER_ROW_NUM;
    }
    
    /** Get organisations table row which has information about the issuer of credential on the current row.
     * @return organisations sheet row
     */
    public XSSFRow getLinkedOrganisation() {
        return organisationLink.getLinkedRow(getCurrentRow());
    }
}