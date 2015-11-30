package nl.rug.blackboard.springbatchfoldermaker;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:j.b.smit [at] gmail.com">Jasper Smit</a>
 */
public class FolderPermissions {


	public static FolderPermissions fromRequest(HttpServletRequest req, String param) {
		FolderPermissions result = new FolderPermissions();

		String[] values = req.getParameterValues(param);
		if (values != null) {
			for (String val : values) {
				if (val != null) {
					result.privs.add(Privilege.valueOf(val));
				}
			}
		}
		return result;
	}


	private final Set<Privilege> privs = new HashSet<Privilege>();


	public boolean isRead() { return privs.contains(Privilege.READ); }
	public boolean isWrite() { return privs.contains(Privilege.WRITE); }
	public boolean isManage() { return privs.contains(Privilege.MANAGE); }
	public boolean isDelete() { return privs.contains(Privilege.DELETE); }
	public boolean permitsAnything() { return isRead() || isRead() || isManage() || isDelete(); }


	public boolean hasPrivileges() { return privs.size() > 0; }


	public void add(Privilege priv) { privs.add(priv); }


	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append("{");
		if (isRead()) sb.append("read ");
		if (isWrite()) sb.append("write ");
		if (isDelete()) sb.append("delete ");
		if (isManage()) sb.append("manage ");
		int lastSpace = sb.lastIndexOf(" ");
		if (lastSpace > 0) {
			sb.deleteCharAt(lastSpace);
		}
		sb.append("}");
		return sb.toString();
	}
}
