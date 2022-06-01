package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Error Object compliant with Json API spec")
@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorObject {

    @Schema(description = "the HTTP status code applicable to this problem", required = true)
    public String status;

    @Schema(description = "an application-specific error code", required = true)
    public String code;

    @Schema(description = "a short, human-readable summary of the problem", required = true)
    public String title;

    @Schema(description = "a human-readable explanation specific to this occurrence of the problem")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String detail;

    public static final String CLONE_OVER_INITIAL_FORBIDDEN_STATUS = "400";
    public static final String CLONE_OVER_INITIAL_FORBIDDEN_CODE = "CLONE_OVER_INITIAL_FORBIDDEN";
    public static final String CLONE_OVER_INITIAL_FORBIDDEN_TITLE = "Cloning over initial variant is forbidden";

    public static ErrorObject cloneOverInitialForbidden() {
        return new ErrorObject(CLONE_OVER_INITIAL_FORBIDDEN_STATUS, CLONE_OVER_INITIAL_FORBIDDEN_CODE,
                CLONE_OVER_INITIAL_FORBIDDEN_TITLE, null);
    }

    public static final String CLONE_OVER_EXISTING_STATUS = "400";
    public static final String CLONE_OVER_EXISTING_CODE = "CLONE_OVER_EXISTING";
    public static final String CLONE_OVER_EXISTING_TITLE = "Cloning over existing variant without mayOverwrite=true";

    public static ErrorObject cloneOverExisting(String targetVariantId) {
        return new ErrorObject(CLONE_OVER_EXISTING_STATUS, CLONE_OVER_EXISTING_CODE, CLONE_OVER_EXISTING_TITLE,
                "Variant " + targetVariantId + " already exists");
    }
}
