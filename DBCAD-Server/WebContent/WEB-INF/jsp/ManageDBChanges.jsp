<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title>Manage Database Changes</title>
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
        		$.each(dbChangesTableValues, function(key,dbChange){
        			if (dbChange.db_change_id == db_change_id){
        				$('#edit_view_db_change_div #db_change_id_text_field').val(dbChange.db_change_id);
                		$('#edit_view_db_change_div #schema_select').val(dbChange.schema_id);
                		$('#edit_view_db_change_div #db_change_text').val(dbChange.db_request_code);	
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
				        	var cllbck = 'deploy()';
				        	if (verifyAuthentication(response,cllbck)){
						        closeDeployWindow();
						        clearCheckedChangeIds();
				        	}
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
			        	var cllbck = 'createDBChange()';
			        	if (verifyAuthentication(response,cllbck)){
					        closeCreateWindow();
					        getPageNumber(dbChangesCurrentPage);
			        	}
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
			        	var cllbck = 'saveDBChange()';
			        	if (verifyAuthentication(response,cllbck)){
				        	closeEditViewWindow();
					        getPageNumber(dbChangesCurrentPage);
			        	}
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
			        	var cllbck = 'getPageNumber('+i+')';
			        	if (verifyAuthentication(response,cllbck)){
				        	var jsonResponse = JSON.parse(response);
				        	console.log(jsonResponse);
				        	dbChangesNoOfPages = jsonResponse["dbChangesNoOfPages"];
				        	dbChangesCurrentPage = jsonResponse["dbChangesCurrentPage"];
				        	dbChangesLobList = jsonResponse["dbChangesLobList"];
				        	dbChangesTableValues = jsonResponse["dbChangesTableValues"];
				        	fillDBChangesTable();
			        	}
			        },
			        error: function(e){
			        	alert('Error: ' + e.responseText);
			        }
		        });
	        }
	        
	        function fillDBChangesTable(){
	        	$('#db-changes-table-body').empty();
	        	var retValue="";
        		$.each(dbChangesTableValues, function(key, value){
        			var deploymentStatus;
        			var escapedDbChangeCode = value.db_request_code.replace(/'/g, "&#39;");
        			retValue += '<tr class="table_row" title=\''+escapedDbChangeCode+'\'>' +
        			<sec:authorize access="hasRole(T(dbcad.DBCADController).ADMIN_GROUP_NAME)">
        			'<td><input type="checkbox"' + 
        			' class="db_change_checkbox" value='+value.db_change_id+' id='+value.db_change_id+'></td>'+
        			</sec:authorize>
        			'<td '+
        			<sec:authorize access="hasRole(T(dbcad.DBCADController).ADMIN_GROUP_NAME)">
        			'class="change_id_adm" onclick=openEditViewWindow(\''+value.db_change_id+'\')' + 
        			</sec:authorize>
        			'>'+value.db_change_id+'</td>'+
                    '<td class="dividing-column">'+value.schema_id+'</td>';
                    deploymentStatus=value.deployment_status;
                    $.each(dbChangesLobList,function(lob_key,lob_value){
                    	if (deploymentStatus[lob_value] == null){
                    		retValue += '<td>---</td>';
                    	}
                    	else{
                    		var tooltip="";
                    		var failed=0;
                    		var succeeded=0;
                    		var running=0;
                    		var na=0;
                    		$.each(deploymentStatus[lob_value], function(dbGroupId,dbGroupData){
                    			$.each(dbGroupData,function(dbId,dbDataValue){
                    				dbData = JSON.stringify(dbDataValue);
                    				if (dbData == -2){
                    					tooltip+=dbGroupId+" - "+dbId+" : N/A\n";
                    					na++;
                    				}
                    				else if (dbData == 0){
                    					tooltip+=dbGroupId+" - "+dbId+" : Succeeded\n";
                    					succeeded++;
                    				}
                    				else if (dbData == -1){
                    					tooltip+=dbGroupId+" - "+dbId+" : Running\n";
                    					running++;
                    				}
                    				else{
                    					tooltip+=dbGroupId+" - "+dbId+" : Failed\n";
                    					failed++;
                    				}
                    			});
                    		});
                    		if (running > 0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-executing.gif" style="width: 20px; height: 20px;"></div></td>';
                    		}
                    		else if (na > 0 && succeeded == 0 && failed == 0){
                    			retValue += '<td></td>';
                    		}
                    		else if (na > 0 && succeeded > 0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-Warning.png" style="width: 20px; height: 20px;"></div></td>';
                    		} 
                    		else if (na > 0 && failed > 0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-Fail.png" style="width: 20px; height: 20px;"></div></td>';
                    		}
                    		else if (failed > 0 && succeeded >0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-Warning.png" style="width: 20px; height: 20px;"></div></td>';
                    		}
                    		else if (failed > 0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-Fail.png" style="width: 20px; height: 20px;"></div></td>';
                    		}
                    		else if (succeeded > 0){
                    			retValue += '<td><div title="'+tooltip+'" onclick="getLog(\''+value.db_change_id+'\',\''+lob_value+'\')"><img src="css/images/Status-OK.png" style="width: 20px; height: 20px;"></div></td>';
                    		}
                    		else{
                    			retValue += '<td>---</td>';	
                    		}
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
		        
		        $('#toggle-login').click(function(){
			      	  $('#login').toggle();
			      	  $('#j_ajax_username').focus();
			      	  setCallbackRefresh();
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
	        function closeEditViewWindow(){
	        	$("#edit_view_db_change_div").removeClass("Display");
	        }
        	function openLoginWindow(){
        		$('#login_div').show();
        		$('#logout_div').hide();
       		 	$('#login').show();
       		 	$('#j_ajax_username').focus();
       		}
        	function closeLoginWindow(){
       		 	$('#login').hide();
       		}
	        function deleteDBChanges() {
	        	$.each(checkedChangeIds,function(index, value){
	        	    $.ajax({
				        type: "POST",
				        url: "rest/db_change/"+value,
				        data: "_method=DELETE",
				        success: function(response){
				        	var cllbck = 'deleteDBChanges()';
				        	if (verifyAuthentication(response,cllbck)){
					        	getPageNumber(dbChangesCurrentPage);
					        	closeDeleteWindow();
					        	clearCheckedChangeIds();
				        	}
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
			        	var cllbck = 'getLog('+db_change_id+','+lob_id+')';
			        	if (verifyAuthentication(response,cllbck)){
			        		openViewLogWindow(response);
			        	}
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
	        
	        function verifyAuthentication(data, cllBackString){
				   //naive check - I put a string in the login form, so I check for existance
				   if (isNaN(data) && (data.indexOf("j_spring_security_check")!= -1)){
				      //if got here then data is a loginform => login required  
				      $("#my_callback").val(cllBackString); 
				  
				      //show ajax login
				      //Get the window height and width
//				      var winH = $(window).height();
//				      var winW = $(window).width();
				               
				      //Set the popup window to center
//				      $("#ajaxLogin").css('top',  winH/2-$("#ajaxLogin").height()/2);
//				      $("#ajaxLogin").css('left', winW/2-$("#ajaxLogin").width()/2);
					  openLoginWindow();
//				      $("#ajaxLogin").fadeIn(2000); 
				      return false;
				      } 
				    // data is not a login form => return true to continue with function processing
				    return true;    
				}
	        
	        function setCallbackRefresh(){
				 $("#my_callback").val("location.reload(true);");
			}
	        function ajaxLogin(){
			     
			    var my_callback = $("#my_callback").val(); // The original function which accessed the protected resource
			    var user_pass = $("#j_ajax_password").val();
			    var user_name = $("#j_ajax_username").val(); 
			//Ajax login - we send credentials to j_spring_security_check (as in form based login
			    $.ajax({
			          url: "j_spring_security_check",    
			          data: { j_username: user_name , j_password: user_pass }, 
			          type: "POST",
			          beforeSend: function (xhr) {
			             xhr.setRequestHeader("X-Ajax-call", "true");
			          },
			          success: function(result) {       
			          //if login is success, hide the login modal and
			          //re-execute the function which called the protected resource
			          //(#7 in the diagram flow)
			          if (result == "ok") {
			        	closeLoginWindow();
			            $("#ajax_login_error").html("");
			            if (my_callback!=null && my_callback!='undefined' && my_callback!=''){
			            		eval(my_callback.replace(/_/g,'"'));
			            }
			             
			            return true;
			          }else {           
			             
			            $("#ajax_login_error").html('<span  class="alert display_b clear_b centeralign">Bad user/password</span>') ;
			            return false;           
			        }
			    },
			    error: function(XMLHttpRequest, textStatus, errorThrown){
			        $("#ajax_login_error").html("Bad user/password") ;
			        return false; 
			    }
			});
			}
        </script>
</head>
<body onload="load()">
<%@ include file="BodyHeader.jsp" %>
<sec:authorize access="hasRole(T(dbcad.DBCADController).ADMIN_GROUP_NAME)">
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
		
		<div id="popups_wrapper">	
		<div id="deploy_db_changes_div" class="ontopwindows">
			<img src="css/images/closex.png" class="closewindowbutton" onclick='closeDeployWindow()'>
			<div class="ontopwindow_heading">Deploy Database Changes</div>
			Please choose on which LOBs would you like to deploy the following DB Changes:
			<div class="checked-db-changes-div"></div>
			<form:select multiple="true" path="options" id="lob_select">
			    <form:options items="${options.lobs}" />
			</form:select>
			<input type="checkbox" id="markOnlyCheckbox"> Mark only <br>
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
	 	</div>
	
	<%@ include file="BodyFooter.jsp" %>
</body>
</html>