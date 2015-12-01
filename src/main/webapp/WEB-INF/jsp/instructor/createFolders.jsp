<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ng" uri="/bbNG" %>
<%@ taglib prefix="rug" uri="http://uocg.rug.nl/taglib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page errorPage="/courseError.jsp" %>

<jsp:useBean id="createFoldersModel" type="nl.rug.blackboard.springbatchfoldermaker.bean.CreateFoldersModel" scope="request"/>


<ng:learningSystemPage ctxId="ctx" hideCourseMenu="false" hideEditToggle="true">
  <ng:breadcrumbBar isContent="true">
    <ng:breadcrumb>Batch Folder Maker</ng:breadcrumb>
  </ng:breadcrumbBar>
  <ng:pageHeader>
    <ng:pageTitleBar iconUrl="/images/ci/sets/set01/grouppages_on.gif" title="Batch Folder Maker"/>
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

  <form method="POST" name="bfmForm">
    <input type="hidden" name="course_id" value="${ctx.courseId.externalString}"/>
    <ng:dataCollection>
      <ng:step title="Step1">
        <c:if test="${empty createFoldersModel.groupBeans}">
          <ng:stepInstructions text="Instructies"/>
        </c:if>
        <ng:dataElement label="Labels">
          <rug:enumRadioSet name="structure" enum="${createFoldersModel.folderStructureValues}"
                            default="${createFoldersModel.defaultFolderStructure}" vertical="true"
                            onclick="displayGroupSets(this)"/>
        </ng:dataElement>
        <ng:dataElement label="Labels" id="groupsetElement">
          <rug:listDropdown name="groupset" items="${createFoldersModel.groupBeans}" sorted="true"/>
        </ng:dataElement>
      </ng:step>

      <ng:step title="Labels">
        <div id="personalFolders">
          <h3>Labels</h3>

          <fmt:message var="test" key="plugin.name" bundle="${bundle}"/>
          <h2>${test}</h2>

          <div class="privileges">
            <span class="label">&nbsp;</span>
            <span class="header">All</span>
            <span class="header">Read</span>
            <span class="header">Write</span>
            <span class="header">Delete</span>
            <span class="header">Manage</span>

            <div style="clear: both;">
              <span class="label">Owner</span>
              <rug:enumCheckboxes name="ownerPrivs" enum="${createFoldersModel.permissionValues}"
                                  defaults="${createFoldersModel.allPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both; display: none;" id="groupPrivsForPersonal">
              <span class="label">Group</span>
              <rug:enumCheckboxes name="groupPrivs" enum="${createFoldersModel.permissionValues}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both;">
              <span class="label">Other</span>
              <rug:enumCheckboxes name="otherPrivs" enum="${createFoldersModel.permissionValues}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true" onchange="changeOther(this)"/>
            </div>
          </div>
          <br style="clear: both;"/>
        </div>

        <div id="groupFolders" style="display: none;">
          <h3>Groupfolders</h3>

          <div class="privileges">
            <span class="label">&nbsp;</span>
            <span class="header">All</span>
            <span class="header">Read</span>
            <span class="header">Write</span>
            <span class="header">Delete</span>
            <span class="header">Manage</span>

            <div style="clear: both;">
              <span class="label">Group</span>
              <rug:enumCheckboxes name="grpGroupPrivs" enum="${createFoldersModel.permissionValues}"
                                  defaults="${createFoldersModel.readOnlyPrivileges}" vertical="false" hideLabels="true"
                                  selectAll="true"/>
            </div>
            <div style="clear: both;">
              <span class="label">Other</span>
              <rug:enumCheckboxes name="grpOtherPrivs" enum="${createFoldersModel.permissionValues}"
                                  defaults="${createFoldersModel.noPrivileges}" vertical="false" hideLabels="true"
                                  id="grpOtherPrivs" selectAll="true"/>
            </div>
          </div>
          <br style="clear: both;"/>
        </div>
      </ng:step>
      <ng:stepSubmit/>
    </ng:dataCollection>
  </form>
</ng:learningSystemPage>

