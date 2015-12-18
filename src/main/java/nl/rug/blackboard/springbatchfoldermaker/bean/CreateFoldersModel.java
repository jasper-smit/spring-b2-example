package nl.rug.blackboard.springbatchfoldermaker.bean;

import nl.rug.blackboard.springbatchfoldermaker.Privilege;

import java.util.ArrayList;
import java.util.List;


public class CreateFoldersModel {
	private List<GroupBean> groupBeans = new ArrayList<>();
	private FolderStructure defaultFolderStructure;
	private Privilege[] allPrivileges;
	private Privilege[] noPrivileges;
	private Privilege[] readOnlyPrivileges;
	private boolean hasGroups;


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


	public boolean isHasGroups() {
		return hasGroups;
	}


	public void setHasGroups(boolean hasGroups) {
		this.hasGroups = hasGroups;
	}
}
