<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<table id="db-changes-table" class="table">
                <thead>
                    <tr>
                        <th></th>
                        <th>Database Change ID</th>
                        <th class="dividing-column">Schema ID</th>
                        <c:forEach items="${options.lobs}" var="lob">
                        	<th>${lob}</th>
                        </c:forEach>
                    </tr>
                </thead>
                <tbody id="db-changes-table-body">
                  <%--   <c:forEach items="${dbChangesTableValues}" var="dbChange">
                        <tr class="table_row" title="${dbChange.db_request_code}">
                        	<td><input type="checkbox" class="db_change_checkbox" value="${dbChange.db_request_id}" id="${dbChange.db_request_id}"></td>
                            <td>${dbChange.db_request_id}</td>
                            <td class="dividing-column">${dbChange.schema_id}</td>
                            <c:forEach items="${options.lobs}" var="lob">
                        		<td><div onclick="getLog('${dbChange.db_request_id}','${lob}')">${dbChange[lob]}</div></td>
                        	</c:forEach>
                        </tr>
                    </c:forEach>
                    <td colspan="${fn:length(options.lobs)+3}" style="text-align: center;">
	                    <ul id="pagination-flickr">
	                    	<c:choose>
	                    		<c:when test="${currentPage == 1}">
	                    			<li class="previous-off">« Previous</li>
	                    		</c:when>
	                    		<c:otherwise>
	                    			<li class="previous" onclick="getPageNumber('${currentPage -1}')">« Previous</li>
	                    		</c:otherwise>  
	                    	</c:choose>
		                    <c:forEach begin="1" end="${noOfPages}" var="i">
				                <c:choose>
				                    <c:when test="${currentPage eq i}">
				                      ${i}  
				                    </c:when>
				                    <c:otherwise>
				                        <li onclick="getPageNumber('${i}')">${i}</li>
				                    </c:otherwise>
				                </c:choose>
			            	</c:forEach>
			            	<c:choose>
	                    		<c:when test="${currentPage == noOfPages}">
	                    			<li class="next-off" >Next »</li>
	                    		</c:when>
	                    		<c:otherwise>
	                    			<li class="next" onclick="getPageNumber('${currentPage +1}')">Next »</li>
	                    		</c:otherwise>  
	                    	</c:choose>
			            	
			            </ul>
	            	</td> --%>
                </tbody>
            </table>