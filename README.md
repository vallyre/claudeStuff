# Standard Codes Gateway Service API

## Overview

The Standard Codes Gateway Service provides a robust API for retrieving standard code responses by their UUIDs. This service allows clients to search for multiple standard codes in a single request and receive consolidated responses.

## UUID Types and Response Behavior

The system supports two different types of UUIDs that determine what content is returned:

1. **Master Content UUID** - When you pass the content master level UUID (found as "id" at the root node):
   - The system returns the latest active version of the content
   - This is useful when you always want the most current version

2. **Version-Specific UUID** - When you pass a specific version UUID (found as "identifier.value" in the response):
   - The system returns that exact version of the content
   - Useful when you need a specific historical version

### Content Status Indicators
- **Active Content**: `effectivePeriod.end` is null
- **Inactive Content**: `effectivePeriod.end` contains a date indicating when the content was deprecated

### Content Types
- **SDOH Content** (Social Determinants of Health): `resourceType="bundle"` 
  - Includes assessments, goals, and interventions
- **List Content**: `resourceType="valueSet"`

### Version Information
- `versionId` field indicates the specific version of the content

## API Endpoints

### Search Responses by UUIDs

Retrieves standard code responses using a list of master UUIDs.

**Endpoint:** `POST /api/v1/standard-codes/responses/search`

**Content Type:** `application/json`

**Authorization:** No authentication currently required (permitAll)

#### Request Body

```json
{
  "operation": "SEARCH",
  "parameters": {
    "uuids": [
      "89916142-7ea8-4cdd-b313-58131b297835",
      "ded9327f-8d0f-4148-a383-f291047831c4",
      "2ccc0965-f500-4602-bd91-83029b42b596",
      "4861b05d-c84e-44a0-aa29-0a7e981b84fd"
    ]
  },
  "priority": "HIGH"
}
```

#### Request Fields

| Field            | Type           | Required | Description                                             |
| ---------------- | -------------- | -------- | ------------------------------------------------------- |
| operation        | String         | Yes      | Operation type (currently only "SEARCH" is supported)   |
| parameters.uuids | Array of UUIDs | Yes      | List of master UUIDs to search for                      |
| priority         | String         | No       | Priority of the request (e.g., "HIGH", "MEDIUM", "LOW") |

#### Response Structure

```json
{
  "responseId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7a39f6f0-3db8-4fc3-b143-39aa13b53756",
  "timestamp": "2025-05-16T12:34:56.789Z",
  "status": "SUCCESS",
  "responses": [
    {
      // Standard code response data (JsonNode)
    },
    {
      // Standard code response data (JsonNode)
    }
    // Additional responses...
  ],
  "error": null
}
```

#### Response Fields

| Field      | Type          | Description                                         |
| ---------- | ------------- | --------------------------------------------------- |
| responseId | String        | Unique ID for this response                         |
| requestId  | String        | Unique ID for the processed request                 |
| timestamp  | ZonedDateTime | Timestamp when the response was generated           |
| status     | String        | Status of the request (e.g., "SUCCESS", "ERROR")    |
| responses  | Array         | List of standard code responses as JsonNode objects |
| error      | Object        | Error details (null if status is "SUCCESS")         |

#### Error Response Structure

```json
{
  "responseId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7a39f6f0-3db8-4fc3-b143-39aa13b53756",
  "timestamp": "2025-05-16T12:34:56.789Z",
  "status": "ERROR",
  "responses": null,
  "error": {
    "timestamp": "2025-05-16T12:34:56.789",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid request parameters",
    "path": "/api/v1/standard-codes/responses/search"
  }
}
```

## Response Codes

| Status Code | Description                                                    |
| ----------- | -------------------------------------------------------------- |
| 200         | Successfully retrieved responses                               |
| 400         | Invalid request (missing required fields, invalid UUIDs, etc.) |
| 500         | Internal server error                                          |

## Sample Usage

### Java Example

```java
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

// ...

public JsonResponse searchStandardCodes(List<UUID> uuids) {
    RestTemplate restTemplate = new RestTemplate();
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    MasterUuidRequest request = new MasterUuidRequest();
    request.setOperation("SEARCH");
    
    UuidParameters parameters = new UuidParameters();
    parameters.setUuids(uuids);
    request.setParameters(parameters);
    
    request.setPriority("HIGH");
    
    HttpEntity<MasterUuidRequest> entity = new HttpEntity<>(request, headers);
    
    return restTemplate.postForObject(
        "http://localhost:8080/api/v1/standard-codes/responses/search",
        entity,
        JsonResponse.class
    );
}
```

## Working with Response Data

### Handling ValueSet Responses

When working with `resourceType="valueSet"` responses:

1. Access `answerOption` array to retrieve available code options
2. Each `valueCoding` contains:
   - `code`: The unique code identifier
   - `system`: The coding system (e.g., "SNOMEDCT")
   - `display`: Human-readable description
   - `extension`: Additional properties including unique IDs

### Determining Content Status

To determine if content is active or inactive:

```java
boolean isActive = response.getEffectivePeriod().getEnd() == null;
```

### Getting Content Version

```java
String version = response.getMeta().getVersionId();
```

## Validation Rules

- The `operation` field must not be blank
- The `parameters` field must not be null
- The `uuids` list must not be empty
- All UUIDs must be in valid UUID format

## Notes

- Response data format depends on the standard codes that are retrieved
- Responses are returned in the same order as the requested UUIDs
- If a UUID is not found, the corresponding response entry will be null
- UUIDs should be provided in the standard UUID format (e.g., "550e8400-e29b-41d4-a716-446655440000")
- In the response payload, note that `resourceType` indicates the type of content:
  - `resourceType="valueSet"` indicates a list (as shown in the example)
  - `resourceType="bundle"` indicates SDOH content (assessments, goals, interventions)
- For `valueSet` responses, the content includes a list of codes in the `answerOption` array
- Empty `effectivePeriod.end` indicates active content, while a populated date indicates the end date of that specific content
- The `versionId` field indicates the specific version of the content

## API Versioning

This API is versioned in the URL path (`/api/v1/`). Future versions will be released under different version paths (e.g., `/api/v2/`).

## Best Practices

1. **Cache Responses**: For frequently accessed standard codes, consider implementing client-side caching to reduce server load and improve performance.

2. **Error Handling**: Implement robust error handling to manage different response codes and error conditions gracefully.

3. **Bulk Requests**: Group related UUIDs in a single request rather than making multiple individual requests to reduce network overhead.

4. **Version Tracking**: If specific versions are required, store the version-specific UUIDs rather than master content UUIDs to ensure consistency.

5. **Priority Setting**: Use the "priority" field appropriately - set to "HIGH" only for time-sensitive operations.

## Implementation Considerations

### Performance
- The service is optimized for batch retrieval - requesting multiple UUIDs in a single call is more efficient than multiple individual requests
- Response sizes can vary significantly depending on the complexity of the requested content

### Security
- While currently the endpoint uses `permitAll()`, it's recommended to implement appropriate authentication in production environments
- Consider using HTTPS to ensure secure transmission of data

### Monitoring
- The service includes logging for request tracking and debugging
- Each response includes a unique `responseId` and `requestId` that can be used for tracing requests

## Support

For any issues or questions regarding this API, please contact the CMT team.

## Appendix

### A. cURL Example

```bash
curl --location 'http://localhost:8080/api/v1/standard-codes/responses/search' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=367DF526EF641BA5ED304CF9D422709E' \
--data '{
    "operation": "SEARCH",
    "parameters": {
      "uuids": [
        "89916142-7ea8-4cdd-b313-58131b297835",
        "ded9327f-8d0f-4148-a383-f291047831c4",
        "2ccc0965-f500-4602-bd91-83029b42b596",
        "4861b05d-c84e-44a0-aa29-0a7e981b84fd"
      ]
    },
    "priority": "HIGH"
  }'
```

### B. Example Response Payloads

#### B.1. Care Team Member Role Value Set Response

```json
{
    "responseId": "9726a3c8-422a-4178-ae93-6897348708f7",
    "requestId": "f24ed5b3-6413-437a-b039-86824c028421",
    "timestamp": "2025-05-16T18:04:04.147769-04:00",
    "status": "SUCCESS",
    "responses": [
       {
    "resourceType": "valueSet",
    "id": "3dadf3a5-1876-40f1-b2d3-123f220bab90",
    "identifier": [
        {
            "system": "http://hl7.org/fhir/us/core/ValueSet",
            "value": "2ccc0965-f500-4602-bd91-83029b42b596"
        }
    ],
    "url": "http://hl7.org/fhir/us/core/ValueSet/care-team-member-role-uscdi-v3",
    "version": "3.0.0",
    "name": "CareTeamMemberRoleUSCDIv3",
    "title": "Care Team Member Role USCDI v3",
    "status": "active",
    "experimental": false,
    "date": "2025-05-16T16:00:00",
    "publisher": "CCMT",
    "description": "This value set defines the comprehensive set of codes that can be used to indicate the Care Team Member Role according to USCDI v3 requirements. It includes care team member role codes from LOINC and SNOMED CT",
    "purpose": "To provide a standardized set of Care Team Member Role codes for healthcare documentation and interoperability.",
    "approvalDate": "2025-05-12",
    "lastReviewDate": "2025-05-16",
    "effectivePeriod": {
        "start": "2025-05-12",
        "end": ""
    },
    "meta": {
        "versionId": "3.0.0",
        "lastUpdated": "2025-05-16T16:00:00Z",
        "profile": [
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-valueset"
        ]
    },
    "item": [
        {
            "linkId": "http://hl7.org/fhir/us/core/ValueSet/care-team-member-role-uscdi-v3",
            "definition": "http://hl7.org/fhir/us/core/StructureDefinition/us-core-valueset",
            "text": "Care Team Member Role USCDI v3",
            "type": "Display",
            "answerType": "Choice",
            "required": false,
            "extension": [
                {
                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                    "valueId": "000de6f0-6cf1-464e-86bd-e09eaaf7f2b5"
                }
            ],
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1012901000168106",
                        "display": "Gynecologic oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2fbb1d8c-a2fe-4d58-a426-cf229558b446"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1032781000168105",
                        "display": "Kinesiology practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3187ab35-733b-436f-ad06-721eead29d07"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106289002",
                        "display": "Dentist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f053c3d2-0f3b-4281-8975-99d9bbfcc8c4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106292003",
                        "display": "Professional nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "db3b9f70-52f6-42b0-b635-2bc1edec75f2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106293008",
                        "display": "Nursing personnel (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9d567572-87fa-4c94-8a16-7611ee839503"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106294002",
                        "display": "Midwifery personnel (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c121ecb-70ae-4a0d-a4f1-8c50f5da59a4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106310008",
                        "display": "Worker in religion (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "83d6ea1f-fa54-4033-93ca-1f7b23193513"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106311007",
                        "display": "Minister of religion/related member of religious order (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "25ad4545-817c-40d6-961f-7be1c880c920"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "106330007",
                        "display": "Philologist, translator/interpreter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1d8d42e7-e5a9-4074-af63-2b9cc5cdac6e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "11015003",
                        "display": "Minister of religion (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "76d10fe4-6131-4156-8e7b-8c1f42149c89"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "116154003",
                        "display": "Patient (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f17930af-49ce-457a-97bb-439482e0f5cc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "11661002",
                        "display": "Neuropathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ff713186-fa67-43ea-a916-c43266e7b2b1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1172950003",
                        "display": "Massage therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b95d025b-2313-4524-867f-1342bf95c8ea"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1186716007",
                        "display": "Intellectual disability psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7d524913-a32e-4199-be5d-450cfb3d1e36"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1186914001",
                        "display": "Intellectual disability nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e1d5398c-1814-4aff-a790-1b2b5c0489e9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "11911009",
                        "display": "Nephrologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6c46a0d2-9bcc-4a1f-8332-402ae6b7b729"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "119246008",
                        "display": "Imam (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "448d9840-d151-48e1-93b7-30dec9410583"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "11935004",
                        "display": "Obstetrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3b90ad51-0436-41a6-ade8-b0ea8231a935"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1251537007",
                        "display": "Sport medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6d75ecfa-f97d-48d8-90c3-beaf5a9e7169"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1251542004",
                        "display": "Medical coder (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6ea0f350-02ce-43ad-8c17-0a650bb6b848"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1251548000",
                        "display": "Neuroradiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fb519393-27dd-46ca-95cd-6c86d4e46657"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1254982001",
                        "display": "Medical surgical nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ea268058-55e1-47cc-85dd-0c5fa86cd614"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1254983006",
                        "display": "Chronic care nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "12b6feb3-53f4-4357-a4db-48566fca8484"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1254984000",
                        "display": "Rehabilitation nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ef246787-7c84-4eaa-8f9a-77e84c3f83a6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255370008",
                        "display": "Specialist in naturopathy (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e326d404-fc28-4b54-97bb-48f3345f87fa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255371007",
                        "display": "Specialist in homeopathy (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8417f940-bedc-451a-ae34-71de0c8c8c1e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255372000",
                        "display": "Phytotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a67e0461-15b7-4409-83ca-a7ec71d51a61"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255373005",
                        "display": "Specialist in traditional Chinese medicine (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2409d2ad-eb52-44ce-a8cc-d598d9addd6b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255374004",
                        "display": "Clinical nutritionist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7e52adec-8bb6-4151-9b9d-a8827d4bb48e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255514008",
                        "display": "Regulatory affairs pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "318f8c88-c810-4e2d-bc1d-3acdb0d67a77"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255515009",
                        "display": "Pharmacogenomics pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dda8f55d-ed4c-45eb-a44a-f41384650e1b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255517001",
                        "display": "Intern in healthcare (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f60c43df-4fe1-4b1f-949f-03248f4e225a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255518006",
                        "display": "Organizational and social psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6de6c2f5-c40c-4051-8fcb-33dd6f350048"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255519003",
                        "display": "Cardiopulmonary technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cd6ee4bd-9fe0-4364-afba-add27bc950b4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1255719001",
                        "display": "Neurophysiology technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e3ec2196-1379-44ed-9bdd-96fd2e4fc151"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1256114007",
                        "display": "Nuclear medicine technologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dd3ee85f-a474-48b5-8dd7-e1e026adc953"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1259214004",
                        "display": "Immunohemotherapy specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "51530bcf-a6f6-43fa-9d75-f62b4030f790"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1259964002",
                        "display": "Oral medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2f744f13-553f-4021-b9f1-093016730461"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1268923002",
                        "display": "Obstetric nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bbfd1355-16de-445c-8a38-4d50940db5ed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1271000175101",
                        "display": "Primary obstetrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6dea65ee-7898-453c-b9ce-b62bb304e1f1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1276561000168102",
                        "display": "Prosthetist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a9cc2f49-075c-437d-ada4-b5cb5b70d117"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1276571000168108",
                        "display": "Orthotist and prosthetist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6c1f396c-a673-4f5c-8c4c-459e932d6b7f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1287116005",
                        "display": "Chaperone (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ea7036cc-1c77-4ff4-9f95-dcb5eec84ce8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1287641002",
                        "display": "Oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c773299d-9b3e-4348-8583-1b8887128e8f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1290289004",
                        "display": "Fellow physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94c93ff6-5e6e-4b26-8992-8cf0a4cef246"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1304201007",
                        "display": "Neurological physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cfe15464-f370-4175-92d2-c178f305777c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1304209009",
                        "display": "Cardiorespiratory physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "639ac3ba-884c-4ec3-8f69-96d516470007"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1304210004",
                        "display": "Musculoskeletal physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b5e3a7e1-94d8-4792-bfec-e44e2da24cc8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "133932002",
                        "display": "Caregiver (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "32c35be1-2e99-4f8d-95c4-c6d472205e81"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "13580004",
                        "display": "School dental assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "79d1f6de-7dfc-4c7e-b85d-b39d67ea7a8e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1421009",
                        "display": "Specialized surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b54331b9-88d6-4fb0-a056-d03f599afd44"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "14613005",
                        "display": "Ordained rabbi (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "69424bb6-915b-4a6a-8ff1-853d43f0425a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "14698002",
                        "display": "Medical microbiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8d563111-e95d-46d1-88e4-ab9b7f65977a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158939004",
                        "display": "Child care officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "30e26db1-fbec-4977-a12f-85112eac281f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158942005",
                        "display": "Residential child care worker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7f0e8039-6568-4749-9b98-543ed7f9ba37"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158943000",
                        "display": "Residential youth care worker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "78b103f5-5361-42ff-9ad4-bb8e7a31b538"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158965000",
                        "display": "Medical practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e0891da-0e45-45d5-9673-002fd698c09c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158966004",
                        "display": "Medical administrator - national (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d472e8b2-150b-4945-84da-0b3d65fca199"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158967008",
                        "display": "Consultant physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2c0d58bb-4445-4608-8fbc-caf4c57ee950"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158968003",
                        "display": "Consultant surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8eb0e0a5-3b1a-4f90-9b98-86fcc24805de"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158969006",
                        "display": "Consultant gynecology/obstetrics (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b2cb7297-f5a2-490b-acbe-6dde7c57809e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158971006",
                        "display": "Hospital registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d365d921-2acf-4357-b585-26ffcdd3c025"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158972004",
                        "display": "House officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7a35776a-321c-4615-8ad5-dab88a2a17e2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158973009",
                        "display": "Occupational physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e8656162-50e3-475f-b853-3e3393df9d1b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158974003",
                        "display": "Clinical medical officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "85a20a65-1177-4478-a490-7c558d3f9526"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158975002",
                        "display": "Medical practitioner - teaching (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b76d470f-317b-4f05-bb11-081fd713c9a8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158977005",
                        "display": "Dental administrator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ce072b4d-39c3-4aa9-9ae8-15027309fbdf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158978000",
                        "display": "Dental consultant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0da6df48-dc6f-497b-912a-8762901ea45b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158979008",
                        "display": "Dental general practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "474d4316-b39e-4825-b52b-74bcdaaad4e6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158980006",
                        "display": "Dental practitioner - teaching (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a85f35ab-215b-4bcf-a705-6e06f881dada"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158983008",
                        "display": "Nurse administrator - national (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c30ba480-adca-4bb5-9526-60722b063507"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158984002",
                        "display": "Nursing officer - region (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3dee73d8-8a2a-4500-810a-f3d97621275f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158985001",
                        "display": "Nursing officer - district (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c3a1e10-93fd-4c05-a6d4-408493ca36f7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158986000",
                        "display": "Nursing administrator - professional body (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c7ac82e-6731-4f68-8841-3260d5910194"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158987009",
                        "display": "Nursing officer - division (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0c7fc5f4-396f-481f-83aa-0ecefdd54878"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158988004",
                        "display": "Nurse education director (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "61d62490-6326-4228-a3f9-863fdf1531f7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158989007",
                        "display": "Occupational health nursing officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1bf42a90-34e7-47a7-ac05-1d37dd4b7868"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158990003",
                        "display": "Nursing officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6a317cc4-f851-4b96-a093-d85912eb0ee8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158992006",
                        "display": "Midwifery sister (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "65ea716c-46b4-4bcd-8aa4-61f2dbd2246e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158993001",
                        "display": "Nursing sister (theater) (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "61e5ae98-bfa9-4fe7-839a-9c713c9b4282"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158994007",
                        "display": "Staff nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7b8ff95c-b817-41c1-bb1a-142188e3cf54"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158995008",
                        "display": "Staff midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "02dc65f1-bc67-4118-a900-295111236b4b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158996009",
                        "display": "State enrolled nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f2527bae-4c98-4efe-ac69-06193c4ee6ca"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158997000",
                        "display": "District nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5f530f64-671b-4a76-911a-e865ec86149b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158998005",
                        "display": "Private nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8f6e2d8c-8c1f-425f-afba-51fe2fa41bbf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "158999002",
                        "display": "Community midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9fbfe5c9-7b81-4b23-ad54-9a81df39f155"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159001001",
                        "display": "Clinic nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dfa183da-3d18-46da-a185-68afcbcaef12"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159002008",
                        "display": "Practice nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "845a9c3a-c6c2-4a17-96bf-27bd45dd76cc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159003003",
                        "display": "School nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fa3ad604-892d-4504-880b-6bdcb45c7262"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159004009",
                        "display": "Nurse teacher (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "68780fb4-291f-481d-9566-0d6ef3b19944"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159005005",
                        "display": "Student nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9388a546-4e11-4806-8dbe-87abc9b94d53"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159006006",
                        "display": "Dental nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6675cf04-2126-4bdc-bf37-f6050daaa305"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159007002",
                        "display": "Community pediatric nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1de83be0-89e1-4ecc-8f36-adeb30789282"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159010009",
                        "display": "Hospital pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "31fd38c9-24e2-4c4b-adbe-4ee388d92903"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159011008",
                        "display": "Retail pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5d261262-2b2a-4d09-acbe-e2eddf962ff1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159012001",
                        "display": "Industrial pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a5248cf6-ace9-404e-b60e-bd527eccc0a9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159014000",
                        "display": "Trainee pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "21784076-5ac6-4e07-8afe-3b687dc45c71"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159016003",
                        "display": "Medical radiographer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f1975efb-59ed-4a1c-984a-d1a2df3cc649"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159017007",
                        "display": "Diagnostic radiographer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c38516a-77ed-4eeb-b547-f5369005a320"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159018002",
                        "display": "Therapeutic radiographer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8f6eaf9d-0eec-46a4-b206-6d3a9dd486df"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159019005",
                        "display": "Trainee radiographer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "55f27f34-be03-44c7-a726-21c3626d4468"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159021000",
                        "display": "Ophthalmic optician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "14e677cf-f4e4-4a8d-a667-1c51f9b4595b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159022007",
                        "display": "Trainee optician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "31278c14-a40a-4bdf-bc5e-8c2787633818"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159025009",
                        "display": "Remedial gymnast (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f40beef0-0d45-4b08-8a1c-8b6713a73855"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159026005",
                        "display": "Speech/language therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3fd703ae-8901-4099-abe7-f17acdebcc30"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159027001",
                        "display": "Orthoptist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a14b495f-3d8f-4282-8205-1ff3c6c8049e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159028006",
                        "display": "Trainee remedial therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1483dc24-7f97-4abe-a002-7bc7e7c3de40"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159033005",
                        "display": "Dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "aebde645-1137-4f4b-a2ab-18f21fff998f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159034004",
                        "display": "Podiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d1886331-b4cd-47d0-b723-88724adf041f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159035003",
                        "display": "Dental auxiliary (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8d5ddffb-cc5d-4d96-bb62-ddbd2b41acb6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159036002",
                        "display": "Electrocardiogram technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b7d6e26d-fb24-4e78-8e36-997c833df77b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159037006",
                        "display": "Electroencephalogram technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9a7a3e2e-ce7f-4592-bbac-bd201a48330f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159038001",
                        "display": "Artificial limb fitter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5eab9626-2d8e-492b-be13-6c597ab49a7d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159039009",
                        "display": "Audiology technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ccebb415-4a11-4fac-b884-4e2659870164"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159040006",
                        "display": "Pharmacy technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d5aeb83d-42d2-4b3b-9960-f4b5b1d41054"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159041005",
                        "display": "Trainee medical technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e6f29740-058b-4842-a4cc-23e1d1835b1e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159141008",
                        "display": "Geneticist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ddf71df4-9b82-4884-8649-509944124be9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159148002",
                        "display": "Research chemist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d9038115-3b45-4047-8ff1-919d3dbeecf1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159174008",
                        "display": "Civil engineer - research (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "866ea33e-44d0-41e2-908b-7504d274ad1b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "159972006",
                        "display": "Surgical corset fitter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "611c910c-84db-4a4e-90ba-b7a49daf288a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "160008000",
                        "display": "Dental technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "65592b9a-4864-48f5-9499-b7d5696b16cf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "17561000",
                        "display": "Cardiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d65dd401-c58d-48e2-9381-84cdb84457d6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "184152007",
                        "display": "Care assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "113aaaa3-567d-4430-b6be-c459fa3aecf9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "184154008",
                        "display": "Care manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9eeb43ea-b7af-413a-8dcc-c0340391d7d2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "18803008",
                        "display": "Dermatologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "65d1e8c4-2d14-4251-bf32-222d68045c54"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "18850004",
                        "display": "Laboratory hematologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "73761172-8a7c-4cd9-9066-5a11cd6bc4cd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "19244007",
                        "display": "Gerodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d54e7541-41e1-434a-af15-4800e6d23c49"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "20145008",
                        "display": "Removable prosthodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a8ed1797-92d3-43a3-a07c-1b132da15660"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "21365001",
                        "display": "Specialized dentist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e5654485-74da-446c-84c5-2e6588768acf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "21450003",
                        "display": "Neuropsychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f5653342-c3a0-4078-aab2-f5ab416b361d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224529009",
                        "display": "Clinical assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "476ed80a-95fa-48ac-bd1f-ad8f8fa0ef3f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224530004",
                        "display": "Senior registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "35eff3de-c39b-427f-aecc-918d791e1a3c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224531000",
                        "display": "Registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4970849d-3105-461d-87ca-2a38145ebca0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224532007",
                        "display": "Senior house officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "af0e8376-41a1-4067-bd88-c4bb801a5968"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224533002",
                        "display": "Medical officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4a583d29-8cdd-484c-8db6-f260e491f103"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224534008",
                        "display": "Health visitor, nurse/midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0e5d5ad0-d3cb-46d4-a816-dfbfe108a163"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224535009",
                        "display": "Registered nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2d632cba-5574-4c05-807c-d2d2f23db751"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224536005",
                        "display": "Midwifery tutor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "874a04f5-f5e2-4aa3-9ece-5a9f4ffe3cc9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224537001",
                        "display": "Accident and Emergency nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c3e6da04-7ce2-4dbc-a9ae-29b51cd82aa1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224538006",
                        "display": "Triage nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "17782642-aff6-482c-8914-e4e585be27cd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224540001",
                        "display": "Community nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "edeeadae-5cec-4897-ac28-e8b0f0a8a591"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224541002",
                        "display": "Nursing continence advisor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9d082810-07c4-4ebd-86b7-e058d51c8231"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224542009",
                        "display": "Coronary care nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "55c1cf96-fb49-4ec1-9baf-906c08013244"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224543004",
                        "display": "Diabetic nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "aadfb04d-1bdd-423d-9dfa-91fb492f92ae"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224544005",
                        "display": "Family planning nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c6bac22-ca1d-4a37-8f64-a50d45876031"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224545006",
                        "display": "Care of the elderly nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fb141400-30bb-46af-b385-9375e44d83c5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224546007",
                        "display": "Infection control nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7097e252-2181-436c-82a9-fd15a57ec132"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224547003",
                        "display": "Intensive therapy nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fad5c194-bee7-416f-8cce-dd5c3360ee0a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224548008",
                        "display": "Learning disabilities nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7e0bf9e7-474b-4ab3-84ea-77c73d911178"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224549000",
                        "display": "Neonatal nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c462df2d-b1ae-49a9-a8e7-11ad6ab15d08"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224550000",
                        "display": "Neurology nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2041fc00-1cec-427a-ab64-6014b17bb8ef"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224551001",
                        "display": "Industrial nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3ed97bb7-befd-4cab-86b5-864038afbad5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224552008",
                        "display": "Oncology nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3907f242-4626-481a-b1ad-09e5d0cd8401"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224554009",
                        "display": "Marie Curie nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "569ce6e0-7047-43f1-b164-6c8cee37c039"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224555005",
                        "display": "Pain control nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1a317259-f2aa-465c-b415-9b5565d95cff"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224556006",
                        "display": "Palliative care nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e5b0015d-13d1-48e7-b2fe-4ef0df500c96"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224557002",
                        "display": "Chemotherapy nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "66f31c81-e064-4ebf-9fe5-70d518d24553"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224558007",
                        "display": "Radiotherapy nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fbdf07df-bdbc-4538-89f5-eb914f0da9e8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224559004",
                        "display": "Recovery nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5d5e5929-2204-4854-bb07-b31a65eb4b3f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224560009",
                        "display": "Stoma care nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7a77f5ea-a51b-42db-b87d-fbd15db5d3fe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224562001",
                        "display": "Pediatric nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "00e22fe4-71f6-4368-8ed9-9eba1e53f82a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224563006",
                        "display": "Mental health nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "59d84f82-f1c8-4d5f-9eec-ef0f86d9c898"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224564000",
                        "display": "Community mental health nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bd024499-5d78-42a6-97fc-6e09b74dfd91"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224565004",
                        "display": "Renal nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3b904480-2332-4834-84ae-7dbc122c7f14"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224566003",
                        "display": "Hemodialysis nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ad2957d8-1b71-4d51-8926-6c224e0cbbe1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224567007",
                        "display": "Tissue viability nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2df9c49d-f082-4913-9c88-27e537c14ddf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224569005",
                        "display": "Nurse grade (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d97d39c7-f8d3-4349-971e-1e0963d4dc0a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224570006",
                        "display": "Clinical nurse specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6f673d05-f804-428d-911a-fc78df60350d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224571005",
                        "display": "Nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d346d6da-da45-4b67-9fb8-32c3c54cf002"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224572003",
                        "display": "Nursing sister (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4d2c775c-32ef-45fa-b30e-22c17ee6a675"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224573008",
                        "display": "Charge nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57a7a2be-7f12-4ead-9b17-03830f2f342a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224574002",
                        "display": "Ward manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f8ec07dc-1922-43ba-93da-1237e2df540b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224575001",
                        "display": "Nursing team leader (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "83f178bc-c5b5-4858-8ffb-f3f5dd52f3de"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224576000",
                        "display": "Nursing assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19cea2f2-7b63-4fcc-8eae-c77fc896f574"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224577009",
                        "display": "Healthcare assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f7fbc02c-1524-4793-93f6-41363978b295"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224578004",
                        "display": "Nursery nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "784a6e0c-090e-4130-84d3-4f724bb5c4b1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224579007",
                        "display": "Healthcare service manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a9ca55ca-b205-401a-a510-767c7decfa67"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224580005",
                        "display": "Occupational health service manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5725eec7-4653-4b2d-a3df-e26fe7575acd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224581009",
                        "display": "Community nurse manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94269884-963c-4435-bb3a-e219522412c0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224583007",
                        "display": "Behavior therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "076729ed-246e-4ba1-9e9b-53015ab27cfa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224584001",
                        "display": "Behavior therapy assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "54599cf7-1287-43ef-8006-738ca409863e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224585000",
                        "display": "Drama therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1ae3434d-b2f5-40ba-902d-dcc9be17e8d9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224586004",
                        "display": "Domiciliary occupational therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "77d8dbec-9f51-43e5-97b0-e0df5ec3efeb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224587008",
                        "display": "Occupational therapy helper (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9d8dc7a4-6da2-42dd-b332-5b8a183e1a06"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224588003",
                        "display": "Psychotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5d51139d-a1ff-4f80-b998-38c0b24460b4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224589006",
                        "display": "Community-based physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8245c38f-3ba6-4dfe-8a2f-31134e3fe08e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224590002",
                        "display": "Play therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d6b2dc8c-f868-4e32-95ef-fee680c86a95"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224591003",
                        "display": "Play specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7209f5e6-d6ab-4912-9eff-36d3b9756465"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224592005",
                        "display": "Play leader (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "46e6a19c-bd1d-4290-af79-36fcd6d2524b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224593000",
                        "display": "Community-based speech/language therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f1645e69-aa1f-4186-a742-fa11ef72494a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224594006",
                        "display": "Speech/language assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "40bd870b-ec79-4b2a-90a6-ab98d40f5379"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224595007",
                        "display": "Professional counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "be4ed8ce-aff5-431a-b615-995ebcfdff5c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224596008",
                        "display": "Marriage guidance counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e3f7ac08-98d6-4e58-92d2-2cbbb0dbb1d7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224597004",
                        "display": "Trained nurse counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4cfb67b1-408f-4dad-9cf9-9ffae059bd0c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224598009",
                        "display": "Trained social worker counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c998b1a5-fe23-4d85-854e-0fd941a4c765"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224599001",
                        "display": "Trained personnel counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8f74fb3c-3996-43f4-8f59-35b081cc5d84"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224600003",
                        "display": "Psychoanalyst (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0b8915f0-a6da-4513-8a82-0fa1e2a7c62b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224601004",
                        "display": "Assistant psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b59b9934-b27d-4818-9a2a-0ca0ef70e297"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224602006",
                        "display": "Community-based podiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "06b05c7a-3507-434c-ad1d-907b3755b51a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224603001",
                        "display": "Foot care worker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94d32972-72e7-4a7c-98f6-a1349c9aba10"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224604007",
                        "display": "Audiometrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "34719b30-fbab-4cf5-81d8-2c73905e947c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224606009",
                        "display": "Technical healthcare occupation (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4b218fbc-4d5e-4dc6-8222-cb649759c9f2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224607000",
                        "display": "Occupational therapy technical instructor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c506d09-0bf3-412d-9258-675cf0e0bf11"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224608005",
                        "display": "Administrative healthcare staff (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0e593587-4210-4cb2-b4b9-001c23a4eebb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224609002",
                        "display": "Complementary health worker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6e9d5c0f-5b68-4e8f-a8d2-37e195106c9f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224610007",
                        "display": "Supporting services personnel (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "55c722aa-6517-46b0-ad51-30f3368d0641"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224614003",
                        "display": "Research associate (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5c1c3010-bf10-425c-b3b8-1bea1e507218"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224615002",
                        "display": "Research nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "782c71ce-5e14-4c04-951d-b0f06c58d964"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224620002",
                        "display": "Human aid to communication (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "235b5189-5690-46bd-b97a-202c1fc35609"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224621003",
                        "display": "Palantypist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9f525707-5533-44ae-b6d9-93534e558aa8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224622005",
                        "display": "Note taker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "af62b89e-a015-4d95-acc4-50c45535f6a0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224623000",
                        "display": "Cuer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ca5c54b1-356d-4317-a530-f0904a255d65"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224624006",
                        "display": "Lipspeaker (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "560790bd-139d-47a6-a07e-1b84f74f80b9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224625007",
                        "display": "Interpreter for British sign language (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "89334980-d17d-4615-9aef-8f8f3c2d27c2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224626008",
                        "display": "Interpreter for Signs supporting English (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b1d4fa75-a530-411a-804d-c76d613a589b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "224936003",
                        "display": "General practitioner locum (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ba460e9a-0989-495d-a42a-4c1c8d482e78"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "22515006",
                        "display": "Medical assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "28ab80e6-5351-4d28-bd45-32eb60316e84"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "225725005",
                        "display": "Chaplain (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cd3dc2b3-4746-4635-9d3f-59dad674e897"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "225726006",
                        "display": "Lactation consultant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cdcccd6b-d534-4c76-85e8-10357cc17b63"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "225727002",
                        "display": "Midwife counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ad870c52-6124-4c9f-8b2e-7fa5c265a6ad"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "22731001",
                        "display": "Orthopedic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e02b7ace-405f-4fa2-b373-e1f5a8519b8d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "229774002",
                        "display": "Caregiver (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bfefa2c9-4117-4215-8dc4-437a7584ec55"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "22983004",
                        "display": "Thoracic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "aa09c507-c88f-4218-b476-1ad8bd227577"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "23278007",
                        "display": "Community health physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "148abd47-6775-42f2-b83e-73f08d6ab3fa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "24430003",
                        "display": "Physical medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f143aa40-101c-438b-8ab1-af5e25635b09"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "24590004",
                        "display": "Urologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "209a83ac-5866-47f0-abd6-7f54491614c5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "25941000087102",
                        "display": "Adult gerontology primary care nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f016a054-7fab-4ea1-a051-416d85201050"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "25961008",
                        "display": "Electroencephalography specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "259b4934-af98-4e40-8c04-4fe32cea755a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "26031000087100",
                        "display": "Pediatric nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fb6036bc-1deb-443e-b117-7d6e913fadd4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "26042002",
                        "display": "Dental hygienist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6af4157a-68a5-4da4-b132-81eded852851"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "26071000087103",
                        "display": "Primary health care nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5080dbef-6c92-4cb4-a915-cf2a6430a8c8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "26091000087104",
                        "display": "Public health nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "086555f7-c11d-4251-a3e3-6316c37fe944"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "26369006",
                        "display": "Public health nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "937d435e-5cf3-4c14-a99e-9d600081b3e0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "265937000",
                        "display": "Nursing occupation (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "43a7b9b2-d1d1-4a12-8bbe-52fa4a69f383"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "265939002",
                        "display": "Medical/dental technicians (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bc4555a8-2584-43f3-9cf3-ff1b647835e4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "28229004",
                        "display": "Optometrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d8f78c65-0d8b-4b42-968a-2911405eeb41"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "283875005",
                        "display": "Parkinson's disease nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "36deaab0-41f1-4993-a66b-4739c231c779"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "28411006",
                        "display": "Neonatologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c34ec352-71cb-4767-94c4-bf2627861e84"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "28544002",
                        "display": "Medical biochemist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1d304bb7-c139-4d89-b4b4-add1e65a9ec7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "302211009",
                        "display": "Specialist registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "31206f26-d274-46ec-ab89-8b1d48bee67c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "303124005",
                        "display": "Member of mental health review tribunal (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a3d44b27-47df-4c88-8c1e-f9e42175b2bc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "303129000",
                        "display": "Hospital manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "03265728-9cf0-40a1-b42c-d2eacc4bd396"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "303133007",
                        "display": "Responsible medical officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b76d5bb5-990f-4a8f-99c3-276af27bbd30"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "303134001",
                        "display": "Independent doctor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c4359da-69c7-4989-a2b8-b609ed7b5b4b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "304291006",
                        "display": "Bereavement counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "428065f0-881b-4a3e-bdf4-9d2455223ecb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "304292004",
                        "display": "Surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e81aacd9-e4e2-4b06-96b2-1574f8daea1e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "307988006",
                        "display": "Medical technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a944f1ee-2cee-43ae-9a0b-36f2ebaaccdd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "308002005",
                        "display": "Remedial therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5eec6338-3f85-4743-9e3c-068f2a4a537f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309294001",
                        "display": "Emergency department physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e6e729bc-f6b9-4038-a34e-ba578cb9277e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309295000",
                        "display": "Clinical oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0d15353c-b605-4700-b2b9-72ff0a70f9f2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309296004",
                        "display": "Family planning doctor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bab6abc0-e6cf-4763-94c5-93829d4f084d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309322005",
                        "display": "Associate general practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c9efaf7-1ac9-48ae-8d12-407a85a02e94"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309323000",
                        "display": "Partner of general practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "45b2a194-a6d6-42b0-a497-b8e3b5d29683"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309324006",
                        "display": "General practitioner assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "822f0881-f7d3-4542-a511-d17b126d25ff"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309326008",
                        "display": "Deputizing general practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2b9741b2-e070-481e-8ca5-3ff9c011c4f3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309327004",
                        "display": "General practitioner registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4527581a-10b6-4ba9-8ea5-beaacf55765d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309328009",
                        "display": "Ambulatory pediatrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3ffc71ca-ccdf-4716-aec7-ba2883494366"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309329001",
                        "display": "Community pediatrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ca19ca76-11f6-4f71-b322-50483db63a4e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309330006",
                        "display": "Pediatric cardiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57f8e5eb-c5ff-41f4-88fa-a0eeb990b3df"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309331005",
                        "display": "Pediatric endocrinologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "17ea1e9e-ae3d-4ae6-ba86-aa79e302f88f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309332003",
                        "display": "Pediatric gastroenterologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e14f4d8d-6001-4116-a42b-d06668f064cf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309333008",
                        "display": "Pediatric nephrologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cf318687-d6dc-44f4-bd2a-b5a7df53a9db"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309334002",
                        "display": "Pediatric neurologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "17748399-9ce8-4ea5-b3d8-30a3df3448ea"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309335001",
                        "display": "Pediatric rheumatologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b9c8116c-e66d-4198-9340-8335b36458ac"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309336000",
                        "display": "Pediatric oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fac54c59-b843-4d72-b45d-bf5cdc632113"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309337009",
                        "display": "Pain management specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a51b7b6a-f20c-4bbf-bcc8-0f969e6fce30"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309338004",
                        "display": "Intensive care specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bba7c09c-6fa6-4197-9480-04cf05f95902"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309339007",
                        "display": "Adult intensive care specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "72e17cae-bec9-4b33-9cb8-72851915b73c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309340009",
                        "display": "Pediatric intensive care specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ffda3e38-6dcf-4dba-a8a1-bcaf316a36c3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309341008",
                        "display": "Blood transfusion doctor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "62d76b7a-86e3-4d6c-b7c8-5418ee515308"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309342001",
                        "display": "Histopathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6585a5cc-2eeb-4a8d-93f9-53047227e236"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309343006",
                        "display": "Physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3b53576f-0b1a-47c9-aa3a-6227989da414"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309345004",
                        "display": "Chest physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "574d1a77-4712-4262-8532-2ade2da5fb03"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309346003",
                        "display": "Thoracic physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ab0a0c36-bfc7-4d46-ae49-908764b34cf3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309347007",
                        "display": "Clinical hematologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "db719c73-c0f1-429c-a577-3a4b623c6c43"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309348002",
                        "display": "Clinical neurophysiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "49352f92-46e4-4278-b431-6fcc70ca81bb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309349005",
                        "display": "Clinical physiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c4c6ec23-0f95-402e-bf30-52210b8d183c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309350005",
                        "display": "Diabetologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "06313ac1-d50f-4058-b6c9-5a59bb713ac8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309351009",
                        "display": "Andrologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "71e369b8-34b7-4e5d-b2b9-2648d211fcad"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309352002",
                        "display": "Neuroendocrinologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "617fe96a-47a1-4491-b097-4944c5265449"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309353007",
                        "display": "Reproductive endocrinologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "efdfb25b-cb2a-4ce9-b9c9-21b972a4a0e0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309354001",
                        "display": "Thyroidologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "20dcf54b-e61e-4231-8b9e-34fda8b4d078"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309355000",
                        "display": "Clinical geneticist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5df17f11-3bb3-4187-af0f-6e3646ae6003"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309356004",
                        "display": "Clinical cytogeneticist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "947ad452-389a-4a45-ace9-47376557cbf6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309357008",
                        "display": "Clinical molecular geneticist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e301e52c-7602-4aeb-8b9e-2c6e52e14ec4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309358003",
                        "display": "Genitourinary medicine physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bc51fbaa-516c-4d14-9fa5-42a8bbadbae7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309359006",
                        "display": "Palliative care physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6e98152e-026f-4df1-bef2-96a1d4a0d9e9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309360001",
                        "display": "Rehabilitation physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "79e8e479-3313-4ed2-a2bd-a2eaa692203a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309361002",
                        "display": "Child and adolescent psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e1145c60-098f-437e-83d0-d4b288d4a1f7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309362009",
                        "display": "Forensic psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dd3b5337-0b1e-41b7-ad3f-fa4b72bfbd04"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309363004",
                        "display": "Liaison psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "eed833e3-cf5b-45b4-9fa8-7391e91b398b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309364005",
                        "display": "Psychogeriatrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7840728b-aff4-4202-bf04-46c6cadb25a5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309366007",
                        "display": "Rehabilitation psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "86dc8591-a0a8-4316-b556-d65d433d27c9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309367003",
                        "display": "Obstetrician and gynecologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57897c42-5daa-4e11-9858-f7208e20b5dd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309368008",
                        "display": "Breast surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "22de8eb4-fdf5-410c-8b49-a54ffe790c64"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309369000",
                        "display": "Cardiothoracic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3e500fa2-e6a3-491a-a484-5338c7125613"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309371000",
                        "display": "Cardiac surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "211c465a-1391-480b-93d6-38f0b318e58e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309372007",
                        "display": "Ear, nose and throat surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b196fe57-b71b-47ee-a748-9ad62704e226"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309373002",
                        "display": "Endocrine surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "efbb45a3-1538-4333-9671-76d130b37644"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309374008",
                        "display": "Thyroid surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "98d892f2-d830-4769-872f-caeae33e43e3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309375009",
                        "display": "Pituitary surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2c7e6492-fbf7-4895-b01d-ddca181146d6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309376005",
                        "display": "Gastrointestinal surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "80ff4d8e-1a1f-454a-a2af-199b540fef27"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309377001",
                        "display": "General gastrointestinal surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "65f36beb-1452-4151-9730-63b623c36690"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309378006",
                        "display": "Upper gastrointestinal surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0bdca84f-228f-438b-8324-71e8880a334a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309379003",
                        "display": "Colorectal surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "91a39a70-9fae-445f-a49e-fbbdfb4c1c87"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309380000",
                        "display": "Hand surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "21158dba-78a3-4f10-a254-c13618926512"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309381001",
                        "display": "Hepatobiliary surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "448f8838-716f-4ecc-ab40-06e4a6250676"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309382008",
                        "display": "Ophthalmic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6bf3c275-5c98-40d3-8499-fb72148c6936"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309383003",
                        "display": "Pediatric surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a8000531-f1a4-40da-8e6b-c89e20c4339c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309384009",
                        "display": "Pancreatic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f526a3b8-a0d0-4112-bae9-f979d09babbe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309385005",
                        "display": "Transplant surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57b8b823-a1a2-460b-90a1-8bce417c1eab"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309386006",
                        "display": "Trauma surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bc904c8b-fb8f-4858-b9ea-a97dec7d4457"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309388007",
                        "display": "Vascular surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cc5f57f2-c47a-4fcd-912b-5dab6dce56db"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309389004",
                        "display": "Medical practitioner grade (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d016fe8f-a564-4819-b241-d47d92d56edf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309390008",
                        "display": "Hospital consultant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5ac48828-ecef-4284-a32e-4d3e2cffdc26"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309391007",
                        "display": "Visiting specialist registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3cd54b9d-9388-447c-afbb-ca40e8a15fa3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309392000",
                        "display": "Research registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "983ca763-d418-4b97-b319-cb1aa4d70ca7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309393005",
                        "display": "General practitioner grade (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "525e52c1-78a3-4919-8d18-fe407db16caa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309394004",
                        "display": "General practitioner principal (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4fb13802-153f-4199-b091-460f780378a5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309395003",
                        "display": "Hospital specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4c2cabfe-c029-499e-acf4-0c20a5f1fff9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309396002",
                        "display": "Associate specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cbaf891f-f291-4226-8de6-2aa0b6c7b5af"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309397006",
                        "display": "Research fellow (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f27a118f-9678-4277-80dc-799e260a2d2b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309398001",
                        "display": "Profession allied to medicine (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "13ce0e27-aad7-4a9e-92da-0dcd58031301"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309399009",
                        "display": "Hospital-based dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "34609710-19ef-483e-9882-856098e98de3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309400002",
                        "display": "Domiciliary physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3424d461-a102-4e42-ae40-c5a2469939a8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309401003",
                        "display": "General practitioner-based physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a960bdef-effa-48f9-b6c9-7f2afd537504"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309402005",
                        "display": "Hospital-based physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d0527826-cba0-473d-a180-f53c034a0712"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309403000",
                        "display": "Private physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8ca69212-3328-472e-9019-69d47b3ecaf4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309404006",
                        "display": "Physiotherapy helper (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "23680ef7-e87a-4db8-bdcd-2e3a25bb7276"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309409001",
                        "display": "Hospital-based speech and language therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c98b277d-d51a-4bdc-8180-e9e0678fafc0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309410006",
                        "display": "Arts therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fd6b9a00-37e3-4316-a5e7-b9f3706a719a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309411005",
                        "display": "Dance therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "756a50f8-d847-4753-8785-5998c0d8dab1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309412003",
                        "display": "Music therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b623690-9825-4b7f-84e1-4519c8b0c2dc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309413008",
                        "display": "Renal dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3d25a7b9-9736-41ba-a73d-5bbb548d9704"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309414002",
                        "display": "Liver dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c77fbf2b-369f-4a80-9af3-913520c1b714"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309415001",
                        "display": "Oncology dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a06f824f-7d35-4670-a4ec-b0a4194c745c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309416000",
                        "display": "Pediatric dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e3233315-2563-4a2d-a647-093781dce0cc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309417009",
                        "display": "Diabetes dietitian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6c0196ea-9e11-42ba-837c-a739408449aa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309418004",
                        "display": "Audiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "28482d4c-7757-458d-8dba-376de711e148"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309419007",
                        "display": "Hearing therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ccb7d510-0bc8-4f31-8a87-aac38677ddb2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309420001",
                        "display": "Audiological scientist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "40ed0e99-96b9-40ff-8c0f-899945eedbdd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309421002",
                        "display": "Hearing aid dispenser (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c6a95ed6-c9a9-473c-8b21-567b6a569891"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309422009",
                        "display": "Community-based occupational therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "27a315ac-5677-4984-9d69-7e67950aea58"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309423004",
                        "display": "Hospital-based occupational therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4b51a277-9314-4316-9319-e51070faf88f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309427003",
                        "display": "Social services occupational therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "02c053f8-fd8b-4946-b5d8-34a9214580a0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309428008",
                        "display": "Orthotist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "22fc385a-210d-4c25-ba09-4ae0f8049e8c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309429000",
                        "display": "Surgical fitter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "411b6c0b-ccc1-4fba-84a2-c3c4172eba28"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309434001",
                        "display": "Hospital-based podiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a86d836f-14a9-41be-9505-8eeb0b9ca663"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309435000",
                        "display": "Podiatry assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7968c157-b703-4bac-88fb-7cf68d32ae10"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309436004",
                        "display": "Lymphedema nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "80e63b0d-f898-4f1b-b602-69d6b17749af"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309437008",
                        "display": "Community learning disabilities nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dd5a5787-335b-438a-b790-a16e62c480f8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309439006",
                        "display": "Clinical nurse teacher (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3012aa20-51f3-4dfb-8c98-10cac8b67907"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309440008",
                        "display": "Community practice nurse teacher (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "843db713-b619-40b4-8622-2e2b7b68e082"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309441007",
                        "display": "Nurse tutor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a81f7203-dc1f-44d9-84ac-ea83385820ba"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309442000",
                        "display": "Nurse teacher practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d1941d29-5603-4d35-872f-f28c33dfb96f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309443005",
                        "display": "Nurse lecturer practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e86e39eb-8849-44c7-aa98-33ef921c09e1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309444004",
                        "display": "Outreach nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "29fc4ecc-9469-4f0d-9c79-4ce5a2b9a4ca"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309445003",
                        "display": "Anesthetic nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "48bbddbf-7141-4612-bfc5-a4f8b2b0624d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309446002",
                        "display": "Nurse manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cd425e51-51bc-465c-9498-b18fef8b776b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309450009",
                        "display": "Nurse administrator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "18854a33-7c0c-4af1-ac8e-6227b0d57c24"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309452001",
                        "display": "Midwifery grade (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8bc00651-c524-4805-93cc-bbf14cee57df"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309453006",
                        "display": "Registered midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8fc24906-cc36-4a71-86ee-d6d49a0dbf2a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309454000",
                        "display": "Student midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3e482ab4-66e0-4f77-946c-c2c173c45b83"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309455004",
                        "display": "Parentcraft sister (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7997a6e1-4398-4272-90fc-24a3e0b9d4aa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309457007",
                        "display": "Vicar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d01d38f0-ec03-477d-9a2c-b8dcf659eb47"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309459005",
                        "display": "Healthcare professional grade (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ab81a9cc-1cd2-4d22-987e-3277768a7269"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "309460000",
                        "display": "Restorative dentist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3ec4ffc1-7e93-4d5f-a974-bdd668593c36"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310170009",
                        "display": "Pediatric audiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5b7d3b25-b4e8-43a5-9337-1c2d8f2f2bc7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310171008",
                        "display": "Immunopathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "adfcd89d-3dd3-4903-874c-efeda34daac3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310172001",
                        "display": "Audiological physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7faadc2e-b11d-48ad-962f-d3718459b213"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310173006",
                        "display": "Clinical pharmacologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "56d75ee2-6cbf-4a95-b821-b53aab930ea2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310174000",
                        "display": "Private doctor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ef0b8099-6142-415a-8c87-975a12c11b8e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310175004",
                        "display": "Agency nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "02699302-22d3-4a8c-b043-29296b3a75c2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310176003",
                        "display": "Behavioral therapist nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f61ddac9-e808-4908-9e96-b5900b53a7b5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310177007",
                        "display": "Cardiac rehabilitation nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2558c1a6-6d67-4124-b09a-9fec165d53fb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310178002",
                        "display": "Genitourinary nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c0ad126-1d4a-45a5-8122-13260b92f336"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310179005",
                        "display": "Rheumatology nurse specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dd5daaa0-522b-43a7-86f2-9ae7775de0d8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310180008",
                        "display": "Continence nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "59651ed6-826b-47a8-a233-0bbd82db3c3f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310181007",
                        "display": "Contact tracing nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "efd295dd-3a17-491c-9059-76bd236762e2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310182000",
                        "display": "General nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6ddbd64a-0037-4b07-985f-1ed6fc0742ee"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310184004",
                        "display": "Liaison nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a8b86cf0-7779-4c2f-9688-0a3fb5a32936"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310185003",
                        "display": "Diabetic liaison nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "349fa48c-46a7-4a3a-8818-b3acd0939ee2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310186002",
                        "display": "Nurse psychotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3e511233-ec53-47a2-bfd8-0acd8f052812"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310187006",
                        "display": "Company nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e5d77f8f-8fc8-4d4d-b171-78aa11e2e255"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310188001",
                        "display": "Hospital midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d1e833bb-d22f-4056-a88d-7ae19c9262a5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310189009",
                        "display": "Genetic counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "510949db-4cda-46d6-9873-1f5c44f0ef95"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310190000",
                        "display": "Mental health counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "614b8cbe-e169-49f6-bf96-c7b8b70fcae7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310191001",
                        "display": "Clinical psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7c517655-9457-4017-bc21-1cd6c8490e37"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310192008",
                        "display": "Educational psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9a3f9326-4750-4842-ad5e-e5b5bc474288"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310193003",
                        "display": "Coroner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f9d11328-6074-4627-82c7-386ef237e5c3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310194009",
                        "display": "Appliance officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5874abad-1029-4950-9d1c-4f556d14bafc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "310512001",
                        "display": "Medical oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8fbf37c5-499a-4b2c-9d81-8adb825990f6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "311441001",
                        "display": "School medical officer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2652364d-8a04-4d84-a660-25c8e82665ed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "312485001",
                        "display": "Integrated midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ced2fdb8-1fea-4317-9ddc-744058c57db1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "3430008",
                        "display": "Radiation therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a158e24e-fd6f-41b6-b72a-6f46700b21c7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "36682004",
                        "display": "Physiotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3f6dd149-1430-40c8-82d9-3e453236b9bd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "37154003",
                        "display": "Periodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "510847d8-026c-4dee-a1de-09b696f6bfa2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "372102007",
                        "display": "Registered nurse first assist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f10563be-622d-4859-8813-5d9f9c7f5a31"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "373864002",
                        "display": "Outpatient (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "86327b36-2a67-4e42-bb6b-b68a3234058b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "37504001",
                        "display": "Orthodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f6b22edc-d414-4739-9bb7-30d5bd233fad"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "3842006",
                        "display": "Chiropractor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a119623b-e140-4278-a174-3f44697e6a9c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "387619007",
                        "display": "Optician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e03f4e56-375a-47fa-9f0b-86a85c013642"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "394572006",
                        "display": "Medical secretary (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "20e44e7f-772c-46a7-bcac-5731f4449ba5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "394618009",
                        "display": "Hospital nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8328432c-4279-4e31-abc5-4a3433fb094e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "39677007",
                        "display": "Internal medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ff91ddb5-1190-40a7-a556-9a7ae3fd00b1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "397897005",
                        "display": "Paramedic (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "50946fa3-f071-4438-9feb-66cfec9bdb46"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "397903001",
                        "display": "Staff grade obstetrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a42ce3f6-85a8-4b10-98b8-a4a9a24038ed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "397908005",
                        "display": "Staff grade practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "90ef1986-635c-438b-b976-cb1c1e1c99be"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "3981000175106",
                        "display": "Nurse complex case manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "097a0f13-d695-478f-92f1-947b0fb84d9b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "398130009",
                        "display": "Medical student (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "07fc20fa-f9e3-4f80-a222-91db61dd0877"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "398238009",
                        "display": "Acting obstetric registrar (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a6bd20bc-2e98-4311-a0a1-bbcf571ab3b0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "40127002",
                        "display": "Dietitian (general) (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cfcadf9e-72e8-4be4-b513-580e3b63b4bb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "40204001",
                        "display": "Hematologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e7816e30-bed2-4ec9-a1a1-7e08288acbff"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "404940000",
                        "display": "Physiotherapist technical instructor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ed1396a1-e39a-469c-814c-67f8c960ef62"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405277009",
                        "display": "Resident physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "30c56fe2-1245-40e6-a03b-551a490bc4ed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405278004",
                        "display": "Certified registered nurse anesthetist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5cd17524-1b03-4568-b3c8-a0ff402948b3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405279007",
                        "display": "Attending physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a185b6c6-d670-4413-bb91-ee3c64ee2bbd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405623001",
                        "display": "Assigned practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "26f8202e-442d-4b25-a09b-09c682113aad"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405684005",
                        "display": "Professional initiating surgical case (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "533c8d1b-85e9-4344-8931-56d26810e4e3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "405685006",
                        "display": "Professional providing staff relief during surgical procedure (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9f2285b2-495d-40b8-b7df-5a91aef2b1ff"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "40570005",
                        "display": "Interpreter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "80410a08-fc29-4a99-8019-d52f0468ae08"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "407542009",
                        "display": "Informal caregiver (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2ba3d141-577c-4824-ab99-acddae0e25b5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "407543004",
                        "display": "Primary caregiver (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0da7a8ec-0fec-4e74-8056-ef74dbf3fa49"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "408290003",
                        "display": "Diabetes key contact (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bca91749-6329-418c-a0d2-7a18378d8d46"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "408798009",
                        "display": "Consultant pediatrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0646ff6a-85c7-40a4-8e12-08f29788569f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "408799001",
                        "display": "Consultant neonatologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7a29c227-1c89-41b9-b766-6fac72875e5e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "409974004",
                        "display": "Health educator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a9ecaba3-5bb3-4043-8171-c070dc2fbdb8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "409975003",
                        "display": "Certified health education specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "61bcbc7c-17ba-47b8-8e04-d4775600dd75"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "413854007",
                        "display": "Circulating nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d063d2b9-2920-45d3-aeb7-697749b04603"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "415075003",
                        "display": "Perioperative nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "31e400e1-96df-4e05-8f8c-f9e0b13d30de"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "415506007",
                        "display": "Scrub nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5a18259e-24f2-45e2-927e-1b9f7b6348a6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "416034003",
                        "display": "Primary screener (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "382af602-f9a7-477d-aa56-7e16603cbdea"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "416035002",
                        "display": "Secondary screener (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b0655545-8b7b-4668-964c-8d61a863dd43"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "416160000",
                        "display": "Fellow of American Academy of Osteopathy (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2392d926-50f5-4399-9489-a502aff413f0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "4162009",
                        "display": "Dental assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a49bd52b-26fd-4788-aff0-432407a61789"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "41672002",
                        "display": "Respiratory disease specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "136c2fe3-2304-4af4-8da2-77d0badc7de6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "416800000",
                        "display": "Inpatient (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dc0a426a-c9ea-4e3c-87cc-50c17d38b1c7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "41904004",
                        "display": "Medical X-ray technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4a9f16f1-4894-4906-8d5d-c1d9a950b3b5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "420158005",
                        "display": "Performer of method (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "40a6f123-f1c2-4f84-85d5-f80a785d1f19"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "420409002",
                        "display": "Oculoplastic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "53e5b1bc-f019-4af7-8320-fa458cde375b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "420678001",
                        "display": "Retinal surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5c8d073c-4b8b-4c3c-a388-02a6a6eff681"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "421841007",
                        "display": "Admitting physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9448ea10-838d-4080-9d26-13e35237bd9e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "422140007",
                        "display": "Medical ophthalmologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b79a5b0d-4658-4b4d-8067-4017634a828a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "422234006",
                        "display": "Ophthalmologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2dc74bb9-c7f2-4781-9126-1d0ca3ab337b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "428024001",
                        "display": "Clinical trial participant (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d6f9ec0c-5ad3-4507-a843-4355e70e711f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "429577009",
                        "display": "Patient advocate (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ae57bba6-cb96-4ee2-9c1c-0ee2c95e4c4c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "43018001",
                        "display": "Babysitter (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c3379d53-07a9-4337-b4c1-2795fd566efe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "432100008",
                        "display": "Health coach (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "30179788-ec6a-4f23-94a2-c24175d39706"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "43702002",
                        "display": "Occupational health nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7a08cea5-3970-4275-9683-e4c99669a456"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "440051000124108",
                        "display": "Medical examiner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1769391c-63ee-4294-af7f-ebeb2e360198"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "442251000124100",
                        "display": "Licensed practical nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5ebb560d-1fa9-41d5-9e8d-b92ea00a2d4f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "442867008",
                        "display": "Respiratory therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "eca36cba-fe96-4f3a-be2a-a8948f926372"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "443090005",
                        "display": "Podiatric surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e93ce767-ee91-4dff-965c-830ba8ae14f2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "444912007",
                        "display": "Hypnotherapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6a71ca31-5f60-438b-bf0c-992b43362501"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445313000",
                        "display": "Asthma nurse specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4fe02527-270f-44b5-82db-ec9dfab1ee87"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445451001",
                        "display": "Nurse case manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b5a091b8-7ab2-452f-9d5c-f67468da0bc2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445521000124102",
                        "display": "Advanced practice midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ec8f76bc-d9ae-48a6-b112-a6eecd1459c8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445531000124104",
                        "display": "Lay midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f191ca38-dae2-4c8f-8e92-9dfda4a31ce3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "446050000",
                        "display": "Primary care physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8586c445-8b73-48f4-813c-5f1104b8f5de"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "44652006",
                        "display": "Pharmaceutical assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8e717bc7-4d6b-4dcb-a019-2f8d638d970e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "446701002",
                        "display": "Addiction medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f6040211-6f00-4b47-862e-ad67f0f53a47"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "449161006",
                        "display": "Physician assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bedad8f6-d973-4224-bd3c-821e85c67bec"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "450044741000087104",
                        "display": "Acupuncturist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "92619d68-bbdd-42d6-9e3b-ec459d7fca37"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45181000087107",
                        "display": "Pediatric ophthalmologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fb3a2486-27bc-426d-921f-9e68f8ae23c9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45191000087109",
                        "display": "Pediatric otorhinolaryngologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fa837d78-80f0-4662-ba43-3fd7af903aef"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45201000087106",
                        "display": "Pediatric urologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "82a4221b-9509-4024-b996-c9b8e2cea508"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453061000124100",
                        "display": "Pharmacist specialist (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3de6a1b1-c47d-4617-b1b8-9d529f79f462"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453071000124107",
                        "display": "Primary care pharmacist (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ff28bf5d-375f-490a-a7d6-0091d66c7279"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453081000124105",
                        "display": "Infusion pharmacist (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "16442e83-37c2-4b76-a6c6-154a31669d27"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453091000124108",
                        "display": "Receiving provider (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57ead19f-462d-4a26-a0de-4a3553197bed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453101000124102",
                        "display": "Consultant pharmacist (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "42e0ea6c-5cb0-4714-87bf-ee2989fad060"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453111000124104",
                        "display": "Dispensing pharmacist (person)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cdaa7e0f-28a8-439f-924d-b5839f0be942"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453121000124107",
                        "display": "Emergency department healthcare professional (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c54d88ca-3839-4e48-a38e-7d50287e6184"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "453231000124104",
                        "display": "Primary care provider (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "23904fac-e8f6-4c14-a8a7-b9fee6bb1c2b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45440000",
                        "display": "Rheumatologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "89593e60-bf27-4144-b5f5-7a37be9f8c1b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45544007",
                        "display": "Neurosurgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3af5abbf-f020-46e0-a5b6-64be0b70a12a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "457141000124107",
                        "display": "Locum tenens attending physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "58f63830-570e-4885-ad5c-ba043ee4a648"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "457151000124109",
                        "display": "Locum tenens admitting physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e93fb23-ef92-4321-9b6f-e3352bd3ddbe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "45956004",
                        "display": "Sanitarian (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5ae2d5aa-4903-4164-8220-19c35aeb84e9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "46255001",
                        "display": "Pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b2c63199-887f-4298-99b6-acff283403e3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "471302004",
                        "display": "Government midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9101b0be-d0f5-4ea6-bc50-add32322573a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "48639005",
                        "display": "Ordained minister (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "193a9a17-8d24-45f2-a08a-41ff333536a2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "48740002",
                        "display": "Philologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4ef58c2b-0fb7-4e7f-944f-b06247be123e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "49203003",
                        "display": "Dispensing optician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b22ce9c-f94d-4d05-84ca-f21bcf51cecf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "49993003",
                        "display": "Oral surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "89fa057e-c71f-40f7-89d3-54dedd755aa0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "50149000",
                        "display": "Endodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1bce0410-d6b2-44e0-929d-a10df2394367"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "50681000087109",
                        "display": "Bariatric surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "91315096-70cd-4c7c-bc53-f2ce0b049fa6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "5191000124109",
                        "display": "Private midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "92d423aa-fbf2-436b-8f71-fb80450fa313"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "5275007",
                        "display": "Auxiliary nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "51193f16-c7dd-448d-80e6-0e52d21ae52a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "53564008",
                        "display": "Ordained clergy (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "52330773-8d84-4362-be43-8d965530ae48"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "54503009",
                        "display": "Faith healer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c9ddddf-1b14-41b6-a441-7254c1567f45"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "56397003",
                        "display": "Neurologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c734743b-b9a1-43cb-80bc-b6cda1bb1e4e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "56466003",
                        "display": "Public health physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fe95f28d-4d48-4c64-a840-c8198dce3ad1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "56542007",
                        "display": "Medical record administrator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "eb10b6aa-a8c2-4193-b36c-ecab89fb2816"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "56545009",
                        "display": "Cardiovascular surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f1943306-a7a8-48e9-9dfc-26f4ec64dfc6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "57654006",
                        "display": "Fixed prosthodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bc2c15c4-6bc4-4b5c-88de-a3e654692779"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "59058001",
                        "display": "General physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6945fd26-f2a5-4b55-8c8e-9c11e41679ab"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "59169001",
                        "display": "Orthopedic technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2f882d3c-20a0-47c6-a854-e969171e0589"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "59317003",
                        "display": "Dental prosthesis maker and repairer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d4127359-dd2a-4c79-b306-df6674c19bf2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "59944000",
                        "display": "Psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4ea84177-ce21-4597-bc40-b9cf4980eeb9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "60008001",
                        "display": "Public health nutritionist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a4d4a082-6c4b-4b99-acad-ffdcf3764553"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "611581000124105",
                        "display": "Cognitive neuropsychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9c409da5-b02a-477a-820b-6da0e61f2d63"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "611601000124100",
                        "display": "Neonatal nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "44665cdd-88d6-4227-b5cf-aa32aaaa9816"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "611611000124102",
                        "display": "Counseling psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19f54827-ef2d-43aa-a5ea-72f8b73d37b6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "611621000124105",
                        "display": "Clinical neuropsychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c43505c8-eb1e-4ae1-a84f-ced807056baa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "611701000124107",
                        "display": "Sleep psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94e14d40-9e19-4957-8db4-c1bbdef59d9e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "61207006",
                        "display": "Medical pathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2a033785-c3af-4adc-9e4f-4eb256ffda5b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "61246008",
                        "display": "Laboratory medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0e07c302-d954-4c2f-bb14-d9409d4ac37b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "61345009",
                        "display": "Otorhinolaryngologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "955f3fb8-f90f-4198-bf41-0c63c86da720"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "61894003",
                        "display": "Endocrinologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "74d650e3-bf37-4411-9d58-aa90723729f1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "62247001",
                        "display": "Family medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e61509a5-f7ec-4729-a87f-5e8cb195079d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "63098009",
                        "display": "Clinical immunologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3853ff25-836b-46f8-bac0-06d8624a62ee"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "64220005",
                        "display": "Religious worker (member of religious order) (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b9feb444-e654-46a5-af66-e3a01a5a5de8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "651501000124106",
                        "display": "Pediatric emergency medicine physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6c65b479-363f-4b50-9b92-9f5a9ab47be8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "65803006",
                        "display": "Missionary (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "13438bc2-1d94-4eea-a67f-13c979260963"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "66476003",
                        "display": "Oral pathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bd1bde0b-0cb7-4a67-993e-9d93afcaccda"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "66862007",
                        "display": "Radiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b05cf91-659f-4fa3-8c27-efcbedfed7f1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "671101000124102",
                        "display": "Family nurse practitioner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "03e289ad-2747-427f-8134-09d78cb228d3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "67811000052107",
                        "display": "Pediatric hematology and oncology physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dcabf5b9-178c-4acb-8b78-0a45d24f4e31"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "6816002",
                        "display": "Specialized nurse (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9db398a5-2107-4110-bf10-9beeb1808ebe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "68191000052106",
                        "display": "Neuropsychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6273d23c-e2c4-45d2-89ac-0ed889d4eb7b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "6868009",
                        "display": "Hospital administrator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "76152991-d0d7-4b69-a330-937c4b793419"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "68867008",
                        "display": "Public health dentist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0f7a3fc8-d342-406f-a118-bc3975e90d8f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "68950000",
                        "display": "Prosthodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "027b6e5a-2c1b-4ea7-baa5-29efad52c8df"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "69280009",
                        "display": "Specialized physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "47c97cc5-94b6-47f5-99e1-62f8329e4ee9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "71838004",
                        "display": "Gastroenterologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c4f505d0-77cc-4629-8175-d1cb668a8a0b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "720503005",
                        "display": "Sleep medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1e837cbc-10f8-49bd-80e4-a1099bff0e0d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721936008",
                        "display": "Occupation medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8965a6c9-3b1c-4f8a-85b4-8ceafc00b64c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721937004",
                        "display": "Preventive medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f345be1c-4687-4895-ae00-ad5ca6ca02f6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721938009",
                        "display": "Tropical medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8778194d-cde1-48ee-b767-88f57851296a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721939001",
                        "display": "Vascular medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "76160059-0d5b-415c-ad5d-3ff7ba1269ed"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721940004",
                        "display": "Legal medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ce2ec553-129a-4642-9256-49a51c7d4bcc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721941000",
                        "display": "Health psychologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94ded6a6-7b52-4c70-97cb-767fd099e0b5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721942007",
                        "display": "Cardiovascular perfusionist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b98bfeb1-cd89-4336-9b23-f2d2d373fca3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "721943002",
                        "display": "Clinical immunology and allergy specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0e3213a2-0827-4349-b371-aca63d0dfcfb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "73265009",
                        "display": "Nursing aid (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d65fe75a-c0c8-4b01-9809-0a01cb167ca5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "734293001",
                        "display": "Clinical pharmacist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c30b6a7a-aab2-4538-8236-3c2cf62c8e4f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "734294007",
                        "display": "Pharmacist prescriber (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8efb4d5c-4b11-4f55-8753-5bfee941d162"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "75271001",
                        "display": "Professional midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8f2faf0e-9b9f-4516-8bff-57591abd52b2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "76166008",
                        "display": "Practical aid (pharmacy) (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d0244f05-53ee-442a-8f40-612d9d85299f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "76231001",
                        "display": "Osteopath (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "02a87a84-40bf-435b-80a7-cece700fe835"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "763292005",
                        "display": "Radiation oncologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2a2369f8-78a8-4e32-b9a5-32a29da27124"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768730001",
                        "display": "Home health aide (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7fbe958b-8149-47b0-afc1-c5478cae24cb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768731002",
                        "display": "Home helper (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c47ca47-43ab-4ffd-8765-02812dc08750"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768732009",
                        "display": "School health educator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6d084672-863c-457b-9d4a-8c8425923360"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768733004",
                        "display": "Spiritual advisor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "15a4b4fb-77d9-43d6-9951-52759a5d2d89"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768734005",
                        "display": "Research study coordinator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b830e441-b9ed-467d-b127-f5d4159b99c4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768815003",
                        "display": "Investigative specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2e038f13-22be-4313-b327-bd7e2d637bce"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768816002",
                        "display": "Associate investigator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "85440b1a-178d-433b-ba2f-555998d0d190"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768817006",
                        "display": "Co-principal investigator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b095490-5c79-459f-943b-21df850e067d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768818001",
                        "display": "Principal investigator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ac224ea3-021b-4040-93b1-c1d46706659b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768819009",
                        "display": "Medically responsible investigator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "18ad7abd-d8a4-4b1e-9c1e-58d6dbe90d37"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768820003",
                        "display": "Care coordinator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a6dda28c-fe04-4e73-ace2-8cc2c9de7709"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768821004",
                        "display": "Care team coordinator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2cdd7019-3591-40af-9bb0-b2441067f479"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768822006",
                        "display": "Rehabilitation coordinator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2211d838-64f3-4cea-ad18-6f3c5358e272"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768825008",
                        "display": "Doula (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4542d0f9-cd46-4f39-852b-cd74dff1b03f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768826009",
                        "display": "Crisis counselor (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "33871d7a-5e5e-4ab5-b199-81ba7ad8dcdf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768827000",
                        "display": "Nutritionist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "70072301-edae-4664-8b0b-4615fb306244"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768828005",
                        "display": "Epidemiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e31ed488-9b89-4fbb-a844-6a204ae7200e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768829002",
                        "display": "Community dietician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "98aa2fdb-4e39-429f-8196-4cc81419a264"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768832004",
                        "display": "Case manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d4a5e44d-55d7-44cf-876d-00d820d7b622"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768833009",
                        "display": "Discharging physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4711bc53-7b48-4cf4-8d1a-dcfb07f3e9fc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768834003",
                        "display": "Disease manager (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "25812d87-f165-4854-8a30-c98dcd22e14d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768836001",
                        "display": "Patient navigator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6d09824f-48d8-46be-9533-f91b1cf7dccf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768837005",
                        "display": "Hospitalist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6ad93dc9-216d-4949-97a2-ac2658f53338"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "768839008",
                        "display": "Consultant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a37435d0-d22f-438c-8539-c8005e9f4adf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "76899008",
                        "display": "Infectious disease specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e23a0a9-ebb7-421a-a89a-079a9f8dcdc2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "769038007",
                        "display": "Researcher (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2db9af99-c24e-4247-af0d-aa8da576a134"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "78703002",
                        "display": "General surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2546e12d-684f-4959-82eb-b04d09b8b5c3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "78729002",
                        "display": "Diagnostic radiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2a0b2ec3-4ba0-40a2-87e0-ca8e03830ab1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "789543004",
                        "display": "Sonographer (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a19a8409-f74e-4f5d-8b4d-c2168f8f73ae"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "79898004",
                        "display": "Auxiliary midwife (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9a32948b-eff3-47fd-95e5-aa48a6d8f787"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "79918004",
                        "display": "Ordained priest (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4cfdae7e-4e21-4826-a65d-8e52d6da004f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "80409005",
                        "display": "Translator (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1f9ca576-f9a3-4d60-990d-2ce9571e2ccd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "80546007",
                        "display": "Occupational therapist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "de092a3d-d8de-42ab-bbd8-bf82eb051ca4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "80584001",
                        "display": "Psychiatrist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cc3f7b71-4b55-4398-8b28-ba5813799318"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "80933006",
                        "display": "Nuclear medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e75f3da5-1a3f-4c70-95d4-ac77852276d0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "81464008",
                        "display": "Clinical pathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "37b59010-e68f-4b59-a3c6-0d1f3bf7edfd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "82296001",
                        "display": "Pediatrician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dbd466ab-6752-4c99-936f-eeac9e106324"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "83273008",
                        "display": "Anatomic pathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ca8136f7-957e-482e-89e9-d1858f92f047"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "83685006",
                        "display": "Gynecologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e717c22e-2525-46ff-8488-638a1231f46b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "840583002",
                        "display": "Allied health assistant (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "11df7cc1-948f-44d9-92f7-e411ce209709"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "840584008",
                        "display": "Allied health student (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "48822924-feef-4b1e-91d1-5a973a56655e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "85733003",
                        "display": "General pathologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "58d29e15-cd40-4797-8b40-5a5ffa276dbf"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "8724009",
                        "display": "Plastic surgeon (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7efda738-1b7d-460d-9227-6405f730c63d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "878785002",
                        "display": "Clinical respiratory physiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "606c1724-4207-4cf1-bf71-dc13b0ac3892"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "878786001",
                        "display": "Operating room technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c713556e-fda5-4cfa-8dd0-6b6508033ee3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "878787005",
                        "display": "Anesthesia technician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c914fdc3-4fbd-4b51-85a9-8c5cff7d0a98"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "88189002",
                        "display": "Anesthesiologist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "50fa7eac-3014-4005-8c06-46b12a0d785b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "897187007",
                        "display": "Sexual assault nurse examiner (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5ec5d1e8-5ef8-4987-9238-3d62b2762a3a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "90201008",
                        "display": "Pedodontist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "542c7f9d-e138-40a3-887c-6bfd8e7dd20b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "90655003",
                        "display": "Geriatrics specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "99801c49-c0d9-4085-b4ca-3c55addf4b06"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "90981000221101",
                        "display": "Endoscopist physician (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "17ac6530-bcc8-40ad-81cf-3b17412d572f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "9371000175105",
                        "display": "Adolescent medicine specialist (occupation)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c3fc2e0c-1168-44ad-9e05-29050a1ece9e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ADMPHYS",
                        "display": "admitting physician",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9b728d9b-ca6e-45f4-a0be-8d86dab96365"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ANEST",
                        "display": "anesthesist",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2e25e442-d5fa-4fa7-b36a-1cfc55346b32"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ANRS",
                        "display": "anesthesia nurse",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8777d56d-1a0b-4fe7-b48e-1b701969a91d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ASSEMBLER",
                        "display": "assembly software",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "98e44f4f-fe0f-4a85-9fa5-d18ee9555760"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ATTPHYS",
                        "display": "attending physician",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c45bafe7-afda-4898-9930-e3513edaf5aa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AUCG",
                        "display": "caregiver information receiver",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8c393592-972e-47ad-977c-f79aa00add4c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AUCOV",
                        "display": "consent overrider",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "916e1fb6-00c1-4276-8b9b-a5ca0e4ba7fb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AUEMROV",
                        "display": "emergency overrider",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "704a8bf4-47fc-4d2e-a788-0cec9a854f77"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AULR",
                        "display": "legitimate relationship information receiver",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4b13d20d-5511-411a-8079-61267f96a276"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AUTM",
                        "display": "care team information receiver",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "94978c7b-dbb2-4e2c-8565-64ba4233b7b8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "AUWA",
                        "display": "work area information receiver",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b438dc24-e933-40e4-bd12-725150f838b9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "CLMADJ",
                        "display": "claims adjudication",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0c4c859f-8cae-4281-ad71-8f39ebaa9164"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "COMPOSER",
                        "display": "composer software",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "53cb336f-9104-47fb-9ae8-f9cc282acf82"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "DISPHYS",
                        "display": "discharging physician",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1310acc2-e106-4a6a-a7d2-c6b29cafd9b4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "ENROLL",
                        "display": "enrollment broker",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e4bcf0e-73c2-4bec-a842-afb58f27323e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "FASST",
                        "display": "first assistant surgeon",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "13240c06-393d-4d48-b84d-027a0440fb49"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "FFSMGT",
                        "display": "ffs management",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "42021d0f-d7d1-45c6-a625-e32207a6289f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "FULINRD",
                        "display": "fully insured",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5ba9c9a4-ba3c-4091-943a-67e3d63bf614"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "GRDCON",
                        "display": "legal guardian consent author",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "84384a7d-1e07-40a8-8819-cb3582f2d95c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "MCMGT",
                        "display": "managed care management",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7fb7b468-eee4-4a7f-b458-00073c8a4117"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "MDWF",
                        "display": "midwife",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c714b4da-6c5e-4365-8bb8-9327c7af7e4a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "NASST",
                        "display": "nurse assistant",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "28b48494-b695-4890-9fdd-fd146ca8c156"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PAYORCNTR",
                        "display": "payor contracting",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "eac65341-2c14-46e1-82b6-6506f0371907"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PCP",
                        "display": "primary care physician",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4faef88f-5a65-46f0-9638-1461b65c443f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "POACON",
                        "display": "healthcare power of attorney consent author",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cbb2cb5b-f640-452e-9695-477005d4a7c8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PRCON",
                        "display": "personal representative consent author",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f89420bf-65c9-4cb4-9712-e40b5f16237a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PRISURG",
                        "display": "primary surgeon",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "15a16e19-175d-470a-a1a2-2abdefd041cb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PROMSK",
                        "display": "authorized provider masking author",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a25ff39b-2e28-4fb6-9743-9f96607b83a6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "PROVMGT",
                        "display": "provider management",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "37aece9f-ea2f-4068-b3ee-df696d4a0b7a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "REINS",
                        "display": "reinsures",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "936672de-6454-4e7c-ab11-95ba38af0e68"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "RETROCES",
                        "display": "retrocessionaires",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e937ea5-013e-4767-bfb8-26bf3401a784"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "REVIEWER",
                        "display": "reviewer",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "835cdea1-cf72-4bcd-a125-23c796c4b393"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "RNDPHYS",
                        "display": "rounding physician",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c39e69cd-cc48-45f1-b4fd-08baacff6060"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "SASST",
                        "display": "second assistant surgeon",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5b209ac6-f5f6-4f3b-a1f7-ebdb17bc2ce8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "SELFINRD",
                        "display": "self insured",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fca490b4-d6b5-4977-8568-d0eb7dd6bb90"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "SNRS",
                        "display": "scrub nurse",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d1aa375c-5ef3-4b42-b076-3b02074fd3e2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "SUBCON",
                        "display": "subject of consent author",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "550e8b61-89a5-420f-a127-15dd065e3813"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "SUBCTRT",
                        "display": "subcontracting risk",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e0cc7f4c-6cb6-45f7-ab07-60a1d9df741b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "TASST",
                        "display": "third assistant",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3ec26a54-e868-411a-9477-d98f68200042"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "UMGT",
                        "display": "utilization management",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "619891d7-1c25-41b2-959f-2f79cbd729dc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "UNDERWRTNG",
                        "display": "underwriting",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "213b1d99-78b0-471e-a072-bf18b32896a7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_AuthorizedParticipationFunction",
                        "display": "AuthorizedParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3086d498-22c1-45f7-9464-56b807b50a8f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_AuthorizedReceiverParticipationFunction",
                        "display": "AuthorizedReceiverParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "536df024-1344-4594-9a32-1d17687e0d24"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_ConsenterParticipationFunction",
                        "display": "ConsenterParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "edb8a7e8-27bc-4d2d-8ead-1a2a9ed04667"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_CoverageParticipationFunction",
                        "display": "CoverageParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1bf0ae5b-2546-48a7-82da-45128358e09c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_OverriderParticipationFunction",
                        "display": "OverriderParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d66af34e-dc81-45fd-8ceb-d67310db0171"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_PayorParticipationFunction",
                        "display": "PayorParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5eed29b7-dc48-4d74-8b52-58c720e33974"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_SponsorParticipationFunction",
                        "display": "SponsorParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "23c920a4-91db-4dbb-bcfd-bd7dae602a74"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://hl7.org/fhir",
                        "system": "ParticipationFunction",
                        "code": "_UnderwriterParticipationFunction",
                        "display": "UnderwriterParticipationFunction",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f8e92c0b-5f7c-42e6-893d-4d93cbc9c016"
                            }
                        ]
                    }
                }
            ]
        }
    ]
}
    ],
    "error": null
}
```

#### B.2. Food Insecurity Assessment Questionnaire Bundle Response

```json
{
    "resourceType": "Bundle",
    "id": "89916142-7ea8-4cdd-b313-58131b297835",
    "identifier": [
        {
            "system": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "value": "83e8fab5-0079-4714-a493-330975de766d"
        }
    ],
    "url": "http://example.org/sdoh/questionnaires/food-insecurity",
    "version": "1.0.0",
    "name": "SDOHFoodInsecurityQuestionnaire",
    "title": "SDOH Food Insecurity Assessment Questionnaire",
    "status": "active",
    "experimental": false,
    "date": "2025-05-12T16:00:00",
    "publisher": "CCMT",
    "description": "USCDI v3 Food Insecurity SDOH Assessment Questionnaire",
    "purpose": "Assess patient food security and nutritional access concerns for healthcare documentation and interoperability.",
    "approvalDate": "2025-05-12",
    "lastReviewDate": "2025-05-12",
    "effectivePeriod": {
        "start": "2025-05-12",
        "end": ""
    },
    "meta": {
        "versionId": "1",
        "lastUpdated": "2025-05-14T10:30:00Z",
        "profile": [
            "https://build.fhir.org/valueset-profiles.html"
        ]
    },
    "item": [
        {
            "item": [
                {
                    "linkId": "food-header-01",
                    "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                    "text": "Hunger Vital Sign [HVS]",
                    "type": "Header",
                    "required": false,
                    "code": [
                        {
                            "url": "http://loinc.org/",
                            "system": "LOINC",
                            "code": "88121-9",
                            "display": "Hunger Vital Sign [HVS]"
                        }
                    ],
                    "extension": [
                        {
                            "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                            "valueId": "c9fbc823-bc65-4ec6-9644-64108a50182d"
                        }
                    ]
                },
                {
                    "linkId": "food-question-88122-7",
                    "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                    "text": "Within the past 12 months we worried whether our food would run out before we got money to buy more.",
                    "type": "Question",
                    "answerType": "Option",
                    "required": true,
                    "code": [
                        {
                            "url": "http://loinc.org/",
                            "system": "LOINC",
                            "code": "88122-7",
                            "display": "Within the past 12 months we worried whether our food would run out before we got money to buy more."
                        }
                    ],
                    "extension": [
                        {
                            "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                            "valueId": "36472ba5-c5f0-4c42-b24e-7bc555ad0310"
                        }
                    ],
                    "answerOption": [
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA28397-0",
                                "display": "Often true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "03ecd2b0-5d9d-4b22-bea4-9f531bdc1e7e"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA6729-3",
                                "display": "Sometimes true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "b8f9b064-7372-4ef9-82ee-2f82aa08d566"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA28398-8",
                                "display": "Never true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "b4ec84bd-000c-4a42-a8de-7660e6494922"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA30968-4",
                                "display": "Don't know/refused",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "3fd04d0b-1af9-4fb4-9bcc-ae96c1eb0c62"
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    "linkId": "food-question-88123-5",
                    "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                    "text": "Within the past 12 months, the food we bought just didn't last and we didn't have money to get more.",
                    "type": "Question",
                    "answerType": "Option",
                    "required": true,
                    "code": [
                        {
                            "url": "http://loinc.org/",
                            "system": "LOINC",
                            "code": "88123-5",
                            "display": "Within the past 12 months, the food we bought just didn't last and we didn't have money to get more."
                        }
                    ],
                    "extension": [
                        {
                            "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                            "valueId": "41b7bbd1-c45e-40eb-9896-f2ce48565719"
                        }
                    ],
                    "answerOption": [
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA28397-0",
                                "display": "Often true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "4e83f432-435b-4755-a92d-c99da1413240"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA6729-3",
                                "display": "Sometimes true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "766d0e5e-ea61-4f1f-8ef9-1d711a55ee0a"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA28398-8",
                                "display": "Never true",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "bc75be3b-4921-41a4-9c31-f5f0481c095d"
                                    }
                                ]
                            }
                        },
                        {
                            "valueCoding": {
                                "url": "http://loinc.org/",
                                "system": "LOINC",
                                "code": "LA30968-4",
                                "display": "Don't know/refused",
                                "extension": [
                                    {
                                        "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                        "valueId": "8dae5494-b454-4cc9-9d4b-5418a4a81b9b"
                                    }
                                ]
                            }
                        }
                    ]
                }
            ]
        },
        {
            "linkId": "food-goals",
            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "text": "Food Insecurity Goals",
            "type": "Display",
            "answerType": "Choice",
            "extension": [
                {
                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                    "valueId": "81b81b67-28e2-4f75-af42-22d78c3fbc15"
                }
            ],
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1078229009",
                        "display": "Food security (finding)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e88cafcb-ea8c-4ef6-9f46-b4bd4f0da5ff"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "671311000124101",
                        "display": "Feels food intake quantity is adequate for meals and snacks (finding)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c7f3e7b5-3776-4b50-8f07-740b2132388c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "671321000124109",
                        "display": "Feels food intake quantity is adequate for snacks (finding)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e36e848-e4f4-4250-8150-3fd35b0ceecb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "671331000124107",
                        "display": "Feels food intake quantity is adequate for meals (finding)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "14f49fc4-1661-41e4-b0fc-e282795f9367"
                            }
                        ]
                    }
                }
            ]
        },
        {
            "linkId": "food-interventions",
            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "text": "Food Insecurity Interventions",
            "type": "Display",
            "answerType": "Choice",
            "extension": [
                {
                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                    "valueId": "b3b22237-9ee9-47d8-9a9c-72800d341269"
                }
            ],
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1002224003",
                        "display": "Assessment for food insecurity (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fcc6e728-892f-441d-b3cd-1650cadd787e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1002225002",
                        "display": "Assessment of barriers in food insecurity care plan (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e4bba543-b760-4fc1-bb41-8483d0f18f76"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1004109000",
                        "display": "Assessment of goals to achieve food security (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0aae8458-eec0-42e0-941a-fb58b32dc635"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1759002",
                        "display": "Assessment of nutritional status (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "394ee158-2c12-462e-8eb9-b9dfa83f30fd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1002223009",
                        "display": "Assessment of progress toward goals to achieve food security (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b42a094a-1b82-4ef8-a9d9-a596e606f232"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467781000124107",
                        "display": "Assistance with application for Child and Adult Care Food Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5e91d1a1-2386-469a-a929-322f88136088"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467801000124106",
                        "display": "Assistance with application for Community Meal Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19ef3898-5a7a-489c-8e8d-b249df5f94bc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467811000124109",
                        "display": "Assistance with application for Farmers' Market Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d19c8bad-4ac1-4b5f-988e-3aa3bf861303"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467791000124105",
                        "display": "Assistance with application for Food Distribution Program on Indian Reservations (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d9d50850-67ae-43a9-9f49-07ddee694e9a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467741000124101",
                        "display": "Assistance with application for Gus Schumacher Nutrition Incentive-funded Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "95665271-f999-461b-ae82-efb984b20319"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467711000124100",
                        "display": "Assistance with application for Senior Farmers' Market Nutrition Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cc1e65f6-d801-4ac7-8148-bdea28e95577"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467691000124103",
                        "display": "Assistance with application for Special Supplemental Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "48de50fe-0ab0-49ee-91ee-17a67ea6035c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "662151000124104",
                        "display": "Assistance with application for State Funded Food Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b84ed8a-371f-4f25-b46a-a59e9c75657a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467681000124101",
                        "display": "Assistance with application for Summer Food Service Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c8282050-c77f-49b0-a405-65816ee0bdac"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467821000124101",
                        "display": "Assistance with application for Supplemental Nutrition Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fa1a9248-7936-4ef7-90c2-9e1dbf43a41b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467771000124109",
                        "display": "Assistance with application for food pantry program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "efff4e8f-445d-4656-a390-a0782042f908"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467761000124102",
                        "display": "Assistance with application for food prescription program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1dcee764-8de4-409f-bbb5-24933cb22eeb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467751000124104",
                        "display": "Assistance with application for garden program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "85e7edb4-76a1-444b-950d-623abdc99d8b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467731000124106",
                        "display": "Assistance with application for home-delivered meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "652200ce-665b-407e-966d-7a1e35a10eca"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467721000124108",
                        "display": "Assistance with application for medically tailored meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7e23631c-ab1b-410f-b23a-9ef5d8c29e03"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470241000124102",
                        "display": "Assistance with application for national school lunch program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "be5cb62f-b485-4517-a207-c0c33b9eb8ec"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470261000124103",
                        "display": "Assistance with application for school breakfast program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "88808efc-f6bd-4ab7-ae57-226c57ffbab7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445301000124102",
                        "display": "Content-related nutrition education (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0768da30-7f67-45b0-a82e-b980d0e1781d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "1004110005",
                        "display": "Coordination of resources to address food insecurity (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c0c7ef53-4a90-4d02-b29e-70bc6ec8a7c6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441041000124100",
                        "display": "Counseling about nutrition (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4c2be9cc-903e-4d24-83f1-ac258d832fc8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441201000124108",
                        "display": "Counseling about nutrition using cognitive behavioral theoretical approach (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "329fd863-33b2-45f9-9d77-b38fb33522c6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441331000124108",
                        "display": "Counseling about nutrition using cognitive restructuring strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "98afd429-53bc-427b-a72b-b7d7a19ea71b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441271000124102",
                        "display": "Counseling about nutrition using goal setting strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fbab746b-272d-453e-86be-e5d2596baa7a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441231000124100",
                        "display": "Counseling about nutrition using health belief model (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "253323ef-b3f6-42bf-a6e0-9adbf74ed2b4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441261000124109",
                        "display": "Counseling about nutrition using motivational interviewing technique (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c1d34a23-cb8a-4d15-94a3-c2b117728914"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441291000124101",
                        "display": "Counseling about nutrition using problem solving strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f1e01bfd-e67b-481a-906b-14237b39220f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441341000124103",
                        "display": "Counseling about nutrition using relapse prevention strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "50852e86-b8f2-498b-8a99-ca1f1141b15f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441351000124101",
                        "display": "Counseling about nutrition using rewards and contingency management strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "380928f4-0cdf-4579-a1e9-83359452d963"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441281000124104",
                        "display": "Counseling about nutrition using self-monitoring strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "22f26ec4-d429-4d4c-943e-e1cd6dc2f25e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441241000124105",
                        "display": "Counseling about nutrition using social learning theory approach (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9d76ffc2-5967-4d77-ad74-e2becfb1d1b1"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441301000124100",
                        "display": "Counseling about nutrition using social support strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "009b2635-af8f-4e6f-a4e6-375b117928eb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441321000124105",
                        "display": "Counseling about nutrition using stimulus control strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "da83049d-24dc-4072-b847-1d21f2156e25"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441311000124102",
                        "display": "Counseling about nutrition using stress management strategy (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d822c8c7-a669-4db1-875c-7c015f3b257d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "441251000124107",
                        "display": "Counseling about nutrition using transtheoretical model and stages of change approach (regime/therapy)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1b483548-af15-4f77-ad55-5fcc0cb2e228"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464691000124107",
                        "display": "Counseling for barriers to achieving food security (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "04c7efbe-c502-4eda-8fd8-b022cf91b997"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464681000124109",
                        "display": "Counseling for food insecurity care plan participation barriers (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e09ea13b-169d-4385-95a8-477f6c6a8c08"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464701000124107",
                        "display": "Counseling for readiness to achieve food security goals (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "525d19cd-1f86-430e-93d9-9ed1b7a88fb5"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464671000124106",
                        "display": "Counseling for readiness to implement food insecurity care plan (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0ed9a6fc-9828-451b-8e5c-4969ae8eb5dd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464201000124103",
                        "display": "Education about Child and Adult Care Food Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9813e4aa-94ad-4b71-83a3-caee02a061e7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464211000124100",
                        "display": "Education about Community Meals Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a813427c-2b9c-4737-899f-7eb5ee8a873d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464331000124103",
                        "display": "Education about Farmers' Market Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "77ca6cfb-dbdb-4f86-84b2-b9bb5e6a173a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464321000124101",
                        "display": "Education about Food Distribution Program on Indian Reservations (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c303fb10-f595-498e-ace6-3e162f4a32bc"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464221000124108",
                        "display": "Education about Gus Schumacher Nutrition Incentive-funded Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5731ee17-daa6-48e3-8f11-bf265dbe6e2c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464341000124108",
                        "display": "Education about Senior Farmers' Market Nutrition Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ace7a5bd-167d-4bd0-853b-c3161efc8de2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464281000124107",
                        "display": "Education about Special Supplement Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dc0c5621-904f-4c80-9aaa-0b5cfe9b1ac6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "661101000124109",
                        "display": "Education about State Funded Food Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "63d7fe11-9a83-49d3-8057-2743fc8e284d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464371000124100",
                        "display": "Education about Summer Food Service Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "16f92088-d918-4713-a832-d1a99f2fca35"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464361000124107",
                        "display": "Education about Supplemental Nutrition Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8ad49950-71de-4611-8a6f-a2a2bd12dc9c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464351000124105",
                        "display": "Education about congregate meal program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c056940c-0ff6-49c4-a795-ecff30669351"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464231000124106",
                        "display": "Education about food pantry program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "6c3d198f-2930-4239-ab34-5a8275854112"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464241000124101",
                        "display": "Education about food prescription program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9b4ed00e-04bd-4771-8c2a-b0c095b6b6b2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464251000124104",
                        "display": "Education about garden program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1bce4ef6-b4a4-4bf8-b6e4-43053fe21f83"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464261000124102",
                        "display": "Education about home-delivered meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0fcfa83f-509b-4b71-a2e4-8ed3719aa121"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464271000124109",
                        "display": "Education about medically tailored meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "773a4da9-56db-40b2-9e34-86f663d110e3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470311000124105",
                        "display": "Education about national school lunch program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0ecd27ec-6134-4266-b751-6150759f177b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470321000124102",
                        "display": "Education about school breakfast program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d893e664-dce2-48f0-b6d0-c429fc662f8b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467671000124104",
                        "display": "Evaluation of eligibility for Child and Adult Care Food Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "22d670c3-23a7-4b0f-bcf6-cc00e88b3930"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467661000124106",
                        "display": "Evaluation of eligibility for Community Meal Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a399f819-60b2-4b33-b678-f012f3d759c7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467611000124108",
                        "display": "Evaluation of eligibility for Farmers' Market Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1db2d01f-ab7e-4639-a064-f33e37d00ea0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467601000124105",
                        "display": "Evaluation of eligibility for Food Distribution Program on Indian Reservations (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2b986d6f-2678-44c5-9a13-68675440aa9e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467641000124107",
                        "display": "Evaluation of eligibility for Gus Schumacher Nutrition Incentive-funded Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "46bf7000-26d4-4333-9833-167706b75d70"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464631000124108",
                        "display": "Evaluation of eligibility for Meals on Wheels Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e6c936e9-f774-4d30-9078-1ef6d437db5d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464651000124101",
                        "display": "Evaluation of eligibility for Senior Farmers' Market Nutrition Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2ed4cdff-8953-4929-ab8e-235aea31d7ef"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464661000124104",
                        "display": "Evaluation of eligibility for Special Supplemental Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "432855bd-1910-4655-9f16-6b98effc4cef"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "662651000124105",
                        "display": "Evaluation of eligibility for State Funded Food Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "63457be1-b942-4160-a2a0-dbae4255a841"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467631000124102",
                        "display": "Evaluation of eligibility for Summer Food Service Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7f38fbbc-1e16-4205-ab5a-d006e6e4fccb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467621000124100",
                        "display": "Evaluation of eligibility for Supplemental Nutrition Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dac2f2ed-12d4-4d0e-8866-eaf58071501c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467591000124102",
                        "display": "Evaluation of eligibility for food pantry program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "7fff2d95-18f9-4eff-a4cb-00d913bbba49"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "468401000124109",
                        "display": "Evaluation of eligibility for food prescription program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "228eb6d7-cf50-495a-a505-65816af81d5a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "467651000124109",
                        "display": "Evaluation of eligibility for garden program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c49b4302-8ce0-4eef-ad4d-bf97549e96b2"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464621000124105",
                        "display": "Evaluation of eligibility for home-delivered meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f5405732-b77f-41ec-b61f-c21486c03deb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464641000124103",
                        "display": "Evaluation of eligibility for medically tailored meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "a5f2cfee-830a-4272-94b8-8a69bbaa4608"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470271000124105",
                        "display": "Evaluation of eligibility for national school lunch program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "34ac7d7a-2c96-4693-968b-49b8cd75bb31"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470281000124108",
                        "display": "Evaluation of eligibility for school breakfast program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "efce32f7-9bad-46e0-a40c-7a070e63fe82"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "385767005",
                        "display": "Meals on wheels provision education (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "adff1aea-00ca-4b1c-a939-4c1e72aec1f6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "61310001",
                        "display": "Nutrition education (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "ae21c529-37d2-4b44-be16-37ff97f1a00e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445291000124103",
                        "display": "Nutrition-related skill education (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "75db88e9-f5b6-42d0-8b55-f60a9bdc2812"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "710925007",
                        "display": "Provision of food (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "46e83a08-9588-4b6b-abf6-c742cf2f88eb"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464721000124102",
                        "display": "Provision of food prescription (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "16ba509b-4535-4b58-a5f1-fb384ede4a43"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464411000124104",
                        "display": "Provision of food voucher (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "13ca15ef-3fcf-43f3-ba88-53f07cf28490"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464401000124102",
                        "display": "Provision of fresh fruit and vegetable voucher (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4fd67dda-895d-4cc9-b12f-29ccea1ec3c7"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464421000124107",
                        "display": "Provision of home-delivered meals (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "aefc377b-43ed-4ff7-ac67-f02b90feef3d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464431000124105",
                        "display": "Provision of medically tailored meals (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9950ab8d-4aa9-4af0-ae9c-742a7a75bf91"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464381000124102",
                        "display": "Provision of prescription for infant formula (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1411dfb0-fe00-4148-9915-00ba4a095428"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464041000124106",
                        "display": "Referral to Child and Adult Care Food Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "689abb6a-f42f-42fe-8996-7661e2365260"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464181000124104",
                        "display": "Referral to Farmers' Market Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f371973b-1c47-4218-bcab-66b8ce406d47"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464191000124101",
                        "display": "Referral to Food Distribution Program on Indian Reservations (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "57d19e6a-e65e-4af2-97b0-538af707dec0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464051000124108",
                        "display": "Referral to Gus Schumacher Nutrition Incentive-funded Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "957db53c-fb41-4cca-a3f7-53dfcaedd30e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464141000124105",
                        "display": "Referral to Meals on Wheels Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "31f1fd05-3bed-4826-bd56-1189e1ab8512"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464171000124102",
                        "display": "Referral to Senior Farmers' Market Nutrition Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "628eb0e9-9faa-4945-8942-4831dd2d2240"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464111000124106",
                        "display": "Referral to Special Supplemental Nutrition Program for Women, Infants and Children (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "13cf8959-e82a-4996-a003-2550df30940a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "663081000124100",
                        "display": "Referral to State Funded Food Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "b1d5f21b-b0cc-4167-abb4-759ef1f108f6"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464121000124103",
                        "display": "Referral to Summer Food Service Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "370584a6-198f-49a9-bacc-d0548ec518fa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464101000124108",
                        "display": "Referral to Supplemental Nutrition Assistance Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "388606d0-be3e-4362-96be-a9b3bd3d08bd"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "713109004",
                        "display": "Referral to community meals service (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5d3b4add-0371-4212-9b7b-e358caf2719c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464151000124107",
                        "display": "Referral to congregate meal program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9c87fd4a-804f-4e07-9f01-36f4055d2ffe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "103699006",
                        "display": "Referral to dietitian (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cdfd7e53-0fc3-4841-b3fc-74db353e3890"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464031000124101",
                        "display": "Referral to food pantry program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d23ca6eb-3c97-4e48-8d53-28305494779c"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464061000124105",
                        "display": "Referral to food prescription program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "0cd849ed-03fd-436e-a4e4-650394485c5b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464071000124103",
                        "display": "Referral to garden program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d30390dd-e62e-488a-8513-81138e321d49"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464081000124100",
                        "display": "Referral to home-delivered meals program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "9c072cd4-2483-4e43-9bbe-583adf85abf4"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "464091000124102",
                        "display": "Referral to medically tailored meal program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "bf9d2fc3-fdf6-407b-83f4-00d0939f8c0b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470291000124106",
                        "display": "Referral to national school lunch program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f68c4a68-2284-432f-a3a1-e075a560fd52"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "470301000124107",
                        "display": "Referral to school breakfast program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "16538adf-b920-44e3-8975-a176d1a46b6f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMEDCT",
                        "code": "445641000124105",
                        "display": "Technical nutrition education (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "2c93763f-297b-4433-8d25-590d20ecb7fc"
                            }
                        ]
                    }
                }
            ]
        }
    ]
}
```

#### B.3. Digital Assessment Questionnaire Bundle Response

```json
{
    "resourceType": "Bundle",
    "id": "ded9327f-8d0f-4148-a383-f291047831c4",
    "identifier": [
        {
            "system": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "value": "93de66d2-3688-4431-b0b7-84e994f1b094"
        }
    ],
    "url": "http://example.org/sdoh/questionnaires/digital-access",
    "version": "1.0.0",
    "name": "SDOHDigitalAccessQuestionnaire",
    "title": "SDOH Digital Assessment Questionnaire",
    "status": "active",
    "experimental": false,
    "date": "2025-05-12T16:00:00",
    "publisher": "CCMT",
    "description": "USCDI v3 Digital Access SDOH Assessment Questionnaire",
    "purpose": "Assess patient access to digital resources and technology codes for healthcare documentation and interoperability.",
    "approvalDate": "2025-05-12",
    "lastReviewDate": "2025-05-12",
    "effectivePeriod": {
        "start": "2025-05-12",
        "end": ""
    },
    "meta": {
        "versionId": "1",
        "lastUpdated": "2025-05-14T10:30:00Z",
        "profile": [
            "https://build.fhir.org/valueset-profiles.html"
        ]
    },
    "item": [
        {
            "item": [
                {
                    "linkId": "digital-assessment-question-99802-1",
                    "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                    "text": "Digital Access Assessment",
                    "type": "Group",
                    "required": true,
                    "code": [
                        {
                            "url": "http://loinc.org/",
                            "system": "LOINC ",
                            "code": "99802-1",
                            "display": "At this house apartment or mobile home, do you or any member of this household own or use any of the following types of computers?"
                        }
                    ],
                    "extension": [
                        {
                            "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                            "valueId": "cc4ba0cf-3c20-4ea4-a8b2-87b66abe387f"
                        }
                    ],
                    "item": [
                        {
                            "linkId": "digital-question-LA33217-3",
                            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                            "text": "Desktop or laptop",
                            "type": "Question",
                            "answerType": "Option",
                            "required": true,
                            "code": [
                                {
                                    "url": "http://loinc.org/",
                                    "system": "LOINC ",
                                    "code": "LA33217-3",
                                    "display": "Desktop or laptop"
                                }
                            ],
                            "extension": [
                                {
                                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                    "valueId": "eb0f86f2-0ad8-456a-8f82-c3e2b6fc4c88"
                                }
                            ],
                            "answerOption": [
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA33-6",
                                        "display": "Yes",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "036876b5-4d07-425c-84b3-5227cc2374f0"
                                            }
                                        ]
                                    }
                                },
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA32-8",
                                        "display": "No",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "98b5be62-39a5-4ae8-ab13-4da047e4d2d2"
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        {
                            "linkId": "digital-question-LA33219-9",
                            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                            "text": "Smartphone",
                            "type": "Question",
                            "answerType": "Option",
                            "required": true,
                            "code": [
                                {
                                    "url": "http://loinc.org/",
                                    "system": "LOINC ",
                                    "code": "LA33219-9",
                                    "display": "Smartphone"
                                }
                            ],
                            "extension": [
                                {
                                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                    "valueId": "ef0d3455-e055-4be0-87d4-d83cd58f278d"
                                }
                            ],
                            "answerOption": [
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA33-6",
                                        "display": "Yes",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "5065f7f5-8465-41da-b205-f34f693155e1"
                                            }
                                        ]
                                    }
                                },
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA32-8",
                                        "display": "No",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "75e2204c-ecfb-48bb-9849-42c37dfd5f0c"
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        {
                            "linkId": "digital-question-LA33218-1",
                            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                            "text": "Tablet or other portable wireless computer",
                            "type": "Question",
                            "answerType": "Option",
                            "required": true,
                            "code": [
                                {
                                    "url": "http://loinc.org/",
                                    "system": "LOINC ",
                                    "code": "LA33218-1",
                                    "display": "Tablet or other portable wireless computer"
                                }
                            ],
                            "extension": [
                                {
                                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                    "valueId": "f5defa94-0cc7-4920-8ac4-082383737aef"
                                }
                            ],
                            "answerOption": [
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA33-6",
                                        "display": "Yes",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "10e94aa2-3f8a-40a6-a6f2-a1b620e3ef71"
                                            }
                                        ]
                                    }
                                },
                                {
                                    "valueCoding": {
                                        "url": "http://loinc.org/",
                                        "system": "LOINC",
                                        "code": "LA32-8",
                                        "display": "No",
                                        "extension": [
                                            {
                                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                                "valueId": "e1e8c8c1-fbe8-4a54-b626-83e9b257cbf6"
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        {
                            "linkId": "digital-question-LL6196-1",
                            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
                            "text": "Some other type of computer",
                            "type": "Question",
                            "answerType": "Text",
                            "required": true,
                            "code": [
                                {
                                    "url": "http://loinc.org/",
                                    "system": "LOINC ",
                                    "code": "LL6196-1",
                                    "display": "Some other type of computer"
                                }
                            ],
                            "extension": [
                                {
                                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                    "valueId": "6c796605-d798-4f11-a532-91fe9b067bb2"
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            "linkId": "digital-goals",
            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "text": "Digital Access Goals",
            "type": "Display",
            "answerType": "Choice",
            "extension": [
                {
                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                    "valueId": "3b9abf0b-7977-4866-99e7-914a06ecf908"
                }
            ],
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "1268660000",
                        "display": "Affordable connectivity program",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "880eb3aa-786d-4e66-9eb9-11b942387a48"
                            }
                        ]
                    }
                }
            ]
        },
        {
            "linkId": "digital-interventions",
            "definition": "http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Questionnaire",
            "text": "Digital Access Interventions",
            "type": "Display",
            "answerType": "Choice",
            "extension": [
                {
                    "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                    "valueId": "ce173c55-3317-415a-a3e8-7998580406c5"
                }
            ],
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661931000124103",
                        "display": "Assistance with application for Affordable Connectivity Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "df9b230a-0179-4b65-a28c-41bb0fdcfe77"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662121000124108",
                        "display": "Assistance with application for Cash Assistance Program for Immigrants (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3fb84bf5-8d19-4036-99b6-544bd43bbe66"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "1254703000",
                        "display": "Assistance with application for financial assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1c685367-afa1-4e48-970b-ca937809e139"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661831000124108",
                        "display": "Assistance with application for state general assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "e7f27404-892d-43f3-abaf-28c3ede28e5a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661771000124102",
                        "display": "Assistance with application for telephone assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1a1c9118-6820-4a88-956d-19703972bae9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662161000124102",
                        "display": "Assistance with application to Lifeline communication program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3c8c9bb5-f47c-4908-aa2d-dce155ffd7b8"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661851000124101",
                        "display": "Assistance with application to utility assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1cf42106-17d1-4e48-9936-c8dcb3d51716"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651241000124106",
                        "display": "Education about Affordable Connectivity Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "dece0df0-956f-4363-a6a2-e8e770d47e52"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661431000124106",
                        "display": "Education about Cash Assistance Program for Immigrants (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8ba230a9-cc8e-4ca6-b8f1-526f4d7bef90"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "661201000124104",
                        "display": "Education about Lifeline communication program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "00ec0fb6-055f-484e-91e9-33198c45518a"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651101000124104",
                        "display": "Education about Telecommunication Relay Service (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19bb4b45-fd7a-4651-8803-3551b1f4bd3e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "1258903001",
                        "display": "Education about financial assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "69676a43-e4fc-4f87-9575-3b46f67e8b58"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651171000124105",
                        "display": "Education about reading service for disability affecting reading (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "408f5ae5-86e6-400d-8a71-a3f565a5c784"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651181000124108",
                        "display": "Education about state general assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "465e4ce5-ac1e-4c28-8a2a-7ebf5a0c6bbe"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651291000124102",
                        "display": "Education about telephone assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "5b148efb-dd8e-4b3f-8ee5-8130cdbc5b1f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "651271000124103",
                        "display": "Education about utility assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "86a377fc-2eda-49a6-9a8a-2e923fc71b34"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662371000124106",
                        "display": "Evaluation of eligibility for Affordable Connectivity Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "033923ab-04ef-4db5-86d8-ee43ecbdbfe9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662741000124102",
                        "display": "Evaluation of eligibility for Cash Assistance Program for Immigrants (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "02701947-ffba-48d4-a233-1f780a60a235"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662581000124101",
                        "display": "Evaluation of eligibility for Lifeline communication program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "325c794b-52d3-4699-95da-10255486416b"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "1258900003",
                        "display": "Evaluation of eligibility for financial assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "d01b1e3b-f13c-4a1b-8019-87474c6986e9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662321000124105",
                        "display": "Evaluation of eligibility for state general assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "eb7f0c6e-25ae-44cf-8faa-ed5408d97cb9"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662421000124104",
                        "display": "Evaluation of eligibility for telephone assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "3459f13d-a394-4d91-a7b1-c71ece2b56fa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662291000124101",
                        "display": "Evaluation of eligibility for utility assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "c9843853-a69f-4ddb-b875-24760befaded"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "243747004",
                        "display": "Provision of wireless (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19923f12-b76f-45fd-8e11-b20079c8b601"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662911000124103",
                        "display": "Referral to Affordable Connectivity Program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "4521c42f-d16c-4a2e-b1c8-d67a96909e5f"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "671001000124101",
                        "display": "Referral to Cash Assistance Program for Immigrants (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "611cb8a5-2c95-4a2d-9693-1b488afa6295"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "663241000124101",
                        "display": "Referral to Lifeline communication program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "58875b40-0b82-4745-bf45-448766c11afa"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662801000124107",
                        "display": "Referral to Telecommunication Relay Service (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "19cb94df-92c9-483b-a89c-ce659fab0476"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "841441000124101",
                        "display": "Referral to digital navigator (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "55483de5-a84d-4e75-b0ff-f52a0c610fd0"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "1254705007",
                        "display": "Referral to financial assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cd582e72-5419-4a9b-bfd9-2cdf06ad266d"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662841000124109",
                        "display": "Referral to reading service for disability affecting reading (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "010013c8-fdd6-4dca-b473-e901653945c3"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662951000124102",
                        "display": "Referral to state general assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "f2f6153f-1922-4be8-af2b-54252300b22e"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662771000124105",
                        "display": "Referral to telephone assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "fda90309-fb73-40db-ac89-2fc69755b292"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://snomed.info/sct",
                        "system": "SNOMED",
                        "code": "662811000124105",
                        "display": "Referral to utility assistance program (procedure)",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "8a77870a-7558-46a3-bfea-9f01786c422b"
                            }
                        ]
                    }
                }
            ]
        }
    ]
}
```

### B.4. Tribal Affiliation Value Set Response

```json
{
    "id": "950fd7aa-c2fb-4def-a123-3a792b1a3a0d",
    "url": "http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS",
    "date": "2025-05-12T16:00:00Z",
    "item": [
        {
            "code": [
                {
                    "code": "tribal-affiliation",
                    "system": "http://example.org/fhir/CodeSystem/item-type",
                    "display": "Tribal Affiliation"
                }
            ],
            "text": "Tribal Affiliation Codes",
            "type": "valueSet",
            "linkId": "https://www.hl7.org/implement/standards/product_brief.cfm?product_id=447",
            "required": false,
            "extension": [
                {
                    "url": "http://terminology.hl7.org/ValueSet/v3-TribalEntityUS",
                    "valueString": "http://terminology.hl7.org/ValueSet/v3-TribalEntityUS"
                }
            ],
            "definition": "https://www.hl7.org/implement/standards/product_brief.cfm?product_id=447",
            "answerOption": [
                {
                    "valueCoding": {
                        "url": "http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS",
                        "code": "1",
                        "system": "v3-TribalEntityUS",
                        "display": "Absentee-Shawnee Tribe of Indians of Oklahoma",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "cdb186c8-853d-4a84-80d2-09e5db01ef91"
                            }
                        ]
                    }
                },
                {
                    "valueCoding": {
                        "url": "http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS",
                        "code": "339",
                        "system": "v3-TribalEntityUS",
                        "display": "Agdaagux Tribe of King Cove",
                        "extension": [
                            {
                                "url": "http://example.org/fhir/StructureDefinition/option-unique-id",
                                "valueId": "1a0e1789-b8d4-4108-a827-8b37b5e4722d"
                            }
                        ]
                    }
                }
                // Additional entries omitted for brevity
            ]
        }
    ],
    "meta": {
        "profile": [
            "https://build.fhir.org/valueset-profiles.html"
        ],
        "versionId": "1.0.0",
        "lastUpdated": "2025-05-12T16:00:00Z"
    },
    "name": "TribalAffiliationUSCDIv3",
    "title": "Tribal Affiliation USCDI v3",
    "status": "active",
    "purpose": "To provide a standardized set of Tribal Affiliation codes for healthcare documentation and interoperability.",
    "version": "1.0.0",
    "publisher": "CCMT",
    "identifier": [
        {
            "value": "4861b05d-c84e-44a0-aa29-0a7e981b84fd",
            "system": "http://terminology.hl7.org/ValueSet/v3-TribalEntityUS"
        }
    ],
    "description": "This value set defines the comprehensive set of codes that can be used to indicate the Tribal Affiliation according to USCDI v3 requirements. It includes all TribalEntityUS codes from the source list",
    "approvalDate": "2025-05-12",
    "experimental": false,
    "resourceType": "valueSet",
    "lastReviewDate": "2025-05-12",
    "effectivePeriod": {
        "end": "",
        "start": "2025-05-12"
    }
}
```

### C. Key Response Fields Explained

| Field                 | Description                                                                    |
| --------------------- | ------------------------------------------------------------------------------ |
| id                    | Master content UUID that can be used to always fetch the latest active version |
| identifier.value      | Version-specific UUID that can be used to fetch a specific version             |
| resourceType          | Type of content ("valueSet" for lists or "bundle" for SDOH content)            |
| status                | Indicates if the content is "active", "draft", "retired", etc.                 |
| meta.versionId        | The version number of the content                                              |
| effectivePeriod.start | When this version became effective                                             |
| effectivePeriod.end   | If null, content is active; if populated, indicates when it was deprecated     |
| answerOption          | For valueSet resources, contains the list of available codes                   |
| item                  | Container for code definitions and metadata in valueSet resources              |

### D. SDOH Questionnaire Types Comparison

The three Bundle examples in sections B.2 and B.3 demonstrate different Social Determinants of Health (SDOH) assessments:

1. **Food Insecurity Assessment (B.2)**:
   - Focuses on identifying food security concerns
   - Includes standardized questions about food availability
   - Contains goals related to food security
   - Provides interventions such as assistance with food programs

2. **Digital Access Assessment (B.3)**:
   - Assesses technology access and digital literacy
   - Contains yes/no questions about device ownership (desktop, smartphone, tablet)
   - Offers goals related to digital connectivity
   - Includes interventions like assistance with connectivity programs

Each SDOH questionnaire follows a similar structure:
- Assessment questions section
- Goals section with available options
- Interventions section with available procedures

This standardized approach allows healthcare providers to consistently evaluate social factors that impact health outcomes and connect patients with appropriate resources.

### E. Response ResourceType Comparison

The examples in sections B.1, B.2, and B.3 illustrate the key differences between the two main content types:

1. **ValueSet (Care Team Member Role and Tribal Affiliation)**: 
   - Uses `resourceType="valueSet"`
   - Contains a collection of code values with descriptions
   - Organized as a flat list of options
   - Typically used for reference data and terminology

2. **Bundle (Food Insecurity and Digital Access Assessments)**:
   - Uses `resourceType="bundle"`
   - Contains structured content with hierarchical items
   - Includes assessment questions, goals, and interventions
   - Used for more complex SDOH (Social Determinants of Health) content

This distinction is important when processing the API responses, as each resource type requires different handling logic in client applications.

### F. Tribal Affiliation ValueSet

The Tribal Affiliation ValueSet (B.4) provides a standardized set of codes for documenting a patient's tribal affiliation according to USCDI v3 requirements:

- **Purpose**: Enables consistent documentation of tribal affiliations in healthcare records
- **System**: Uses the `http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS` coding system
- **Content**: Includes comprehensive list of federally recognized tribes in the United States
- **Structure**: Each tribe entry contains:
  - A unique code identifier
  - The full official name of the tribe
  - A unique option ID for reference
- **Usage**: Can be used in clinical documentation, demographics, and reporting systems
- **Integration**: Compatible with other USCDI v3 data elements for interoperability

This valueSet is particularly important for healthcare settings serving indigenous populations, allowing proper documentation of tribal affiliations to support culturally appropriate care, eligibility for tribal health programs, and accurate demographic reporting.

Working with this valueSet follows the same patterns as other valueSet resources in the API, with access to codes through the `answerOption` array, each containing a `valueCoding` object with the tribe's information.
