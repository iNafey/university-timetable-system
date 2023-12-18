package edu.leicester.co2103.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.leicester.co2103.domain.Convenor;
import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.domain.Session;
import edu.leicester.co2103.repo.ConvenorRepository;
import edu.leicester.co2103.repo.ModuleRepository;
import edu.leicester.co2103.repo.SessionRepository;

@RestController
@RequestMapping("/sessions")
public class SessionRestController {
	@Autowired
	SessionRepository repo;
	@Autowired
	ConvenorRepository crepo;
	@Autowired
	ModuleRepository mrepo;
	
	
	@DeleteMapping("")
	public ResponseEntity<?> delAll() {
		if(repo.count() == 0) {
			return new ResponseEntity<>(new String("there are no sessions"),HttpStatus.BAD_REQUEST);
		}else {
			repo.deleteAll();
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}
	
	@GetMapping("")
	public ResponseEntity<?> getSessionsFiltered(@RequestParam(name="convenor", required=false) Long id, @RequestParam(name="module", required=false) String code){
		if(id!=null && code == null) {
			if(crepo.existsById(id)) {
				Convenor c = crepo.findById(id).orElse(null);
				List<Module> m = c.getModules();
				List<Session> s = new ArrayList<>();
				for(Module mod : m) {
					for(Session ses : mod.getSessions()) {
						s.add(ses);
					}
				}
				if(s.isEmpty()) {
					return new ResponseEntity<>(new String("empty session list"),HttpStatus.NOT_FOUND);
				}else {
					return new ResponseEntity<List<Session>>(s, HttpStatus.OK);
				}
			}else {
				return new ResponseEntity<>(new String("convenor doesnt exist"),HttpStatus.NOT_FOUND);
			}
			
			
		}else if(id!= null && code != null) {
			if(crepo.existsById(id) && mrepo.existsById(code)) {
				Convenor c = crepo.findById(id).orElse(null);
				List<Module> m = c.getModules();
				List<Session> s = new ArrayList<>();
				for(Module mod : m) {
					if(mod.getCode().equals(code)) {
						for(Session ses : mod.getSessions()) {
							s.add(ses);
						}
					}
				}
				if(s.isEmpty()) {
					return new ResponseEntity<>(new String("empty3 session list"),HttpStatus.NOT_FOUND);
				}else {
					return new ResponseEntity<List<Session>>(s, HttpStatus.OK);
				}
			}else {
				return new ResponseEntity<>(new String("bad request from filter"),HttpStatus.BAD_REQUEST);
			}
		}else if(id==null && code != null) {
			if(mrepo.existsById(code)) {
				Module m = mrepo.findById(code).orElse(null);
				List<Session> s = m.getSessions();
				if(s.isEmpty()) {
					return new ResponseEntity<>(new String("empty session list"),HttpStatus.NOT_FOUND);
				}else {
					return new ResponseEntity<List<Session>>(s, HttpStatus.OK);
				}
			}else {
				return new ResponseEntity<>(new String("module doesnt exist"),HttpStatus.BAD_REQUEST);
			}
		}else {
			return new ResponseEntity<>(new String("you must use atleast 1 filter"),HttpStatus.BAD_REQUEST);
		}
	}
}
