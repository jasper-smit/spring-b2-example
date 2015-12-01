package nl.rug.blackboard.springbatchfoldermaker;


import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.course.impl.GroupDAO;
import blackboard.platform.intl.BbResourceBundle;
import blackboard.platform.spring.beans.annotations.ContextValue;
import blackboard.platform.spring.service.BundleManager;
import nl.rug.blackboard.springbatchfoldermaker.bean.CreateFoldersModel;
import nl.rug.blackboard.springbatchfoldermaker.bean.FolderStructure;
import nl.rug.blackboard.springbatchfoldermaker.bean.GroupBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CreateFoldersController {


	@RequestMapping("/createFolders")
	public String getForm(@ContextValue Course course, Model model) {
		FolderMaker fm = new FolderMaker(course);
		List<GroupBean> groupSets = new ArrayList<>();
		for (Group grp : GroupDAO.get().loadGroupSetsOnly(course.getId())) {
			groupSets.add(new GroupBean(grp));
		}

		CreateFoldersModel createFoldersModel = new CreateFoldersModel();
		createFoldersModel.setAllPrivileges(Privilege.values());
		createFoldersModel.setDefaultFolderStructure(FolderStructure.STUDENTS);
		createFoldersModel.setFolderStructureValues(FolderStructure.values());
		createFoldersModel.setPermissionValues(Privilege.values());
		createFoldersModel.setGroupBeans(groupSets);
		createFoldersModel.setNoPrivileges(new Privilege[]{});
		createFoldersModel.setReadOnlyPrivileges(new Privilege[]{Privilege.READ});

		model.addAttribute("createFoldersModel", createFoldersModel);

		return "instructor/createFolders";

//	String message;
//	if (!fm.hasCourseFolder()) {
//			message = ".This course can not use the content system, it probably predates the installation " +
//					"  of the content system.";
//		} else if (fm.hasEnoughPrivileges()) {
//			return "/instructor/createFolders";
//		} else {
//			message = "You do not have enough privileges for creating items in the Content System.";
//		}
//		return failureUrl();
	}

}
