package pl.umcs.rafalkloc.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ClientMessageCH {

    public static String serializeToString(ClientMessage msg)
    {
        try {
            return new ObjectMapper().writeValueAsString(msg);
        } catch (JsonMappingException ignored) {

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ClientMessage deserializeFromString(String msg)
    {
        try {
            return new ObjectMapper().readValue(msg, ClientMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
