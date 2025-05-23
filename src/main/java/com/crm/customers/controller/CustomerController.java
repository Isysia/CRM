package com.crm.customers.controller;

import com.crm.customers.model.Customer;
import com.crm.customers.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * MVC‑контролер для керування Customer через Thymeleaf‑шаблони.
 */
@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Показати список всіх клієнтів.
     */
    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customers"; // templates/customers.html
    }

    /**
     * Показати форму створення нового клієнта.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer_form"; // templates/customer_form.html
    }

    /**
     * Обробити збереження нового клієнта.
     */
    @PostMapping
    public String createCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    /**
     * Показати форму редагування існуючого клієнта.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customer_form"; // reuse same шаблон
    }

    /**
     * Обробити оновлення даних клієнта.
     */
    @PostMapping("/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @ModelAttribute("customer") Customer customer) {
        customerService.updateCustomer(id, customer);
        return "redirect:/customers";
    }

    /**
     * Видалити клієнта.
     */
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}
