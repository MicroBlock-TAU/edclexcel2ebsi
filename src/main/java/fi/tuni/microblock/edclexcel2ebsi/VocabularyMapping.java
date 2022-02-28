package fi.tuni.microblock.edclexcel2ebsi;

/** Used to map labels used in the excel to corresponding URIs used in the EBSI model. For example learning opportunity type Course to the corresponding URI.
 * Currently there is an implementation that just uses hardcoded java Maps. Other implementations that use for example information read from files could be implemented.   
 * @author hylli
 *
 */
public interface VocabularyMapping {
    
    /** Get URI for the given label.
     * @param label name of a label.
     * @return thecorresponding URI 
     * @throws MappingNotFoundException URI not found for the label.
     */
    public String getUri( String label ) throws MappingNotFoundException;
    
/** Exception when mapping is not found.
 * @author hylli
 *
 */
static public class MappingNotFoundException extends RuntimeException {
        
        private static final long serialVersionUID = 6412865397503832798L;

        /** Create for given label.
         * @param label the label.
         */
        public MappingNotFoundException( String label) {
            super("No mapping found for label " +label);
        }
    }
}