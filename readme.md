# edclexcel2ebsi

A command line tool and java library for creating [EBSI](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/EBSI) credentials from [European Digital Certificate for Learning](https://europa.eu/europass/fi/node/797)
data stored to an [excel
file](https://europa.eu/europass/digital-credentials/issuer/#/home). Used in the
[MicroBlock project's EBSI demo](http://microblock.rd.tuni.fi/). Does not cover
all possible fields of the credential data model.

## Requirements

- Java JDK version 16

## Build

Build with gradle using the included gradle wrapper:

```bash
./gradlew build   
```

## Usage

Currently the tool expects the credential data to be located in the current
working directory in a file named `credentials.xlsm`. An example file is
provided with this tool.

The tool can be run with gradle:

```bash
./gradlew run --args "command line arguments"
```

Or the zip distribution created by the gradle build can be extracted and the tool can then be executed with:

bin/edcl2excel2ebsi command line arguments

Use the help command to see a list of commands.

A configuration file named `config.properties` located in the working directory
is used to set some options:

- `generateMissingDids`: Automatically generates dis for holder and issuer if they
  have not been defined in the config file. Value true or false.
- `issuer.did`: Did for issuer.
- `issuer.keyFile`: File containing issuer key pair in jwk format.
- `holder.did`: Did for holder.
- `holder.keyFile`: File containing holder key pair in jwk format. This is used
  when creating a verifiable presentation.

### Usage example

With the included example data a credential for the data and software business
module can be created for student who has the email address anna.makkara@tautest.edu
and stored to file diploma.json using the following command:

```bash
./gradlew run --args "issue -f diploma.json 'anna.makkara@tautest.edu' 'Data and Software Business module'"
```

## For developers

The [walt.id ssikit](https://github.com/walt-id/waltid-ssikit) is used for EBSI
related operations. [Apache poi](https://poi.apache.org/components/spreadsheet/)
is used for accessing the excel files. The CLI is implemented with [Picocli](https://picocli.info/).

You can create a javadoc for the project with gradle:

```bash
./gradlew javadoc
```

The main entry point for the library is the CredentialLib class. another notable
class is the custom DiplomaDataProvider class to be used with the ssikit for
providing credential contents from the Excel file.
