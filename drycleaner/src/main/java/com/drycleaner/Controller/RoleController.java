package com.drycleaner.Controller;

import com.drycleaner.Model.Role; // Ensure this import is correct based on your project structure
import com.drycleaner.Service.RoleService; // Ensure this import is correct based on your project structure
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/add")
    public String createRole(Model model, @RequestParam String name, @RequestParam(required = false) String allowedMenus) {
        List<String> menus = allowedMenus != null ? Arrays.asList(allowedMenus.split(",")) : null;
        Role newRole = new Role(name, menus);
        roleService.save(newRole);
        model.addAttribute("successMessage", "Role added successfully!");
        return "add-role"; // Reload add-role view, or consider redirecting
    }

    @GetMapping("/add")
    public String showAddRolePage() {
        return "add-role"; // View for adding a new role
    }

    @GetMapping
    public String listRoles(Model model) {
        List<Role> roles = roleService.findAll();
        model.addAttribute("roles", roles);
        return "role-list"; // View for displaying the list of roles
    }
}
