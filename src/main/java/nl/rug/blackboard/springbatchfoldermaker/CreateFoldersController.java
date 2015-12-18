package nl.rug.blackboard.springbatchfoldermaker;


import blackboard.data.ReceiptMessage;
import blackboard.data.ReceiptOptions;
import blackboard.data.course.*;
import blackboard.data.user.User;
import blackboard.persist.*;
import blackboard.persist.course.impl.GroupDAO;
import blackboard.platform.course.CourseEntitlement;
import blackboard.platform.intl.BbResourceBundle;
import blackboard.platform.security.SecurityUtil;
import blackboard.platform.servlet.InlineReceiptUtil;
import blackboard.platform.spring.beans.annotations.ContextValue;
import nl.rug.blackboard.springbatchfoldermaker.bean.*;
import nl.rug.blackboard.springbatchfoldermaker.util.BatchFolderMakerLocalizationUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Controller
public class CreateFoldersController {
	private BbResourceBundle bundle = BatchFolderMakerLocalizationUtil.get().getBundle();


	@RequestMapping(value = "/createFolders", method = RequestMethod.GET)
	public String getForm(@ContextValue Course course, Model model) {
		List<GroupBean> groupSets = new ArrayList<>();
		for (Group grp : GroupDAO.get().loadGroupSetsOnly(course.getId())) {
			groupSets.add(new GroupBean(grp));
		}

		CreateFoldersModel createFoldersModel = new CreateFoldersModel();
		createFoldersModel.setAllPrivileges(Privilege.values());
		createFoldersModel.setDefaultFolderStructure(FolderStructure.STUDENTS);
		createFoldersModel.setGroupBeans(groupSets);
		createFoldersModel.setNoPrivileges(new Privilege[]{});
		createFoldersModel.setReadOnlyPrivileges(new Privilege[]{Privilege.READ});
		createFoldersModel.setHasGroups(!groupSets.isEmpty());

		model.addAttribute("createFoldersModel", createFoldersModel);

		return "instructor/createFolders";
	}



	@RequestMapping(value = "/createFolders", method = RequestMethod.POST)
	public String postForm(@ContextValue Course course,  @ContextValue User user, @ContextValue CourseMembership courseMembership, Model model, HttpServletRequest request) {
		FolderMaker folderMaker = new FolderMaker(course);
		if (!folderMaker.hasEnoughPrivileges()
				|| !SecurityUtil.userHasEntitlement(user, courseMembership, CourseEntitlement.MODIFY_COURSE.getEntitlement())) {
			addNotAuthorizedMessage();
			return getForm(course, model);
		}

		folderMaker.setOwnerPermissions(FolderPermissions.fromRequest(request, "ownerPrivs"));
		folderMaker.setFellowGroupMemberPermissions(FolderPermissions.fromRequest(request, "groupPrivs"));
		folderMaker.setOtherMembersPermissions(FolderPermissions.fromRequest(request, "otherPrivs"));
		folderMaker.setFellowGroupMemberGroupPermissions(FolderPermissions.fromRequest(request, "grpGroupPrivs"));
		folderMaker.setOtherGroupPermissions(FolderPermissions.fromRequest(request, "grpOtherPrivs"));

		int numFoldersCreated;
		Id groupSetId = Id.UNSET_ID;
		if (request.getParameter("groupset") != null) {
			try {
				groupSetId = Id.generateId(Group.DATA_TYPE, request.getParameter("groupset"));
			} catch (PersistenceException e) {
				throw new PersistenceRuntimeException(e);
			}
		}
		FolderStructure option = FolderStructure.fromRequest(request, "structure");
		switch (option) {
			case GROUPS:
				numFoldersCreated = folderMaker.createFolderForEachGroup(groupSetId);
				break;
			case GROUPS_STUDENTS:
				numFoldersCreated = folderMaker.createStudentFoldersInGroups(groupSetId);
				break;
			case STUDENTS:
			default:
				numFoldersCreated = folderMaker.createFolderForEachStudent();
		}

		String message = String.format(bundle.getString("CreateFoldersController.java.success"), numFoldersCreated);
		ReceiptOptions receiptOptions = new ReceiptOptions();
		receiptOptions.addSuccessMessage(message);
		InlineReceiptUtil.addReceiptToRequest(receiptOptions);

		return "redirect:createFolders?course_id=" + course.getId().toExternalString();
	}


	private void addNotAuthorizedMessage() {
		String message = bundle.getString("CreateFoldersController.java.notAuthorized");
		ReceiptOptions receiptOptions = new ReceiptOptions();
		receiptOptions.addMessage(message, ReceiptMessage.messageTypeEnum.FAILURE, null);
		InlineReceiptUtil.addReceiptToRequest(receiptOptions);
	}
}