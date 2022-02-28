package fi.tuni.microblock.edclexcel2ebsi;

import java.util.Map;

/** Mapping implementation that uses java maps as the container for the mapping info.
 * @author hylli
 *
 */
public class MapBasedVocabularyMapping implements VocabularyMapping {
    
    private Map<String, String> mappings;
    
    /** Build from the given map.
     * @param mappings contains the mapping information.
     */
    public MapBasedVocabularyMapping( Map<String, String> mappings ) {
        this.mappings = mappings;
    }

    @Override
    public String getUri(String label) {
        String uri =  mappings.get(label);
        if ( uri == null ) {
            throw new VocabularyMapping.MappingNotFoundException(label);        
        }
        return uri;
    }
}