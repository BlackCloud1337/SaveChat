package paukstadt.savechat.chat;

import java.util.Date;

import paukstadt.savechat.ClientServerContractClass;

public class ChatDTO
{
    private int id;
    private boolean fromMe;
    private Date timestamp;
    private String mime;
    private byte[] content;

    public ChatDTO()
    {
        this.id = -1;
        this.fromMe = true;
        this.timestamp = new Date();
        this.mime = ClientServerContractClass.MIME_TEXT;
        this.content = null;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public void setFromMe(boolean fromMe)
    {
        this.fromMe = fromMe;
    }

    public boolean getFromMe()
    {
        return this.fromMe;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public Date getTimestamp()
    {
        return this.timestamp;
    }

    public void setMime(String mime)
    {
        this.mime = mime;
    }

    public String getMime()
    {
        return this.mime;
    }

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    public byte[] getContent()
    {
        return this.content;
    }
}
