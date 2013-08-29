<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<html>
<head>
    <title>Add Database Types</title>
    <link href="css/dbcad.css" rel="stylesheet" type="text/css" />
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
	        function getPageNumber(i) {
		        $.ajax({
			        type: "POST",
			        url: "getDbChangesTablePage",
			        data: "page="+i,
			        success: function(response){
			        	$('#db-changes-table').replaceWith(response);
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function load()
	        {
	        	$('#deploy-link').addClass("current");
	        }
        </script>
</head>
<body onload="load()">
<%@ include file="BodyHeader.jsp" %>
		<form:select path="options" id="lob_select">
		    <form:options items="${options.lobs}" />
		</form:select>
		<br/>
 
<input type="button" value="Deploy" onclick="doAjaxPost()">
     
		<%@ include file="DeployDBChangesTable.jsp" %>
		
	<%@ include file="BodyFooter.jsp" %>
</body>
</html>