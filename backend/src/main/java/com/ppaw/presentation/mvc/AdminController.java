package com.ppaw.presentation.mvc;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.PlanLimit;
import com.ppaw.dataaccess.entity.User;
import com.ppaw.service.PlanLimitService;
import com.ppaw.service.PlanService;
import com.ppaw.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("admin")
public class AdminController {

    private final UserService userService;
    private final PlanService planService;
    private final PlanLimitService planLimitService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, 
                       HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getAllUsers().stream()
                .filter(u -> u.getEmail().equals(email) && u.getPasswordHash().equals(password))
                .findFirst()
                .orElse(null);
            if (user != null && user.getRole() == User.UserRole.ADMIN) {
                session.setAttribute("adminUserId", user.getId().toString());
                return "redirect:/admin/dashboard";
            }
        } catch (Exception e) {
            // Ignore
        }
        redirectAttributes.addFlashAttribute("error", "Invalid credentials");
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminUserId") == null) {
            return "redirect:/admin/login";
        }
        return "admin/dashboard";
    }

    // Plans management
    @GetMapping("/plans")
    public String plansList(HttpSession session, Model model) {
        if (session.getAttribute("adminUserId") == null) {
            return "redirect:/admin/login";
        }
        List<Plan> plans = planService.getAllPlansForAdmin();
        model.addAttribute("plans", plans);
        return "admin/plans-list";
    }

    @GetMapping("/plans/new")
    public String planForm(Model model) {
        model.addAttribute("plan", new Plan());
        return "admin/plan-form";
    }

    @GetMapping("/plans/edit/{id}")
    public String planEditForm(@PathVariable UUID id, Model model) {
        Plan plan = planService.getPlanById(id);
        model.addAttribute("plan", plan);
        return "admin/plan-form";
    }

    @PostMapping("/plans/save")
    public String savePlan(@ModelAttribute Plan plan, RedirectAttributes redirectAttributes) {
        if (plan.getId() == null) {
            planService.createPlan(toPlanDto(plan));
        } else {
            planService.updatePlan(plan.getId(), toPlanDto(plan));
        }
        redirectAttributes.addFlashAttribute("message", "Plan saved successfully");
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/delete/{id}")
    public String deletePlan(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        planService.softDeletePlan(id);
        redirectAttributes.addFlashAttribute("message", "Plan deleted successfully");
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/hard-delete/{id}")
    public String hardDeletePlan(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        planService.hardDeletePlan(id);
        redirectAttributes.addFlashAttribute("message", "Plan permanently deleted");
        return "redirect:/admin/plans";
    }

    // Plan Limits management
    @GetMapping("/plans/{planId}/limits")
    public String planLimitsList(@PathVariable UUID planId, Model model) {
        Plan plan = planService.getPlanById(planId);
        List<PlanLimit> limits = planLimitService.getPlanLimitsByPlanId(planId);
        model.addAttribute("plan", plan);
        model.addAttribute("limits", limits);
        return "admin/limits-list";
    }

    @GetMapping("/plans/{planId}/limits/new")
    public String limitForm(@PathVariable UUID planId, Model model) {
        Plan plan = planService.getPlanById(planId);
        model.addAttribute("plan", plan);
        model.addAttribute("limit", new PlanLimit());
        return "admin/limit-form";
    }

    @GetMapping("/limits/edit/{id}")
    public String limitEditForm(@PathVariable UUID id, Model model) {
        PlanLimit limit = planLimitService.getPlanLimitById(id);
        model.addAttribute("limit", limit);
        model.addAttribute("plan", limit.getPlan());
        return "admin/limit-form";
    }

    @PostMapping("/plans/{planId}/limits/save")
    public String saveLimit(@PathVariable UUID planId, 
                           @RequestParam(required = false) UUID limitId,
                           @RequestParam String key, 
                           @RequestParam String value,
                           RedirectAttributes redirectAttributes) {
        if (limitId != null) {
            planLimitService.updatePlanLimit(limitId, value);
        } else {
            planLimitService.createPlanLimit(planId, key, value);
        }
        redirectAttributes.addFlashAttribute("message", "Limit saved successfully");
        return "redirect:/admin/plans/" + planId + "/limits";
    }

    @PostMapping("/limits/delete/{id}")
    public String deleteLimit(@PathVariable UUID id, 
                             @RequestParam UUID planId,
                             RedirectAttributes redirectAttributes) {
        planLimitService.deletePlanLimit(id);
        redirectAttributes.addFlashAttribute("message", "Limit deleted successfully");
        return "redirect:/admin/plans/" + planId + "/limits";
    }

    // Users management
    @GetMapping("/users")
    public String usersList(HttpSession session, Model model) {
        if (session.getAttribute("adminUserId") == null) {
            return "redirect:/admin/login";
        }
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users-list";
    }

    @GetMapping("/users/edit/{id}")
    public String userEditForm(@PathVariable UUID id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam UUID id,
                          @RequestParam String email,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        userService.updateUser(id, email, User.UserRole.valueOf(role));
        redirectAttributes.addFlashAttribute("message", "User updated successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/password")
    public String updatePassword(@RequestParam UUID id,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        userService.updateUserPassword(id, password);
        redirectAttributes.addFlashAttribute("message", "Password updated successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        userService.softDeleteUser(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/hard-delete/{id}")
    public String hardDeleteUser(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        userService.hardDeleteUser(id);
        redirectAttributes.addFlashAttribute("message", "User permanently deleted");
        return "redirect:/admin/users";
    }

    private com.ppaw.service.dto.PlanDto toPlanDto(Plan plan) {
        return com.ppaw.service.dto.PlanDto.builder()
            .id(plan.getId())
            .code(plan.getCode())
            .name(plan.getName())
            .priceCents(plan.getPriceCents())
            .billingPeriod(plan.getBillingPeriod())
            .isActive(plan.getIsActive())
            .build();
    }
}
