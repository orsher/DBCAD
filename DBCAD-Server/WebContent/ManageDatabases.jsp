<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>DBCAD - Manage Databases</title>
</head>
<body>
<jsp:useBean id="metaDataBean" type="dbcad.MetaDataBean" scope="request" />
<jsp:getProperty property="metadataJson" name="metaDataBean"/>

</body>
</html>