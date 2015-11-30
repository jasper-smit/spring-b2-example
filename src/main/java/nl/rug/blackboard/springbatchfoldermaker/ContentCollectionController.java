package nl.rug.blackboard.springbatchfoldermaker;

import blackboard.cms.filesystem.CSContext;
import blackboard.cms.filesystem.CSDirectory;
import blackboard.data.course.Course;
import blackboard.platform.spring.web.annotations.IdParam;
import nl.rug.lib.blackboard.cs.CsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
@Controller
@RequestMapping("/contentCollection")
public class ContentCollectionController {
	private static final String COURSE_TOC_URL = "/webapps/cmsmain/webui%1$s?action=frameset&subaction=view&course_id=%2$s";


	@RequestMapping("")
	public RedirectView redirectToBbUI(@IdParam("course_id") Course course) {
		return getBbUI(course, null);
	}


	@RequestMapping("/{path}")
	public RedirectView redirectToBbUI(@IdParam("course_id") Course course, @PathVariable String path) {
		return getBbUI(course, path);
	}


	private RedirectView getBbUI(final Course course, final String path) {
		CourseDropBoxLocationBuilder CourseRootBuilder = new CourseDropBoxLocationBuilder(course, path);
		CsUtil.sudo(CourseRootBuilder);
		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(String.format(COURSE_TOC_URL, CourseRootBuilder.getFullPath(), course.getId().toExternalString()));
		return redirectView;
	}


	private class CourseDropBoxLocationBuilder implements CsUtil.SudoCommand {
		private final Course course;
		private final String path;
		private CSContext csCtx;
		private String fullPath;

		private CourseDropBoxLocationBuilder(Course crs, String subDir) {
			course = crs;
			path = subDir;
		}

		public String getFullPath() { return fullPath; }
		public void setContext(CSContext ctx) { csCtx = ctx; }

		public void execute() {
			// build link to folders. Start with the course root, check if the "Dropbox" sub folder exists
			// if so, link there, otherwise use the course's root path.
			fullPath = csCtx.getCourseDirectory(course).getFullPath();
			if (path != null) {
				fullPath += "/" + path;
			}
			CSDirectory rootFolder = (CSDirectory) csCtx.findEntry(path + "/" + FolderMaker.ROOT_FOLDER_NAME);
			if (rootFolder != null) {
				fullPath += "/" + FolderMaker.ROOT_FOLDER_NAME;
			}
		}
	}
}
