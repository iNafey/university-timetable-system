package edu.leicester.co2103.controller;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.domain.Session;
import edu.leicester.co2103.repo.ModuleRepository;

@RestController
@RequestMapping("/modules")
public class ModuleRestController {
	@Autowired
	ModuleRepository repo;
	
	
	//endpoint to list all of the modules in the repo
	
	@GetMapping("")
	public ResponseEntity<?> showAllModules(){
		List<Module> modules = (List<Module>) repo.findAll();
		if(modules.isEmpty()) {
			return new ResponseEntity<>(new String("there are currently no modules"),HttpStatus.BAD_REQUEST);
		}else {
			return new ResponseEntity<List<Module>>(modules, HttpStatus.OK);
		}
	}
	
	//endpoint to get a specific module
	@GetMapping("/{code}")
	public ResponseEntity<?> showModule(@PathVariable("code") String code){
		Module module = repo.findById(code).orElse(null);
		if(module==null) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			return new ResponseEntity<Module>(module, HttpStatus.OK);
		}
	}
	
	//endpoint to add a new module
	@PostMapping("")
	public ResponseEntity<?> createModule(@RequestBody Module module, UriComponentsBuilder ucBuilder){
		if(repo.existsById(module.getCode())) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}else {
			repo.save(module);
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(ucBuilder.path("/{code}").buildAndExpand(module.getCode()).toUri());
			return new ResponseEntity<String>(headers, HttpStatus.CREATED);	
		}
	}
	
	//update a module
	@PatchMapping("/{code}")
	public ResponseEntity<?> updateModule(@PathVariable("code") String code, @RequestBody Map<String,Object> module){
		Module updateThisModule = repo.findById(code).orElse(null);
		
		if(updateThisModule==null) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			module.forEach((key, value) ->{
				if(key != "code" && key != "sessions") {
					Field field = ReflectionUtils.findRequiredField(Module.class, key);
					field.setAccessible(true);
					ReflectionUtils.setField(field, updateThisModule, value);
				}
				
			});
			
			repo.save(updateThisModule);
			
			return new ResponseEntity<Module>(updateThisModule, HttpStatus.OK);
		}
	}
	
	//delete a module
	
	@DeleteMapping("/{code}")
	public ResponseEntity<?> deleteModule(@PathVariable("code") String code){
		if(!repo.existsById(code)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {
			repo.deleteById(code);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
	
	//list all sessions in module
	@GetMapping("/{code}/sessions")
	public ResponseEntity<?> getAllSessions(@PathVariable("code") String code){
		Module module = repo.findById(code).orElse(null);
		if(module==null) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			return new ResponseEntity<List<Session>>(module.getSessions(), HttpStatus.OK);
		}
	}
	
	//add new session to module
	@PostMapping("/{code}/sessions")
	public ResponseEntity<?> addNewSession(@PathVariable("code") String code, @RequestBody Session session, UriComponentsBuilder ucBuilder){
		Module module = repo.findById(code).orElse(null);
		for(Session s : module.getSessions()) {
			if(s.getId() == session.getId()) {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
		}
		module.getSessions().add(session);
		repo.save(module);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/{id}").buildAndExpand(session.getId()).toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);	
	}
	
	//get a session within a module
	@GetMapping("/{code}/sessions/{id}")
	public ResponseEntity<?> findSession(@PathVariable("code") String code, @PathVariable("id") long id){
		if(!repo.existsById(code)) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			Module module = repo.findById(code).orElse(null);
			for(Session s : module.getSessions()) {
				if(s.getId() == id) {
					return new ResponseEntity<Session>(s,HttpStatus.OK);
				}
			}
			return new ResponseEntity<>(new String("that session doesnt exist"),HttpStatus.NOT_FOUND);
		}
	}
	
	//update (PUT) a session in a module
	@PutMapping("/{code}/sessions/{id}")
	public ResponseEntity<?> updateSession(@PathVariable("code") String code, @PathVariable("id") long id, @RequestBody Session session){
		if(!repo.existsById(code)) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			Module module = repo.findById(code).orElse(null);
			for(Session s : module.getSessions()) {
				if(s.getId() == id) {
					s.setDatetime(session.getDatetime());
					s.setDuration(session.getDuration());
					s.setTopic(session.getTopic());
					repo.save(module);
					return new ResponseEntity<Session>(s, HttpStatus.OK);
				}
			}
			return new ResponseEntity<>(new String("that session doesnt exist"),HttpStatus.NOT_FOUND);
		}
	}

	//update (PATCH) a session in a module
	@PatchMapping("/{code}/sessions/{id}")
	public ResponseEntity<?> updateSessionPartial(@PathVariable("code") String code, @PathVariable("id") long id, @RequestBody Map<String,Object> session){
		if(!repo.existsById(code)) {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}else {
			Module module = repo.findById(code).orElse(null);
			for(Session s : module.getSessions()) {
				if(s.getId() == id) {
					session.forEach((key, value) ->{
						if(key != "id" && key!="datetime") {
							Field field = ReflectionUtils.findRequiredField(Session.class, key);
							field.setAccessible(true);
							ReflectionUtils.setField(field, s, value);
						}else if(key=="datetime") {
							Timestamp t = Timestamp.valueOf((String) session.get("datetime"));
							s.setDatetime(t);
						}
					});
					repo.save(module);
					return new ResponseEntity<Session>(s, HttpStatus.OK);
				}
				
			}
			return new ResponseEntity<>(new String("that session doesnt exist"),HttpStatus.NOT_FOUND);
				
		}
	}
	//delete a session in a module
	@DeleteMapping("/{code}/sessions/{id}")
	public ResponseEntity<?> deleteSession(@PathVariable("code") String code, @PathVariable("id") long id){
		if(repo.existsById(code)) {
			Module m = repo.findById(code).orElse(null);
			for(Session s : m.getSessions()) {
				if(s.getId()==id) {
					m.getSessions().remove(s);
					repo.save(m);
					return new ResponseEntity<>(HttpStatus.OK);
				}
			}
			return new ResponseEntity<>(new String("that session doesnt exist"),HttpStatus.NOT_FOUND); 
		}else {
			return new ResponseEntity<>(new String("that module doesnt exist"),HttpStatus.NOT_FOUND);
		}
	}
}
