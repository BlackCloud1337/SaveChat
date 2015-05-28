package paukstadt.savechat.chat;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import paukstadt.savechat.ClientServerContractClass;

public class ChatSender extends AsyncTask<ChatConnectionInfo, Void, ChatConnectionInfo>
{
    @Override
    protected ChatConnectionInfo doInBackground(ChatConnectionInfo... params)
    {
        if(params.length != 1) return null;
        ChatConnectionInfo conInfo = params[0];

        //Create json message
        JSONObject message = new JSONObject();
        try
        {
            message.put(ClientServerContractClass.MSG_CONTACTID, conInfo.getPhonenumber());
            message.put(ClientServerContractClass.MSG_MIME, conInfo.getMime());
            message.put(ClientServerContractClass.MSG_CONTENT, Base64.encodeToString(conInfo.getContent(), Base64.DEFAULT));
        }
        catch (JSONException e)
        {
            //Ignore for the moment
        }

        int retry = 0;
        Socket connection = null;
        PrintWriter sender = null;
        BufferedReader receiver = null;

        while(!conInfo.getSuccess() && retry < 3)
        {
            try
            {
                //Connect to communication partner and create sender and receiver
                connection = new Socket(conInfo.getIp(), conInfo.getPort());
                sender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
                receiver = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //Send message
                sender.println(message.toString());
                sender.flush();

                //Wait for answer from communication partner
                String input = receiver.readLine();
                if (input.equals(ClientServerContractClass.RESPONSE_ACK))
                {
                    conInfo.setSuccess(true);
                }
            }
            catch (IOException e)
            {
                retry++;
            }
            finally
            {
                try
                {
                    if (sender != null) sender.close();
                    if (receiver != null) receiver.close();
                    if (connection != null) connection.close();
                }
                catch (IOException e)
                {
                    //Nothing to do here
                }
            }
        }

        return conInfo;
    }
}
