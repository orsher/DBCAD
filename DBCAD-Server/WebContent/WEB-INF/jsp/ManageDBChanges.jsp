<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title>Add Database Types</title>
    <link href="css/dbcad.css" rel="stylesheet" type="text/css" />
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
        	var dbChangesCurrentPage= ${dbChangesCurrentPage};
        	var dbChangesNoOfPages = ${dbChangesNoOfPages};
        	var dbChangesLobList = ${dbChangesLobList};
        	var dbChangesTableValues = ${dbChangesTableValues};
        	var lastLogJson = {};
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
        	function openEditViewWindow(db_change_id){
        		$.each(dbChangesTableValues, function(){
        			if (this['db_request_id'] == db_change_id){
        				$('#edit_view_db_change_div #db_change_id_text_field').val(this['db_request_id']);
                		$('#edit_view_db_change_div #schema_select').val(this.schema_id);
                		$('#edit_view_db_change_div #db_change_text').val(this.db_request_code);	
        			}
        		});
       		 	$('#edit_view_db_change_div').addClass("Display");
       		}
	        function deploy() {
		        $('#lob_select :selected').each(function(){
		        	$('#wait_text').html("Deploying database changes...");
			        $.ajax({
				        type: "POST",
				        url: "rest/deploy/"+$(this).val(),
				        data: "db_changes=["+checkedChangeIds+ "]&mark_only="+ $('#markOnlyCheckbox').is(":checked") +"&_method=PUT",
				        success: function(response){
					        closeDeployWindow();
					        clearCheckedChangeIds();
				        },				       
				        error: function(e){
				        }
			        });
		        });
		        getPageNumber(dbChangesCurrentPage);
	        }
	        
	        function createDBChange() {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_change/"+$('#create_db_change_div #schema_select :selected').val()+"/"+$('#create_db_change_div #db_change_id_text_field').val(),
			        data: "db_change_text="+encodeURIComponent($('#create_db_change_div #db_change_text').val())+ "&_method=PUT",
			        success: function(response){
				        closeCreateWindow();
				        getPageNumber(dbChangesCurrentPage);
			        },
			        error: function(e){
			        }
		        });
	        }
	        
	        function saveDBChange() {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_change/"+$('#edit_view_db_change_div #schema_select :selected').val()+"/"+$('#edit_view_db_change_div #db_change_id_text_field').val(),
			        data: "db_change_text="+encodeURIComponent($('#edit_view_db_change_div #db_change_text').val())+ "&_method=POST",
			        success: function(response){
			        	closeEditViewWindow();
				        getPageNumber(dbChangesCurrentPage);
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
			        	var jsonResponse = JSON.parse(response);
			        	console.log(jsonResponse);
			        	dbChangesNoOfPages = jsonResponse["dbChangesNoOfPages"];
			        	dbChangesCurrentPage = jsonResponse["dbChangesCurrentPage"];
			        	dbChangesLobList = jsonResponse["dbChangesLobList"];
			        	dbChangesTableValues = jsonResponse["dbChangesTableValues"];
			        	fillDBChangesTable();
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function fillDBChangesTable(){
	        	$('#db-changes-table-body').empty();
	        	var retValue="";
        		$.each(dbChangesTableValues, function(){
        			var dbChangeRow = this;
        			retValue += '<tr class="table_row" title=\''+this.db_request_code+'\'>'+
        			'<td><input type="checkbox" class="db_change_checkbox" value='+this['db_request_id']+' id='+this['db_request_id']+'></td>'+
        			'<td onclick=openEditViewWindow(\''+this['db_request_id']+'\')>'+this['db_request_id']+'</td>'+
                    '<td class="dividing-column">'+this['schema_id']+'</td>';
                    $.each(dbChangesLobList,function(key,value){
                    	if (dbChangeRow[value] == null){
                    		retValue += '<td>---</td>';
                    	}
                    	else{
                    		retValue += '<td><div onclick="getLog(\''+dbChangeRow['db_request_id']+'\',\''+value+'\')">'+dbChangeRow[value]+'</div></td>';
                    	}
                    });
        		});
        		
        		retValue +='</tr><tr><td colspan="'+ dbChangesLobList.length+3 +'" style="text-align: center;">'+
                '<ul id="pagination-flickr">';
                
                if (dbChangesCurrentPage == 1){
                	retValue += '<li class="previous-off">« Previous</li>';
                }
                else{
                	retValue +='<li class="previous" onclick="getPageNumber(';
                	retValue += dbChangesCurrentPage-1;
                	retValue +=')">« Previous</li>';
                }
                
                for (var i=1;i<=dbChangesNoOfPages;i++){
                	if (i == dbChangesCurrentPage){
                		retValue += i;
                	}
                	else{
                		retValue += '<li onclick="getPageNumber('+ i +')">'+ i +'</li>';
                	}
                	
                }
                
                if (dbChangesCurrentPage == dbChangesNoOfPages){
                	retValue += '<li class="next-off" >Next »</li>';	
                }
                else{
                	var nextDbChangesPage = dbChangesCurrentPage+1;
                	retValue +='<li class="next" onclick="getPageNumber('+ nextDbChangesPage +')">Next »</li>';
                }
                
                retValue +='	</ul>'+
        		'</td>';
         		
	        	$('#db-changes-table-body').append(retValue);
	        	setCheckBoxesStatus();
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
		        
		        $('#generalFilterText').on('keyup', function(e) {
		            if (e.which == 13) {
		                filterChanges();
		            }
		        });
		        fillDBChangesTable();
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
	        function closeEditViewWindow(){
	        	$("#edit_view_db_change_div").removeClass("Display");
	        }
	        function deleteDBChanges() {
	        	$.each(checkedChangeIds,function(index, value){
	        	    $.ajax({
				        type: "POST",
				        url: "rest/db_change/"+value,
				        data: "_method=DELETE",
				        success: function(response){
				        	getPageNumber(dbChangesCurrentPage);
				        	closeDeleteWindow();
				        	clearCheckedChangeIds();
				        },
				        error: function(e){
				        }
			        });
	        	});
		        
	        }
	        function getLog(db_change_id,lob_id){
	        	$.ajax({
			        type: "POST",
			        url: "rest/getLog",
			        data: "_method=POST&db_change_id="+db_change_id+"&lob_id="+lob_id,
			        success: function(response){
			        	openViewLogWindow(response);
			        },
			        error: function(e){
			        }
		        });
	        }
	        function openViewLogWindow(log){
	        	lastLogJson = JSON.parse(log);
	        	$('#view_log_div').addClass("Display");
	        	//$('#view_log_div #log_text').val(log);
	        	$('#view_log_div #log_tree_ul').empty();
	        	$('#log_text').val("");
	        	$.each(JSON.parse(log),function(index, value){
	        		console.log(index);
	        		var instance_sub_tree = '<li class="instance_tree_entry" onclick="displayInstanceRunTree(this)">'+index+'<ul>';
	        		$.each(value,function(run_index,run_value){
	        			instance_sub_tree+='<li class="instance_run_tree_entry_nodisplay" onclick=\'displayRunLog("'+index+'",'+run_index+')\'>'+run_value['run_date']+'</li>';
	        		});
	        		instance_sub_tree+='</ul></li>';
	        		$('#view_log_div #log_tree_ul').append(instance_sub_tree);
	        	});
	        	$('.instance_tree_entry li').click(function(e){
	        		e.stopPropagation();
	        	});
	        }
	        function closeViewLogWindow(log){
	        	$('#view_log_div').removeClass("Display");
	        }
	        
	        function displayInstanceRunTree(instanceli){
	        	$(instanceli).find('li').each(function(){
	        		$(this).toggleClass("instance_run_tree_entry_nodisplay");
	        	});
	        }
	        
	        function displayRunLog(instance,runIndex){
	        	$('#log_text').val(lastLogJson[instance][runIndex]['log']);
	        }
	        
	        
        </script>
</head>
<body onload="load()">
<%@ include file="BodyHeader.jsp" %>
<sec:authorize access="hasRole('ROLE_ADMIN')">
 <input type="button" value="Deploy" onclick="openDeployWindow()">
 <input type="button" value="Delete" onclick="openDeleteWindow()">
 <input type="button" value="Create" onclick="openCreateWindow()">
 <br/>
 </sec:authorize>
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
		<input type="checkbox" id="markOnlyCheckbox" checked> Mark only <br>
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
	<div id="edit_view_db_change_div" class="ontopwindows">
		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeEditViewWindow()'>
		<div class="ontopwindow_heading">Edit Database Change</div>
		<input type="text" placeholder="DB Change ID..." class="input_field" id="db_change_id_text_field" name="db_change_id_text_field" disabled="true"/>
		<form:select path="options" id="schema_select" disabled="true">
			<form:option value="" label="Select DB Schema" disabled="true" selected="true"/>
		    <form:options items="${options.db_schemas}" />
		</form:select>
		<br/>
		<textarea id="db_change_text" placeholder='SQL Script..' class="input_field" style="width: 500px; height: 170px;"></textarea>
		<br/>
		<input type="button" value="Save" onclick="saveDBChange()">
		<br/>
	</div>
	<div id="view_log_div" class="ontopwindows">
		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeViewLogWindow()'>
		<div class="ontopwindow_heading">Log</div>
		<div id="log_tree_div"><ul id="log_tree_ul"></ul></div>
		<textarea id="log_text"></textarea>
	</div>
</body>
</html>