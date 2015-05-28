package paukstadt.savechat;

public class ClientServerContractClass
{
    public static final int CLIENT_LISTENER_PORT = 1337;

    public static final String SERVER_IP = "192.168.1.71";
    public static final int SERVER_PORT = 4711;

    public static final String RESPONSE_ACK = "ACK";
    public static final String RESPONSE_ERR = "ERR";

    public static final String MSG_MIME = "mime";
    public static final String MSG_CONTENT = "content";
    public static final String MSG_CONTACTID = "phonenumber";

    public static final String MIME_TEXT = "text";
    public static final String MIME_IMG_JPG = "img/jpg";
    public static final String MIME_IMG_PNG = "img/png";

    public static final String JSON_USER_ID = "id";
    public static final String JSON_ALIAS = "al";
    public static final String JSON_STATUS = "st";
    public static final String JSON_LASTCONTACT = "lc";
    public static final String JSON_IP = "ip";
    public static final String JSON_PORT = "po";
    public static final String JSON_HEARTBEAT = "hb";
    public static final String JSON_AVATAR = "av";
    public static final String JSON_AVATAR_MIME = "mi";
    public static final String JSON_AVATAR_CONTENT = "co";

    public static String normalizePhoneNumber(String wildPhoneNumber)
    {
        wildPhoneNumber = wildPhoneNumber.replace("-", "");
        wildPhoneNumber = wildPhoneNumber.replace("/", "");
        wildPhoneNumber = wildPhoneNumber.replace(" ", "");

        if(!wildPhoneNumber.startsWith("+49"))
        {
            while(wildPhoneNumber.startsWith("0")) wildPhoneNumber = wildPhoneNumber.substring(1);
            wildPhoneNumber = "+49" + wildPhoneNumber;
        }

        return wildPhoneNumber;
    }
}
