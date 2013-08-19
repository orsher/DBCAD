<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Add Database Types</title>
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
	        function doAjaxPost() {
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
	        
	        function doDeleteDBType(dbTypeId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_type",
			        data: "db_type_id=" + dbTypeId + "&_method=DELETE",
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
        </script>
</head>
<body>
 
<h2>Add Database Types</h2>
		<input type="text" id="db_vendor" name="db_vendor" list="db_vendors"/>
		<datalist id="db_vendors">
		    <c:forEach items="${options.db_vendors}" var="dbVendor">
		             <option value="${dbVendor}">${dbVendor}</option>
		    </c:forEach>
		</datalist>
		<input type="text" id="db_role" name="db_role" list="db_roles"/>
		<datalist id="db_roles">
		    <c:forEach items="${options.db_roles}" var="dbRole">
		             <option value="${dbRole}">${dbRole}</option>
		    </c:forEach>
		</datalist>
		<div id="info" style="color: green;">info...</div>
<br/>
 
<input type="button" value="Add" onclick="doAjaxPost()">
     

<table id="types-table" class="table">
                <thead>
                    <tr>
                        <th>Database Vendor</th>
                        <th>Database Role</th>
                    </tr>
                </thead>
                <tbody id="types-table-body">
                    <c:forEach items="${table_values}" var="tableRow">
                        <tr class="table_row">    
                            <td>${tableRow.db_vendor}</td>
                            <td>${tableRow.db_role}</td>
                            <td><input type="button" value="Delete" onclick="doDeleteDBType('${tableRow.db_type_id}',this)"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
</body>
</html>