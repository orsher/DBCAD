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
		        $.ajax({
			        type: "POST",
			        url: "rest/deploy/"+$('#lob_select :selected').val(),
			        data: "db_changes=["+checkedChangeIds+ "]&_method=PUT",
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
			        	addCheckBoxListener();
			        	setCheckBoxesStatus();
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function load()
	        {
	        	$('#deploy-link').addClass("current");
	        	addCheckBoxListener();
	        }
	        
	        var checkedChangeIds = [];
	        function addCheckBoxListener(){
		        $('#db-changes-table-body').on('change','input:checkbox', function() {
		            if( $(this).is(":checked") ) {
		            	checkedChangeIds.push($(this).val());
		            }
		            else{
		            	if ((index = checkedChangeIds.indexOf($(this).val())) !== -1){
		            		checkedChangeIds.splice(index,1);	
		            	}
		            }
		            $('#checked-db-changes-div').html(checkedChangeIds.toString());
		        });
	        }
	        
	        function setCheckBoxesStatus(){
	        	$(".db_change_checkbox").each(function () {
						if (checkedChangeIds.indexOf($(this).val()) !== -1) {
							$(this).prop('checked', true);
						}
					});
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
 <div id="checked-db-changes-div"></div>
     
		<%@ include file="DeployDBChangesTable.jsp" %>
		
	<%@ include file="BodyFooter.jsp" %>
</body>
</html>