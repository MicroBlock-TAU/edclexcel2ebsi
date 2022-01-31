/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import id.walt.signatory.ProofConfig;
import id.walt.signatory.SignatoryDataProvider;
import id.walt.vclib.credentials.VerifiableDiploma;
import id.walt.vclib.credentials.Europass;
import id.walt.vclib.model.VerifiableCredential;

/** A custom data provider to be used with ssikit for getting diploma contents from the EDCL excel file.
 * @author Otto Hylli
 *
 */
public class DiplomaDataProvider implements SignatoryDataProvider {
    
    
    // credential data from xml file
    protected CredentialData data;
    
    private String email;
    private String title;
    
    /** Create provider for creating a credential for the given student and credential. 
     * @param data the credential data from which this creates credentials.
     * @param email email of the student this is used to create a credential for.
     * @param title Title of the credential from the excel this will create a credential for.
     */
    public DiplomaDataProvider( CredentialData data, String email, String title ) {
        this.data = data;
        this.email = email;
        this.title = title;
    }
    
    /** Create the contents of the verifiable diploma.
     *
     */
    @Override
    public VerifiableCredential populate( VerifiableCredential template, ProofConfig proofConfig ) {
        if (template instanceof Europass) {
            // get excel row containing the student matching the email and achievement.
            XSSFRow credentialInfo = data.getCredential(email, title);
            // get the corresponding personal info
            var personalInfo = data.getPerson(credentialInfo);
            //printRow(personalInfo);
            data.credentialsTable.setCurrentRow(credentialInfo.getRowNum());
            // get the organisation row the credential row points to
            data.organisationsTable.setCurrentRow( data.credentialsTable.getLinkedOrganisation().getRowNum() );
            
            Europass diploma = (Europass)template;
            diploma.setIssuer(proofConfig.getIssuerDid());
            diploma.setId( "education#higherEducation#" +UUID.randomUUID().toString());
            diploma.setIssuanceDate(getCurrentDate());
            var subject = new Europass.EuropassSubject();
            diploma.setCredentialSubject(subject);
            subject.setId(proofConfig.getSubjectDid());
            data.personsTable.setCurrentRow(personalInfo.getRowNum());
            
            var course = data.personsTable.getAchievement();
            data.achievementsTable.setCurrentRow( data.achievementsTable.getRowForAchievement(course));
            String assessment = data.achievementsTable.getAssessment();
            var wasAwardedBy = new Europass.EuropassSubject.Achieved.WasAwardedBy("id", List.of(proofConfig.getIssuerDid()), "date", null); 
            var achievement = new Europass.EuropassSubject.Achieved("urn:epass:learningAchievement:1", course, null, null, List.of(createAssessment(assessment)), null, wasAwardedBy, null, null, List.of() );
            subject.setAchieved(List.of(achievement));
            /*var awardingBody = new VerifiableDiploma.VerifiableDiplomaSubject.AwardingOpportunity.AwardingBody( 
                    "id", null, 
                    data.organisationsTable.getLegalIdentifier(), 
                    data.organisationsTable.getCommonName(), 
                    data.organisationsTable.getHomepage() );
            var awardingOpportunity = new VerifiableDiploma.VerifiableDiplomaSubject.AwardingOpportunity(
                    "id", 
                    "identifier", 
                    awardingBody, 
                    data.organisationsTable.getLocation(), null, null );
            
            var specification = new VerifiableDiploma.VerifiableDiplomaSubject.LearningSpecification("urn:epass:qualification:1", new ArrayList<>(), null, null, new ArrayList<>());
            subject.setLearningSpecification(specification);
            subject.setAwardingOpportunity(awardingOpportunity);
            diploma.setCredentialSubject(subject);*/
            diploma.setValidFrom( dateToUtcString(data.credentialsTable.getValidFrom()));
            return diploma;
        }
        
        throw new IllegalArgumentException("Only diploma is supported.");
    }
    
    private Europass.EuropassSubject.Achieved.WasDerivedFrom createAssessment( String assessmentName ) {
        Double grade = data.personsTable.getAssesments().get(assessmentName);
        return new Europass.EuropassSubject.Achieved.WasDerivedFrom( "id", assessmentName, grade.toString(), null, null);
    }
    
    /** Get current state as string.
     * @return Current date.
     */
    private String getCurrentDate() {
        // todo: fix time zone.
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
    
    /** Format the given date into a ISO 8601  UTC date string.
     * @param date date to be formatted
     * @return ISO 8601 utc representation of the date.
     */
    public String dateToUtcString( Date date ) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return formatter.format(date);
    }
    

    /** Used in debugging to print a row from the excel.
     * @param row row to be printed.
     */
    @SuppressWarnings("unused")
    private void printRow(XSSFRow row) {
        for ( var item : row ) {
            if ( item.getCellType() == CellType.FORMULA ) {
                if (item.getCachedFormulaResultType() == CellType.STRING) {
                    System.out.println(item.getAddress() +": " +item.getStringCellValue() +" from " +item);
                }
                
                else {
                    System.out.println(item.getAddress() +": " +item +" " +item.getCachedFormulaResultType() );
                }
            }
            
            else {
                System.out.println(item.getAddress() +": " +item +" " +item.getCellType());
            }
        }
    }
    
    /** Old method for getting a specific cell by address.
     * @param sheet excel sheet
     * @param addressStr cell address e.g. b5
     * @return the cell at the address.
     */
    @SuppressWarnings("unused")
    private XSSFCell getCellByAddress(XSSFSheet sheet, String addressStr ) {
        var address = new CellAddress(addressStr);
        return sheet.getRow(address.getRow()).getCell(address.getColumn());
    }
    
    
    
    /** Indicates that the stucture of the excel file does not match what was expected.
     * @author Otto Hylli
     *
     */
    static public class ExcelStructureException extends RuntimeException {
        
        private static final long serialVersionUID = 5345869342839398427L;

        /** Create with error message.
         * @param message error message.
         */
        public ExcelStructureException( String message) {
            super(message);
        }
    }
    
    /** Indicates that some required data was missing from the excel.
     * @author Otto Hylli
     *
     */
    static public class RequiredDataNotFoundException extends RuntimeException {
        
        private static final long serialVersionUID = 6412865597503832798L;

        /** Create with given error message.
         * @param message error message.
         */
        public RequiredDataNotFoundException( String message ) {
            super(message);
        }
    }
}