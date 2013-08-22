<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Add Database Types</title>
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
	        function doAjaxPost() {
	        	var checked_db_chages = [];
		        $(".db_change_checkbox").each(function () {
	               							if (this.checked) {
	               								checked_db_chages.push($(this).val());
	               								console.log("Checked");
	               							}
           								});
		
		        $.ajax({
			        type: "POST",
			        url: "rest/deploy/"+$('#lob_select :selected').val(),
			        data: "db_changes=["+checked_db_chages+ "]&_method=PUT",
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
	        
        </script>
</head>
<body>
 
<h2>Deploy Database Changes</h2>
		<form:select path="options" id="lob_select">
		    <form:options items="${options.lobs}" />
		</form:select>
		<br/>
 
<input type="button" value="Deploy" onclick="doAjaxPost()">
     
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
                        	<td><input type="checkbox" class="db_change_checkbox" value="${dbChange}" id="${dbChange}"></td>
                            <td>${dbChange}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
</body>
</html>