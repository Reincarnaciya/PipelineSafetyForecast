package gas.pipeline.safety.forecast.controller;

import gas.pipeline.safety.forecast.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    @Autowired
    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping(value = {"", "/index", "/"})
    public String main(){
        return "redirect:/predictions";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Ошибка входа. Проверьте имя пользователя и пароль.");
        }
        if (logout != null) {
            model.addAttribute("logout", "Вы успешно вышли из системы.");
        }
        return "login";
    }



    @GetMapping("/registration")
    public String registration(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Ошибка регистрации. Проверьте введенные данные.");
        }
        if (success != null) {
            model.addAttribute("success", "Регистрация прошла успешно!");
        }
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model
    ) {
        // Проверка паролей
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают.");
            return "redirect:/registration?error";
        }

        // Регистрация пользователя
        boolean isRegistered = registrationService.registerUser(username, email, password);
        if (isRegistered) {
            return "redirect:/registration?success";
        } else {
            model.addAttribute("error", "Пользователь с таким именем или email уже существует.");
            return "redirect:/registration?error";
        }
    }
}