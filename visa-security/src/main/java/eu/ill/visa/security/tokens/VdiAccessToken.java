package eu.ill.visa.security.tokens;


import java.security.Principal;

public class VdiAccessToken implements Principal {


    public VdiAccessToken() {
    }


    @Override
    public String getName() {
        return "VDI Access";
    }
}

