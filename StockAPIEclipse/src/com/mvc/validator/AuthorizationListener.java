package com.mvc.validator;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

public class AuthorizationListener implements PhaseListener {

	@Override
	public void afterPhase(PhaseEvent event) 
	{
		FacesContext facesContext = event.getFacesContext();
		String currentPage = facesContext.getViewRoot().getViewId();
		 
		boolean isLoginPage = (currentPage.lastIndexOf("index.xhtml") > -1);
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		 
		if(session==null)
		{
			NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
			nh.handleNavigation(facesContext, null, "index.xhtml");
		}
		else
		{
			Object currentUser = session.getAttribute("username");
		 
			if (!isLoginPage && (currentUser == null || currentUser == "")) 
			{
				NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
				nh.handleNavigation(facesContext, null, "index.xhtml");
			}
		}
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public PhaseId getPhaseId() 
	{
		return PhaseId.RESTORE_VIEW;
	}

}
