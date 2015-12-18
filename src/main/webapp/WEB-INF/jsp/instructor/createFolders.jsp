<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ng" uri="/bbNG" %>
<%@ taglib prefix="rug" uri="http://uocg.rug.nl/taglib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page errorPage="/courseError.jsp" %>

<jsp:useBean id="createFoldersModel" type="nl.rug.blackboard.springbatchfoldermaker.bean.CreateFoldersModel" scope="request"/>

<fmt:message var="labelPluginName" key="plugin.name" bundle="${bundle}"/>
<fmt:message var="labelStep1Title" key="createFolders.jsp.step1.title" bundle="${bundle}"/>
<fmt:message var="labelStep1Instructions" key="createFolders.jsp.step1.instructions" bundle="${bundle}"/>
<fmt:message var="labelStep1Structure" key="createFolders.jsp.step1.structureLabel" bundle="${bundle}"/>
<fmt:message var="labelStep1Groupset" key="createFolders.jsp.step1.groupsetLabel" bundle="${bundle}"/>
<fmt:message var="labelStep2Title" key="createFolders.jsp.step2.title" bundle="${bundle}"/>
<fmt:message var="labelStep2Advanced" key="createFolders.jsp.step2.advanced" bundle="${bundle}"/>
<fmt:message var="labelStep2PersonalFolder" key="createFolders.jsp.step2.personalfolders" bundle="${bundle}"/>
<fmt:message var="labelStep2GroupFolders" key="createFolders.jsp.step2.groupfolders" bundle="${bundle}"/>
<fmt:message var="labelStep2All" key="createFolders.jsp.step2.all" bundle="${bundle}"/>
<fmt:message var="labelStep2Read" key="createFolders.jsp.step2.read" bundle="${bundle}"/>
<fmt:message var="labelStep2Write" key="createFolders.jsp.step2.write" bundle="${bundle}"/>
<fmt:message var="labelStep2Delete" key="createFolders.jsp.step2.delete" bundle="${bundle}"/>
<fmt:message var="labelStep2Manage" key="createFolders.jsp.step2.manage" bundle="${bundle}"/>
<fmt:message var="labelStep2Owner" key="createFolders.jsp.step2.owner" bundle="${bundle}"/>
<fmt:message var="labelStep2Group" key="createFolders.jsp.step2.group" bundle="${bundle}"/>
<fmt:message var="labelStep2Other" key="createFolders.jsp.step2.other" bundle="${bundle}"/>
<fmt:message var="labelFolderStructureStudents" key="createFolders.jsp.folderStructure.students" bundle="${bundle}"/>
<fmt:message var="labelFolderStructureGroups" key="createFolders.jsp.folderStructure.groups" bundle="${bundle}"/>
<fmt:message var="labelFolderStructureGroupsStudents" key="createFolders.jsp.folderStructure.groupsStudents" bundle="${bundle}"/>


<ng:learningSystemPage ctxId="ctx" hideCourseMenu="false" hideEditToggle="true">
  <ng:breadcrumbBar isContent="true">
    <ng:breadcrumb>${labelPluginName}</ng:breadcrumb>
  </ng:breadcrumbBar>
  <ng:pageHeader>
    <ng:pageTitleBar iconUrl="/images/ci/sets/set01/grouppages_on.gif" title="${labelPluginName}"/>
  </ng:pageHeader>

  <ng:cssBlock>
    <style type="text/css">
      li#groupsetElement {
        display: none;
      }

      div.privileges span.label {
        float: left;
        width: 12em;
      }

      div.privileges input, div.privileges span.header {
        width: 7em;
        float: left;
        text-align: center;
        margin: 0;
      }

      div.privileges {
        clear: both;
      }
    </style>
  </ng:cssBlock>
  <ng:jsBlock>
    <script type="text/javascript">
      function changeOther(input) {
        // if you give "others" access to a personal folder then they
        // need read permissions on the parent folder as well
        // there will be a check on the server for this as well
        if (input.checked) {
          document.getElementById("grpOtherPrivs_0").checked = true;
        }
      }

      function displayGroupSets(input) {
        document.getElementById('groupsetElement').style.display =
            (input.value == 'STUDENTS' ? 'none' : 'block');
        document.getElementById('groupFolders').style.display =
            (input.value == 'STUDENTS' ? 'none' : 'block');
        document.getElementById('groupPrivsForPersonal').style.display =
            (input.value == 'STUDENTS' ? 'none' : 'block');
        document.getElementById('personalFolders').style.display =
            (input.value == 'GROUPS'  ? 'none' : 'block');
      }
    </script>
  </ng:jsBlock>

  <ng:form method="POST" name="bfmForm" isSecure="true" nonceId="/createFolders">
    <input type="hidden" name="course_id" value="${ctx.courseId.externalString}"/>
    <ng:dataCollection>
      <ng:step title="${labelStep1Title}">
        <c:if test="${empty createFoldersModel.groupBeans}">
          <ng:stepInstructions text="${labelStep1Instructions}"/>
        </c:if>
        <ng:dataElement label="${labelStep1Structure}">
            <fieldset id="structureSet" class="radioButtons">
              <div class="radioSelectors">
                <input type="radio" id="structure_0" name="structure" onclick="displayGroupSets(this)"  value="STUDENTS" checked="checked" />
                <label for="structure_0">${labelFolderStructureStudents}</label>
                <br style="display: block;"/>
                <c:if test="${createFoldersModel.hasGroups}">
                  <input type="radio" id="structure_1" name="structure" onclick="displayGroupSets(this)"
                         value="GROUPS"/>
                  <label for="structure_1">${labelFolderStructureGroups}</label>
                  <br style="display: block;"/>
                  <input type="radio" id="structure_2" name="structure" onclick="displayGroupSets(this)"
                         value="GROUPS_STUDENTS"/>
                  <label for="structure_2">${labelFolderStructureGroupsStudents}</label>
                  <br style="display: block;"/>
                </c:if>
              </div>
            </fieldset>
        </ng:dataElement>
        <ng:dataElement label="${labelStep1Groupset}" id="groupsetElement">
          <rug:listDropdown name="groupset" items="${createFoldersModel.groupBeans}" sorted="true"/>
        </ng:dataElement>
      </ng:step>

      <ng:step title="${labelStep2Title}">
        <div id="personalFolders">
          <h3>${labelStep2PersonalFolder}</h3>

          <div class="privileges">
            <span class="label">&nbsp;</span>
            <span class="header">${labelStep2All}</span>
            <span class="header">${labelStep2Read}</span>
            <span class="header">${labelStep2Write}</span>
            <span class="header">${labelStep2Delete}</span>
            <span class="header">${labelStep2Manage}</span>

            <div style="clear: both;">
              <span class="label">Owner</span>
              <rug:enumCheckboxes name="ownerPrivs" enum="${createFoldersModel.allPrivileges}"
                                  defaults="${createFoldersModel.allPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both; display: none;" id="groupPrivsForPersonal">
              <span class="label">Group</span>
              <rug:enumCheckboxes name="groupPrivs" enum="${createFoldersModel.allPrivileges}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both;">
              <span class="label">Other</span>
              <rug:enumCheckboxes name="otherPrivs" enum="${createFoldersModel.allPrivileges}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true" onchange="changeOther(this)"/>
            </div>
          </div>
          <br style="clear: both;"/>
        </div>

        <div id="groupFolders" style="display: none;">
          <h3>${labelStep2GroupFolders}</h3>

          <div class="privileges">
            <span class="label">&nbsp;</span>
            <span class="header">${labelStep2All}</span>
            <span class="header">${labelStep2Read}</span>
            <span class="header">${labelStep2Write}</span>
            <span class="header">${labelStep2Delete}</span>
            <span class="header">${labelStep2Manage}</span>

            <div style="clear: both;">
              <span class="label">Group</span>
              <rug:enumCheckboxes name="grpGroupPrivs" enum="${createFoldersModel.allPrivileges}"
                                  defaults="${createFoldersModel.readOnlyPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both;">
              <span class="label">Other</span>
              <rug:enumCheckboxes name="grpOtherPrivs" enum="${createFoldersModel.allPrivileges}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  id="grpOtherPrivs" selectAll="true"/>
            </div>
          </div>
          <br style="clear: both;"/>
        </div>
      </ng:step>
      <ng:stepSubmit/>
    </ng:dataCollection>
  </ng:form>
</ng:learningSystemPage>

