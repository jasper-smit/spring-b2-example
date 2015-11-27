package blackboard.plugin.springdemo.spring.web;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.spring.web.annotations.IdParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloCourseController {
  @Autowired
  private CourseMembershipDbLoader _membershipLoader;

  @Autowired
  private UserDbLoader _userLoader;

  @RequestMapping("/course_users")
  public ModelAndView listCourseUsers(@IdParam("cid") Course course) throws KeyNotFoundException, PersistenceException {
    ModelAndView mv = new ModelAndView("course_users");
    mv.addObject("course", course);

    List<CourseMembership> members = _membershipLoader.loadByCourseId(course.getId());
    List<User> users = new ArrayList<>();
    for (CourseMembership member : members) {
      users.add(_userLoader.loadById(member.getUserId()));
    }
    mv.addObject("users", users);
    return mv;
  }
}
