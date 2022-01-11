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

//import id.walt.vclib.VcLibManager;
import id.walt.vclib.model.VerifiableCredential;
import id.walt.vclib.credentials.VerifiableDiploma;

/** Tests for the CredentialLib class.
 * @author Otto Hylli
 *
 */
class CredentialLibTest {
    
    private CredentialLib credentials;
    
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
        String diploma = credentials.createDiploma("jane2.doe2@test.edu", "Data and Software Business module");
        assertNotNull(diploma, "app should create a diploma.");
        return diploma;
    }
    
    /** Test that the diploma is created and it contains correct information.
     * 
     */
    @Test void appCreatesCredential() {
        String diploma = createTestDiploma();
        VerifiableDiploma vc = (VerifiableDiploma)VerifiableCredential.Companion.fromString(diploma);
        var subject = vc.getCredentialSubject();
        assertEquals(subject.getFamilyName(), "Doe2");
        assertEquals(subject.getGivenNames(), "Jane2" );
        var achievement = subject.getLearningAchievement();
        assertEquals(achievement.getTitle(), "Data and Software Business" );
        assertEquals( subject.getAwardingOpportunity().getAwardingBody().getPreferredName(), "Tampere University" );
    }
    
    /** Check that verification of diploma succeeds.
     * 
     */
    @Test void verifyDiploma() {
        String diploma = createTestDiploma(); 
        checkVerification(diploma);
    }
    
    /** Check that verification of presentation of the diploma works.
     * 
     */
    @Test void verifyPresentation() {
        var diploma = createTestDiploma();
        var vp = credentials.createPresentation(diploma);
        assertNotNull( vp, "Should create a presentation." );
        checkVerification(vp);
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
        var email = "jane2.doe2@test.edu"; 
        var expectedAchievements = List.of("Data and Software Business module");
        var achievements = credentials.listCredentialsForStudent(email);
        assertTrue( achievements.size() == expectedAchievements.size() && expectedAchievements.containsAll(achievements) && achievements.containsAll(expectedAchievements), "expected " +expectedAchievements + " got " +achievements  );
    }
}
