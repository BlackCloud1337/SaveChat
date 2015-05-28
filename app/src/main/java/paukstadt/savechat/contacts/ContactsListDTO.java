package paukstadt.savechat.contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.InetAddress;
import java.util.Date;

import paukstadt.savechat.ClientServerContractClass;
import paukstadt.savechat.R;

public class ContactsListDTO
{
    private Context context;
    private int id = -1;
    private String alias = "";
    private String number = "";
    private Bitmap avatar = null;
    private String status = "";
    private Date lastContact = new Date();
    private InetAddress ip = null;
    private int port = -1;
    private boolean heartbeat = false;

    public ContactsListDTO(Context context)
    {
        this.context = context;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public void setAlias(String alias)
    {
        if(alias == null)
        {
            this.alias = "";
        }
        else
        {
            this.alias = alias;
        }
    }

    public String getAlias()
    {
        return this.alias;
    }

    public void setNumber(String number)
    {
        if(number == null || number.equals("")) return;
        this.number = ClientServerContractClass.normalizePhoneNumber(number);
    }

    public String getNumber()
    {
        return this.number;
    }

    public void setAvatar(Bitmap avatar)
    {
        if(avatar == null)
        {
            this.avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        }
        else
        {
            this.avatar = avatar;
        }
    }

    public Bitmap getAvatar()
    {
        return this.avatar;
    }

    public void setStatus(String status)
    {
        if(status == null)
        {
            this.status = "";
        }
        else
        {
            this.status = status;
        }
    }

    public String getStatus()
    {
        return this.status;
    }

    public void setLastContact(Date lastContact)
    {
        if(lastContact == null) return;
        if(lastContact.before(this.lastContact)) return;

        this.lastContact = lastContact;
    }

    public Date getLastContact()
    {
        return this.lastContact;
    }

    public void setIp(InetAddress ip)
    {
        this.ip = ip;
    }

    public InetAddress getIp()
    {
        return this.ip;
    }

    public void setPort(int port)
    {
        if(port <= 0) return;
        this.port = port;
    }

    public int getPort()
    {
        return this.port;
    }

    public void setHeartbeat(boolean heartbeat)
    {
        this.heartbeat = heartbeat;
    }

    public boolean getHeartbeat()
    {
        return this.heartbeat;
    }
}
