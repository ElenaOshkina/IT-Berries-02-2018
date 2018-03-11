package com.itberries2018.demo.controllers;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.itberries2018.demo.models.LoginForm;
import com.itberries2018.demo.models.User;
import com.itberries2018.demo.services.UserService;

//TODO CORS
@RestController
@RequestMapping("/api")
public class RestApiController {
    private static final String SESSION_USER_ID = "Id";

	private static final Logger LOGGER = LoggerFactory.getLogger(RestApiController.class);

    private final UserService userService; 

    public RestApiController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/users/")
    public ResponseEntity<List<User>> listAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") long id) {
        LOGGER.info("Fetching User with id {}", id);
        final User user = userService.findById(id);
        if (user == null) {
            LOGGER.error("User with id {} not found.", id);
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping(value = "/signUp/")
    public ResponseEntity<Void> createUser(@RequestBody User user, UriComponentsBuilder ucBuilder) {
        LOGGER.info("Creating User : {}", user);

        if (userService.isUserExist(user)) {
            LOGGER.error("Unable to create. A User with name {} already exist", user.getName());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        userService.saveUser(user);

        return ResponseEntity.created(ucBuilder.path("/api/user/{id}").buildAndExpand(user.getId()).toUri()).build();
    }

    @PutMapping("/users/")
    public ResponseEntity<User> updateUser(HttpSession httpSession, @RequestBody User user) {
    	Long id = (Long) httpSession.getAttribute(SESSION_USER_ID);
    	if(id == null) {
    		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    	}
    	//TODO при изменении пароля обычно просят повторить старый пароль
        LOGGER.info("Updating User with id {}", id);

        final User currentUser = userService.findById(id);

        currentUser.setLogin(user.getLogin());
        currentUser.setEmail(user.getEmail());
        currentUser.setPassword(user.getPassword());

        userService.updateUser(currentUser);
        return new ResponseEntity<>(currentUser, HttpStatus.OK);
    }


    @PostMapping("/login/")
    public ResponseEntity<Void> login(@RequestBody LoginForm loginForm, HttpSession httpSession) {

        LOGGER.info("Trying to login user");

        //TODO нельзя перелогинаться в другого пользователя?
        if (httpSession.getAttribute(SESSION_USER_ID) != null) {
            LOGGER.info("Already in");
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }

        final User user = userService.findByLogin(loginForm.getLogin());
        if (user == null || !user.getPassword().equals(loginForm.getPassword())) {
            LOGGER.error("Unable to delete. User with id {} not found.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        httpSession.setAttribute(SESSION_USER_ID, user.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/logOut/")
    public ResponseEntity<Void> logOut(HttpServletResponse response,
                                    HttpSession httpSession) {
    	LOGGER.info("Trying to logOut user " + httpSession.getAttribute(SESSION_USER_ID));
    	httpSession.invalidate();
    	return ResponseEntity.ok().build();
    }

	@PostMapping("/currentUser/")
	public ResponseEntity<User> currentUser(HttpSession httpSession) {
		Long id = (Long) httpSession.getAttribute(SESSION_USER_ID);
		if (id == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(userService.findById(id));
	}

}

