<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Add Database Types</title>
    <link href="css/dbcad.css" rel="stylesheet" type="text/css" />
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
	        function addDbType() {
		        // get the form values
		        var dbVendor = $('#db_vendor').val();
		        var dbRole = $('#db_role').val();
		
		        $.ajax({
			        type: "POST",
			        url: "rest/db_type",
			        data: "dbVendor=" + dbVendor + "&dbRole=" + dbRole + "&_method=PUT",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        $('#types-table-body').append('<tr class="table_row"><td>'
								+dbVendor+'</td><td>'+dbRole+'</td>'+
								'<td><input type="button" value="Delete" onclick="doDeleteDBType(\''+response+'\',this)"></td></tr>');
				        
			        },
			        error: function(e){
			        	$('#info').html("error");
			        	alert('Error: ' + e.responseText);
			        }
		        });       
	        }
	        function addDbInstance() {
		        // get the form values
		        var dbGroupId = $('#group_select').val();
		        var dbHost = $('#host').val();
		        var dbPort = $('#port').val();
		        var dbSid = $('#sid').val();
		        $.ajax({
			        type: "POST",
			        url: "rest/db_instance",
			        data: "dbGroupId=" + dbGroupId + "&dbHost=" + dbHost + "&dbPort=" + dbPort + "&dbSid=" + dbSid + "&_method=PUT",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        $('#types-table-body').append('<tr class="table_row"><td>'
								+dbGroupId+'</td><td>'+dbHost+'</td>'+dbPort+'</td>'+dbSid+'</td>'+
								'<td><input type="button" value="Delete" onclick="doDeleteDBInstance(\''+response+'\',this)"></td></tr>');
				        
			        },
			        error: function(e){
			        	$('#info').html("error");
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function doDeleteDBType(dbTypeId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_type/"+dbTypeId,
			        //data: "db_type_id=" + dbTypeId + "&_method=DELETE",
			        data: "_method=DELETE",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        var p=object.parentNode.parentNode;
				        p.parentNode.removeChild(p);
			        },
			        error: function(e){
			        	$('#info').html("error"+dbTypeId);
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function doDeleteDBInstance(dbId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_instance/"+dbId,
			        //data: "db_type_id=" + dbTypeId + "&_method=DELETE",
			        data: "_method=DELETE",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        var p=object.parentNode.parentNode;
				        p.parentNode.removeChild(p);
			        },
			        error: function(e){
			        	$('#info').html("error"+dbId);
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function load()
	        {
	        	$('#manage-databases-link').addClass("current");
	        }
        </script>
</head>
<body onload="load()">
 <%@ include file="BodyHeader.jsp" %>
		<input type="text" id="db_vendor" class="input_field" name="db_vendor" list="db_vendors"/>
		<datalist id="db_vendors">
		    <c:forEach items="${options.db_vendors}" var="dbVendor">
		             <option value="${dbVendor}">${dbVendor}</option>
		    </c:forEach>
		</datalist>
		<input type="text" id="db_role" class="input_field" name="db_role" list="db_roles"/>
		<datalist id="db_roles">
		    <c:forEach items="${options.db_roles}" var="dbRole">
		             <option value="${dbRole}">${dbRole}</option>
		    </c:forEach>
		</datalist>
		<div id="info" style="color: green;">info...</div>
<br/>
 
<input type="button" value="Add" onclick="addDbType()">
     

<table id="types-table" class="table">
                <thead>
                    <tr>
                        <th>Database Vendor</th>
                        <th>Database Role</th>
                    </tr>
                </thead>
                <tbody id="types-table-body">
                    <c:forEach items="${type_table_values}" var="tableRow">
                        <tr class="table_row">    
                            <td>${tableRow.db_vendor}</td>
                            <td>${tableRow.db_role}</td>
                            <td><input type="button" value="Delete" onclick="doDeleteDBType('${tableRow.db_type_id}',this)"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            
<h2>Add Database Instance</h2>
		<form:select path="options" id="group_select">
		    <form:options items="${options.db_groups}" />
		</form:select>
		<input type="text" id="host" name="host" class="input_field"/>
		<input type="text" id="port" name="port" class="input_field"/>
		<input type="text" id="sid" name="sid" class="input_field"/>
		<div id="info" style="color: green;">info...</div>
<br/>
 
<input type="button" value="Add" onclick="addDbInstance()">
     

			<table id="types-table" class="table">
                <thead>
                    <tr>
                        <th>Database Group Id</th>
                        <th>Database Host</th>
                        <th>Database Port</th>
                        <th>Database Sid</th>
                    </tr>
                </thead>
                <tbody id="types-table-body">
                    <c:forEach items="${instance_table_values}" var="tableRow">
                        <tr class="table_row">    
                            <td>${tableRow.db_group_id}</td>
                            <td>${tableRow.host}</td>
                            <td>${tableRow.port}</td>
                            <td>${tableRow.sid}</td>
                            <td><input type="button" value="Delete" onclick="doDeleteDBInstance('${tableRow.db_id}',this)"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
	<%@ include file="BodyFooter.jsp" %>        
</body>
</html>
