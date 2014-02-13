<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<table id="instances-table" class="table">
	                <thead>
	                    <tr>
	                    	<th>Database Id</th>
	                    	<th>Database Plugin Type</th>
	                        <th>Database Host</th>
	                        <th>Database Port</th>
	                        <th>Plugin Instance Parameters</th>
	                        <th></th>
	                    </tr>
	                </thead>
	                <tbody id="instances-table-body">
<%-- 	                    <c:forEach items="${instance_table_values}" var="tableRow"> --%>
<!-- 	                        <tr class="table_row">     -->
<%-- 	                            <td>${tableRow.dbGroupId}</td> --%>
<%-- 	                            <td>${tableRow.dbHost}</td> --%>
<%-- 	                            <td>${tableRow.dbPort}</td> --%>
<%-- 	                            <td>${tableRow.dbSid}</td> --%>
<!-- 	                            <td> -->
<!-- 		                            <ul> -->
<%-- 			                            <c:forEach items="${tableRow.pluginInstanceParameters}" var="parameterEntry"> --%>
<%-- 					    	     			<li>${parameterEntry.key} = ${parameterEntry.value}</li> --%>
<%-- 					    	     		</c:forEach> --%>
<!-- 		                            </ul> -->
<!-- 	                            </td> -->
<!-- 	                            <td> -->
<!-- 	                           		<input type="button" value="Edit" onclick="openEditInstanceWindow()"> -->
<%-- 	                            	<input type="button" value="Delete" onclick="doDeleteDBInstance('${tableRow.dbId}',this)"> --%>
<!-- 	                            </td> -->
<!-- 	                        </tr> -->
<%-- 	                    </c:forEach> --%>
<!-- 	                    <td colspan="5" style="text-align: center;"> -->
<!-- 		                    <ul id="pagination-flickr"> -->
<%-- 		                    	<c:choose> --%>
<%-- 		                    		<c:when test="${instancesCurrentPage == 1}"> --%>
<!-- 		                    			<li class="previous-off">« Previous</li> -->
<%-- 		                    		</c:when> --%>
<%-- 		                    		<c:otherwise> --%>
<%-- 		                    			<li class="previous" onclick="getInstancesPageNumber('${instancesCurrentPage -1}')">« Previous</li> --%>
<%-- 		                    		</c:otherwise>   --%>
<%-- 		                    	</c:choose> --%>
<%-- 			                    <c:forEach begin="1" end="${instancesNumOfPages}" var="i"> --%>
<%-- 					                <c:choose> --%>
<%-- 					                    <c:when test="${instancesCurrentPage eq i}"> --%>
<%-- 					                      ${i}   --%>
<%-- 					                    </c:when> --%>
<%-- 					                    <c:otherwise> --%>
<%-- 					                        <li onclick="getInstancesPageNumber('${i}')">${i}</li> --%>
<%-- 					                    </c:otherwise> --%>
<%-- 					                </c:choose> --%>
<%-- 				            	</c:forEach> --%>
<%-- 				            	<c:choose> --%>
<%-- 		                    		<c:when test="${instancesCurrentPage == instancesNumOfPages}"> --%>
<!-- 		                    			<li class="next-off" >Next »</li> -->
<%-- 		                    		</c:when> --%>
<%-- 		                    		<c:otherwise> --%>
<%-- 		                    			<li class="next" onclick="getInstancesPageNumber('${instancesCurrentPage +1}')">Next »</li> --%>
<%-- 		                    		</c:otherwise>   --%>
<%-- 		                    	</c:choose> --%>
				            	
<!-- 				            </ul> -->
<!-- 		            	</td> -->
	                </tbody>
	            </table>