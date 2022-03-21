/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import id.walt.auditor.VerificationResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** Main class which implements the picocli based command line interface.
 * @author Otto Hylli
 *
 */
@Command( name = "edcl2ebsi", subcommands = {CommandLine.HelpCommand.class},
        description = "Create ebsi credentials from EDCL excel data.")
public class Cli {
    
    @Spec CommandSpec spec;
    /** Used for actual credential operations: issue, present verify
     */
    private CredentialLib credentials;

    /** Create the cli with a CredentialLib instance.
     * 
     */
    public Cli() {
        credentials = new CredentialLib();
    }
    
    /** A convenience command for quicly issuing, presenting and veryfying.
     * 
     * Diploma and presentation are saved to files and also output to the console.
     */
    @Command( name = "demo", description = "Demo app by creating, presenting and veryfying a diploma for student in the test data.")
    public void demo() {
        Logger logger = LoggerFactory.getLogger(Cli.class);
        logger.info("Starting ssikit test.");
        
        try {
            var id = credentials.createId("jane2.doe2@test.edu");
            System.out.println("Student id:");
            System.out.println(id);
            CredentialLib.writeToFile("id.json", id);
            var diploma = credentials.createDiploma( "jane2.doe2@test.edu", "Data and Software Business module" );
            System.out.println("Diploma:");
            System.out.println(diploma);
            CredentialLib.writeToFile("diploma.json", diploma);
            var diplomaVp = credentials.createPresentation( List.of(CredentialLib.readStringFromFile("diploma.json"), CredentialLib.readStringFromFile("id.json")) );
            System.out.println("Diploma presentation:");
            System.out.println(diplomaVp);
            CredentialLib.writeToFile("presentation.json", diplomaVp);
            var result = credentials.verifyDiploma( CredentialLib.readStringFromFile("presentation.json") );
            printVerificationResult(result);
        }
        
        catch (DiplomaDataProvider.ExcelStructureException e) {
            System.out.println("Invalid excel file: " +e.getMessage());
        }
        
        catch (DiplomaDataProvider.RequiredDataNotFoundException e ) {
            System.out.println("Required data for creating diploma not found " +e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /** Issue a diploma from the excel data.
     * @param fileName File where the created diploma should be saved to.
     * @param email Email address of the student the diploma should be issued for.
     * @param achievement Achievement i..e course of the student the diploma is for.
     */
    @Command( name = "issue", description = "Issue a diploma.")
    public void issueDiploma(
            @Option(names = { "-f", "--file" }, required = true, paramLabel = "CREDENTIAL_FILE", description = "File name for the issued credential")
            String fileName,
            @Parameters( index = "0", paramLabel = "email", description = "The email address of the student the credential should be issued to." )
            String email,
            @Parameters( index = "1", paramLabel = "achievement", description = "Name of the course the diploma is issued for." )
            String achievement
            ) {
        try {
            var diploma = credentials.createDiploma( email, achievement );
            CredentialLib.writeToFile(fileName, diploma);
        } catch (IOException e) {
            System.out.println( "Unable to write diploma to file " +fileName +": " +e.getMessage());
        }
        
        catch ( DiplomaDataProvider.RequiredDataNotFoundException e) {
            System.out.println( "Unable to create diploma: " +e.getMessage());
        }
        
        catch ( DiplomaDataProvider.ExcelStructureException e) {
            System.out.println( "Unable to create diploma: " +e.getMessage());
        }
    }
    
    /** Create a presentation of the given diploma.
     * @param presentationFile File where the presentation should be saved to.
     * @param credentialFile File containing the credential that should be presented.
     */
    @Command( name = "present", description = "Create a presentation of the given credential.")
    public void present(
            @Option(names = { "-f", "--file" }, required = true, paramLabel = "PRESENTATION_FILE", description = "File name for the verifiable presentation.")
            String presentationFile,
            @Parameters( index = "0", paramLabel = "CREDENTIAL_FILE", description = "File containing the credential the presentation is created for." )
            String credentialFile
            ) {
        try {
            var credential = CredentialLib.readStringFromFile(credentialFile);
            var presentation = credentials.createPresentation(List.of(credential));
            CredentialLib.writeToFile(presentationFile, presentation);
        }
        
        catch (IOException e) {
            System.out.println("Unable to create presentation " +e.getMessage());
        }
    }
    
    /** Verify the given presentation or credential.
     * @param fileName File containing the presentation or credential to be verified.
     */
    @Command( name = "verify", description = "Verify a verifiable presentation or credential.")
    public void verify(
            @Parameters( index = "0", paramLabel = "FILE", description = "File containing the credential or presentation to verify." )
            String fileName
            ) {
        try {
            var credential = CredentialLib.readStringFromFile(fileName);
            var result = credentials.verifyDiploma(credential);
            printVerificationResult(result);
        }
        
        catch ( IOException e) {
            System.out.println("Unable to open file: " +e.getMessage());
        }
    }
    
    /** Prints information about the given verification result: the overall status and status of each policy.
     * @param result Verification result whose information should be printed.
     */
    private void printVerificationResult( VerificationResult  result ) {
        System.out.println("Verification result status: " +result.getValid());
        for ( var part : result.getPolicyResults().entrySet() ) {
            System.out.println( "Verification policy " +part.getKey() +" status: " +part.getValue() );            
        }
    }
     
    /** Initialise the picocli application.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
         int exitCode = new CommandLine(new Cli()).execute(args);
         System.exit(exitCode);
    }
}