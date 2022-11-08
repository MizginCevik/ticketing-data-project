package com.cydeo.controller;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.service.ProjectService;
import com.cydeo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/project")
public class ProjectController {

    private final UserService userService;
    private final ProjectService projectService;

    public ProjectController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/create")
    public String createProject(Model model) {

        model.addAttribute("project", new ProjectDTO());
        model.addAttribute("managers", userService.listAllByRole("manager"));
        model.addAttribute("projects", projectService.listAllProjectDetails()); // only show the projects belongs to manager who logged in

        return "/project/create";

    }

    @PostMapping("/create")
    public String insertProject(@Valid @ModelAttribute("project") ProjectDTO project, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("managers", userService.listAllByRole("manager"));
            model.addAttribute("projects", projectService.listAllProjectDetails());

            return "/project/create";

        }

        projectService.save(project);

        return "redirect:/project/create";

    }

    @GetMapping("/delete/{projectCode}")
    public String deleteProject(@PathVariable("projectCode") String projectCode) {
        projectService.delete(projectCode);
        return "redirect:/project/create";
    }

    @GetMapping("/complete/{projectCode}")
    public String completeProject(@PathVariable("projectCode") String projectCode) {
        projectService.complete(projectCode);
        return "redirect:/project/create";
    }

    @GetMapping("/update/{projectCode}")
    public String editProject(@PathVariable("projectCode") String projectCode, Model model){

        model.addAttribute("project", projectService.getByProjectCode(projectCode));
        model.addAttribute("managers", userService.listAllByRole("manager"));
        model.addAttribute("projects", projectService.listAllProjectDetails());

        return "/project/update";

    }

    @PostMapping("/update")
    public String updateProject(@Valid @ModelAttribute("project") ProjectDTO project, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("managers", userService.listAllByRole("manager"));
            model.addAttribute("projects", projectService.listAllProjectDetails());

            return "/project/update";

        }

        projectService.update(project);

        return "redirect:/project/create";

    }

    @GetMapping("/manager/project-status")
    public String getProjectByManager(Model model) {

        List<ProjectDTO> projects = projectService.listAllProjectDetails();

        model.addAttribute("projects", projects);

        return "/manager/project-status";

    }

    @GetMapping("/manager/complete/{projectCode}")
    public String managerCompleteProject(@PathVariable("projectCode") String projectCode) {
        projectService.complete(projectCode);
        return "redirect:/project/manager/project-status";
    }

}
/*
    projectCode is unique in Project Create Form
    1. Later on if I delete one project and want to create new project with same projectCode ??

        @Column(unique = true)  -->  can't create project with same project code by this
        private String projectCode;

        -- I should be able to use the same projectCode.
        @Where(clause = "is_deleted=false")  --> I have this one, it's not deleted actually

        to solve this problem,
        I'm changing the projectCode (change to anything) after delete operation
        -- SP00 -> SP00-1

    2. When I delete the project from Project List on Project Create page, it's deleting without checking unfinished tasks
    And also when we delete the project, related tasks should be deleted as well

    to solve this problem,
    -- created method in task service
        taskService.deleteByProject(projectMapper.convertToDto(project));
            - find tasks which is to be deleted by Project and delete them one by one
            - because in delete logic we need to set the isDeleted field to true one by one

    3. If I want to complete the project from Project List on Project Create page, all the tasks should be completed too
    On Project Status page, we check if there is any unfinished tasks before completing
    But here I am able to complete without checking that condition by changing all tasks to completed status

    to solve this problem,
    -- created method in task service
        taskService.completeByProject(projectMapper.convertToDto(project));
            - find tasks and set the status to Completed then update it

    4. in update method we are saying if task status is not null then don't change it, get it and use it
        convertedTask.setTaskStatus(task.get().getTaskStatus());
        but if there is new status coming ???

        to solve this problem,
        if status is null which means user is sending information from the form
        - get me status from database -> task.get().getTaskStatus()
        if user sending new status to me
        - get that new status -> dto.getTaskStatus()

        basically if there is no status just get from database, if there is a status just use the one is coming from user

*/
