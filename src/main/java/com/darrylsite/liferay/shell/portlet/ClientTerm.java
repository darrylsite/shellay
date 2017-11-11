package com.darrylsite.liferay.shell.portlet;

import com.darrylsite.liferay.shell.constants.Constants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.websocket.Session;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;

/**
 * 
 * @author Darryl Kpizingui
 *
 */
public class ClientTerm 
{
	private String path = ".";
	private Session session;
	private final String _TERM = "TERM";
	private final String _SHELL = "xterm";
	private LogOutputStream terminalOutputStream;
	private ExecuteResultHandler executeResultHandler;
	
	private ShellStreamHandler processStreamHandler;
	private Map<String, String> ENV_VARS;
	
	private boolean processRunning;
	
	
	public ClientTerm(Session session)
	{
		this.session = session;
		
		try
		{
			processStreamHandler = new ShellStreamHandler(getOutputStream(), getOutputStream());
			ENV_VARS = EnvironmentUtils.getProcEnvironment();
			if(!ENV_VARS.containsKey(_TERM)) ENV_VARS.put(_TERM, _SHELL);
		} 
		catch (Exception e) 
		{
			_log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public boolean isOpen()
	{
		return session != null && session.isOpen();
	}
	
	private LogOutputStream getOutputStream()
	{
		if(terminalOutputStream != null) return terminalOutputStream;
		
		return terminalOutputStream = new LogOutputStream() 
	    {
			protected void processLine(String line, int logLevel) 
			{
				sendText(line);
			}
		};
	}
	
	private ExecuteResultHandler getExecuteResultHandler()
	{
		if(executeResultHandler != null) return executeResultHandler;
		
		return executeResultHandler = new DefaultExecuteResultHandler()
		{
			@Override
			public void onProcessComplete(int exitValue)
			{
				super.onProcessComplete(exitValue);
				processRunning = false;
			}
			
			public void onProcessFailed(ExecuteException e) 
			{
				super.onProcessFailed(e);
				processRunning = false;
			}
		};
	}
	
	public void runCmd(String command)
	{
		if(command == null || command.trim().isEmpty()) return;
				
		//new directory
		if(command.startsWith("cd ")) 
		{
			String newPath = command.substring(3);
			
			if(!newPath.startsWith(File.separator))
			{
				if("..".equals(newPath))
				{
					if(Paths.get(path).getParent() != null)
					{
						newPath = Paths.get(path).getParent().toFile().getAbsolutePath();
					}
				}
				else
				{
					newPath = Paths.get(path, newPath).toFile().getAbsolutePath();
				}
			}
			
			if(Files.exists(Paths.get(newPath)))
			{
				path = Paths.get(newPath).toAbsolutePath().toString();
				sendText("current dir > " + path);
			}
			else
			{
				sendText("Invalid dir > " + newPath);
			}
			
			return;
		}
		
		try
		{
			if(processRunning)
			{
				sendText("... processRunning");
				if(processStreamHandler.getProcessOutputStream() != null)
				{
					processStreamHandler.getProcessOutputStream().write(command.getBytes(StandardCharsets.UTF_8));
				}
			}
			else
			{
				sendText("... new process");
				CommandLine commandline = CommandLine.parse(command);

			    DefaultExecutor executor = new DefaultExecutor();
		
			    ExecuteWatchdog watchdog = new ExecuteWatchdog(Constants.PROCESS_TIME_OUT);
			    executor.setExitValue(0);
			    executor.setWatchdog(watchdog);
		
			    executor.setStreamHandler(processStreamHandler);
			    executor.setWorkingDirectory(new File(path));
			    
			    processRunning = true;
			    executor.execute(commandline, ENV_VARS, getExecuteResultHandler());
			}
		}
		catch (Exception e)
		{
			processRunning = false;
			_log.error(e);
		}
	}
	
	private void sendText(String message)
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
	
	 private static Log _log = LogFactoryUtil.getLog(ClientTerm.class);
}