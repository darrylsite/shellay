package com.darrylsite.liferay.shell.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.osgi.service.component.annotations.Component;

/**
 * 
 * @author Darryl Kpizingui
 *
 */
@Component(
	    immediate = true,
	    property = {"org.osgi.http.websocket.endpoint.path=/o/ShellSocket/tty"},
	    service = Endpoint.class
	)
public class WebSocketTTY extends javax.websocket.Endpoint
{
	private static final Map<String, ClientTerm> terminals;
	
	static
	{
		terminals = new HashMap<>();
	}
	
	protected void bindTTY(final Session session, final EndpointConfig endPoint, final String message)
	{
		ClientTerm clientTerm = terminals.get(session.getId());
		
		if(clientTerm == null)
		{
			clientTerm = new ClientTerm(session);
			terminals.put(session.getId(), clientTerm);
		}
		
		clientTerm.runCmd(message);
	}

	@Override
	public void onOpen(final Session session, final EndpointConfig endPoint) 
	{
		sendText(session, "total terminals : " + (terminals.size()+1) + "\n");
		
		session.addMessageHandler(new MessageHandler.Whole<String>() 
		{
			public void onMessage(String message) 
			{
				if(message == null) return;				
				bindTTY(session, endPoint, message);
			}
		});			
	}
	
	@Override
	public void onClose(Session session, CloseReason closeReason) 
	{
		super.onClose(session, closeReason);
		
		terminals.remove(session.getId());
	}
	
	@Override
	public void onError(Session session, Throwable thr) 
	{
		if(!(thr instanceof IOException))
		{
			_log.error(thr);
		}
		
	}
	
	private static void sendText(Session session, String message)
	{
		try 
		{
			session.getBasicRemote().sendText(message);
		} 
		catch (IOException e)
		{
			_log.debug(e);
		}
	}

    private static Log _log = LogFactoryUtil.getLog(WebSocketTTY.class);
}