/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import id.walt.auditor.Auditor;
import id.walt.auditor.JsonSchemaPolicy;
import id.walt.auditor.TrustedIssuerRegistryPolicy;
import id.walt.auditor.SignaturePolicy;
import id.walt.auditor.TrustedIssuerDidPolicy;
import id.walt.auditor.TrustedSubjectDidPolicy;
import id.walt.auditor.VerificationResult;
import id.walt.crypto.KeyId;
import id.walt.custodian.Custodian;
import id.walt.model.DidEbsi;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.servicematrix.ServiceRegistry;
import id.walt.services.did.DidService;
import id.walt.services.key.KeyService;
import id.walt.signatory.ProofConfig;
import id.walt.signatory.ProofType;
import id.walt.signatory.Signatory;

/** Class for creating, presenting and verifying credentials based on EDCL excel data. 
 * @author Otto Hylli
 *
 */
public class CredentialLib {

    String holderDid;
    String issuerDid;
    private Config config;
    private CredentialData credentialData;
    
    /** Create CredentialLib from the default config file location.
     * 
     */
    public CredentialLib() {
        this("config.properties");
    }
    
    /** Create CredentialLib using the given config file which determines the issuer and holder dids and keys.
     * 
     * Registers the custom diploma data provider to be used with diploma vcs. Also creates walt.id service configuration if it has not been created already.
     * @param configFile File name of used config file.
     */
    public CredentialLib(String configFile) {
        config = new Config(configFile);
        // this may be used of a part of application that has already created its own ser´vice configuration.
        if ( ServiceRegistry.INSTANCE.getServices().isEmpty() ) {
            new ServiceMatrix("service-matrix.properties");
        }
        
        credentialData = new CredentialData();
        issuerDid = config.get("issuer.did");
        var createDids = config.is( "generateMissingDids" );
        if ( issuerDid == null ) {
            if ( createDids ) {
                issuerDid = DidService.INSTANCE.create(DidMethod.ebsi, null, null);
            }
            
            else {
                System.out.println("Issuer did missing and autogeneration set to false.");
                System.exit(1);
            }
        }
        
        holderDid = config.get("holder.did");
        if ( holderDid == null ) {
            if ( createDids ) {
                holderDid = DidService.INSTANCE.create(DidMethod.ebsi, null, null);
            }
            
            else {
                System.out.println("Holder did missing and autogeneration set to false.");
                System.exit(1);
            }
        }
        
        try {
            var issuerKeyFile = config.get("issuer.keyFile");
            if ( issuerKeyFile != null ) {
                importDid( issuerDid, readStringFromFile( issuerKeyFile ));
            }
            
            var holderKeyFile = config.get("holder.keyFile");
            if ( holderKeyFile != null ) {
                importDid( holderDid, readStringFromFile( holderKeyFile ));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println( "Unable to read key file: " +e.getMessage());
            System.exit(1);
        }
    }

    /** Create diploma for student with given email who has the given achievement.
     * @param email Email address of a student that should be in the excel file.
     * @param title Title of credential  that a student in the excel has.
     * @return The verifiable diploma created from the source data.
     * @throws DiplomaDataProvider.RequiredDataNotFoundException Some required data was not found for example there is no student with given email.
     * @throws DiplomaDataProvider.ExcelStructureException The structure of the excel file was not what was expected for example there is no column for student email address.
     */
    public String createDiploma( String email, String title ) throws DiplomaDataProvider.RequiredDataNotFoundException, DiplomaDataProvider.ExcelStructureException {
        var signatory = Signatory.Companion.getService();
        
        var proofConfig = new ProofConfig(issuerDid, holderDid, null, null, ProofType.LD_PROOF, null, null, null, null, null, null, null, null );
        var diploma = signatory.issue("Europass", proofConfig, createDataProvider( email, title ));
        return diploma;
    }
    
    public String createId( String email) throws DiplomaDataProvider.RequiredDataNotFoundException, DiplomaDataProvider.ExcelStructureException {
        var signatory = Signatory.Companion.getService();
        
        var proofConfig = new ProofConfig(issuerDid, holderDid, null, null, ProofType.LD_PROOF, null, null, null, null, null, null, null, null );
        var id = signatory.issue("VerifiableId", proofConfig, createDataProvider( email, null ));
        return id;
    }

    /** Create a presentation of the given diploma credential.
     * 
     * Requires that holder did and key have been given in config.
     * @param diploma The verifiable diploma json.
     * @return The verifiable presentation of the diploma.
     */
    public String createPresentation( List<String> credentials) {
        var custodian = Custodian.Companion.getService();
        var diplomaVp = custodian.createPresentation(credentials, holderDid, null, null, null, null);
        return diplomaVp;
    }
    
    /** Import the given did and private key to be used with it.
     * @param didStr The ebsi did to be imported.
     * @param keyStr The key in the jwk format.
     */
    void importDid(String didStr, String keyStr) {
        var ebsiDid = (DidEbsi)DidService.INSTANCE.loadOrResolveAnyDid(didStr);
        if ( ebsiDid == null ) {
            throw new IllegalArgumentException("could not resolve did.");
        }
    
        var keyId = new KeyId(ebsiDid.getVerificationMethod().get(0).getPublicKeyJwk().getKid());
        
        var keyService = KeyService.Companion.getService();
        try {
            // todo: remove this now unnecessary reflection stuff which is no longer required since import method which could not be called from java was renamed.
            var method = KeyService.class.getDeclaredMethod("importKey", String.class);
            keyId = (KeyId)method.invoke(keyService, keyStr );
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            //System.out.println("avain jo olemasssa");
        }
        
        try {
            keyService.load(didStr);
        }
        
        catch (Exception e) {
            keyService.addAlias( keyId, didStr);
        }
    }

    /** Verify the given diploma or presentation.
     * 
     * Verified using trusted did policy for subject and holder, and signature policy.
     * @param diplomaVp The credential to verify.
     * @return Verification result.
     */
    public VerificationResult verifyDiploma(String diplomaVp) {
        //var result = Auditor.Companion.getService().verify(diplomaVp, List.of(new TrustedIssuerDidPolicy(), new TrustedSubjectDidPolicy(), new SignaturePolicy(), new JsonSchemaPolicy(), new TrustedIssuerRegistryPolicy() ));
        var result = Auditor.Companion.getService().verify(diplomaVp, List.of(new TrustedIssuerDidPolicy(), new TrustedSubjectDidPolicy(), new SignaturePolicy() ));
        return result;
    }
    
    public List<String> listCredentialsForStudent( String email ) {
        return credentialData.listCredentialsForStudent(email);
    }
    
    /** Check if there is personal data for student with given email.
     * @param email student email
     * @return true if data is found, false if not.
     */
    public boolean studentExists( String email ) {
        return credentialData.studentExists(email);
    }
    
    /** Create a DiplomaDataProvider for creating a credential for the given student for credential with given title.
     * @param email student email address.
     * @param title title of a credential
     * @return diploma data provider for the given parameters.
     */
    public DiplomaDataProvider createDataProvider( String email, String title ) {
        return new DiplomaDataProvider( credentialData, email, title );
    }
    
    /** Get the did of configured issuer.
     * @return issuer did
     */
    public String getIssuerDid() {
        return issuerDid;
    }
    
    /** Helper method used to write contents of given string to a file with given path.
     * @param fileName Name of file.
     * @param content Content to be written to the file.
     * @throws IOException Issue in writing to the file.
     */
    public static void writeToFile( String fileName, String content ) throws IOException {
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }
    
    /** Helper utility method for reading contents of a file to a string.
     * @param fileName File to read.
     * @return Contents of the file.
     * @throws IOException Unable to read the file for example file not found.
     */
    public static String readStringFromFile(String fileName) throws IOException {
        var path = Path.of(fileName);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}