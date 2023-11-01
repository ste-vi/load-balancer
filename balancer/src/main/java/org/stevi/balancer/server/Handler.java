package org.stevi.balancer.server;

import java.io.IOException;
import java.net.Socket;

public interface Handler {
    void handle(Socket clientSocket) throws IOException;
}
