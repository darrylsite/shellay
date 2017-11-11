package com.darrylsite.liferay.shell.portlet;

import com.darrylsite.liferay.shell.constants.ShellSocketPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

/**
 * @author Darryl Kpizingui
 */
@Component(
	immediate = true,
	property = 
{
		"com.liferay.portlet.display-category=tools",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=Shellay",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/views/view.jsp",
		"javax.portlet.name=" + ShellSocketPortletKeys.ShellSocket,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"com.liferay.portlet.footer-portlet-javascript=/js/shellay.js"
	},
	service = Portlet.class
)
public class ShellSocketPortlet extends MVCPortlet 
{
}