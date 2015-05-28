package paukstadt.savechat.Server;

import java.net.Socket;

public class Connection implements Runnable
{
    private Socket connection;

    public Connection(Socket connection)
    {
        this.connection = connection;
    }

    @Override
    public void run()
    {
        if(connection == null) return;
    }
}
