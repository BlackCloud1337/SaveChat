package paukstadt.savechat.Server;

import java.io.IOException;
import java.net.ServerSocket;

import paukstadt.savechat.ClientServerContractClass;

public class Server
{
    public static void main(String[] args)
    {
        new Server();
    }

    public Server()
    {
        while(true)
        {
            try
            {
                listenForConnections();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void listenForConnections() throws IOException
    {
        while(true)
        {
            ServerSocket listener = new ServerSocket(ClientServerContractClass.SERVER_PORT);
            Connection conThread = new Connection(listener.accept());
            new Thread(conThread).start();
        }
    }
}