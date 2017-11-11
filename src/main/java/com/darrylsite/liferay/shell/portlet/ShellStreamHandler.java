package com.darrylsite.liferay.shell.portlet;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.PumpStreamHandler;

public class ShellStreamHandler extends PumpStreamHandler 
{
	private OutputStream processOutputStream;
	

	public ShellStreamHandler() 
	{
		super();
	}

	public ShellStreamHandler(OutputStream out, OutputStream err, InputStream input) 
	{
		super(out, err, input);
	}

	public ShellStreamHandler(OutputStream out, OutputStream err) 
	{
		super(out, err);
	}

	public ShellStreamHandler(OutputStream outAndErr) 
	{
		super(outAndErr);
	}

	public void setProcessInputStream(OutputStream os) 
	{
		super.setProcessInputStream(os);
		processOutputStream = os;
	}
	
	public OutputStream getProcessOutputStream() 
	{
		return processOutputStream;
	}
}
