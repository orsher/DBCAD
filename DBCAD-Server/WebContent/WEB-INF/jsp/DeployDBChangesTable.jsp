<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<table id="db-changes-table" class="table">
                <thead>
                    <tr>
                        <th></th>
                        <th>Database Change ID</th>
                        <c:forEach items="${options.lobs}" var="lob">
                        	<th>${lob}</th>
                        </c:forEach>
                    </tr>
                </thead>
                <tbody id="db-changes-table-body">
                    <c:forEach items="${dbChangesTableValues}" var="dbChange">
                        <tr class="table_row" title="${dbChange.db_request_code}">
                        	<td><input type="checkbox" class="db_change_checkbox" value="${dbChange.db_request_id}" id="${dbChange.db_request_id}"></td>
                            <td>${dbChange.db_request_id}</td>
                            <c:forEach items="${options.lobs}" var="lob">
                        		<td>${dbChange[lob]}</td>
                        	</c:forEach>
                        </tr>
                    </c:forEach>
                    <td colspan="${fn:length(options.lobs)+2}">
	                    <c:forEach begin="1" end="${noOfPages}" var="i">
			                <c:choose>
			                    <c:when test="${0 eq i}">
			                      ${i}  
			                    </c:when>
			                    <c:otherwise>
			                        <div onclick="getPageNumber('${i}')">${i}</div>
			                    </c:otherwise>
			                </c:choose>
		            	</c:forEach>
	            	</td>
                </tbody>
            </table>