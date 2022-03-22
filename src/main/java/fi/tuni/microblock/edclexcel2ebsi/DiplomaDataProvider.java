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
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import id.walt.signatory.ProofConfig;
import id.walt.signatory.SignatoryDataProvider;
import id.walt.vclib.credentials.VerifiableDiploma;
import id.walt.vclib.credentials.VerifiableId;
import id.walt.vclib.credentials.Europass;
import id.walt.vclib.model.VerifiableCredential;

/** A custom data provider to be used with ssikit for getting diploma and student id contents from the EDCL excel file.
 * @author Otto Hylli
 *
 */
public class DiplomaDataProvider implements SignatoryDataProvider {
    
    
    // credential data from xml file
    protected CredentialData data;
    
    private String email;
    private String title;
    
    /** Create provider for creating credentials for the given student and Europass credential. 
     * @param data the credential data from which this creates credentials.
     * @param email email of the student this is used to create a credential for.
     * @param title Title of the credential from the excel this will create a credential for. Can be null if only the student id will be created.
     */
    public DiplomaDataProvider( CredentialData data, String email, String title ) {
        this.data = data;
        this.email = email;
        this.title = title;
    }
    
    /** Get the URI of the schema of the micro-credential this creates.
     * @return schema URI
     */
    public static String getCredentialSchema() {
        return Europass.Companion.getTemplate().invoke().getCredentialSchema().getId();
    }
    
    /** Get the type of the credential this creates.
     * @return credential type
     */
    public static String getCredentialType() {
        return Europass.Companion.getType().get(Europass.Companion.getType().size() -1);
    }
    
    /** Get the URI of the schema of the id credential  this creates.
     * @return schema URI
     */
    public static String getIdCredentialSchema() {
        return VerifiableId.Companion.getTemplate().invoke().getCredentialSchema().getId();
    }
    
    /** Get the type of the id credential this creates.
     * @return credential type
     */
    public static String getIdCredentialType() {
        return VerifiableId.Companion.getType().get(Europass.Companion.getType().size() -1);
    }
    
    /** Create the contents of the verifiable diploma or id depending on the type of the template.
     *
     */
    @Override
    public VerifiableCredential populate( VerifiableCredential template, ProofConfig proofConfig ) {
        if (template instanceof Europass) {
            return createDiploma(template, proofConfig);
        }
        
        else if ( template instanceof VerifiableId ) {
            return createId( template, proofConfig );
        }
        
        throw new IllegalArgumentException("Only Europass and VerifiableId are supported.");
    }
    
    /** Create micro-credential from the excel data.
     * @param template credential template
     * @param proofConfig proofconfig
     * @return The credential.
     */
    private VerifiableCredential createDiploma( VerifiableCredential template, ProofConfig proofConfig ) {
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
        diploma.setId( generateId("credential"));
        //diploma.setIssuanceDate(getCurrentDate());
        diploma.setIssued(null);
        var subject = new Europass.EuropassSubject();
        diploma.setCredentialSubject(subject);
        subject.setId(proofConfig.getSubjectDid());
        data.personsTable.setCurrentRow(personalInfo.getRowNum());
        String identifierScheme = data.personsTable.getOtherIdentifier1SchemeName();
        String identifier = data.personsTable.getOtherIdentifier1();
        if ( !identifierScheme.isBlank() && !identifier.isBlank()) {
            subject.setIdentifier(new Europass.EuropassSubject.Identifier(identifierScheme, identifier));
        }
        
        var course = data.personsTable.getAchievement();
        data.achievementsTable.setCurrentRow( data.achievementsTable.getRowForAchievement(course));
        String assessment = data.achievementsTable.getAssessment();
        var wasAwardedBy = new Europass.EuropassSubject.Achieved.WasAwardedBy(generateId("awardingProcess"), List.of(proofConfig.getIssuerDid()), null, null);
        var activities = getLearningActivities();
        var achievement = new Europass.EuropassSubject.Achieved(generateId("learningAchievement"), course, null, null, List.of(createAssessment(assessment)), activities, wasAwardedBy, null, null, List.of(createLearningSpecification()) );
        subject.setAchieved(List.of(achievement));
        diploma.setValidFrom( dateToUtcString(data.credentialsTable.getValidFrom()));
        return diploma;
    }
    
    /** Create student id.
     * @param template template for the credential
     * @param proofConfig proof config for the credential
     * @return the student id.
     */
    private VerifiableCredential createId( VerifiableCredential template, ProofConfig proofConfig ) {
        VerifiableId id = (VerifiableId)template;
        id.setIssuer(proofConfig.getIssuerDid());
        id.setValidFrom(getCurrentDate());
        id.setIssued(null);
        id.setEvidence(null);
        int personRow = data.personsTable.getRowForPerson(email);
        data.personsTable.setCurrentRow(personRow);
        var subject = new VerifiableId.VerifiableIdSubject();
        subject.setId(proofConfig.getSubjectDid());
        subject.setFamilyName(data.personsTable.getFamilyName());
        subject.setFirstName(data.personsTable.getGivenName());
        var identifier = new VerifiableId.VerifiableIdSubject.Identifier(data.personsTable.getOtherIdentifier1SchemeName(), data.personsTable.getOtherIdentifier1());
        subject.setIdentifier(List.of(identifier));
        id.setCredentialSubject(subject);
        id.setId("identity#verifiableID#" +UUID.randomUUID().toString());
        return id;
    }
    
    private Europass.EuropassSubject.Achieved.WasDerivedFrom createAssessment( String assessmentName ) {
        Double grade = data.personsTable.getAssesments().get(assessmentName);
        data.assessmentsTable.setCurrentRow( data.assessmentsTable.getRowForAssessment(assessmentName));
        var specificationTitle = data.assessmentsTable.getSpecificationTitle();
        var gradingSchemeTitle = data.assessmentsTable.getGradingSchemeTitle();
        Europass.EuropassSubject.Achieved.WasDerivedFrom.SpecifiedBy.GradingScheme grading = null;
        if ( gradingSchemeTitle != null && !gradingSchemeTitle .isEmpty()) {
            grading = new Europass.EuropassSubject.Achieved.WasDerivedFrom.SpecifiedBy.GradingScheme( generateId("gradingScheme"), gradingSchemeTitle, null);
        }
        var specification = new Europass.EuropassSubject.Achieved.WasDerivedFrom.SpecifiedBy(generateId("assessmentSpecification"), specificationTitle, grading);
        var subAssessmentNames = data.assessmentsTable.getSubAssessments();
        List<Europass.EuropassSubject.Achieved.WasDerivedFrom> subAssessments = null;
        if ( !subAssessmentNames.isEmpty()) {
            subAssessments = new ArrayList<>();
            for ( String subAssessment : subAssessmentNames ) {
                subAssessments.add(createAssessment(subAssessment));
            }
        }
        
        return new Europass.EuropassSubject.Achieved.WasDerivedFrom( generateId("assessment"), assessmentName, grade.toString(), null, subAssessments, specification );
    }
    
    private Europass.EuropassSubject.Achieved.SpecifiedBy createLearningSpecification() {
        String specificationTitle = data.achievementsTable.getSpecificationTitle();
        var specification = new Europass.EuropassSubject.Achieved.SpecifiedBy(generateId("learningSpecification"), null, specificationTitle, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        specification.setLearningSetting(data.achievementsTable.getLearningSetting());
        specification.setLearningOpportunityType(List.of(data.achievementsTable.getLearningOpportunityType()));
        specification.setECTSCreditPoints(data.achievementsTable.getEctsCreditPoints());
        var outcomes = new ArrayList<Europass.EuropassSubject.Achieved.SpecifiedBy.LearningOutcome>();
        for ( String outcomeName : data.achievementsTable.getLearningOutcomes()) {
            var outcome = new Europass.EuropassSubject.Achieved.SpecifiedBy.LearningOutcome(generateId("learningOutcome"), outcomeName, null, null, null, null, null, null);
            data.outcomesTable.setCurrentRow(data.outcomesTable.getRowForLearningOutcome(outcomeName));
            outcome.setDefinition(data.outcomesTable.getDescription());
            outcome.setRelatedESCOSkill(data.outcomesTable.getEscoSkills());
            outcomes.add(outcome);
        }
        specification.setLearningOutcome(outcomes);
        return specification; 
    }
    
    private List< Europass.EuropassSubject.Achieved.WasInfluencedBy > getLearningActivities() {
        List< Europass.EuropassSubject.Achieved.WasInfluencedBy > activities = new ArrayList<>();
        for ( String activityName : data.achievementsTable.getActivities() ) {
            int row = data.activitiesTable.getRowForActivity(activityName);
            data.activitiesTable.setCurrentRow(row);
            var specificationTitle = data.activitiesTable.getSpecificationTitle();
            String activityType = data.activitiesTable.getActivityType();
            var specification = new Europass.EuropassSubject.Achieved.WasInfluencedBy.SpecifiedBy( generateId("learningActivitySpecification"), specificationTitle, null, List.of(activityType), null, null, null, null, null, null, null, null, null, null, null);
            specification.setMode(List.of(data.activitiesTable.getModeOfLearning()));
            String description = data.activitiesTable.getDescription();
            activities.add( new Europass.EuropassSubject.Achieved.WasInfluencedBy(generateId("learningActivity"), null, activityName, description, null, null, null, null, null, specification));
        }
        return activities;
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
    
    /** Generate a random id for the given type of object.
     * @param type Type that will be a part of the id.
     * @return Id of the form urn:epass:type:random_uuid
     */
    private static String generateId(String type) {
        return "urn:epass:" +type +":" +UUID.randomUUID().toString();
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