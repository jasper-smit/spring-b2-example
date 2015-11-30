package nl.rug.blackboard.springbatchfoldermaker.bean;

import nl.rug.blackboard.springbatchfoldermaker.FolderPermissions;
import nl.rug.blackboard.springbatchfoldermaker.Privilege;

import java.util.ArrayList;
import java.util.List;

public class CreateFoldersModel {
	private List<GroupBean> groupBeans = new ArrayList<>();
	private FolderStructure defaultFolderStructure;
	private FolderStructure[] folderStructureValues;
	private Privilege[] permissionValues;
	private Privilege[] allPrivileges;
	private Privilege[] noPrivileges;
	private Privilege[] readOnlyPrivileges;


	public List<GroupBean> getGroupBeans() {
		return groupBeans;
	}

	public void setGroupBeans(List<GroupBean> groupBeans) {
		this.groupBeans = groupBeans;
	}

	public FolderStructure getDefaultFolderStructure() {
		return defaultFolderStructure;
	}

	public void setDefaultFolderStructure(FolderStructure defaultFolderStructure) {
		this.defaultFolderStructure = defaultFolderStructure;
	}

	public FolderStructure[] getFolderStructureValues() {
		return folderStructureValues;
	}

	public void setFolderStructureValues(FolderStructure[] folderStructureValues) {
		this.folderStructureValues = folderStructureValues;
	}

	public Privilege[] getPermissionValues() {
		return permissionValues;
	}

	public void setPermissionValues(Privilege[] permissionValues) {
		this.permissionValues = permissionValues;
	}

	public Privilege[] getAllPrivileges() {
		return allPrivileges;
	}

	public void setAllPrivileges(Privilege[] allPrivileges) {
		this.allPrivileges = allPrivileges;
	}

	public Privilege[] getNoPrivileges() {
		return noPrivileges;
	}

	public void setNoPrivileges(Privilege[] noPrivileges) {
		this.noPrivileges = noPrivileges;
	}

	public Privilege[] getReadOnlyPrivileges() {
		return readOnlyPrivileges;
	}

	public void setReadOnlyPrivileges(Privilege[] readOnlyPrivileges) {
		this.readOnlyPrivileges = readOnlyPrivileges;
	}
}
