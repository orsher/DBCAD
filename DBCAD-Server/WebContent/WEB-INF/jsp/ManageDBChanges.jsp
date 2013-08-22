<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Manage Database Changes</title>
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
	        function addDBChange() {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_change/"+$('#schema_select :selected').val()+"/"+$('#db_change_id_text_field').val(),
			        data: "db_change_text="+$('#db_change_text').val()+ "&_method=PUT",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
			        },
			        error: function(e){
			        	$('#info').html("error");
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function deleteDBChange(dbChangeId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_change/"+dbChangeId,
			        //data: "db_type_id=" + dbTypeId + "&_method=DELETE",
			        data: "_method=DELETE",
			        success: function(response){
				        // we have the response
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
 
<h2>MAnage Database Changes</h2>
		<form:select path="options" id="schema_select">
		    <form:options items="${options.db_schemas}" />
		</form:select>
		<input type="text" id="db_change_id_text_field" name="db_change_id_text_field"/>
		<br/>
		<textarea id="db_change_text" style="width: 500px; height: 170px;"></textarea>
		<br/>
		<input type="button" value="Create" onclick="addDBChange()">
		<table id="db-changes-table" class="table">
                <thead>
                    <tr>
                        <th></th>
                        <th>Database Change ID</th>
                    </tr>
                </thead>
                <tbody id="db-changes-table-body">
                    <c:forEach items="${options.db_changes}" var="dbChange">
                        <tr class="table_row">
                        	<td>${dbChange}</td>
                        	<td><input type="button" value="Delete" onclick="deleteDBChange('${dbChange}',this)"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
</body>
</html>