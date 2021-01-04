package pl.umcs.rafalkloc.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ServerMessageCH {

    public static String serializeToString(ServerMessage msg)
    {
        try {
            return new ObjectMapper().writeValueAsString(msg);
        } catch (JsonMappingException ignored) {

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ServerMessage deserializeFromString(String msg)
    {
        try {
            return new ObjectMapper().readValue(msg, ServerMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
