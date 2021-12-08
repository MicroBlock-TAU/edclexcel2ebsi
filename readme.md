# edclexcel2ebsi

A command line tool and java library for creating [EBSI](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/EBSI) credentials from [European Digital Certificate for Learning](https://europa.eu/europass/fi/node/797)
data stored to an [excel file](https://europa.eu/europass/digital-credentials/issuer/#/home). Very much a work in progress. Currently does not
create full, useful credentials.

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
course can be created for student who has the email address test2.dan2@test.dat
and stored to file diploma.json using the following command:

```bash
./gradlew run --args "issue -f diploma.json 'test2.dan2@test.dat' 'Data and software business'"
```

The following JSON is then produced and as can be seen it does not yet contain
much, the latest diploma schema is not yet used  and it has some placeholder
content:

```json
{
  "@context" : [ "https://www.w3.org/2018/credentials/v1" ],
  "credentialSchema" : {
    "id" : "https://api.preprod.ebsi.eu/trusted-schemas-registry/v1/schemas/0xbf78fc08a7a9f28f5479f58dea269d3657f54f13ca37d380cd4e92237fb691dd",
    "type" : "JsonSchemaValidator2018"
  },
  "credentialStatus" : {
    "id" : "https://essif.europa.eu/status/education#higherEducation#392ac7f6-399a-437b-a268-4691ead8f176",
    "type" : "CredentialStatusList2020"
  },
  "credentialSubject" : {
    "awardingOpportunity" : {
      "awardingBody" : {
        "homepage" : "https://www.tuni.fi/en/about-us/tampere-university",
        "id" : "id",
        "preferredName" : "Tampere University",
        "registration" : "FI-12345678"
      },
      "id" : "id",
      "identifier" : "identifier",
      "location" : "Tampere"
    },
    "dateOfBirth" : "2021-02-15",
    "familyName" : "dan2",
    "givenNames" : "test2",
    "gradingScheme" : null,
    "id" : "did:ebsi:zcfoE1icNUnE69dte1fu47Z",
    "identifier" : null,
    "learningAchievement" : {
      "id" : "urn:epass:learningAchievement:1",
      "title" : "Data and software business"
    },
    "learningSpecification" : {
      "id" : "urn:epass:qualification:1",
      "iscedfCode" : [ ],
      "nqfLevel" : [ ]
    }
  },
  "evidence" : {
    "documentPresence" : [ "Physical" ],
    "evidenceDocument" : [ "Passport" ],
    "id" : "https://essif.europa.eu/tsr-va/evidence/f2aeec97-fc0d-42bf-8ca7-0548192d5678",
    "subjectPresence" : "Physical",
    "type" : [ "DocumentVerification" ],
    "verifier" : "did:ebsi:2962fb784df61baa267c8132497539f8c674b37c1244a7a"
  },
  "expirationDate" : "2022-08-31T00:00:00Z",
  "id" : "education#higherEducation#50894b7b-5864-450f-82b7-4f9805164301",
  "issuanceDate" : "2021-12-08T16:24:45Z",
  "issuer" : "did:ebsi:zZHPCeQjSYdyeHWTpDKGwDJ",
  "validFrom" : "2021-08-31T00:00:00Z",
  "type" : [ "VerifiableCredential", "VerifiableAttestation", "VerifiableDiploma" ],
  "proof" : {
    "type" : "Ed25519Signature2018",
    "creator" : "did:ebsi:zZHPCeQjSYdyeHWTpDKGwDJ",
    "created" : "2021-12-08T14:24:45Z",
    "domain" : "https://api.preprod.ebsi.eu",
    "nonce" : "ade6cb66-4495-4908-8588-9def68415e42",
    "jws" : "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFZERTQSJ9..KjIxiTdjPMAnem9rNnz6xMf-9pgLXEbTd1UtQrxb6aTVRhQMQxkQ4e9WDd9MP7vVCtSc05CE749e882YMHtVAw"
  }
}
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
