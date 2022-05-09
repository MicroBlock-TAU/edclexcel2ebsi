/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

//import id.walt.vclib.VcLibManager;
import id.walt.vclib.model.VerifiableCredential;
import id.walt.vclib.credentials.Europass;
import id.walt.vclib.credentials.VerifiableDiploma;
import id.walt.vclib.credentials.VerifiableId;

/** Tests for the CredentialLib class.
 * @author Otto Hylli
 *
 */
class CredentialLibTest {
    
    private CredentialLib credentials;
    private final String TEST_STUDENT_EMAIL = "anna.makkara@tautest.edu";
    
    /** Create based on config file for tests.
     * 
     */
    public CredentialLibTest() {
        var configFile = Path.of("src", "test", "resources", "config.properties");
        credentials = new CredentialLib(configFile.toString());
    }
    
    /** Helper method for creating a test diploma.
     * @return the credential
     */
    private String createTestDiploma() {
        String diploma = credentials.createDiploma("anna.makkara@tautest.edu", "Data and Software Business module");
        assertNotNull(diploma, "app should create a diploma.");
        return diploma;
    }
    
    /** Helper method for creating a test id.
     * @return the credential
     */
    private String createTestId() {
        String id = credentials.createId("anna.makkara@tautest.edu");
        assertNotNull(id, "app should create a id.");
        return id;
    }
    
    /** Test that the diploma is created and it contains correct information.
     * 
     */
    @Test void appCreatesCredential() {
        String diploma = createTestDiploma();
        Europass vc = (Europass)VerifiableCredential.Companion.fromString(diploma);
        assertEquals("2021-12-15T00:00:00Z", vc.getValidFrom());
        var subject = vc.getCredentialSubject();
        var achievement = subject.getAchieved().get(0);
        assertEquals(achievement.getTitle(), "Data and Software Business" );
        var assessment = achievement.getWasDerivedFrom().get(0);
        assertEquals( "Overall grade", assessment.getTitle());
        assertEquals( "4.0", assessment.getGrade());
        var subAssessments = assessment.getHasPart();
        Map<String, String> expectedGrades = Map.of( "Individual assignment1", "3.0", "Individual assignment2", "5.0", "Project assignment", "4.0" );
        assertEquals( expectedGrades.keySet().size(), subAssessments.size());
        for ( var subAssessment : subAssessments ) {
            assertTrue( expectedGrades.containsKey(subAssessment.getTitle()));
            assertEquals( expectedGrades.get(subAssessment.getTitle()), subAssessment.getGrade());
        }
    }
    
    /** Check that verification of diploma succeeds.
     * 
     */
    @Test void verifyDiploma() {
        String diploma = createTestDiploma(); 
        checkVerification(diploma);
    }
    
    /** Check that verification of presentation of the diploma and id works.
     * 
     */
    @Test void verifyPresentation() {
        var diploma = createTestDiploma();
        var id = createTestId();
        var vp = credentials.createPresentation(List.of(diploma, id));
        assertNotNull( vp, "Should create a presentation." );
        checkVerification(vp);
    }
    
    /** Create a test id and check that it has expected values.
     * 
     */
    @Test void testCreateId() {
        String idStr = createTestId();
        VerifiableId id = (VerifiableId)VerifiableCredential.Companion.fromString(idStr);
        var subject = id.getCredentialSubject();
        assertEquals( "Anna", subject.getFirstName());
        assertEquals( "Makkara", subject.getFamilyName());
        assertEquals( "1994-03-15", subject.getDateOfBirth());
        var identifier = subject.getIdentifier().get(0);
        assertEquals( "Student identification number", identifier.getSchemeID() );
        assertEquals( "x94476556", identifier.getValue());
    }
    
    /** Test verification of student id.
     * 
     */
    @Test void verifyId() {
        var id = createTestId();
        checkVerification(id);
    }
    
    /** Helper method for checking that a verification result is successful.
     * @param credential presentation or diploma.
     */
    private void checkVerification(String credential) {
        var result = credentials.verifyDiploma(credential);
        for ( var policy : result.getPolicyResults().entrySet() ) {
            assertTrue( policy.getValue(), policy.getKey() + " verification policy.");
        }
        assertTrue( result.getValid(), "Overall verification status." );
    }
    
    @Test void listCredentialsForStudent() {
        var email = "anna.makkara@tautest.edu"; 
        var expectedAchievements = List.of("Data and Software Business module");
        var achievements = credentials.listCredentialsForStudent(email);
        assertTrue( achievements.size() == expectedAchievements.size() && expectedAchievements.containsAll(achievements) && achievements.containsAll(expectedAchievements), "expected " +expectedAchievements + " got " +achievements  );
    }
    
    /** Test we get credential type and schema.
     * 
     */
    @Test void getCredentialSchemaAndType() {
        assertEquals( "Europass", DiplomaDataProvider.getCredentialType());
        assertEquals("https://raw.githubusercontent.com/walt-id/waltid-ssikit-vclib/master/src/test/resources/schemas/Europass.json", DiplomaDataProvider.getCredentialSchema());
    }
    
    /** Test that checking if student exists works.
     * 
     */
    @Test void studentExists() {
        assertTrue( credentials.studentExists(TEST_STUDENT_EMAIL));
        assertFalse( credentials.studentExists("test@test.fi"));
    }
    
}
