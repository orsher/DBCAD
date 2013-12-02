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
        	var currentPage=1;
        	var searchFilter={};
        	function openDeployWindow(){
        		 $('#deploy_db_changes_div').addClass("Display");
        	}
        	function openDeleteWindow(){
       		 	$('#delete_db_changes_div').addClass("Display");
       		}
        	function openCreateWindow(){
       		 	$('#create_db_change_div').addClass("Display");
       		}
	        function deploy() {
		        $('#lob_select :selected').each(function(){
		        	$('#wait_text').html("Deploying database changes...");
			        $.ajax({
				        type: "POST",
				        url: "rest/deploy/"+$(this).val(),
				        data: "db_changes=["+checkedChangeIds+ "]&_method=PUT",
				        success: function(response){
					        closeDeployWindow();
					        clearCheckedChangeIds();
				        },				       
				        error: function(e){
				        }
			        });
		        });
		        getPageNumber(currentPage);
	        }
	        
	        function createDBChange() {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_change/"+$('#schema_select :selected').val()+"/"+$('#db_change_id_text_field').val(),
			        data: "db_change_text="+$('#db_change_text').val()+ "&_method=PUT",
			        success: function(response){
				        closeCreateWindow();
				        getPageNumber(currentPage);
			        },
			        error: function(e){
			        }
		        });
	        }
	        
	        function getPageNumber(i) {
		        $.ajax({
			        type: "POST",
			        url: "getDbChangesTablePage",
			        data: "page="+i+"&searchFilter="+JSON.stringify(searchFilter),
			        success: function(response){
			        	$('#db-changes-table').replaceWith(response);
			        	addCheckBoxListener();
			        	setCheckBoxesStatus();
			        	currentPage=i;
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function filterChanges() {
	        	searchFilter.generalFilter = $('#generalFilterText').val();
	        	getPageNumber(1);
	        }
	        function load()
	        {
	        	$('#manage-db-changes-link').addClass("current");
	        	addCheckBoxListener();
		        $(document).on({
		            ajaxStart: function() { 
		                $('body').addClass("loading"); 
		            },
		            ajaxStop: function() { 
		                $('body').removeClass("loading"); 
		            }    
		        });
	        }
	        
	        var checkedChangeIds = [];
	        function clearCheckedChangeIds(){
	        	checkedChangeIds = [];
	        	setCheckBoxesStatus();
	        }
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
		            $('.checked-db-changes-div').each(function(){$(this).html(checkedChangeIds.toString());});
		        });
	        }
	        
	        function setCheckBoxesStatus(){
	        	$(".db_change_checkbox").each(function () {
						if (checkedChangeIds.indexOf($(this).val()) !== -1) {
							$(this).prop('checked', true);
						}
						else{
							$(this).prop('checked', false);
						}
					});
	        }
	        function closeDeployWindow(){
	        	$("#deploy_db_changes_div").removeClass("Display");
	        }
	        
	        function closeDeleteWindow(){
	        	$("#delete_db_changes_div").removeClass("Display");
	        }
	        function closeCreateWindow(){
	        	$("#create_db_change_div").removeClass("Display");
	        }
	        function deleteDBChanges() {
	        	$.each(checkedChangeIds,function(index, value){
	        	    $.ajax({
				        type: "POST",
				        url: "rest/db_change/"+value,
				        data: "_method=DELETE",
				        success: function(response){
				        	getPageNumber(currentPage);
				        	closeDeleteWindow();
				        	clearCheckedChangeIds();
				        },
				        error: function(e){
				        }
			        });
	        	});
		        
	        }
        </script>
</head>
<body onload="load()">
<%@ include file="BodyHeader.jsp" %>
 <input type="button" value="Deploy" onclick="openDeployWindow()">
 <input type="button" value="Delete" onclick="openDeleteWindow()">
 <input type="button" value="Create" onclick="openCreateWindow()">
 <br/>
 <div search_div>
	Filter Database Changes:
	<input type="text" id="generalFilterText">
	<input type="button" value="Search" onclick="filterChanges()">
 </div>
     
		<%@ include file="ManageDBChangesTable.jsp" %>
		
	<%@ include file="BodyFooter.jsp" %>
	<div id="deploy_db_changes_div" class="ontopwindows">
		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeDeployWindow()'>
		<div class="ontopwindow_heading">Deploy Database Changes</div>
		Please choose on which LOBs would you like to deploy the following DB Changes:
		<div class="checked-db-changes-div"></div>
		<form:select multiple="true" path="options" id="lob_select">
		    <form:options items="${options.lobs}" />
		</form:select>
		<input type="button" value="Finish" onclick="deploy()">
		<br/>
	</div>
	<div id="delete_db_changes_div" class="ontopwindows">
		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeDeleteWindow()'>
		<div class="ontopwindow_heading">Delete Database Changes</div>
		Are you sure you want to delete the following DB Changes:
		<div class="checked-db-changes-div"></div>
		<input type="button" value="Delete" onclick="deleteDBChanges()">
		<br/>
	</div>
	<div id="create_db_change_div" class="ontopwindows">
		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeCreateWindow()'>
		<div class="ontopwindow_heading">Create Database Change</div>
		<input type="text" placeholder="DB Change ID..." class="input_field" id="db_change_id_text_field" name="db_change_id_text_field"/>
		<form:select path="options" id="schema_select">
			<form:option value="" label="Select DB Schema" disabled="true" selected="true"/>
		    <form:options items="${options.db_schemas}" />
		</form:select>
		<br/>
		<textarea id="db_change_text" placeholder='SQL Script..' class="input_field" style="width: 500px; height: 170px;"></textarea>
		<br/>
		<input type="button" value="Create" onclick="createDBChange()">
		<br/>
	</div>
</body>
</html>