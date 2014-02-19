	<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
	<div id="dbcad_wrapper">
    <div id="dbcad_header">
	  	<div id="site_title"><h1><a href="/DBCAD-Server/">DBCAD</a></h1></div>
	  	<div id="login_logout">
	  		<sec:authorize ifAnyGranted="ROLE_ANONYMOUS">
	  			Hello Guest,
	  			<a href="spring_security_login" >Login</a>
	  		</sec:authorize>
	  		<sec:authorize ifNotGranted="ROLE_ANONYMOUS">
	  			Hello <sec:authentication property="principal.username" />,
	  			<a href="<c:url value="j_spring_security_logout" />" >Logout</a>
	  		</sec:authorize>
	  	</div>
	 </div>
      <div id="dbcad_menu">
        <ul>
        	<sec:authorize access="hasRole('ROLE_ADMIN')">
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