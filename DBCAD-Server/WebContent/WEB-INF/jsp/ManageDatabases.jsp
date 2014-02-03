<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Add Database Types</title>
    <script src="scripts/tabcontent.js" type="text/javascript"></script>
    <link href="css/dbcad.css" rel="stylesheet" type="text/css" />
        <script src="scripts/jquery-2.0.3.min.js"></script>
        <script type="text/javascript">
        	var instanceTableValuesJson=${instance_table_values_json};
        	var currentManageSchemaPage=1;
        	var currentManageInstancePage=1;
        	var instancesNumOfPages=${instancesNumOfPages};
        	function fillInstancesTable(){
        		$('#instances-table-body').empty();
        		var retValue="";
        		$.each(instanceTableValuesJson, function(){
        			retValue += '<tr class="table_row">'+    
                    '<td>'+this['dbGroupId']+'</td>'+
                    '<td>'+this['dbHost']+'</td>'+
                    '<td>'+this['dbPort']+'</td>'+
                    '<td>'+this['dbSid']+'</td>'+
                    '<td>'+
                    '    <ul>';
                    
                    $.each(this['pluginInstanceParameters'],function(key,value){
                    		retValue += '<li>'+key+' = '+value+'</li>'
                    });
                    
                    retValue +='    </ul>'+
                    '</td>'+
                    '<td>'+
                   	'	<input type="button" value="Edit" onclick="openEditInstanceWindow(\''+this['dbId']+'\')">'+
                    '	<input type="button" value="Delete" onclick="doDeleteDBInstance(\''+this['dbId']+'\',this)">'+
                    '</td>'+
                	'</tr>';
                	
        			console.log(this);
        		});
            	retValue += '<td colspan="5" style="text-align: center;">'+
                '<ul id="pagination-flickr">';
                
                if (currentManageInstancePage == 1){
                	retValue += '<li class="previous-off">« Previous</li>';
                }
                else{
                	retValue +='<li class="previous" onclick="getInstancesPageNumber(';
                	retValue += currentManageInstancePage-1;
                	retValue +=')">« Previous</li>';
                }
                
                for (var i=1;i<=instancesNumOfPages;i++){
                	if (i == currentManageInstancePage){
                		retValue += i;
                	}
                	else{
                		retValue += '<li onclick="getInstancesPageNumber('+ i +')">'+ i +'</li>';
                	}
                	
                }
                
                if (currentManageInstancePage == instancesNumOfPages){
                	retValue += '<li class="next-off" >Next »</li>';	
                }
                else{
                	var nextManageInstancePage = currentManageInstancePage+1;
                	retValue +='<li class="next" onclick="getInstancesPageNumber('+ nextManageInstancePage +')">Next »</li>';
                }
                
                retValue +='	</ul>'+
        				   '</td>';
        				   
        		$('#instances-table-body').append(retValue);
        	}
        	
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
		        var dbGroupId = $('#create_db_instance_div  #group_select').val();
		        var dbHost = $('#create_db_instance_div  #host').val();
		        var dbPort = $('#create_db_instance_div  #port').val();
		        var dbSid = $('#create_db_instance_div  #sid').val();
		        var pluginInstanceParameters = {};
		        var dbPluginType = $('#create_db_instance_div #group_select :selected').attr("dbplugintype");
		        $('#create_db_instance_div '+'.'+dbPluginType+'_InstanceParameter').each(function(){
		        	pluginInstanceParameters[$(this).attr('id')] = $(this).val();
		        });
		        $.ajax({
			        type: "POST",
			        url: "rest/db_instance",
			        data: "dbGroupId=" + dbGroupId + "&dbHost=" + dbHost + "&dbPort=" + dbPort + "&dbSid=" + dbSid + "&pluginInstanceParameters="+JSON.stringify(pluginInstanceParameters)+"&_method=PUT",
			        success: function(response){
			        	closeCreateInstanceWindow()
			        	getInstancesPageNumber(currentManageInstancePage);
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function saveDbInstance() {
	        	var dbGroupId = $('#edit_db_instance_div  #group_select').val();
		        var dbHost = $('#edit_db_instance_div  #host').val();
		        var dbPort = $('#edit_db_instance_div  #port').val();
		        var dbSid = $('#edit_db_instance_div  #sid').val();
		        var dbId = $('#edit_db_instance_div  #dbid').val();
		        var pluginInstanceParameters = {};
		        var dbPluginType = $('#edit_db_instance_div #group_select :selected').attr("dbplugintype");
		        $('#edit_db_instance_div '+'.'+dbPluginType+'_InstanceParameter').each(function(){
		        	pluginInstanceParameters[$(this).attr('id')] = $(this).val();
		        });
		        $.ajax({
			        type: "POST",
			        url: "rest/db_instance/"+dbId,
			        data: "dbGroupId=" + dbGroupId + "&dbHost=" + dbHost + "&dbPort=" + dbPort + "&dbSid=" + dbSid + "&pluginInstanceParameters="+JSON.stringify(pluginInstanceParameters)+"&_method=PUT",
			        success: function(response){
			        	closeEditInstanceWindow();
			        	getInstancesPageNumber(currentManageInstancePage);
			        },
			        error: function(e){
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
	        function addDbGroup() {
		        // get the form values
		        var dbGroupId = $('#new_group_id_input').val();
		        var dbTypeId = $('#new_group_type_select').val();
		        var dbLobList = [];
		        $("#new_group_lob_select :selected").each(function(){
		        	dbLobList.push($(this).val()); 
		        });
		        
		        $.ajax({
			        type: "POST",
			        url: "rest/db_group/"+dbGroupId,
			        data: "dbTypeId=" + dbTypeId + "&dbLobList=["+ dbLobList +"]&_method=PUT",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        $('#groups-table-body').append('<tr class="table_row"><td>'
								+dbGroupId+'</td><td>'+dbTypeId+'</td><td>'+dbLobList+'</td>'+
								'<td><input type="button" value="Delete" onclick="deleteDBGroup(\''+dbGroupId+'\',this)"></td></tr>');
				        
			        },
			        error: function(e){
			        	$('#info').html("error");
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function deleteDBGroup(dbGroupId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_group/"+dbGroupId,
			        data: "_method=DELETE",
			        success: function(response){
				        // we have the response
				        $('#info').html(response);
				        var p=object.parentNode.parentNode;
				        p.parentNode.removeChild(p);
			        },
			        error: function(e){
			        	$('#info').html("error");
			        }
		        });
	        }
	        function refreshAvailableDBIds() {
	        	
		        $.ajax({
			        type: "POST",
			        url: "rest/db_instance/",
			        data: "_method=GET"+"&dbTypeId="+$("#new_schmea_type_select option:selected").text(),
			        success: function(response){
			        	var dbIds = JSON.parse(response);
			        	console.log(dbIds);
			        	$("#new_schema_deployable_select").empty();
			        	$.each(dbIds,function(index,value){$("#new_schema_deployable_select").append('<option val="'+value+'">'+value+'</option>');});
			        },
			        error: function(e){
			        	$('#info').html("error");
			        }
		        });
	        }
	        function addDbSchema() {
		        // get the form values
		        var dbSchemaId = $('#new_schema_id_input').val();
		        var dbSchemaName = $('#new_schema_name_input').val();
		        var dbTypeId = $('#new_schmea_type_select').val();
		        var dbDeployableList = [];
		        $("#new_schema_deployable_select :selected").each(function(){
		        	dbDeployableList.push('"'+$(this).val()+'"'); 
		        });
		        $.ajax({
			        type: "POST",
			        url: "rest/db_schema/"+dbSchemaId,
			        data: "dbTypeId=" + dbTypeId + "&dbDeployableList=["+ dbDeployableList +"]&schemaName="+dbSchemaName+"&_method=PUT",
			        success: function(response){
			        	getSchemasPageNumber(currentManageSchemaPage);
			        },
			        error: function(e){
			        	$('#info').html("error");
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function deleteDBSchema(dbSchemaId,object) {
		        $.ajax({
			        type: "POST",
			        url: "rest/db_schema/"+dbSchemaId,
			        data: "_method=DELETE",
			        success: function(response){
			        	 getSchemasPageNumber(currentManageSchemaPage);
			        },
			        error: function(e){
			        	$('#info').html("error");
			        }
		        });
	        }
	        function getSchemasPageNumber(i) {
		        $.ajax({
			        type: "POST",
			        url: "getDbSchemasTablePage",
			        data: "page="+i,
			        success: function(response){
			        	$('#schemas-table').replaceWith(response);
			        	currentManageSchemaPage=i;
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function getInstancesPageNumber(i) {
		        $.ajax({
			        type: "POST",
			        url: "getDbInstancesTablePage",
			        data: "page="+i,
			        success: function(response){
			        	var jsonResponse = JSON.parse(response);
			        	instancesNumOfPages = jsonResponse["instancesNumOfPages"];
			        	currentManageInstancePage= jsonResponse["instancesCurrentPage"];
			        	instanceTableValuesJson = JSON.parse(jsonResponse["instanceTableValues"]);
			        	fillInstancesTable();
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        function addLobToList(){
	        	$('#new_group_lob_select').append('<option value="'+$('#new_lob_id_input').val()+'">'+$('#new_lob_id_input').val()+'</option>');
	        }
	        function SaveDBPluginConfig(dbPluginType){
	        	var params = {};
		        $("."+dbPluginType+"_Parameter").each(function(){
		        	params[this.id] = this.value;
		        });
		        $.ajax({
			        type: "POST",
			        url: "saveDbPluginConfig",
			        data: "dbPluginType="+dbPluginType+"&params="+JSON.stringify(params),
			        success: function(response){
			        },
			        error: function(e){
			        }
		        });
	        }
	        
	        function load()
	        {
	        	$('#manage-databases-link').addClass("current");
	        	fillInstancesTable();
	        }
        	function openCreateInstanceWindow(){
        		setNewInstancePluginTypeParametersInput();
       		 	$('#create_db_instance_div').addClass("Display");
       		}
        	function closeCreateInstanceWindow(){
       		 	$('#create_db_instance_div').removeClass("Display");
       		}
        	function openEditInstanceWindow(dbId){
        		setEditInstanceValues(dbId);
        		setEditInstancePluginTypeParametersInput();
       		 	$('#edit_db_instance_div').addClass("Display");
       		}
        	function closeEditInstanceWindow(){
       		 	$('#edit_db_instance_div').removeClass("Display");
       		}
        	
        	function setNewInstancePluginTypeParametersInput(){
        		$('#create_db_instance_div div').each(function(){
        			if (this.id == $('#create_db_instance_div #group_select :selected').attr('dbPluginType')+"_InstanceParameters"){
        				this.style.display = "block";
        			} 
        			else{
        				this.style.display = "none";
        			}
        		});
        	}
        	function setEditInstancePluginTypeParametersInput(){
        		$('#edit_db_instance_div div').each(function(){
        			if (this.id == $('#edit_db_instance_div #group_select :selected').attr('dbPluginType')+"_InstanceParameters"){
        				this.style.display = "block";
        			} 
        			else{
        				this.style.display = "none";
        			}
        		});
        	}
        	function setEditInstanceValues(dbId){
        		for (var i in instanceTableValuesJson){
        			if (dbId == instanceTableValuesJson[i].dbId){
        				$('#edit_db_instance_div #group_select').val(instanceTableValuesJson[i].dbGroupId);
        				$('#edit_db_instance_div #host').val(instanceTableValuesJson[i].dbHost);
        				$('#edit_db_instance_div #port').val(instanceTableValuesJson[i].dbPort);
        				$('#edit_db_instance_div #sid').val(instanceTableValuesJson[i].dbSid);
        				$('#edit_db_instance_div #dbid').val(dbId);
        				$.each((instanceTableValuesJson[i].pluginInstanceParameters),function(key,value){
        					$('#edit_db_instance_div #'+$('#edit_db_instance_div #group_select :selected').attr('dbPluginType')+"_InstanceParameters"+' #'+key).val(value);
        				});
        				console.log(instanceTableValuesJson[i].dbId);
        				console.log(instanceTableValuesJson[i].dbGroupId);
        				console.log(instanceTableValuesJson[i].dbHost);
        				console.log(instanceTableValuesJson[i].dbPort);
        				console.log(instanceTableValuesJson[i].pluginInstanceParameters);
        			}
        		}
        		
        		//$('#edit_db_instance_div #group_select') =
        	}
        	
        </script>
</head>
<body onload="load()">
 <%@ include file="BodyHeader.jsp" %>
		

            
		<ul class="tabs" data-persist="true">
			<li class=""><a href="#database-plugins-div"><span>Database Plugins</span></a></li>
        	<li class="selected"><a href="#database-types-div"><span>Database Types</span></a></li>
        	<li class=""><a href="#database-groups-div"><span>Database Groups</span></a></li>
        	<li class=""><a href="#database-instances-div"><span>Database Instances</span></a></li>
        	<li class=""><a href="#database-schemas-div"><span>Database Schemas</span></a></li>
    	</ul>
    	<div class="tabcontents">
    	     <div id="database-plugins-div" style="display: none;">
    	     	<c:forEach items="${dbPluginsConfig}" var="pluginConfig">
    	     		<h1>${pluginConfig.dbPluginType}</h1>
    	     		<c:forEach items="${pluginConfig.globalParameterValues}" var="parameterEntry">
    	     			${parameterEntry.key}
    	     			<input type="text" class="${pluginConfig.dbPluginType}_Parameter" id="${parameterEntry.key}" value="${parameterEntry.value}"/>
    	     		</c:forEach>
    	     		<br/>
					<input type="button" value="Save" onclick="SaveDBPluginConfig('${pluginConfig.dbPluginType}')">
    	     	</c:forEach>
            </div>
            <div id="database-types-div" style="display: block;">
                
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
			                        <th></th>
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
                
            </div>
            <div id="database-instances-div" style="display: none;">
	           <div id="create_db_instance_div" class="ontopwindows">
	           		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeCreateInstanceWindow()'>
	           		<div class="ontopwindow_heading">Create Database Instance</div>
		           <select path="options" id="group_select" onchange="setNewInstancePluginTypeParametersInput()">
		           		<c:forEach items="${group_table_values}" var="db_group">
				    		<option id="${db_group.db_group_id}" value="${db_group.db_group_id}" dbPluginType="${db_group.db_plugin_type}">${db_group.db_group_id}</option>
				    	</c:forEach>
					</select>
					<input type="text" id="host" name="host" placeholder="host..." class="input_field"/>
					<input type="text" id="port" name="port" placeholder="port..."class="input_field"/>
					<input type="text" id="sid" name="sid" placeholder="sid..." class="input_field"/>
					<br/>
					
	    	     	<c:forEach items="${dbPluginsConfig}" var="pluginConfig">
	    	     		<div id="${pluginConfig.dbPluginType}_InstanceParameters">
		    	     		<c:forEach items="${pluginConfig.instanceParameterNames}" var="instanceParameterName">
		    	     			${instanceParameterName}
		    	     			<input type="text" class="${pluginConfig.dbPluginType}_InstanceParameter" id="${instanceParameterName}"/>
		    	     		</c:forEach>
						</div>
    	     		</c:forEach>
    	     	
					<input type="button" value="Add" onclick="addDbInstance()">
	           </div>
	           <div id="edit_db_instance_div" class="ontopwindows">
	           		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeEditInstanceWindow()'>
	           		<div class="ontopwindow_heading">Edit Database Instance</div>
		           <select path="options" id="group_select" onchange="setEditInstancePluginTypeParametersInput()">
		           		<c:forEach items="${group_table_values}" var="db_group">
				    		<option id="${db_group.db_group_id}" value="${db_group.db_group_id}" dbPluginType="${db_group.db_plugin_type}">${db_group.db_group_id}</option>
				    	</c:forEach>
					</select>
					<input type="text" id="host" name="host" placeholder="host..." class="input_field"/>
					<input type="text" id="port" name="port" placeholder="port..."class="input_field"/>
					<input type="text" id="sid" name="sid" placeholder="sid..." class="input_field"/>
					<input type="text" id="dbid" name="dbid" class="input_field" style="display: none;"/>
					<br/>
					
	    	     	<c:forEach items="${dbPluginsConfig}" var="pluginConfig">
	    	     		<div id="${pluginConfig.dbPluginType}_InstanceParameters">
		    	     		<c:forEach items="${pluginConfig.instanceParameterNames}" var="instanceParameterName">
		    	     			${instanceParameterName}
		    	     			<input type="text" class="${pluginConfig.dbPluginType}_InstanceParameter" id="${instanceParameterName}"/>
		    	     		</c:forEach>
						</div>
    	     		</c:forEach>
    	     	
					<input type="button" value="Save" onclick="saveDbInstance()">
	           </div>
	           <input type="button" value="Add" onclick="openCreateInstanceWindow()">
				<%@ include file="ManageDatabaseInstanceTable.jsp" %>
            </div>
            <div id="database-groups-div" style="display: none;">
           		<input type="text" id="new_group_id_input" name="new_group_id_input" class="input_field"/>
				<form:select path="options" id="new_group_type_select">
			    	<form:options items="${options.db_types}" />
				</form:select>
				<form:select multiple="true" path="options" id="new_group_lob_select">
		    		<form:options items="${options.lobs}" />
				</form:select>
				<input type="text" id="new_lob_id_input" name="new_lob_id_input" class="input_field"/>
				<input type="button" value="Add lob to list" onclick="addLobToList()">
				<br/>
				<input type="button" value="Add" onclick="addDbGroup()">
				<table id="groups-table" class="table">
                <thead>
                    <tr>
                        <th>Database Group Id</th>
                        <th>Database Type</th>
                        <th>Lobs</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody id="groups-table-body">
                    <c:forEach items="${group_table_values}" var="tableRow">
                        <tr class="table_row">    
                            <td>${tableRow.db_group_id}</td>
                            <td>${tableRow.db_type_id}</td>
                            <td>${tableRow.db_group_lob_mapping}</td>
                            <td><input type="button" value="Delete" onclick="deleteDBGroup('${tableRow.db_group_id}',this)"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            </div>
			<div id="database-schemas-div" style="display: none;">
				<input type="text" id="new_schema_id_input" name="new_schema_id_input" class="input_field"/>
				<input type="text" id="new_schema_name_input" name="new_schema_name_input" class="input_field"/>
				<form:select path="options" id="new_schmea_type_select" onchange="refreshAvailableDBIds()">
			    	<form:options items="${options.db_types}" />
				</form:select>
				<form:select multiple="true" path="options" id="new_schema_deployable_select">
		    		<form:options />
				</form:select>
				<br/>
				<input type="button" value="Add" onclick="addDbSchema()">


				<%@ include file="ManageDatabaseSchemaTable.jsp" %>
            </div>
    		    		
    </div>

		
	<%@ include file="BodyFooter.jsp" %>        
</body>
</html>

