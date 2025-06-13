package com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups;

/**
 * Class representing a property of a LOINC code
 */
public class Property {
    private String id;
    private String name;
    private String value;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
