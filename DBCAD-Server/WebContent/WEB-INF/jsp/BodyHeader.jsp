	<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
	<div id="login_logout">
		<div id="login_wrapper">
	  		<sec:authorize ifAnyGranted="ROLE_ANONYMOUS">
	  			<div id="login_div">
	  		</sec:authorize>
	  		<sec:authorize ifNotGranted="ROLE_ANONYMOUS">
	  			<div id="login_div" style="display: none;">
	  		</sec:authorize>
		  			<span href="#" class="button" id="toggle-login">Log in</span>
	
					<div id="login">
					  <div id="triangle"></div>
					  <h1>Log in</h1>
					  <form id="login_form" onSubmit="ajaxLogin(); return false;">
					  	<input type="hidden" id="my_callback" name="my_callback" class="input_field"/>
					    <input type="username" placeholder="Username" id="j_ajax_username" name="j_ajax_username" />
					    <input type="password" placeholder="Password" id="j_ajax_password" name="j_ajax_password"/>
						<div id="ajax_login_error"></div>
						<input type="submit" value="Log in" />
					  </form>
					</div>
				</div>
	  		<sec:authorize ifNotGranted="ROLE_ANONYMOUS">
	  			<div id="logout_div">
	  				<div id="hello div"> 
	  					Hello <sec:authentication property="principal.username" />,
	  					<a href="<c:url value="j_spring_security_logout" />" >Logout</a>
	  				</div>
	  		</sec:authorize>
	  		<sec:authorize ifAnyGranted="ROLE_ANONYMOUS">
				<div id="logout_div" style="display: none;">
	  				<div id="hello div">
	  					<a href="<c:url value="j_spring_security_logout" />" >Logout</a>
	  				</div>
	  		</sec:authorize>
	  			</div>
	  	</div>
	  </div>
	<div id="dbcad_wrapper">
    <div id="dbcad_header">
	  	<div id="site_title"><h1><a href="/DBCAD-Server/">DBCAD</a></h1></div>
	 </div>
      <div id="dbcad_menu">
        <ul>
        	<sec:authorize access="hasRole(T(dbcad.DBCADController).ADMIN_GROUP_NAME)">
            	<li><a id="manage-databases-link" href="manage-databases">Manange Databases</a></li>
            </sec:authorize>
            <li><a id="manage-db-changes-link" href="manage-db-changes">Manage DB Changes</a></li>
        </ul>    	
        <div id="search_box">
            <form action="#" method="post">
              <input name="q" type="text" id="searchfield" title="searchfield" onfocus="clearText(this)" onblur="clearText(this)" value="Search" size="10" maxlength="60" />
            </form>
        </div>
        <div class="cleaner"></div>
    </div>
 	<div id="dbcad_main">
 		<div id="dbcad_content">