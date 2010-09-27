<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" />
		</a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Registers">
		<li
			<c:if test='<%= request.getRequestURI().contains("module/register/createRegisterForm") %>'>class="active"</c:if>>
			<a
				href="${pageContext.request.contextPath}/module/register/createRegister.form">
				<spring:message code="register.create.link" /> </a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Registers,Manage Registers">
		<li
			<c:if test='<%= request.getRequestURI().contains("module/register/manageRegisterList") %>'>class="active"</c:if>>
			<a
				href="${pageContext.request.contextPath}/module/register/manageRegister.list">
				<spring:message code="register.manage.link" /> </a>
		</li>
	</openmrs:hasPrivilege>
</ul>