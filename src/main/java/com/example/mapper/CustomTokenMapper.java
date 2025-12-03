package com.example.mapper;

import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.*;

public class CustomTokenMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger logger = Logger.getLogger(CustomTokenMapper.class);
    public static final String PROVIDER_ID = "custom-value-token-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomTokenMapper.class);
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
                            UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {

        MultivaluedMap<String, String> formParams = keycloakSession.getContext()
                .getHttpRequest()
                .getDecodedFormParameters();

        Map<String, Object> customObject = new HashMap<>();

        formParams.forEach((key, values) -> {
            if (key.startsWith("custom_value.")) {
                String field = key.replace("custom_value.", ""); 
                String value = values.get(0);
                Object finalValue;
                try {
                    finalValue = Integer.parseInt(value);
                } catch (Exception e) {
                    finalValue = value;
                }
                
                customObject.put(field, finalValue);
            }
        });

        if (!customObject.isEmpty()) {
            String claimName = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);

            token.getOtherClaims().put(claimName, customObject);
            logger.info("Custom object added to token => " + customObject);
        } else {
            logger.warn("No custom_value.* fields received in request");
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() { return configProperties; }

    @Override
    public String getHelpText() { return "Adds object from request prefixed with custom_value.* into token"; }

    @Override
    public String getDisplayCategory() { return "Token Mapper"; }

    @Override
    public String getDisplayType() { return "Custom Value Object Mapper"; }

    @Override
    public String getId() { return PROVIDER_ID; }
}
