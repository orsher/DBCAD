<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<table id="schemas-table" class="table">
            <thead>
                <tr>
                    <th>Schema Id</th>
                    <th>Schema Name</th>
                    <th>Database Type</th>
                    <th>Deployables</th>
                    <th></th>
                </tr>
            </thead>
            <tbody id="schemas-table-body">
                <c:forEach items="${schema_table_values}" var="tableRow">
                    <tr class="table_row">    
                        <td>${tableRow.schemaId}</td>
                        <td>${tableRow.schemaName}</td>
                        <td>${tableRow.dbTypeId}</td>
                        <td>
                        	<ul>
                          <c:forEach items="${tableRow.deployableInstances}" var="deployable_instance">
                          	<li>	${deployable_instance} </li>
                          </c:forEach>
                         </ul>
                        </td>
                        <td><input type="button" value="Delete" onclick="deleteDBSchema('${tableRow.schemaId}',this)"></td>
                    </tr>
                </c:forEach>
	                <td colspan="5" style="text-align: center;">
		                    <ul id="pagination-flickr">
		                    	<c:choose>
		                    		<c:when test="${schemasCurrentPage == 1}">
		                    			<li class="previous-off">« Previous</li>
		                    		</c:when>
		                    		<c:otherwise>
		                    			<li class="previous" onclick="getSchemasPageNumber('${schemasCurrentPage -1}')">« Previous</li>
		                    		</c:otherwise>  
		                    	</c:choose>
			                    <c:forEach begin="1" end="${schemasNumOfPages}" var="i">
					                <c:choose>
					                    <c:when test="${schemasCurrentPage eq i}">
					                      ${i}  
					                    </c:when>
					                    <c:otherwise>
					                        <li onclick="getSchemasPageNumber('${i}')">${i}</li>
					                    </c:otherwise>
					                </c:choose>
				            	</c:forEach>
				            	<c:choose>
		                    		<c:when test="${schemasCurrentPage == schemasNumOfPages}">
		                    			<li class="next-off" >Next »</li>
		                    		</c:when>
		                    		<c:otherwise>
		                    			<li class="next" onclick="getSchemasPageNumber('${schemasCurrentPage +1}')">Next »</li>
		                    		</c:otherwise>  
		                    	</c:choose>
				            	
				            </ul>
		            	</td>
            </tbody>
        </table>