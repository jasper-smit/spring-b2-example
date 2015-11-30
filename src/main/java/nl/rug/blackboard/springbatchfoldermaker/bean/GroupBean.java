package nl.rug.blackboard.springbatchfoldermaker.bean;

import blackboard.data.course.Group;
import nl.rug.jsp.tags.ListDropDown;

import java.io.Serializable;

public class GroupBean implements ListDropDown.Entry, Serializable {
	private static final long serialVersionUID = -6624729365128512146L;

	private final String label, value;

	public GroupBean(Group grp) {
		label = grp.getTitle();
		value = grp.getId().toExternalString();
	}

	public String getLabel() { return label; }
	public String getValue() { return value; }
}
