package edu.leicester.co2103.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


import edu.leicester.co2103.domain.Convenor;
import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.repo.ConvenorRepository;
import edu.leicester.co2103.repo.ModuleRepository;

@RestController
@RequestMapping("/convenors")
public class ConvenorRestController {
	@Autowired
	ConvenorRepository repo;
	@Autowired
	ModuleRepository mrepo;//find orphaned module(s) directly from repo since @ManyToMany doesn't support orphanremoval
	
	//endpoint for listing all convenors
	
	@GetMapping("")
	public ResponseEntity<?> showAllConvenors() {
		List<Convenor> convenors = (List<Convenor>) repo.findAll();
		if(convenors.isEmpty()) {
			return new ResponseEntity<>(new String("there are currently no convenors"),HttpStatus.NOT_FOUND);//ADD ERROR MESSAGES TO ALL MAPPINGS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}else {
			return new ResponseEntity<List<Convenor>>(convenors, HttpStatus.OK);
		}
	}
	
	//endpoint for listing a specific convenor
	@GetMapping("/{id}")
	public ResponseEntity<?> showConvenor(@PathVariable("id") long id){
		Convenor convenor = repo.findById(id).orElse(null);
		if(convenor==null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {
			return new ResponseEntity<Convenor>(convenor, HttpStatus.OK);
		}	
	}
	
	//endpoint for adding a convenor
	@PostMapping("")
	public ResponseEntity<?> createConvenor(@RequestBody Convenor convenor, UriComponentsBuilder ucB){
		if(repo.existsById(convenor.getId())) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}else {
			repo.save(convenor);
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(ucB.path("/{id}").buildAndExpand(convenor.getId()).toUri());
			
			return new ResponseEntity<String>(headers, HttpStatus.CREATED);	
		}
		
	}

	//endpoint for updating a convenor
	@PutMapping("/{id}")
	public ResponseEntity<?> updateConvenor(@PathVariable("id") long id, @RequestBody Convenor convenor){
		Convenor updateThisConvenor = repo.findById(id).orElse(null);
		
		if(updateThisConvenor==null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {
			updateThisConvenor.setModules(convenor.getModules());
			updateThisConvenor.setName(convenor.getName());
			updateThisConvenor.setPosition(convenor.getPosition());
			
			repo.save(updateThisConvenor);
			
			return new ResponseEntity<Convenor>(updateThisConvenor, HttpStatus.OK);
		}
	}

	//endpoint for deleting a specific convenor
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteConvenor(@PathVariable("id") long id){
		if(!repo.findById(id).isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {//process for deleting convenor AND the module (which has no convenor parent) - cant do through @ManyToMany
			Convenor convenor = repo.findById(id).orElse(null);
			List<Convenor> convenors = (List<Convenor>) repo.findAll();
			convenors.remove(convenor);
			
			List<Module> modules = convenor.getModules();
			for(Module m : modules) {
				for(Convenor c : convenors) {
					if (c.getModules().contains(m)) {
						modules.remove(m);
					}
				}
			}
			repo.deleteById(id);
			mrepo.deleteAll(modules);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
	
	//endpoint for listing the modules from a specific convenor
	@GetMapping("/{id}/modules")
	public ResponseEntity<?> showModulesFromConvenor(@PathVariable("id") long id){
		Convenor convenor = repo.findById(id).orElse(null);
		
		if(convenor==null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {
			List<Module> modules = convenor.getModules();
			return new ResponseEntity<List<Module>>(modules,HttpStatus.OK);
		}
	}
}
