package com.optimagrowth.license.utils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserContext {

    private String correlationId;
    private String authToken;
    private String userId;
    private String organizationId;
}
