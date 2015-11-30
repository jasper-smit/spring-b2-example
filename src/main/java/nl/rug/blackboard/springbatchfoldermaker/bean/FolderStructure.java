package nl.rug.blackboard.springbatchfoldermaker.bean;

import javax.servlet.http.HttpServletRequest;

public enum FolderStructure {
	STUDENTS, GROUPS, GROUPS_STUDENTS;

	public static FolderStructure fromRequest(HttpServletRequest req, String param) {
		try {
			return valueOf(req.getParameter(param));
		} catch (Exception e) {
			return STUDENTS;
		}
	}
}