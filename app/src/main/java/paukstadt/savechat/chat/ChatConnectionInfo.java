package paukstadt.savechat.chat;

import java.net.InetAddress;

public class ChatConnectionInfo
{
    private boolean success = false;
    private InetAddress ip;
    private int port;
    private String mime;
    private byte[] content;
    private String phonenumber;

    public ChatConnectionInfo(InetAddress ip, int port, String mime, byte[] content, String phonenumber)
    {
        this.ip = ip;
        this.port = port;
        this.mime = mime;
        this.content = content;
        this.phonenumber = phonenumber;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public boolean getSuccess()
    {
        return this.success;
    }

    public InetAddress getIp()
    {
        return this.ip;
    }

    public int getPort()
    {
        return this.port;
    }

    public String getMime()
    {
        return this.mime;
    }

    public byte[] getContent()
    {
        return this.content;
    }

    public String getPhonenumber()
    {
        return this.phonenumber;
    }
}
