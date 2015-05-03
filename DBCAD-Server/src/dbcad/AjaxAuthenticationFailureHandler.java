package dbcad;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

@Component("ajaxAuthenticationFailureHandler")
public class AjaxAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	
	public AjaxAuthenticationFailureHandler() {    
    }
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		//HttpSession session = request.getSession();
		 //DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) session.getAttribute(WebAttributes.SAVED_REQUEST);
	     //check if login is originated from ajax call
		System.out.println("Not Authenticated by");
		if ("true".equals(request.getHeader("X-Ajax-call"))){
			try{
				response.getWriter().print("not ok");
				response.getWriter().flush();
				System.out.println(exception.toString() + request.getParameter("j_username") + request.getParameter("j_password"));
			} catch (IOException e){ 
				e.printStackTrace();
			}
		}
//		else {
//			setAlwaysUseDefaultTargetUrl(false);
//		}
	}	
}