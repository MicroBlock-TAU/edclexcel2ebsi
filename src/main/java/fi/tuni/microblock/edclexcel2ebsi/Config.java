/* Copyright 2021 Tampere University
 * This software was developed as a part of the MicroBlock project: https://www.tuni.fi/en/research/microblock-advancing-exchange-micro-credentials-ebsi
 * This source code is licensed under the MIT license. See LICENSE in the repository root directory.
 * Author(s): Otto Hylli <otto.hylli@tuni.fi>
*/
package fi.tuni.microblock.edclexcel2ebsi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/** Class for reading the configuration file of this application and for accessing the configuration values.
 * 
 * Configuration file is a java properties file.
 * @author Otto Hylli
 *
 */
public class Config {
    
    private Properties properties;
    final static public String DEFAULT_CONFIG_FILE = "config.properties"; 
    
    /** Reads configuration from given file.
     * @param fileName configuration file name.
     */
    public Config( String fileName ) {
        try ( var input = new FileInputStream(fileName)) {
            properties = new Properties();
            properties.load(input);
        }
        
        catch ( IOException e ) {
            System.out.println("unable to read config file " +e.getMessage());
            System.exit(1);
        }
    }
    
    /** Read configuration from default file.
     * 
     */
    public Config() {
        this( DEFAULT_CONFIG_FILE );
    }
    
    /** Get value of given configuration option.
     * @param name Name of configuration option.
     * @return Value for configuration value. Null if not found.
     */
    public String get(String name) {
        return properties.getProperty(name);
    }
    
    /** Get value for a boolean config option.
     * @param name config name
     * @return The config value. Null if not set or incorrect value.
     */
    public Boolean is( String name ) {
        var value = get(name);
        if ( value == null ) {
            return null;
        }
        return Map.of( "true", true, "false", false ).get(value);
    }
}