package nl.rug.blackboard.springbatchfoldermaker;

import blackboard.cms.filesystem.*;
import blackboard.cms.filesystem.security.*;
import blackboard.data.course.*;
import blackboard.data.navigation.CourseToc;
import blackboard.data.user.User;
import blackboard.persist.*;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.impl.GroupDAO;
import blackboard.persist.navigation.CourseTocDbLoader;
import blackboard.persist.navigation.CourseTocDbPersister;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.coursemap.CourseMapManagerFactory;
import com.xythos.common.api.XythosException;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * @author <a href="mailto:j.b.smit [at] gmail.com">Jasper Smit</a>
 */
public class FolderMaker {
	private static final String COURSE_TOC_LABEL = "Dropbox";
	public static final String ROOT_FOLDER_NAME = "Dropbox";
	public static final String CONTENT_DROPBOX_NAV = "UG-bfmCc-nav-contentDropBox";

	private final Logger log4j = Logger.getLogger(getClass());

	private final Course course;
	private List<String> existingStudentFolders = new LinkedList<String>(); // Do not create these folders
	private List<String> existingGroupFolders = new LinkedList<String>();
	private CSContext cscontext;

	//These define the permissions on the personal folders
	private FolderPermissions ownerPermissions, fellowGroupMemberPermissions, otherMembersPermissions;

	//The permissions on group folders
	private FolderPermissions fellowGroupMemberGroupPermissions, otherGroupPermissions;


	public FolderMaker(Course crs) {
		course = crs;

		// we used to set read privileges for all course users on the root course 
		// folder, but this is not necessary anymore and it makes the changes 
		// Blackboard made for the Adaptive Release functionality useless 
	}


	public void setOwnerPermissions(FolderPermissions value) { ownerPermissions = value; }
	public void setFellowGroupMemberPermissions(FolderPermissions value) { fellowGroupMemberPermissions = value; }
	public void setOtherMembersPermissions(FolderPermissions value) { otherMembersPermissions = value; }
	public void setFellowGroupMemberGroupPermissions(FolderPermissions value) { fellowGroupMemberGroupPermissions = value; }
	public void setOtherGroupPermissions(FolderPermissions value) { otherGroupPermissions = value; }


	public boolean hasCourseFolder() {
		try {
			cscontext = CSContext.getContext();
			cscontext.getCourseDirectory(course);
			return true;
		} catch (Exception e) {
			log4j.warn("course " + course.getCourseId() + " has no folder in the content system");
			cscontext.rollback();
		} finally {
			commit();
		}
		return false;
	}


	public boolean hasEnoughPrivileges() {
		try {
			cscontext = CSContext.getContext();
			CSDirectory dir = cscontext.getCourseDirectory(course);
			log4j.debug(dir.getFullPath() + " ->[quota=" + dir.getAvailableQuota()
					+ ", writeable:" + cscontext.canWrite(dir) + "]");
			return cscontext.canWrite(dir);
		} catch (Exception e) {
			log4j.error("hasEnoughPrivileges(): " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
			cscontext.rollback();
		} finally {
			commit();
		}
		return false;
	}


	public int createFolderForEachStudent() {
		try {
			cscontext = CSContext.getContext();
			List<User> students = fetchStudentsInCourse();
			existingStudentFolders = getExistingStudentFolders();
			return createUserFolders(students);
		} catch (Exception e) {
			log4j.error("createFolderForEachStudent(): " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
			cscontext.rollback();
		} finally {
			commit();
		}
		return 0;
	}


	public int createFolderForEachGroup(Id groupSetId) {
		List<Group> groups = GroupDAO.get().loadGroupSetList(groupSetId);
		int result = 0;
		try {
			cscontext = CSContext.getContext();
			existingGroupFolders = getExistingFolders();
			log4j.debug("createFolderForEachGroup(): " + groups.size() + " groups");
			for (Group group : groups) {
				log4j.trace("Creating group folder " + group.getTitle());
				result += createGroupFolder(group, false);
			}
			createMenuItem(cscontext.getCourseDirectory(course).getFullPath());
		} catch (Exception e) {
			log4j.error("createFolderForEachStudent(): " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
			cscontext.rollback();
		} finally {
			commit();
		}
		return result;
	}


	public int createStudentFoldersInGroups(Id groupSetId) {
		checkPermissionConsistency();
		List<Group> groups = GroupDAO.get().loadGroupSetList(groupSetId);
		int result = 0;
		try {
			cscontext = CSContext.getContext();
			existingGroupFolders = getExistingFolders();
			log4j.debug("createStudentFoldersInGroups(): " + groups.size() + " groups");
			for (Group group : groups) {
				log4j.trace("Creating group folder " + group.getTitle());
				result += createGroupFolder(group, true);
			}
			createMenuItem(cscontext.getCourseDirectory(course).getFullPath());
		} catch (Exception e) {
			log4j.error("createFolderForEachStudent(): " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
			cscontext.rollback();
		} finally {
			commit();
		}
		return result;
	}


	public void checkPermissionConsistency() {
		// if "others" have access to a personal folder then they
		// need read permissions on the parent (group) folder as well
		if (otherMembersPermissions.hasPrivileges()) {
			otherGroupPermissions.add(Privilege.READ);
		}
	}


	/*
	 * Creates the root folder where all folders are created, if it does not exists yet
   */
	private CSDirectory getRootFolder() {
		CSDirectory courseDir = cscontext.getCourseDirectory(course);
		CSDirectory rootFolder = (CSDirectory) cscontext.findEntry(courseDir.getFullPath() + "/" + ROOT_FOLDER_NAME);
		if (rootFolder == null) {
			rootFolder = createDirectory(courseDir.getFullPath(), ROOT_FOLDER_NAME);
			removePermissions(rootFolder);
			setFullInstructorAccess(rootFolder);
			setAllCourseUsersPermissions(rootFolder, true, false, false, false);
		}
		return rootFolder;
	}


	/**
	 * @param group the group that needs a folder in the content system
	 *              with sub folders for each member
	 * @return the number of (user) folders created in the group folder
	 */
	private int createGroupFolder(Group group, boolean createStudentSubFolders) {
		CSDirectory rootDir = getRootFolder();
		CSDirectory groupDir;
		String folderName = safeCSName(group.getTitle());
		int result = 0;

		//Get or create directory
		if (existingGroupFolders.contains(folderName)) {
			groupDir = (CSDirectory) cscontext.findEntry(rootDir.getFullPath() + "/" + folderName);
		} else {
			groupDir = createDirectory(rootDir.getFullPath(), folderName);

			//Set permissions for group folder
			//Only set permissions if folder did not exist
			//otherwise, the new permissions are transferred to the possible existing sub folders
			removePermissions(groupDir);
			setFullInstructorAccess(groupDir);
			//Add group access
			CSPrincipal coursePrincipal = CourseGroupPrincipal.createInstance(course, group);
			CSAccessControlEntry courseEntry = groupDir.getAccessControlEntry(coursePrincipal.getPrincipalID());
			//persons in group get only read access
			courseEntry.updatePermissions(fellowGroupMemberGroupPermissions.isRead(),
					fellowGroupMemberGroupPermissions.isWrite(),
					fellowGroupMemberGroupPermissions.isManage(),
					fellowGroupMemberGroupPermissions.isDelete());

			CSPrincipal othersPrincipal = CoursePrincipal.createInstance(course);
			CSAccessControlEntry othersEntry = groupDir.getAccessControlEntry(othersPrincipal.getPrincipalID());
			othersEntry.updatePermissions(otherGroupPermissions.isRead(),
					otherGroupPermissions.isWrite(),
					otherGroupPermissions.isManage(),
					otherGroupPermissions.isDelete());
			result++;
		}

		if (createStudentSubFolders) {
			// Get existing student folders, is used by createSingleFolder()
			// to prevent creating already existing folders
			existingStudentFolders = getExistingStudentFolders(groupDir);
			try {
				UserDbLoader userLoader = UserDbLoader.Default.getInstance();
				List<User> users = userLoader.loadByGroupId(group.getId());
				for (User user : users) {
					if (createSingleFolder(groupDir, user, group)) {
						result++;
					}
				}
			} catch (PersistenceException e) {
				log4j.warn("createGroupFolder(): " + e.getMessage());
				log4j.trace("stacktrace:", e);
			}
		}
		return result;
	}


	private int createUserFolders(List<User> users) {
		int result = 0;
		CSDirectory parent = getRootFolder();
		for (User user : users) {
			try {
				if (createSingleFolder(parent, user, null)) {
					result++;
				}
			} catch (Exception e) {
				log4j.warn("Could not create folder for user " + user.getUserName() + ": " + e.getMessage());
				log4j.trace("stacktrace:", e);
			}
		}
		createMenuItem(parent.getFullPath());
		return result;
	}


	private boolean createSingleFolder(CSDirectory parent, User user, Group group) {
		if (existingStudentFolders.contains(user.getUserName())) {
			return false;
		}

		//Create folder
		String newFolderName = safeCSName(String.format("%s, %s (%s)",
				user.getFamilyName(), user.getGivenName(), user.getUserName()));
		CSDirectory dir = createDirectory(parent.getFullPath(), newFolderName);

		//Remove all permissions from folder
		removePermissions(dir);
		setFullInstructorAccess(dir);

		//Add student access
		CSPrincipal studentPrincipal = UserPrincipal.createInstance(user);
		CSAccessControlEntry studentEntry = dir.getAccessControlEntry(studentPrincipal.getPrincipalID());
		studentEntry.updatePermissions(ownerPermissions.isRead(), ownerPermissions.isWrite(),
				ownerPermissions.isManage(), ownerPermissions.isDelete());

		//Add group member access
		if (group != null && fellowGroupMemberPermissions.permitsAnything()) {
			CSPrincipal coursePrincipal = CourseGroupPrincipal.createInstance(course, group);
			CSAccessControlEntry courseEntry = dir.getAccessControlEntry(coursePrincipal.getPrincipalID());
			//persons in group get only read access
			courseEntry.updatePermissions(fellowGroupMemberPermissions.isRead(), fellowGroupMemberPermissions.isWrite(),
					fellowGroupMemberPermissions.isManage(), fellowGroupMemberPermissions.isDelete());
		}

		//Add other course members access
		if (otherMembersPermissions.permitsAnything()) {
			//UserPrincipal
			CSPrincipal coursePrincipal = CoursePrincipal.createInstance(course);
			CSAccessControlEntry courseEntry = dir.getAccessControlEntry(coursePrincipal.getPrincipalID());
			//persons in group get only read access
			courseEntry.updatePermissions(otherMembersPermissions.isRead(), otherMembersPermissions.isWrite(),
					otherMembersPermissions.isManage(), otherMembersPermissions.isDelete());
		}
		return true;
	}


	private CSDirectory createDirectory(String fullPath, String newFolderName) {
		try {
			CSDirectory dir = (CSDirectory) cscontext.findEntry(fullPath + "/" + newFolderName);
			if (dir != null) {
				return dir;
			} else {
				return cscontext.createDirectory(fullPath, newFolderName);
			}
		} catch (CSFileSystemException ex) {
			return cscontext.createDirectory(fullPath, newFolderName);
		}
	}


	private void setFullInstructorAccess(CSDirectory dir) {
		CSPrincipal instructorPrincipal = CourseRolePrincipal.createInstance(course, CourseMembership.Role.INSTRUCTOR);
		CSAccessControlEntry instructorEntry = dir.getAccessControlEntry(instructorPrincipal.getPrincipalID());
		// instructor gets all permissions: read write manage delete
		instructorEntry.updatePermissions(true, true, true, true);
	}


	private void setAllCourseUsersPermissions(CSDirectory dir, boolean read, boolean write, boolean manage, boolean delete) {
		CSPrincipal coursePrincipal = CoursePrincipal.createInstance(course);
		CSAccessControlEntry studentEntry = dir.getAccessControlEntry(coursePrincipal.getPrincipalID());
		studentEntry.updatePermissions(read, write, manage, delete);
	}


	private void removePermissions(CSDirectory dir) {
		try {
			CSAccessControlEntry[] accessEntries = dir.getAccessControlEntries();
			for (CSAccessControlEntry accessEntry : accessEntries) {
				String principalID = accessEntry.getPrincipalID();
				//Do not remove system admin or other system permissions, or we won't be able
				//to access the item at all!
				if (!principalID.contains("@") && !principalID.startsWith("G:SR")) {
					dir.deleteAccessControlEntry(principalID);
				}
			}
		} catch (Exception e) {
			log4j.warn("removePermissions(): " + e.getMessage());
			log4j.trace("stacktrace:", e);
		}
	}


	private List<String> getExistingFolders(CSDirectory dir) {
		List<String> result = new ArrayList<String>();

		for (CSEntry item : dir.getDirectoryContents()) {
			result.add(item.getBaseName());
		}
		return result;
	}


	private List<String> getExistingFolders() {
		return getExistingFolders(getRootFolder());
	}


	/**
	 * Assumes that folders contain the username between parentheses.
	 *
	 * @param dir the directory where we'll be creating student folders
	 * @return a list with usernames for students who already have a folder
	 *         in this directory
	 */
	private List<String> getExistingStudentFolders(CSDirectory dir) {
		List<String> folders = getExistingFolders(dir);
		List<String> usernames = new ArrayList<String>();
		for (String name : folders) {
			int start = name.lastIndexOf("(");
			int stop = name.lastIndexOf(")");
			if (start >= 0 && stop >= 0 && stop > start) {
				usernames.add(name.substring(start + 1, stop));
			}
		}
		return usernames;
	}


	private List<String> getExistingStudentFolders() {
		return getExistingStudentFolders(getRootFolder());
	}


	private List<User> fetchStudentsInCourse() {
		List<User> users = new LinkedList<User>();
		try {
			CourseMembershipDbLoader cml = CourseMembershipDbLoader.Default.getInstance();
			for (CourseMembership cm : cml.loadByCourseId(course.getId(), null, true)) {
				if (cm.getRole().equals(CourseMembership.Role.STUDENT)) {
					users.add(cm.getUser());
				}
			}
		} catch (PersistenceException e) {
			log4j.warn("fetchStudentsInCourse():" + e.getMessage());
			log4j.trace("stacktrace:", e);
		}
		return users;
	}


	private void createMenuItem(String name) {
		try {
			try {
				CourseToc toc = CourseTocDbLoader.Default.getInstance().loadByCourseIdAndLabel(course.getId(), COURSE_TOC_LABEL);
				log4j.debug("coursetoc already exists: " + toc);
			} catch (KeyNotFoundException e) {
				CourseToc toc = new CourseToc();
				toc.setIsEntryPoint(false);
				toc.setId(Id.newId(CourseToc.DATA_TYPE));
				toc.setCourseId(course.getId());
				toc.setLabel(COURSE_TOC_LABEL);
				toc.setIsEnabled(true);
				toc.setTargetType(CourseToc.Target.APPLICATION);
				toc.setInternalHandle(CONTENT_DROPBOX_NAV);
				CourseTocDbPersister.Default.getInstance().persist(toc);
				CourseMapManagerFactory.getInstance().invalidateCache(course.getId());
			}
		} catch (Exception e) {
			// yes, catching Exception is not a good practice
			// but creating the menu item is not that important
			log4j.error("createMenuItem(" + name + ") caused: " + e.getMessage(), e);
		}
	}


	private String safeCSName(String unsafe) {
		String illegalChars = "*?\"<>|\\/+%.:";
		StringBuilder safe = new StringBuilder();
		for (int i = 0; i < unsafe.length(); i++) {
			if (illegalChars.indexOf(unsafe.charAt(i)) == -1) {
				safe.append(unsafe.substring(i, i + 1));
			}
		}
		return safe.toString();
	}


	private void commit() {
		try {
			if (cscontext != null) {
				cscontext.commit();
			}
		} catch (XythosException e) {
			log4j.error("could not commit: " + e.getMessage(), e);
		}
	}
}
